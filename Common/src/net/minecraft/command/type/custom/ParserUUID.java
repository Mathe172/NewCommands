package net.minecraft.command.type.custom;

import java.util.regex.Matcher;

import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CTypeParse;

public class ParserUUID extends CTypeParse<String>
{
	public static final ParserUUID parser = new ParserUUID();
	
	@Override
	public ArgWrapper<String> parse(final Parser parser) throws SyntaxErrorException, CompletionException
	{
		final ArgWrapper<String> ret = ParsingUtilities.generalParse(parser, TypeIDs.UUID);
		
		if (ret != null)
			return ret;
		
		final Matcher m = parser.stringMatcher;
		
		if (parser.findInc(m))
			return new ArgWrapper<>(TypeIDs.UUID, m.group(1));
		
		throw parser.SEE("Expected identifier around index ");
	}
}
