package net.minecraft.command.type.custom;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CTypeParse;

public final class TypeNull extends CTypeParse<Void>
{
	public static final CTypeParse<Void> parser = new TypeNull(); // TODO: Integrate into TypeAlternatives
	
	private TypeNull()
	{
	};
	
	@Override
	public ArgWrapper<Void> iParse(final Parser parser, final Context parserData) throws SyntaxErrorException
	{
		return null;
	}
}
