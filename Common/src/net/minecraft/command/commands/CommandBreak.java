package net.minecraft.command.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.construction.CommandConstructable;
import net.minecraft.command.construction.CommandDescriptorDefault.CParserData;

public class CommandBreak extends CommandArg<Integer>
{
	public static final CommandConstructable constructable = new CommandConstructable()
	{
		@Override
		public CommandArg<Integer> construct(final CParserData data) throws SyntaxErrorException
		{
			return command;
		}
	};
	
	public static final CommandBreak command = new CommandBreak();
	
	private CommandBreak()
	{
	}
	
	@Override
	public Integer eval(final ICommandSender sender) throws CommandException
	{
		throw BreakException.ex;
	}
	
	@SuppressWarnings("serial")
	public static class BreakException extends CommandException
	{
		public static final BreakException ex = new BreakException();
		
		private BreakException()
		{
			super("Break", null, null, new Object[0]);
		}
	}
}
