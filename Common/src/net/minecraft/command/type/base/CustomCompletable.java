package net.minecraft.command.type.base;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IComplete;
import net.minecraft.command.type.IType;

public abstract class CustomCompletable<R> extends CustomParse<R> implements IType<R, Context>, IComplete
{
	@Override
	public final R parse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
	{
		return parser.parse(this, context);
	}
	
	@Override
	public abstract R iParse(Parser parser, Context context) throws SyntaxErrorException, CompletionException;
	
	@Override
	public final void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData, final Context context)
	{
		this.complete(tcDataSet, parser, startIndex, cData);
	}
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
	{
	}
}