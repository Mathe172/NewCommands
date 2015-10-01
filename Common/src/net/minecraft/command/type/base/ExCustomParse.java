package net.minecraft.command.type.base;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.collections.MetaColl;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IExParse;
import net.minecraft.command.type.metadata.Metadata;

public abstract class ExCustomParse<R, D> extends Metadata<D> implements IExParse<R, D>
{
	public ExCustomParse()
	{
		super(MetaColl.typeSPA);
	}
	
	@Override
	public final R parse(final Parser parser, final D parserData) throws SyntaxErrorException
	{
		final boolean toPop = parser.pushMetadata(this, parserData);
		
		final R ret = this.iParse(parser, parserData);
		
		if (toPop)
			parser.popMetadata(this);
		
		return ret;
	}
	
	public abstract R iParse(Parser parser, D parserData) throws SyntaxErrorException;
	
	@Override
	public final R parseSnapshot(final Parser parser, final D parserData) throws SyntaxErrorException
	{
		return parser.parseSnapshot(this, parserData);
	}
}
