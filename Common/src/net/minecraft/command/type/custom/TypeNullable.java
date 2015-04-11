package net.minecraft.command.type.custom;

import java.util.regex.Matcher;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IDataType;
import net.minecraft.command.type.TypeParse;

public class TypeNullable<R extends ArgWrapper<?>> extends TypeParse<R>
{
	private final IDataType<R> type;
	
	public TypeNullable(IDataType<R> type)
	{
		this.type = type;
	}
	
	@Override
	public R parse(Parser parser, Context parserData) throws SyntaxErrorException, CompletionException
	{
		final Matcher m = parser.listEndMatcher;
		
		if (parser.find(m) && !m.group(1).equals("}"))
			return null;
		
		return this.type.parse(parser);
	}
}
