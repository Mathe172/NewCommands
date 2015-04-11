package net.minecraft.command.type.custom;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CTypeParse;

public class ParserDouble extends CTypeParse<Double>
{
	public static final Pattern doublePattern = Pattern.compile("\\G\\s*+([+-]?+(?=\\.?+\\d)\\d*+\\.?+\\d*+)");
	
	public static final ParserDouble parser = new ParserDouble();
	
	@Override
	public ArgWrapper<Double> parse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
	{
		final ArgWrapper<Double> ret = context.generalParse(parser, TypeIDs.Double);
		
		if (ret != null)
			return ret;
		
		final Matcher m = parser.doubleMatcher;
		
		if (parser.findInc(m))
			return TypeIDs.Double.wrap(Double.parseDouble(m.group(1)));
		
		throw parser.SEE("Unable to parse int around index ");
	}
	
}
