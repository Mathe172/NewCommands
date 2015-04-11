package net.minecraft.command.type.base;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IParse;

public abstract class CustomParse<R> extends ExCustomParse<R, Context> implements IParse<R>
{
	@Override
	public final R parse(final Parser parser) throws SyntaxErrorException, CompletionException
	{
		return this.parse(parser, parser.defContext);
	}
	
	@Override
	public final R parseSnapshot(final Parser parser) throws SyntaxErrorException, CompletionException
	{
		return this.parseSnapshot(parser, parser.defContext);
	}
	
}
