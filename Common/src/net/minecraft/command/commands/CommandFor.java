package net.minecraft.command.commands;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.IPermission;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.arg.CounterArg;
import net.minecraft.command.construction.CommandConstructable;
import net.minecraft.command.type.custom.TypeIDs;

public class CommandFor extends CommandBase
{
	public static final CommandConstructable constructable = new CommandConstructable()
	{
		@Override
		public CommandFor construct(final List<ArgWrapper<?>> params, final IPermission permission)
		{
			return new CommandFor((CounterArg) params.get(0).get(TypeIDs.Integer), params.get(1).get(TypeIDs.Integer), permission, false);
		}
	};
	
	public static final CommandConstructable ignoreErrorConstructable = new CommandConstructable()
	{
		@Override
		public CommandFor construct(final List<ArgWrapper<?>> params, final IPermission permission)
		{
			return new CommandFor((CounterArg) params.get(0).get(TypeIDs.Integer), params.get(1).get(TypeIDs.Integer), permission, true);
		}
	};
	
	private final CounterArg counter;
	private final CommandArg<Integer> command;
	private final boolean ignoreErrors;
	
	public CommandFor(final CounterArg counter, final CommandArg<Integer> command, final IPermission permission, final boolean ignoreErrors)
	{
		super(permission);
		this.counter = counter;
		this.command = command;
		this.ignoreErrors = ignoreErrors;
	}
	
	@Override
	public int procCommand(final ICommandSender sender) throws CommandException
	{
		int ret = 0;
		while (this.counter.endNotReached())
		{
			try
			{
				ret += this.command.eval(sender);
			} catch (final CommandException ex)
			{
				if (!this.ignoreErrors)
					throw ex;
			}
			
			this.counter.inc();
		}
		
		return ret;
	}
}
