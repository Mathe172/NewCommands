package net.minecraft.command.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.arg.CounterArg;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.construction.CommandConstructable;
import net.minecraft.command.descriptors.CommandDescriptor.ParserData;

public class CommandFor extends CommandArg<Integer>
{
	public static final CommandConstructable constructable = new CommandConstructable()
	{
		@Override
		public CommandFor construct(final ParserData data)
		{
			return new CommandFor(
				(CounterArg) data.get(0).get(TypeIDs.Integer),
				data.get(1).get(TypeIDs.Integer),
				false);
		}
	};
	
	public static final CommandConstructable ignoreErrorConstructable = new CommandConstructable()
	{
		@Override
		public CommandFor construct(final ParserData data)
		{
			return new CommandFor(
				(CounterArg) data.get(0).get(TypeIDs.Integer),
				data.get(1).get(TypeIDs.Integer),
				true);
		}
	};
	
	private final CounterArg counter;
	private final CommandArg<Integer> command;
	private final boolean ignoreErrors;
	
	public CommandFor(final CounterArg counter, final CommandArg<Integer> command, final boolean ignoreErrors)
	{
		this.counter = counter;
		this.command = command;
		this.ignoreErrors = ignoreErrors;
	}
	
	@Override
	public Integer eval(final ICommandSender sender) throws CommandException
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
