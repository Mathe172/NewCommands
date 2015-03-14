package net.minecraft.command.arg;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class PrimitiveParameter<T> extends CommandArg<T>
{
	
	protected final T value;
	
	public PrimitiveParameter(T value)
	{
		this.value = value;
	}
	
	@Override
	public T eval(ICommandSender sender) throws CommandException
	{
		return this.value;
	}
	
}
