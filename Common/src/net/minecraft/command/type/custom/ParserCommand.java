package net.minecraft.command.type.custom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;

import net.minecraft.command.CommandBase;
import net.minecraft.command.IPermission;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.completion.DataRequest;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.completion.TabCompletionData.Weighted;
import net.minecraft.command.descriptors.CommandDescriptor;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IComplete;
import net.minecraft.command.type.base.CustomCompletable;

public class ParserCommand extends CustomCompletable<CommandBase>
{
	@Override
	public final CommandBase iParse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
	{
		final Matcher m = parser.keyMatcher;
		
		if (!parser.find(m))
			throw parser.SEE("No command name found around index ");
		
		final String name = m.group(1);
		
		CommandDescriptor descriptor = CommandDescriptor.getDescriptor(name);
		
		if (descriptor == null)
			throw ParsingUtilities.SEE("Unknown command around index " + parser.getIndex() + ": " + name);
		
		parser.incIndex(m);
		
		IPermission permission = descriptor.permission;
		String usage = descriptor.usage;
		
		final Matcher endingMatcher = parser.endingMatcher;
		
		final List<ArgWrapper<?>> params = new ArrayList<>(descriptor.getParamCount());
		
		while (true)
		{
			for (int i = 0; i < descriptor.getParamCount(); ++i)
			{
				if (!parser.checkSpace())
					throw ParsingUtilities.WUE(usage);
				
				params.add(descriptor.parse(parser, i));
			}
			
			if (parser.find(endingMatcher))
			{
				// CommandsParser REQUIRES that endingMatcher is in the following state: whitespaces processed + found match
				parser.incIndex(endingMatcher.group(1).length());
				final CommandBase command = descriptor.construct(params, permission == null ? IPermission.PermissionUnrestricted : permission);
				if (command == null)
					throw ParsingUtilities.WUE(usage);
				// throw parser.SEE("Expected more arguments for command '" + name + "' around index ");
				
				return command;
			}
			
			if (!parser.checkSpace() || (descriptor = descriptor.getSubType(parser)) == null)
				throw ParsingUtilities.WUE(usage);
			
			if (descriptor.permission != null)
				permission = descriptor.permission;
			
			if (descriptor.usage != null)
				usage = descriptor.usage;
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
			final Map<Weighted, CommandDescriptor> completions = new HashMap<>();
			
			for (final Entry<ITabCompletion, CommandDescriptor> e : CommandDescriptor.commandCompletions.entrySet())
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
					for (final Entry<Weighted, CommandDescriptor> e : completions.entrySet())
					{
						final IPermission permission = e.getValue().permission;
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
