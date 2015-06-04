package net.minecraft.command.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.construction.CommandConstructable;
import net.minecraft.command.descriptors.CommandDescriptor.CParserData;

public class CommandIf extends CommandArg<Integer>
{
	public static final CommandConstructable constructable = new CommandConstructable()
	{
		@Override
		public CommandArg<Integer> construct(final CParserData data) throws SyntaxErrorException
		{
			return new CommandIf(
				data.get(TypeIDs.Boolean),
				data.get(TypeIDs.Integer));
		}
	};
	
	public static final CommandConstructable constructableElse = new CommandConstructable()
	{
		@Override
		public CommandArg<Integer> construct(final CParserData data) throws SyntaxErrorException
		{
			return new Else(
				data.get(TypeIDs.Boolean),
				data.get(TypeIDs.Integer),
				data.get(TypeIDs.Integer));
		}
	};
	
	private final CommandArg<Boolean> cond;
	private final CommandArg<Integer> command;
	
	public CommandIf(final CommandArg<Boolean> cond, final CommandArg<Integer> command)
	{
		this.cond = cond;
		this.command = command;
	}
	
	@Override
	public Integer eval(final ICommandSender sender) throws CommandException
	{
		if (this.cond.eval(sender))
			return this.command.eval(sender);
		
		return this.procElse(sender);
	}
	
	@SuppressWarnings("unused")
	protected Integer procElse(final ICommandSender sender) throws CommandException
	{
		return 0;
	};
	
	public static class Else extends CommandIf
	{
		private final CommandArg<Integer> commandElse;
		
		public Else(final CommandArg<Boolean> cond, final CommandArg<Integer> command, final CommandArg<Integer> commandElse)
		{
			super(cond, command);
			this.commandElse = commandElse;
		}
		
		@Override
		protected Integer procElse(final ICommandSender sender) throws CommandException
		{
			return this.commandElse.eval(sender);
		}
	}
}
