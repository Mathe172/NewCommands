package net.minecraft.command.type;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;

public interface ICachedParse
{
	public ArgWrapper<?> iCachedParse(Parser parser, Context context) throws SyntaxErrorException;
}
