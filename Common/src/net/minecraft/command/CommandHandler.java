package net.minecraft.command;

import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.parser.Parser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandHandler
{
	private static final Logger logger = LogManager.getLogger();
	@SuppressWarnings("unused")
	private static final String __OBFID = "CL_00001765";
	
	public static int executeCommand(final ICommandSender sender, final CommandArg<Integer> command)
	{
		try
		{
			return command.eval(sender);
		} catch (final CommandException e)
		{
			CommandBase.errorMessage(sender, e.getMessage(), e.getErrorOjbects());
		} catch (final Throwable t)
		{
			CommandBase.errorMessage(sender, "commands.generic.exception");
			logger.error("Couldn\'t process command: \'" + command + "\'", t);
		}
		return 0;
	}
	
	public static int executeCommand(final ICommandSender sender, final String command)
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
			CommandBase.errorMessage(sender, e.getMessage(), e.getErrorOjbects());
		}
		return 0;
	}
}
