package net.minecraft.command.type.custom;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CTypeParse;

public class ParserInt extends CTypeParse<Integer>
{
	public static final Pattern IntegerPatternConverter = Pattern.compile("\\G\\s*+([+-]?+\\d++)");
	
	public static final ParserInt intParser = new ParserInt();
	
	@Override
	public ArgWrapper<Integer> parse(final Parser parser) throws SyntaxErrorException, CompletionException
	{
		final ArgWrapper<Integer> ret = ParsingUtilities.generalParse(parser, TypeIDs.Integer);
		
		if (ret != null)
			return ret;
		
		final Matcher m = parser.intMatcher;
		
		if (parser.findInc(m))
			return new ArgWrapper<>(TypeIDs.Integer, Integer.parseInt(m.group(1)));
		
		throw parser.SEE("Unable to parse int around index ");
	}
	
}
