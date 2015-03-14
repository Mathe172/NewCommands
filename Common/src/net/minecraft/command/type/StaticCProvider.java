package net.minecraft.command.type;

import java.util.Set;

import net.minecraft.command.completion.TabCompletion;
import net.minecraft.command.parser.Parser;

public class StaticCProvider extends CListProvider
{
	private final Set<TabCompletion> completions;
	
	public StaticCProvider(Set<TabCompletion> completions)
	{
		this.completions = completions;
	}	
	
	@Override
	public Set<TabCompletion> getList(Parser parser)
	{
		return completions;
	}
	
}
