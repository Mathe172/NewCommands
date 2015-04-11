package net.minecraft.command.arg;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class PrimitiveParameter<T> extends CommandArg<T>
{
	public final T value;
	
	public PrimitiveParameter(final T value)
	{
		this.value = value;
	}
	
	@Override
	public final T eval(final ICommandSender sender) throws CommandException
	{
		return this.value;
	}
	
}
