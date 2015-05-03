package net.minecraft.command.completion;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CListProvider;
import net.minecraft.command.type.IComplete;

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
	
	public ProviderCompleter(final ITabCompletion... completions)
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
	
	public static ProviderCompleter create(final Collection<String> names)
	{
		final Set<ITabCompletion> completions = new HashSet<>(names.size());
		
		for (final String name : names)
			completions.add(new TabCompletion(name));
		
		return new ProviderCompleter(completions);
	}
	
	public static ProviderCompleter createEscaped(final String... names)
	{
		final Set<ITabCompletion> completions = new HashSet<>(names.length);
		
		for (final String name : names)
			completions.add(new TabCompletion.Escaped(name));
		
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
