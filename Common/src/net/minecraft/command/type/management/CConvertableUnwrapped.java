package net.minecraft.command.type.management;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;

public class CConvertableUnwrapped<T> extends CConvertable<T, T>
{
	public CConvertableUnwrapped(final String name)
	{
		super(name);
	}
	
	@Override
	public T convertFrom(final ArgWrapper<?> toConvert) throws SyntaxErrorException
	{
		return toConvert.iConvertTo(this);
	}
}
