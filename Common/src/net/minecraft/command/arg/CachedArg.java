package net.minecraft.command.arg;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.arg.CommandArg.Primitive;

public class CachedArg<T> extends Primitive<T> implements Setter<T>
{
	private T value;
	
	public final String name;
	
	public CachedArg(final String name)
	{
		this.name = name;
	}
	
	@Override
	public T eval(final ICommandSender sender) throws CommandException
	{
		return this.get();
	}
	
	@Override
	public T get() throws CommandException
	{
		if (this.value == null)
			throw new CommandException("Could not evaluate '" + this.name + "': No value set");
		return this.value;
	}
	
	@Override
	public void set(final T value)
	{
		this.value = value;
	}
	
	@Override
	public CommandArg<T> commandArg()
	{
		return this;
	}
	
	public static class Initialized<T> extends CachedArg<T>
	{
		private final CommandArg<T> arg;
		
		public Initialized(final CommandArg<T> arg)
		{
			super("CachedArg");
			this.arg = arg;
		}
		
		@Override
		public void process(final ICommandSender sender) throws CommandException
		{
			this.set(this.arg.eval(sender));
		}
	}
}
