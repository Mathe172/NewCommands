package net.minecraft.command.completion;

import java.util.Set;

public abstract class DataRequest
{
	public abstract void process();
	
	public abstract void createCompletions(Set<TabCompletionData> tcDataSet);
}
