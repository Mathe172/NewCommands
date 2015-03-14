package net.minecraft.command.type.base;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IExParse;

public abstract class ExCustomParse<R, D> implements IExParse<R, D>
{
	@Override
	public final R parseSnapshot(final Parser parser, final D parserData) throws SyntaxErrorException, CompletionException
	{
		return parser.parseSnapshot(this, parserData);
	}
}
