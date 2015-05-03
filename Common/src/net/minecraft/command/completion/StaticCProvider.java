package net.minecraft.command.completion;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CListProvider;

public class StaticCProvider extends CListProvider
{
	private final Set<ITabCompletion> completions;
	
	public StaticCProvider(final Set<ITabCompletion> completions)
	{
		this.completions = completions;
	}
	
	public StaticCProvider(final ITabCompletion... completions)
	{
		this.completions = new HashSet<>(Arrays.asList(completions));
	}
	
	@Override
	public Set<ITabCompletion> getList(final Parser parser)
	{
		return this.completions;
	}
	
}
