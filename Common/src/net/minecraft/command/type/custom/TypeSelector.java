package net.minecraft.command.type.custom;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.PermissionWrapper;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.parser.CacheID;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.ICachedParse;
import net.minecraft.command.type.base.CustomCompletable;
import net.minecraft.command.type.management.CConvertable;

public class TypeSelector<T> extends CustomCompletable<T> implements ICachedParse
{
	private final CConvertable<?, T> target;
	
	public TypeSelector(final CConvertable<?, T> target)
	{
		this.target = target;
	}
	
	@Override
	public T iParse(final Parser parser, final Context context) throws SyntaxErrorException
	{
		return parser.parseCached(this, context, CacheID.selectorCache).convertTo(parser, this.target);
	}
	
	@Override
	public ArgWrapper<?> iCachedParse(final Parser parser, final Context context) throws SyntaxErrorException
	{
		return TypeUntypedSelector.parseName(parser).parse(parser);
	}
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
	{
		PermissionWrapper.complete(tcDataSet, startIndex, cData, this.target.getSelectorCompletions());
	}
}
