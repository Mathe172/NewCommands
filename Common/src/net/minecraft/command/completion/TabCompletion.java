package net.minecraft.command.completion;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.command.completion.TabCompletionData.Weighted;
import net.minecraft.command.parser.CompletionParser.CompletionData;

public class TabCompletion extends TabCompletionBase
{
	private final String replacement;
	
	private final double[] weights;
	private final double maxWeight;
	
	private static final double vowelWeight = 0.5;
	
	public TabCompletion(final Pattern pattern, final String replacement, final String name)
	{
		super(pattern, name);
		this.replacement = replacement;
		
		this.weights = new double[replacement.length()];
		double maxWeight = 0.0;
		
		int lastWordStart = 0;
		int wordCount = 0;
		
		for (int i = 0; i < replacement.length(); ++i)
		{
			final char c = replacement.charAt(i);
			
			if (i > 0 && Character.isUpperCase(c))
			{
				lastWordStart = i;
				++wordCount;
			}
			
			final double decay = decay(i - lastWordStart);
			maxWeight += (this.weights[i] = weight(c) * decay * decay * decay(wordCount));
			
			if (i > 0 && c == '_')
			{
				lastWordStart = i;
				++wordCount;
			}
		}
		
		this.maxWeight = maxWeight;
	}
	
	public static final double decay(final int distance)
	{
		return 1.0 / (distance + 1);
	}
	
	public TabCompletion(final String replacement, final String keyword, final String name, final boolean allowSpaces)
	{
		this(createPattern(keyword, allowSpaces), replacement, name);
	}
	
	public TabCompletion(final String replacement, final String keyword, final String name)
	{
		this(createPattern(keyword, true), replacement, name);
	}
	
	public TabCompletion(final String keyword, final String name, final boolean allowSpaces)
	{
		this(keyword, keyword, name, allowSpaces);
	}
	
	public TabCompletion(final String name, final boolean allowSpaces)
	{
		this(name, name, name, allowSpaces);
	}
	
	public TabCompletion(final String keyword, final String name)
	{
		this(keyword, keyword, name);
	}
	
	public TabCompletion(final String name)
	{
		this(name, name, name);
	}
	
	private static boolean isVowel(final char c)
	{
		final char lowerC = Character.toLowerCase(c);
		return lowerC == 'e' || lowerC == 'o' || lowerC == 'a' || lowerC == 'i' || lowerC == 'u';
	}
	
	private static double weight(final char c)
	{
		return isVowel(c) ? vowelWeight : 1.0;
	}
	
	private static Pattern createPattern(final String keyword, final boolean allowSpaces)
	{
		final StringBuilder sb = new StringBuilder(keyword.length() * 3 + (allowSpaces ? 13 : 9)); // 3 * #chars [char + '?+'] + 9:5 ['\A(\s*+)(':'\A()('] - 2 [no '?+' after first] + 6 [')?+\Z'] at the end)
		
		sb.append(allowSpaces ? "\\A(\\s*+)(" : "\\A()(");
		sb.append(keyword, 0, 1);
		
		for (int i = 1; i < keyword.length(); ++i)
		{
			sb.append(keyword.charAt(i));
			sb.append("?+");
		}
		
		sb.append(")?+\\z");
		
		return Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE);
	}
	
	public static void addEscaped(final StringBuilder sb, final char c)
	{
		switch (c)
		{
		case '(':
		case '[':
		case '{':
		case '\\':
		case '^':
		case '-':
		case '=':
		case '$':
		case '!':
		case '|':
		case ']':
		case '}':
		case ')':
		case '?':
		case '*':
		case '+':
		case '.':
			sb.append('\\');
		}
		sb.append(c);
	}
	
	public static class Escaped extends TabCompletion
	{
		public Escaped(final String replacement, final String keyword, final String name, final boolean allowSpaces)
		{
			super(createPattern(keyword, allowSpaces), replacement, name);
		}
		
		private static Pattern createPattern(final String keyword, final boolean allowSpaces)
		{
			final StringBuilder sb = new StringBuilder(keyword.length() * 3 + (allowSpaces ? 13 : 9)); // 3 * #chars [char + '?+'] + 9:5 ['\A(\s*+)(':'\A()('] - 2 [no '?+' after first] + 6 [')?+\Z'] at the end)
			
			sb.append(allowSpaces ? "\\A(\\s*+)(" : "\\A()(");
			
			addEscaped(sb, keyword.charAt(0));
			
			for (int i = 1; i < keyword.length(); ++i)
			{
				addEscaped(sb, keyword.charAt(i));
				sb.append("?+");
			}
			
			sb.append(")?+\\z");
			
			return Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE);
		}
		
		public Escaped(final String keyword, final String name)
		{
			this(keyword, keyword, name, true);
		}
		
		public Escaped(final String name)
		{
			this(name, name, true);
		}
		
		public Escaped(final String keyword, final String name, final boolean allowSpaces)
		{
			this(keyword, keyword, name, allowSpaces);
		}
		
		public Escaped(final String name, final boolean allowSpaces)
		{
			this(name, name, allowSpaces);
		}
	}
	
	@Override
	public boolean complexFit()
	{
		return true;
	}
	
	@Override
	public double weightOffset(final Matcher m, final CompletionData cData)
	{
		return 0.0;
	}
	
	@Override
	public double[] getWeights(final Matcher m, final CompletionData cData)
	{
		return this.weights;
	}
	
	@Override
	public double getMaxWeight(final Matcher m, final CompletionData cData)
	{
		return this.maxWeight;
	}
	
	@Override
	public int getCursorOffset(final Matcher m, final CompletionData cData)
	{
		return 0;
	}
	
	@Override
	public int getSkipOffset(final Matcher m, final CompletionData cData)
	{
		return m.group(1).length();
	}
	
	@Override
	public String getReplacementString(final Matcher m, final CompletionData cData)
	{
		return this.replacement;
	}
	
	@Override
	public boolean fullMatch(final Matcher m, final CompletionData cData, final String replacement)
	{
		return m.group(2) != null && m.group(2).length() == replacement.length();
	}
	
	public static class SingleChar extends ITabCompletion
	{
		private final String replacement;
		private final char lowerReplacement;
		
		public SingleChar(final char replacement, final String name)
		{
			super(name);
			this.replacement = String.valueOf(replacement);
			this.lowerReplacement = Character.toLowerCase(replacement);
		}
		
		public SingleChar(final char name)
		{
			super(String.valueOf(name));
			this.replacement = this.name;
			this.lowerReplacement = Character.toLowerCase(name);
		}
		
		public static final Pattern leadingSpacePattern = Pattern.compile("\\A\\s*+\\z");
		
		@Override
		public Weighted getMatchData(final int startIndex, final CompletionData cData)
		{
			final Matcher m = leadingSpacePattern.matcher(cData.toMatch).region(startIndex, cData.cursorIndex);
			
			if (!m.find())
				return null;
			
			final int cursorIndex = cData.cursorIndex;
			
			if (cursorIndex < cData.lowerToMatch.length() && cData.lowerToMatch.charAt(cursorIndex) == this.lowerReplacement)
				return null;
			
			return new Weighted(this.name, cursorIndex, cursorIndex, this.replacement, cursorIndex + 1, this.weight());
		}
		
		public double weight()
		{
			return 0.0;
		}
	}
}
