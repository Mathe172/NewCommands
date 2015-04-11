package net.minecraft.command.type;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletion;
import net.minecraft.command.completion.TabCompletionData;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Parser;

public class ProviderCompleter implements IComplete
{
	private final CListProvider provider;
	
	public ProviderCompleter(final CListProvider provider)
	{
		this.provider = provider;
	}
	
	public ProviderCompleter(final Set<ITabCompletion> completions)
	{
		this(new StaticCProvider(completions));
	}
	
	public ProviderCompleter(final String... names)
	{
		final Set<ITabCompletion> completions = new HashSet<>(names.length);
		
		for (final String name : names)
			completions.add(new TabCompletion(name));
		
		this.provider = new StaticCProvider(completions);
	}
	
	public static ProviderCompleter create(final Set<String> names)
	{
		final Set<ITabCompletion> completions = new HashSet<>(names.size());
		
		for (final String name : names)
			completions.add(new TabCompletion(name));
		
		return new ProviderCompleter(completions);
	}
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
	{
		final Set<ITabCompletion> possibilites = this.provider.getList(parser);
		
		for (final ITabCompletion tc : possibilites)
			TabCompletionData.addToSet(tcDataSet, startIndex, cData, tc);
	}
	
}
