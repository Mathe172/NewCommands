package net.minecraft.command.type.custom;

import java.util.regex.Matcher;

import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CTypeParse;

public class ParserName extends CTypeParse<String>
{
	public static final ParserName parser = new ParserName();
	
	@Override
	public ArgWrapper<String> parse(final Parser parser) throws SyntaxErrorException, CompletionException
	{
		final ArgWrapper<String> ret = ParsingUtilities.generalParse(parser, TypeIDs.String);
		
		if (ret != null)
			return ret;
		
		final Matcher m = parser.stringMatcher;
		
		if (parser.findInc(m))
			return new ArgWrapper<>(TypeIDs.String, m.group(1));
		
		throw parser.SEE("Expected identifier around index ");
	}
}
