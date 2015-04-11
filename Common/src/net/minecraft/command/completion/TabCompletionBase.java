package net.minecraft.command.completion;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.command.completion.TabCompletionData.Weighted;
import net.minecraft.command.parser.CompletionParser.CompletionData;

public abstract class TabCompletionBase extends ITabCompletion
{
	protected final Pattern pattern;
	
	public TabCompletionBase(final Pattern pattern, final String name)
	{
		super(name);
		this.pattern = pattern;
	}
	
	@Override
	public Weighted getMatchData(final int startIndex, final CompletionData cData)
	{
		final Matcher m = this.pattern.matcher(cData.toMatch).region(startIndex, cData.cursorIndex);
		
		if (!m.find())
			return null;
		
		return this.matchInto(startIndex + this.getSkipOffset(m, cData), m, cData);
	}
	
	public Weighted matchInto(final int startIndex, final Matcher m, final CompletionData cData)
	{
		final String toMatch = cData.lowerToMatch;
		
		final String replacement = this.getReplacementString(m, cData);
		final String lowerReplacement = replacement.toLowerCase();
		
		if (this.fullMatch(m, cData, replacement))
			return null;
		
		final int replLen = replacement.length();
		final int endIndex = toMatch.length();
		
		double weight = 0.0;
		final double maxWeight = this.getMaxWeight(m, cData);
		final double[] weights = this.getWeights(m, cData);
		
		final int cursorIndex = cData.cursorIndex;
		
		int index = startIndex;
		
		int replIndex = -1;
		
		while (replIndex < replLen && index < cursorIndex)
		{
			replIndex = lowerReplacement.indexOf(toMatch.charAt(index), replIndex + 1);
			
			if (replIndex == -1)
				break;
			
			weight += weights[replIndex];
			
			++index;
		}
		
		index = cursorIndex;
		
		if (this.complexFit())
		{
			while (replIndex < replLen && index < endIndex)
			{
				replIndex = lowerReplacement.indexOf(toMatch.charAt(index), replIndex) + 1;
				
				if (replIndex == 0)
					break;
				
				++index;
			}
		}
		
		return new Weighted(this.name, startIndex, index, replacement, startIndex + replLen + this.getCursorOffset(m, cData), weight / maxWeight + this.weightOffset(m, cData));
	}
	
	public abstract boolean complexFit();
	
	public abstract double weightOffset(final Matcher m, final CompletionData cData);
	
	public abstract double[] getWeights(final Matcher m, final CompletionData cData);
	
	public abstract double getMaxWeight(final Matcher m, final CompletionData cData);
	
	public abstract int getCursorOffset(final Matcher m, final CompletionData cData);
	
	public abstract int getSkipOffset(final Matcher m, final CompletionData cData);
	
	public abstract String getReplacementString(final Matcher m, final CompletionData cData);
	
	public abstract boolean fullMatch(final Matcher m, final CompletionData cData, final String replacement);
}
