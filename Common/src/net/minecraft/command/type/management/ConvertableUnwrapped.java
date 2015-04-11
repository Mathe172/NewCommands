package net.minecraft.command.type.management;

import net.minecraft.command.CommandException;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;

public class ConvertableUnwrapped<T, E extends CommandException> extends Convertable<T, T, E>
{
	public ConvertableUnwrapped(final String name)
	{
		super(name);
	}
	
	@Override
	public T convertFrom(final ArgWrapper<?> toConvert) throws E, SyntaxErrorException
	{
		return toConvert.iConvertTo(this);
	}
}
