package net.minecraft.command.type.custom;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CTypeParse;

public class ParserInt extends CTypeParse<Integer>
{
	public static final Pattern intPattern = Pattern.compile("\\G\\s*+([+-]?+\\d++)");
	
	public static final ParserInt parser = new ParserInt();
	
	@Override
	public ArgWrapper<Integer> parse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
	{
		final ArgWrapper<Integer> ret = context.generalParse(parser, TypeIDs.Integer);
		
		if (ret != null)
			return ret;
		
		final Matcher m = parser.intMatcher;
		
		if (parser.findInc(m))
			return TypeIDs.Integer.wrap(Integer.parseInt(m.group(1)));
		
		throw parser.SEE("Unable to parse int around index ");
	}
	
}
