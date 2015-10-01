package net.minecraft.command.arg;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.arg.TypedWrapper.Getter;
import net.minecraft.command.type.management.TypeID;

public abstract class CommandArg<T> implements Processable
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
	
	public static <T> T eval(final CommandArg<T> toEval, final ICommandSender sender) throws CommandException
	{
		return toEval == null ? null : toEval.eval(sender);
	}
	
	public static <T> T get(final Getter<T> getter) throws CommandException
	{
		return getter == null ? null : getter.get();
	}
	
	@Override
	public void process(final ICommandSender sender) throws CommandException
	{
		this.eval(sender);
	}
	
	public ArgWrapper<T> wrap(final TypeID<T> type)
	{
		return ArgWrapper.create(type, this);
	}
	
	public static abstract class Primitive<T> extends CommandArg<T> implements Getter<T>
	{
	}
}