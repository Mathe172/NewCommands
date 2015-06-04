package net.minecraft.command.type.management;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.parser.Parser;

public class CConvertableUnwrapped<T> extends CConvertable<T, T>
{
	public CConvertableUnwrapped(final String name)
	{
		super(name);
	}
	
	@Override
	public T convertFrom(final Parser parser, final ArgWrapper<?> toConvert) throws SyntaxErrorException
	{
		return toConvert.iConvertTo(parser, this);
	}
}
