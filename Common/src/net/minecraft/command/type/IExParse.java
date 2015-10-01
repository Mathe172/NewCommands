package net.minecraft.command.type;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.metadata.IMetadata;

public interface IExParse<R, D> extends IMetadata<D>
{
	public R parse(final Parser parser, D parserData) throws SyntaxErrorException;
	
	public R parseSnapshot(final Parser parser, D parserData) throws SyntaxErrorException;
}
