package net.minecraft.command.arg;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public abstract class CommandArg<T>
{
	/**
	 * DO NOT modify the return value in any way!
	 */
	public abstract T eval(ICommandSender sender) throws CommandException;
	
	public final Processable processable()
	{
		return new Processable()
		{
			@Override
			public void process(final ICommandSender sender) throws CommandException
			{
				CommandArg.this.eval(sender);
			}
		};
	}
	
	public final CachedArg<T> cached()
	{
		return new CachedArg<>(this);
	}
	
	public static <T> T eval(final CommandArg<T> toEval, final ICommandSender sender) throws CommandException
	{
		return toEval == null ? null : toEval.eval(sender);
	}
	
}