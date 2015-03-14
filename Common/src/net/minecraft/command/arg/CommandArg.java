package net.minecraft.command.arg;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public abstract class CommandArg<T>
{
	/**
	 * DO NOT modify the return value in any way!
	 */
	public abstract T eval(ICommandSender sender) throws CommandException;
	
	//
	// /**
	// * Equivalent to SuppressWarnings("unchecked")
	// */
	// @SuppressWarnings("unchecked")
	// public final <U> CommandArg<U> get()
	// {
	// return (CommandArg<U>) this;
	// }
	//
	// /**
	// * Equivalent to SuppressWarnings("unchecked")
	// */
	// @SuppressWarnings("unchecked")
	// public static final <U> CommandArg<U> get(final CommandArg<?> commandArg)
	// {
	// return (CommandArg<U>) commandArg;
	// }
	//
	// /**
	// * Equivalent to SuppressWarnings("unchecked")
	// */
	// @SuppressWarnings("unchecked")
	// public static final <U> CommandArg<U> get(final List<CommandArg<?>> commandArgs, final int index)
	// {
	// return index < commandArgs.size() ? (CommandArg<U>) commandArgs.get(index) : null;
	// }
	
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
}