package net.minecraft.command.type.metadata;

import net.minecraft.command.parser.Parser;

public abstract class MetaEntry<T, D> implements MetaProvider<D>
{
	public final MetaID<T> id;
	
	public MetaEntry(final MetaID<T> id)
	{
		this.id = id;
	}
	
	@Override
	public boolean canProvide(final MetaID<?> metaID)
	{
		return this.id == metaID;
	}
	
	public abstract T get(final Parser parser, final D parserData);
	
	// Checked...
	@Override
	@SuppressWarnings("unchecked")
	public <U> U getData(final MetaID<U> id, final Parser parser, final D parserData)
	{
		return this.id == id ? (U) this.get(parser, parserData) : null;
	}
	
	public static class PrimitiveHint extends MetaEntry<PrimitiveHint, Void>
	{
		public PrimitiveHint(final MetaID<PrimitiveHint> id)
		{
			super(id);
		}
		
		@Override
		public PrimitiveHint get(final Parser parser, final Void parserData)
		{
			return this;
		}
	}
}