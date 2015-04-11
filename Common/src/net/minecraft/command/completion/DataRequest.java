package net.minecraft.command.completion;

import java.util.Set;

import net.minecraft.command.completion.TabCompletionData.Weighted;

public abstract class DataRequest
{
	public abstract void process();
	
	public abstract void createCompletions(Set<Weighted> tcDataSet);
}
