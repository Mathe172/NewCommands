package net.minecraft.command.type;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Parser;

public interface IExParse<R, D>
{
	public R parse(final Parser parser, D parserData) throws SyntaxErrorException, CompletionException;
	
	public R parseSnapshot(final Parser parser, D parserData) throws SyntaxErrorException, CompletionException;
}
