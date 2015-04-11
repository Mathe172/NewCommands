package net.minecraft.command.type;

import java.util.Set;

import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.parser.Parser;

public class StaticCProvider extends CListProvider
{
	private final Set<ITabCompletion> completions;
	
	public StaticCProvider(Set<ITabCompletion> completions)
	{
		this.completions = completions;
	}	
	
	@Override
	public Set<ITabCompletion> getList(Parser parser)
	{
		return completions;
	}
	
}
