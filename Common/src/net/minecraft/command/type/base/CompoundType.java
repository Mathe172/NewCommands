package net.minecraft.command.type.base;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.completion.ProviderCompleter;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CListProvider;
import net.minecraft.command.type.CTypeParse;
import net.minecraft.command.type.IComplete;
import net.minecraft.command.type.IDataType;
import net.minecraft.command.type.metadata.ICompletable;

public class CompoundType<R> extends CTypeParse<R>
{
	private final IDataType<ArgWrapper<R>> tParser;
	
	public CompoundType(final IDataType<ArgWrapper<R>> tParser, final IComplete completer)
	{
		this.tParser = tParser;
		this.addEntry(new ICompletable.Default(completer));
	}
	
	public CompoundType(final IDataType<ArgWrapper<R>> tParser, final CListProvider provider)
	{
		this(tParser, new ProviderCompleter(provider));
	}
	
	@Override
	public ArgWrapper<R> iParse(final Parser parser, final Context context) throws SyntaxErrorException
	{
		return this.tParser.parse(parser, context);
	}
}
