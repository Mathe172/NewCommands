package net.minecraft.command.type.custom;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.CTypeParse;

public class TypeCommand
{
	public static final CDataType<Integer> parser = new CTypeParse<Integer>()
	{
		@Override
		public ArgWrapper<Integer> parse(final Parser parser) throws SyntaxErrorException, CompletionException
		{
			return new ArgWrapper<>(TypeIDs.Integer, parser.parseInit(ParserCommands.parser));
		}
	};
	
	/**
	 * Parses a single command (to parse multiple commands, enclose them in parentheses). This parser is "non-greedy"
	 */
	public static final CDataType<Integer> parserSingleCmd = new CTypeParse<Integer>()
	{
		@Override
		public ArgWrapper<Integer> parse(final Parser parser) throws SyntaxErrorException, CompletionException
		{
			return new ArgWrapper<>(TypeIDs.Integer, parser.parseInit(ParserPreCommand.parser));
		}
	};
}
