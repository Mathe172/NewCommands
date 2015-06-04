package net.minecraft.command.completion;

import java.util.Set;

import net.minecraft.command.parser.CompletionParser.CompletionData;

public class TabCompletionData
{
	public final String name;
	public final int startIndex;
	public final int endIndex;
	public final String replacement;
	public final int newCursorIndex;
	
	public TabCompletionData(final String name, final int startIndex, final int endIndex, final String replacement, final int newCursorIndex)
	{
		this.name = name;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.replacement = replacement;
		this.newCursorIndex = newCursorIndex;
	}
	
	public static final void addToSet(final TCDSet tcDataSet, final int startIndex, final CompletionData cData, final ITabCompletion tc)
	{
		final Weighted tcData = tc.getMatchData(startIndex, cData);
		if (tcData != null)
			tcDataSet.add(tcData);
	}
	
	public static final void addToSet(final TCDSet tcDataSet, final int startIndex, final CompletionData cData, final Set<? extends ITabCompletion> tcSet)
	{
		for (final ITabCompletion tc : tcSet)
			addToSet(tcDataSet, startIndex, cData, tc);
	}
	
	public static final void addToSet(final Set<Weighted> tcDataSet, final int startIndex, final CompletionData cData, final Set<? extends ITabCompletion> tcSet)
	{
		for (final ITabCompletion tc : tcSet)
			addToSet(tcDataSet, startIndex, cData, tc);
	}
	
	public static final void addToSet(final Set<Weighted> tcDataSet, final int startIndex, final CompletionData cData, final ITabCompletion tc)
	{
		final Weighted tcData = tc.getMatchData(startIndex, cData);
		if (tcData != null)
			tcDataSet.add(tcData);
	}
	
	public static class Weighted extends TabCompletionData implements Comparable<Weighted>
	{
		public final double weight;
		
		public Weighted(final String name, final int startIndex, final int endIndex, final String replacement, final int newCursorIndex, final double weight)
		{
			super(name, startIndex, endIndex, replacement, newCursorIndex);
			this.weight = weight;
		}
		
		@Override
		public int compareTo(final Weighted tcData)
		{
			final int ret = Double.compare(tcData.weight, this.weight);
			
			return ret != 0 ? ret : this.name.compareToIgnoreCase(tcData.name);
		}
		
		@Override
		public boolean equals(final Object obj)
		{
			return obj instanceof Weighted ? compareTo((Weighted) obj) == 0 : false;
		}
		
		@Override
		public int hashCode()
		{
			return this.name.hashCode();
		}
	}
}
