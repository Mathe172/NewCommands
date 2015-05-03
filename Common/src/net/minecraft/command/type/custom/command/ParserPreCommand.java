package net.minecraft.command.type.custom.command;

import java.util.regex.Matcher;

import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IParse;
import net.minecraft.command.type.base.CustomParse;
import net.minecraft.command.type.custom.TypeUntypedSelector;

public final class ParserPreCommand
{
	private ParserPreCommand()
	{
	}
	
	public static final CommandArg<Integer> parse(final Parser parser, final boolean standalone) throws SyntaxErrorException, CompletionException
	{
		final Matcher oParenthMatcher = parser.getMatcher(ParsingUtilities.oParenthMatcher);
		
		if (parser.findInc(oParenthMatcher))
		{
			// state is correct after this call ('inParenths-block' in ParserCommands.parse)
			final CommandArg<Integer> ret = parser.parseInit(ParserCommands.parserInParenths);
			
			// If this is not the topmost instance of the command-parsing routine called, endingMatcher must be in a valid state
			if (!standalone)
			{
				final Matcher endingMatcher = parser.getMatcher(ParsingUtilities.endingMatcher);
				
				// Ensure that the endingMatcher is in the required state (whitespaces processed + match found)
				if (!parser.find(endingMatcher))
					throw parser.SEE("Expected ')' or end of string around index ");
				
				parser.incIndex(endingMatcher.group(1).length());
			}
			return ret;
		}
		
		final Matcher idMatcher = parser.getMatcher(ParsingUtilities.idMatcher);
		
		final IParse<ArgWrapper<?>> selectorParser = TypeUntypedSelector.parser;
		
		while (parser.findInc(idMatcher) && !"/".equals(idMatcher.group(1)))
		{
			selectorParser.parse(parser);
			if (!parser.checkSpace())
				throw parser.SEE("Missing space around index ");
		}
		
		// CommandsParser REQUIRES that endingMatcher is in the following state: whitespaces processed + found match (ensured by CommandParser)
		return ParserCommand.parser.parse(parser);
	}
	
	/** NEVER call this parser unless you know what you're doing! (see comments in code) */
	public static final IParse<CommandArg<Integer>> parserInternal = new CustomParse<CommandArg<Integer>>()
	{
		@Override
		public CommandArg<Integer> parse(final Parser parser, final Context parserData) throws SyntaxErrorException, CompletionException
		{
			return ParserPreCommand.parse(parser, false);
		}
	};
	
	public static final IParse<CommandArg<Integer>> parser = new CustomParse<CommandArg<Integer>>()
	{
		@Override
		public CommandArg<Integer> parse(final Parser parser, final Context parserData) throws SyntaxErrorException, CompletionException
		{
			return ParserPreCommand.parse(parser, true);
		}
	};
}
