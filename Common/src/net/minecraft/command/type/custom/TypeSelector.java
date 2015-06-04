package net.minecraft.command.type.custom;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.PermissionWrapper;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.parser.CacheID;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IComplete;
import net.minecraft.command.type.IType;
import net.minecraft.command.type.base.CustomParse;
import net.minecraft.command.type.management.CConvertable;

public class TypeSelector<T> extends CustomParse<T> implements IType<ArgWrapper<?>, Context>, IComplete // TODO:...
{
	private final CConvertable<?, T> target;
	
	public TypeSelector(final CConvertable<?, T> target)
	{
		this.target = target;
	}
	
	@Override
	public T parse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
	{
		return parser.parseCached(this, context, CacheID.selectorCache).convertTo(parser, this.target);
	}
	
	@Override
	public ArgWrapper<?> iParse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
	{
		return TypeUntypedSelector.parseName(parser).parse(parser);
	}
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
	{
		PermissionWrapper.complete(tcDataSet, startIndex, cData, this.target.getSelectorCompletions());
	}
	
	@Override
	public final void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData, final Context context)
	{
		this.complete(tcDataSet, parser, startIndex, cData);
	}
}
