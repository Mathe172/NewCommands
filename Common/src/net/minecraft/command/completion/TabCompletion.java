package net.minecraft.command.completion;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.command.parser.CompletionParser.CompletionData;

public class TabCompletion implements Comparable<TabCompletion>
{
	private final Pattern pattern;
	private final String replacement;
	public final String name;
	
	public TabCompletion(final Pattern pattern, final String replacement, final String name)
	{
		this.pattern = pattern;
		this.replacement = replacement;
		this.name = name;
	}
	
	public TabCompletion(final String replacement, final String keyword, final String name, final boolean allowEmpty)
	{
		this.replacement = replacement;
		
		final StringBuilder sb = new StringBuilder(keyword.length() * 3 + (allowEmpty ? 10 : 8)); // 3 * #chars (char + '?+') + 8 ('^(\s*+)(') - 2 (no '?+' after first) + 4 (')?+$' at the end - 2 (no '?+' after group if not optional))
		
		sb.append("\\A(\\s*+)(");
		sb.append(keyword, 0, 1);
		
		for (int i = 1; i < keyword.length(); ++i)
		{
			sb.append(keyword, i, i + 1);
			sb.append("?+");
		}
		
		sb.append(allowEmpty ? ")?+\\z" : ")\\z");
		
		this.pattern = Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE);
		this.name = name;
	}
	
	public TabCompletion(final String replacement, final String keyword, final String name)
	{
		this(replacement, keyword, name, true);
	}
	
	public TabCompletion(final String keyword, final String name)
	{
		this(keyword, keyword, name);
	}
	
	public TabCompletion(final String keyword)
	{
		this(keyword, keyword);
	}
	
	public TabCompletionData getMatchData(final String toMatch, final int startIndex, final CompletionData cData)
	{
		final Matcher m = this.pattern.matcher(toMatch).region(startIndex, cData.cursorIndex);
		
		if (!m.find())
			return null;
		
		final String replacement = this.getReplacementString(m, cData);
		if (this.fullMatch(m, cData, replacement))
			return null;
		
		final int newStartIndex = startIndex + this.getSkipOffset(m, cData);
		final int cursorOffset = this.getCursorOffset(m, cData);
		
		return new TabCompletionData(this.name, newStartIndex, replacement, newStartIndex + replacement.length() + cursorOffset, this.primitveFit(m, cData));
		
	}
	
	public int getCursorOffset(final Matcher m, final CompletionData cData)
	{
		return 0;
	}
	
	public int getSkipOffset(final Matcher m, final CompletionData cData)
	{
		return m.group(1).length();
	}
	
	public String getReplacementString(final Matcher m, final CompletionData cData)
	{
		return this.replacement;
	}
	
	public boolean primitveFit(final Matcher m, final CompletionData cData)
	{
		return m.group(2) == null;
	}
	
	public boolean fullMatch(final Matcher m, final CompletionData cData, final String replacement)
	{
		return m.group(2) != null && m.group(2).length() == replacement.length();
	}
	
	@Override
	public final int compareTo(final TabCompletion tc)
	{
		return this.name.compareTo(tc.name);
	}
}
