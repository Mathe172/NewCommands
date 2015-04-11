package net.minecraft.command.type.custom;

import java.util.regex.Matcher;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IParse;
import net.minecraft.command.type.base.CustomParse;

public class ParserCommands
{
	public static final IParse<CommandArg<Integer>> parser = new CustomParse<CommandArg<Integer>>()
	{
		@Override
		public CommandArg<Integer> parse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
		{
			return ParserCommands.parse(parser, false);
		}
	};
	
	public static final IParse<CommandArg<Integer>> parserInParenths = new CustomParse<CommandArg<Integer>>()
	{
		@Override
		public CommandArg<Integer> parse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
		{
			return ParserCommands.parse(parser, true);
		}
	};
	
	public static final CommandArg<Integer> parse(final Parser parser, final boolean inParenths) throws SyntaxErrorException, CompletionException
	{
		// Ending matcher is still in the right state
		final Matcher endingMatcher = parser.endingMatcher;
		
		while (true)
		{
			// Whitespaces are processed by PreCommandParser
			final CommandArg<Integer> lastCommand = ParserPreCommand.parserInternal.parse(parser);
			
			// endingMatcher is in the required state (see above)
			final String endingChar = endingMatcher.group(2);
			
			if (")".equals(endingChar))
			{
				if (inParenths)
					parser.incIndex(1);
				
				// endingMatcher will be returned to valid state in ParserPreCommand if necessary
				return lastCommand;
			}
			
			if ("".equals(endingChar) || "]".equals(endingChar))
			{
				if (inParenths)
					throw parser.SEE("Expected ')' around index ");
				
				return lastCommand;
			}
			
			parser.incIndex(1);
			
			parser.addToProcess(lastCommand);
			
			parser.addIgnoreErrors(";".equals(endingChar));
		}
	}
}
