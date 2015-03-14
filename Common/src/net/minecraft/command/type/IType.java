package net.minecraft.command.type;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Parser;

public interface IType<R, D>
{
	public R iParse(Parser parser, D parserData) throws SyntaxErrorException, CompletionException;
	
	public void complete(TCDSet tcDataSet, Parser parser, int startIndex, CompletionData cData, D parserData);
}
