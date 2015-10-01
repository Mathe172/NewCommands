package net.minecraft.command.type.metadata;

import net.minecraft.command.parser.Parser;

public interface MetaProvider<D>
{
	public <T> T getData(final MetaID<T> id, final Parser parser, final D parserData);
	
	public boolean canProvide(final MetaID<?> metaID);
}