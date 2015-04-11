package net.minecraft.command.completion;

import net.minecraft.command.completion.TabCompletionData.Weighted;
import net.minecraft.command.parser.CompletionParser.CompletionData;

public abstract class ITabCompletion implements Comparable<ITabCompletion>
{
	public final String name;
	
	public ITabCompletion(final String name)
	{
		this.name = name;
	}
	
	public abstract Weighted getMatchData(final int startIndex, final CompletionData cData);
	
	@Override
	public int compareTo(final ITabCompletion tc)
	{
		return this.name.compareToIgnoreCase(tc.name);
	}
	
	@Override
	public boolean equals(final Object o)
	{
		return o instanceof ITabCompletion && this.name.equalsIgnoreCase(((ITabCompletion) o).name);
	}
	
	@Override
	public int hashCode()
	{
		return this.name.hashCode();
	}
}