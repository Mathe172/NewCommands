package net.minecraft.command.type.custom;

import java.util.regex.Matcher;

import net.minecraft.command.MatcherRegistry;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CTypeParse;

public final class ParserDouble extends CTypeParse<Double>
{
	public static final MatcherRegistry doubleMatcher = new MatcherRegistry("\\G\\s*+([+-]?+(?=\\.?+\\d)\\d*+\\.?+\\d*+)");
	
	public static final ParserDouble parser = new ParserDouble();
	
	private ParserDouble()
	{
	}
	
	@Override
	public ArgWrapper<Double> parse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
	{
		final ArgWrapper<Double> ret = context.generalParse(parser, TypeIDs.Double);
		
		if (ret != null)
			return ret;
		
		final Matcher m = parser.getMatcher(doubleMatcher);
		
		if (parser.findInc(m))
			return TypeIDs.Double.wrap(Double.parseDouble(m.group(1)));
		
		throw parser.SEE("Unable to parse int around index ");
	}
	
}
