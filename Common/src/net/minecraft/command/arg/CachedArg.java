package net.minecraft.command.arg;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class CachedArg<T> extends CommandArg<T> implements Processable
{
	
	private T value;
	private final CommandArg<? extends T> arg;
	
	public CachedArg(final CommandArg<? extends T> arg)
	{
		this.arg = arg;
	}
	
	@Override
	public void process(final ICommandSender sender) throws CommandException
	{
		this.value = this.arg.eval(sender);
	}
	
	@Override
	public T eval(final ICommandSender sender) throws CommandException
	{
		if (this.value == null)
			throw new CommandException("Could not evaluate chached Argument: No value set", new Object[] {});
		return this.value;
	}
}
