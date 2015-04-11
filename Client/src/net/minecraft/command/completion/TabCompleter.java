package net.minecraft.command.completion;

public class TabCompleter
{
	private final TabCompletionData tcData;
	private String result = null;
	
	public TabCompleter(final TabCompletionData tcData)
	{
		this.tcData = tcData;
	}
	
	public String matchInto(final String toMatch)
	{
		return this.result == null ? this.result = toMatch.substring(0, this.tcData.startIndex) + this.tcData.replacement + toMatch.substring(this.tcData.endIndex) : this.result;
	}
	
	public final String name()
	{
		return this.tcData.name;
	}
	
	public final int newCursorIndex()
	{
		return this.tcData.newCursorIndex;
	}
}
