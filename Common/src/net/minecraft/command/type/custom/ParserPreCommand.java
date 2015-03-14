package net.minecraft.command.type.custom;

import java.util.regex.Matcher;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IParse;
import net.minecraft.command.type.base.CustomParse;

public class ParserPreCommand extends CustomParse<CommandArg<Integer>>
{
	@Override
	public final CommandArg<Integer> parse(final Parser parser) throws SyntaxErrorException, CompletionException
	{
		final Matcher oParenthMatcher = parser.oParenthMatcher;
		
		if (parser.findInc(oParenthMatcher))
			// state is correct after this call ('inParenths-block' in ParserCommands.parse)
			return parser.parseInit(ParserCommands.parserInParenths);
		
		final Matcher idMatcher = parser.idMatcher;
		
		final IParse<ArgWrapper<?>> selectorParser = ParserUntypedSelector.parser;
		
		while (parser.findInc(idMatcher) && !"/".equals(idMatcher.group(1)))
		{
			selectorParser.parse(parser);
			if (!parser.checkSpace())
				throw parser.SEE("Missing space around index ");
		}
		
		// CommandsParser REQUIRES that endingMatcher is in the following state: whitespaces processed + found match (ensured by CommandParser)
		return ParserCommand.parser.parse(parser);
	}
	
	public static final IParse<CommandArg<Integer>> parser = new ParserPreCommand();
}
