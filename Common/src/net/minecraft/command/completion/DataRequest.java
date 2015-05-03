package net.minecraft.command.completion;

import java.util.Set;

import net.minecraft.command.completion.TabCompletionData.Weighted;
import net.minecraft.command.parser.CompletionParser.CompletionData;

public abstract class DataRequest
{
	public abstract void process();
	
	public abstract void createCompletions(Set<Weighted> tcDataSet);
	
	public static abstract class SimpleAdd extends DataRequest
	{
		public Set<String> tcSet;
		private final int startIndex;
		private final CompletionData cData;
		
		public SimpleAdd(final int startIndex, final CompletionData cData)
		{
			this.startIndex = startIndex;
			this.cData = cData;
		}
		
		@Override
		public void createCompletions(final Set<Weighted> tcDataSet)
		{
			for (final String tc : this.tcSet)
				TabCompletionData.addToSet(tcDataSet, this.startIndex, this.cData, new TabCompletion(tc));
		}
	}
}
