package net.minecraft.command.type.metadata;

public interface IMetadata<D> extends MetaProvider<D>
{
	public void addEntry(final MetaEntry<?, ? super D> entry);
	
	public <T> MetaEntry<T, ? super D> getEntry(final MetaID<T> metaID);
}
