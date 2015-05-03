package net.minecraft.command.type.base;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.completion.ProviderCompleter;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CListProvider;
import net.minecraft.command.type.CTypeCompletable;
import net.minecraft.command.type.IComplete;
import net.minecraft.command.type.IDataType;

public class CompoundType<R> extends CTypeCompletable<R>
{
	private final IDataType<ArgWrapper<R>> tParser;
	private final IComplete completer;
	
	public CompoundType(final IDataType<ArgWrapper<R>> tParser, final IComplete completer)
	{
		this.tParser = tParser;
		this.completer = completer;
	}
	
	public CompoundType(final IDataType<ArgWrapper<R>> tParser, final CListProvider provider)
	{
		this.tParser = tParser;
		this.completer = new ProviderCompleter(provider);
	}
	
	@Override
	public ArgWrapper<R> iParse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
	{
		return this.tParser.parse(parser, context);
	}
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
	{
		this.completer.complete(tcDataSet, parser, startIndex, cData);
	}
}
