package net.minecraft.command.type.custom.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;

import net.minecraft.command.IPermission;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.arg.PermissionWrapper;
import net.minecraft.command.arg.PermissionWrapper.Command;
import net.minecraft.command.completion.DataRequest;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletionData.Weighted;
import net.minecraft.command.descriptors.CommandDescriptor;
import net.minecraft.command.descriptors.CommandDescriptor.CParserData;
import net.minecraft.command.descriptors.CommandDescriptor.WUEProvider;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IComplete;
import net.minecraft.command.type.base.CustomCompletable;

public final class ParserCommand extends CustomCompletable<CommandArg<Integer>>
{
	private ParserCommand()
	{
	}
	
	@Override
	public final CommandArg<Integer> iParse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
	{
		final Matcher m = parser.getMatcher(ParsingUtilities.keyMatcher);
		
		if (!parser.find(m))
			throw parser.SEE("No command name found ");
		
		final String name = m.group(1);
		
		final CommandDescriptor<?> descriptor = CommandDescriptor.getDescriptor(name);
		
		if (descriptor == null)
			throw parser.SEE("Unknown command ", ": " + name);
		
		parser.incIndex(m);
		
		return this.parserHelper(parser, descriptor);
	}
	
	public <D extends CParserData> Command parserHelper(final Parser parser, final CommandDescriptor<D> descriptor) throws SyntaxErrorException, CompletionException
	{
		CommandDescriptor<? super D> currDescriptor = descriptor;
		
		IPermission permission = descriptor.permission;
		WUEProvider usage = descriptor.usage;
		
		final Matcher endingMatcher = parser.getMatcher(ParsingUtilities.endingMatcher);
		
		final D data = descriptor.parserData(parser);
		
		while (true)
		{
			currDescriptor.parse(parser, data, usage);
			
			if (parser.find(endingMatcher))
			{
				// CommandsParser REQUIRES that endingMatcher is in the following state: whitespaces processed + found match
				parser.incIndex(endingMatcher.group(1).length());
				
				final CommandArg<Integer> command = currDescriptor.construct(data);
				
				if (command == null)
					throw usage.create(data);
				
				return new PermissionWrapper.Command(command, permission);
			}
			
			if (!parser.checkSpace() || (currDescriptor = currDescriptor.getSubType(parser, data)) == null)
				throw usage.create(data);
			
			if (currDescriptor.permission != null)
				permission = currDescriptor.permission;
			
			if (currDescriptor.usage != null)
				usage = currDescriptor.usage;
		}
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
			final Map<Weighted, IPermission> completions = new HashMap<>();
			
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
