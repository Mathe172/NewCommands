package net.minecraft.command.type.base;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IType;

public abstract class ExCustomCompletable<R, D> extends ExCustomParse<R, D> implements IType<R, D>
{
	@Override
	public final R parse(final Parser parser, final D parserData) throws SyntaxErrorException, CompletionException
	{
		return parser.parse(this, parserData);
	}
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData, final D parserData)
	{
	}
}