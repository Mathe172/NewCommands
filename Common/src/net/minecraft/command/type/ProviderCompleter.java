package net.minecraft.command.type;

import java.util.HashSet;
import java.util.Set;

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
	
	public ProviderCompleter(final Set<TabCompletion> completions)
	{
		this(new StaticCProvider(completions));
	}
	
	public ProviderCompleter(final String... names)
	{
		final Set<TabCompletion> completions = new HashSet<>();
		
		for (final String name : names)
			completions.add(new TabCompletion(name));
		
		this.provider = new StaticCProvider(completions);
	}
	
	public static ProviderCompleter create(final Set<String> names)
	{
		final Set<TabCompletion> completions = new HashSet<>(names.size());
		
		for (final String name : names)
			completions.add(new TabCompletion(name));
		
		return new ProviderCompleter(completions);
	}
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
	{
		final Set<TabCompletion> possibilites = this.provider.getList(parser);
		
		for (final TabCompletion tc : possibilites)
			TabCompletionData.addToSet(tcDataSet, parser.toParse, startIndex, cData, tc);
	}
	
}
