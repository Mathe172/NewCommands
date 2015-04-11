package net.minecraft.command;

import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.parser.Parser;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandHandler implements ICommandManager
{
	private static final Logger logger = LogManager.getLogger();
	@SuppressWarnings("unused")
	private static final String __OBFID = "CL_00001765";
	
	@Override
	public int executeCommand(final ICommandSender sender, final CommandArg<Integer> command)
	{
		try
		{
			return command.eval(sender);
		} catch (final CommandException e)
		{
			final ChatComponentTranslation errorMsg = new ChatComponentTranslation(e.getMessage(), e.getErrorOjbects());
			errorMsg.getChatStyle().setColor(EnumChatFormatting.RED);
			sender.addChatMessage(errorMsg);
		} catch (final Throwable t)
		{
			final ChatComponentTranslation errorMsg = new ChatComponentTranslation("commands.generic.exception");
			errorMsg.getChatStyle().setColor(EnumChatFormatting.RED);
			sender.addChatMessage(errorMsg);
			logger.error("Couldn\'t process command: \'" + command + "\'", t);
		}
		return 0;
	}
	
	@Override
	public int executeCommand(final ICommandSender sender, final String command)
	{
		return this.executeCommand(sender, command, 0);
	}
	
	@Override
	public int executeCommand(final ICommandSender sender, final String command, final int startIndex)
	{
		try
		{
			return this.executeCommand(sender, Parser.parseCommand(command, startIndex));
		} catch (final SyntaxErrorException e)
		{
			final ChatComponentTranslation errorMsg = new ChatComponentTranslation(e.getMessage());
			errorMsg.getChatStyle().setColor(EnumChatFormatting.RED);
			sender.addChatMessage(errorMsg);
		}
		return 0;
	}
}
