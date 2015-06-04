package net.minecraft.command.type.custom.command;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.arg.CompoundArg;
import net.minecraft.command.arg.Processable;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Parser;

public final class ParserCommands
{
	private ParserCommands()
	{
	}
	
	public static final CommandArg<Integer> parse(final Parser parser, final boolean inParenths) throws SyntaxErrorException, CompletionException
	{
		// Ending matcher is still in the right state
		final Matcher endingMatcher = parser.getMatcher(ParsingUtilities.endingMatcher);
		
		final List<Processable> toProcess = new ArrayList<>();
		final List<Boolean> ignoreErrors = new ArrayList<>();
		
		while (true)
		{
			// Whitespaces are processed by PreCommandParser
			final CommandArg<Integer> lastCommand = ParserPreCommand.parse(parser, toProcess, ignoreErrors, false);
			
			// endingMatcher is in the required state (see above)
			final String endingChar = endingMatcher.group(2);
			
			char endingChar_;
			
			if (endingChar.isEmpty() || ']' == (endingChar_ = endingChar.charAt(0)))
			{
				if (inParenths)
					throw parser.SEE("Expected ')' ");
				
				return CompoundArg.create(toProcess, ignoreErrors, lastCommand);
			}
			
			if (')' == endingChar_)
			{
				if (inParenths)
					parser.incIndex(1);
				
				// endingMatcher will be returned to valid state in ParserPreCommand if necessary
				return CompoundArg.create(toProcess, ignoreErrors, lastCommand);
			}
			
			parser.incIndex(1);
			
			toProcess.add(lastCommand);
			
			ignoreErrors.add(';' == endingChar_);
		}
	}
}
