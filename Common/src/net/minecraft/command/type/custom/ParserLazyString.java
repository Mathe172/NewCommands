package net.minecraft.command.type.custom;

import java.util.regex.Matcher;

import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.MatcherRegistry;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.CTypeParse;

public class ParserLazyString extends CTypeParse<String>// TODO:..., Completion...
{
	public final static MatcherRegistry lazyStringMatcher = new MatcherRegistry("\\G\\s*+(\"|\\\\[@\\$])");
	
	public static final CDataType<String> parser = new ParserLazyString();
	
	private ParserLazyString()
	{
	}
	
	@Override
	public ArgWrapper<String> parse(final Parser parser, final Context parserData) throws SyntaxErrorException, CompletionException
	{
		final Matcher m = parser.getMatcher(lazyStringMatcher);
		
		if (parser.findInc(m))
		{
			switch (m.group(1))
			{
			case "\"":
				return TypeIDs.String.wrap(ParsingUtilities.parseQuotedString(parser));
			case "\\@":
				return TypeIDs.String.selectorParser.parse(parser);
			case "\\$":
				return TypeIDs.String.labelParser.parse(parser);
			}
		}
		
		return TypeIDs.String.wrap(ParsingUtilities.parseLazyString(parser, ParsingUtilities.baseMatcher));
	}
}
