package net.minecraft.command.type.custom;

import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CTypeParse;

public class ParserUUID extends CTypeParse<String>
{
	@Override
	public ArgWrapper<String> parse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
	{
		final ArgWrapper<String> ret = ParsingUtilities.parseString(parser, context, TypeIDs.UUID);
		
		if (ret != null)
			return ret;
		
		throw parser.SEE("Expected UUID around index ");
	}
}
