package net.minecraft.command.type.base;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IParse;

public abstract class CustomParse<R> extends ExCustomParse<R, Void> implements IParse<R>
{
	@Override
	public final R parse(final Parser parser, final Void parserData) throws SyntaxErrorException, CompletionException
	{
		return this.parse(parser);
	}
	
	@Override
	public final R parseSnapshot(final Parser parser) throws SyntaxErrorException, CompletionException
	{
		return this.parseSnapshot(parser, null);
	}
	
}
