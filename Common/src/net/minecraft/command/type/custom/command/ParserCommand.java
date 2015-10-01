package net.minecraft.command.type.custom.command;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;

import net.minecraft.command.IPermission;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.completion.DataRequest;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletionData.Weighted;
import net.minecraft.command.descriptors.CommandDescriptor;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.MatcherRegistry;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IComplete;
import net.minecraft.command.type.base.CustomCompletable;

public final class ParserCommand extends CustomCompletable<CommandArg<Integer>>
{
	private ParserCommand()
	{
	}
	
	public static final MatcherRegistry commandNameMatcher = new MatcherRegistry("\\G\\s*+([\\w-]++|\\?)"); //don't ask...
	
	@Override
	public final CommandArg<Integer> iParse(final Parser parser, final Context context) throws SyntaxErrorException
	{
		final Matcher m = parser.getMatcher(commandNameMatcher);
		
		if (!parser.find(m))
			throw parser.SEE("No command name found ");
		
		final String name = m.group(1);
		
		final CommandDescriptor<?> descriptor = CommandDescriptor.getDescriptor(name);
		
		if (descriptor == null)
			throw parser.SEE("Unknown command ", ": " + name);
		
		parser.incIndex(m);
		
		return descriptor.parse(parser);
	}
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
	{
		nameCompleter.complete(tcDataSet, parser, startIndex, cData);
	}
	
	public static final IComplete nameCompleter = new IComplete()
	{
		@Override
		public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
		{
			final Map<Weighted, IPermission> completions = new IdentityHashMap<>();
			
			for (final Entry<ITabCompletion, IPermission> e : CommandDescriptor.commandCompletions.entrySet())
			{
				final Weighted tcData = e.getKey().getMatchData(startIndex, cData);
				
				if (tcData != null)
					completions.put(tcData, e.getValue());
			}
			
			tcDataSet.add(new DataRequest()
			{
				@Override
				public void process()
				{
					for (final Entry<Weighted, IPermission> e : completions.entrySet())
					{
						final IPermission permission = e.getValue();
						if (permission == null || permission.canCommandSenderUseCommand(cData.sender))
							tcDataSet.add(e.getKey());
					}
				}
				
				@Override
				public void createCompletions(final Set<Weighted> tcDataSet)
				{
				}
			});
		}
	};
	
	public static final ParserCommand parser = new ParserCommand();
}
