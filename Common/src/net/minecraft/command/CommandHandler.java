package net.minecraft.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;

import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.descriptors.CommandDescriptor;
import net.minecraft.command.legacy.LegacyCommand;
import net.minecraft.command.parser.Parser;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;

public class CommandHandler implements ICommandManager
{
	private static final Logger logger = LogManager.getLogger();
	@SuppressWarnings("unused")
	private static final String __OBFID = "CL_00001765";
	
	@Deprecated
	private static final Map<String, ICommand> commandMap = new HashMap<>();
	
	/**
	 * adds the command and any aliases it has to the internal map of available commands
	 */
	@Deprecated
	public static ICommand registerCommand(final ICommand command)
	{
		CommandDescriptor.registerCommand(command.getCommandName(), new LegacyCommand(command));
		CommandDescriptor.addAlias(command.getCommandName(), command.getCommandAliases().toArray(new String[0]));
		
		commandMap.put(command.getCommandName(), command);
		
		for (final String alias : command.getCommandAliases())
			commandMap.put(alias, command);
		
		return command;
	}
	
	@Deprecated
	protected static boolean func_175786_a(final ICommandSender sender, final String[] args, final ICommand command, final String commandString)
	{
		try
		{
			command.processCommand(sender, args);
			return true;
		} catch (final WrongUsageException wue)
		{
			CommandUtilities.errorMessage(sender, "commands.generic.usage", new ChatComponentTranslation(wue.getMessage(), wue.getErrorOjbects()));
		} catch (final CommandException e)
		{
			CommandUtilities.errorMessage(sender, e.getMessage(), e.getErrorOjbects());
		} catch (final Throwable t)
		{
			CommandUtilities.errorMessage(sender, "commands.generic.exception");
			logger.error("Couldn\'t process command: \'" + commandString + "\'", t);
		}
		
		return false;
	}
	
	/**
	 * returns a map of string to commads. All commands are returned, not just ones which someone has permission to use.
	 */
	@Override
	@Deprecated
	public Map<String, ICommand> getCommands()
	{
		return commandMap;
	}
	
	/**
	 * returns all commands that the commandSender can use
	 */
	@Override
	@Deprecated
	public List<ICommand> getPossibleCommands(final ICommandSender sender)
	{
		final List<ICommand> ret = new ArrayList<>();
		
		for (final ICommand command : commandMap.values())
			if (command.canCommandSenderUseCommand(sender))
				ret.add(command);
		
		return ret;
	}
	
	@Override
	@Deprecated
	public List<String> getTabCompletionOptions(final ICommandSender sender, final String input, final BlockPos pos)
	{
		final String[] args = input.split(" ", -1);
		final String commandName = args[0];
		
		if (args.length == 1)
		{
			final List<String> completions = Lists.newArrayList();
			
			for (final Entry<String, ICommand> entry : commandMap.entrySet())
				if (CommandBase.doesStringStartWith(commandName, entry.getKey()) && entry.getValue().canCommandSenderUseCommand(sender))
					completions.add(entry.getKey());
			
			return completions;
		}
		
		if (args.length > 1)
		{
			final ICommand command = commandMap.get(commandName);
			
			if (command != null && command.canCommandSenderUseCommand(sender))
				return command.addTabCompletionOptions(sender, dropFirstString(args), pos);
		}
		
		return null;
	}
	
	@Deprecated
	private static String[] dropFirstString(final String[] strings)
	{
		final String[] ret = new String[strings.length - 1];
		System.arraycopy(strings, 1, ret, 0, strings.length - 1);
		return ret;
	}
	
	public static int executeCommand(final ICommandSender sender, final CommandArg<Integer> command)
	{
		try
		{
			return command.eval(sender);
		} catch (final CommandException e)
		{
			CommandUtilities.errorMessage(sender, e.getMessage(), e.getErrorOjbects());
		} catch (final Throwable t)
		{
			CommandUtilities.errorMessage(sender, "commands.generic.exception");
			logger.error("Couldn\'t process command: \'" + command + "\'", t);
		}
		return 0;
	}
	
	@Override
	@Deprecated
	public int executeCommand(final ICommandSender sender, final String command)
	{
		return executeCommand(sender, command, 0);
	}
	
	public static int executeCommand(final ICommandSender sender, final String command, final int startIndex)
	{
		try
		{
			return executeCommand(sender, Parser.parseCommand(command, startIndex));
		} catch (final SyntaxErrorException e)
		{
			CommandUtilities.errorMessage(sender, e.getMessage(), e.getErrorOjbects());
		}
		return 0;
	}
}
