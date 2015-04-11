package net.minecraft.command.type.custom;

import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CTypeParse;

public class ParserName extends CTypeParse<String>
{
	public static final ParserName parser = new ParserName();
	
	@Override
	public ArgWrapper<String> parse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
	{
		final ArgWrapper<String> ret = ParsingUtilities.parseString(parser, context, TypeIDs.String);
		
		if (ret != null)
			return ret;
		
		throw parser.SEE("Expected identifier around index ");
	}
}
