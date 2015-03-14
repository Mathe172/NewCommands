package net.minecraft.command.type;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Parser;

public interface IParse<R>
{
	public R parse(final Parser parser) throws SyntaxErrorException, CompletionException;
	
	public R parseSnapshot(final Parser parser) throws SyntaxErrorException, CompletionException;
}
