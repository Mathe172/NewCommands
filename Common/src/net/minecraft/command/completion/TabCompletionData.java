package net.minecraft.command.completion;

import java.util.Set;

import net.minecraft.command.parser.CompletionParser.CompletionData;

public class TabCompletionData implements Comparable<TabCompletionData>
{
	public final String name;
	public final int startIndex;
	public final String replacement;
	public final int newCursorIndex;
	public final boolean primitiveFit;
	
	public TabCompletionData(final String name, final int startIndex, final String replacement, final int newCursorIndex, final boolean primitiveFit)
	{
		this.name = name;
		this.startIndex = startIndex;
		this.replacement = replacement;
		this.newCursorIndex = newCursorIndex;
		this.primitiveFit = primitiveFit;
	}
	
	public static final void addToSet(final TCDSet tcDataSet, final String toMatch, final int startIndex, final CompletionData cData, final TabCompletion tc)
	{
		final TabCompletionData tcData = tc.getMatchData(toMatch, startIndex, cData);
		if (tcData != null)
			tcDataSet.add(tcData);
	}
	
	public static final void addToSet(final TCDSet tcDataSet, final String toMatch, final int startIndex, final CompletionData cData, final Set<TabCompletion> tcSet)
	{
		for (final TabCompletion tc : tcSet)
			addToSet(tcDataSet, toMatch, startIndex, cData, tc);
	}
	
	public static final void addToSet(final Set<TabCompletionData> tcDataSet, final String toMatch, final int startIndex, final CompletionData cData, final TabCompletion tc)
	{
		final TabCompletionData tcData = tc.getMatchData(toMatch, startIndex, cData);
		if (tcData != null)
			tcDataSet.add(tcData);
	}
	
	@Override
	public int compareTo(TabCompletionData tcData)
	{
		return this.name.compareToIgnoreCase(tcData.name);
	}
}
