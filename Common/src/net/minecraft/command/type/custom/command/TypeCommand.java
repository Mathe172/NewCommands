package net.minecraft.command.type.custom.command;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.CTypeParse;

public final class TypeCommand
{
	private TypeCommand()
	{
	}
	
	public static final CDataType<Integer> parser = new CTypeParse<Integer>()
	{
		@Override
		public ArgWrapper<Integer> parse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
		{
			return TypeIDs.Integer.wrap(parser.parseInit(ParserCommands.parser));
		}
	};
	
	/**
	 * Parses a single command (to parse multiple commands, enclose them in parentheses). This parser is "non-greedy"
	 */
	public static final CDataType<Integer> parserSingleCmd = new CTypeParse<Integer>()
	{
		@Override
		public ArgWrapper<Integer> parse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
		{
			return TypeIDs.Integer.wrap(parser.parseInit(ParserPreCommand.parser));
		}
	};
}
