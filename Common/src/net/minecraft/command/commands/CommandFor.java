package net.minecraft.command.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.arg.Setter;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.commands.CommandBreak.BreakException;
import net.minecraft.command.construction.CommandConstructable;
import net.minecraft.command.descriptors.CommandDescriptor.CParserData;

public class CommandFor extends CommandArg<Integer>
{
	public static final CommandConstructable constructable = new CommandConstructable()
	{
		@Override
		public CommandFor construct(final CParserData data)
		{
			return new CommandFor(data, false);
		}
	};
	
	public static final CommandConstructable ignoreErrorConstructable = new CommandConstructable()
	{
		@Override
		public CommandFor construct(final CParserData data)
		{
			return new CommandFor(data, true);
		}
	};
	
	private final Setter<Integer> label;
	private final CommandArg<Integer> start, end, step;
	private final CommandArg<Integer> command;
	private final boolean ignoreErrors;
	
	public CommandFor(final CParserData data, final boolean ignoreErrors)
	{
		this.label = data.getLabel(TypeIDs.Integer);
		this.start = data.get(TypeIDs.Integer);
		this.end = data.get(TypeIDs.Integer);
		this.step = data.path.size() == (ignoreErrors ? 0 : 1) ? null : data.get(TypeIDs.Integer);
		this.command = data.get(TypeIDs.Integer);
		this.ignoreErrors = ignoreErrors;
	}
	
	@Override
	public Integer eval(final ICommandSender sender) throws CommandException
	{
		final int start = this.start.eval(sender);
		final int end = this.end.eval(sender);
		final int step = this.step == null ? 1 : this.step.eval(sender);
		
		int ret = 0;
		
		if (step == 0)
			throw new CommandException("Step size 0 not allowed");
		
		for (int index = start; step > 0 ? index <= end : index >= end; index += step)
		{
			this.label.set(index);
			
			if (!this.ignoreErrors)
			{
				ret += this.command.eval(sender);
				continue;
			}
			
			try
			{
				ret += this.command.eval(sender);
			} catch (final BreakException ex)
			{
				throw ex;
			} catch (final CommandException ex)
			{
			}
		}
		
		return ret;
	}
}
