package net.minecraft.command.type.metadata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.metadata.MetaID.MetaType;

public class Metadata<D> implements IMetadata<D>
{
	private final MetaType type;
	
	private final List<MetaEntry<?, ? super D>> entries;
	private final Set<MetaID<?>> ownEntries;
	
	private Set<Metadata<? extends D>> childNodes;
	
	private Metadata(final MetaType type, final Set<MetaID<?>> ownEntries)
	{
		this.type = type;
		this.entries = new ArrayList<>(type.getCount());
		this.ownEntries = ownEntries;
	}
	
	public Metadata(final MetaType type)
	{
		this(type, (Set<MetaID<?>>) null);
	}
	
	public Metadata(final MetaType type, final Metadata<? super D> parent)
	{
		this(type, new HashSet<MetaID<?>>(type.getCount()));
		if (parent.type != type)
			throw new IllegalArgumentException("Metadata of type " + type.name + " can't inherit from data of type " + parent.type.name);
		
		parent.addChildNode(this);
	}
	
	private void addChildNode(final Metadata<? extends D> child)
	{
		if (this.childNodes == null)
			this.childNodes = new HashSet<>();
		
		this.childNodes.add(child);
		
		for (final MetaEntry<?, ? super D> entry : this.entries)
			if (entry != null)
				child.addParentEntry(entry);
	}
	
	private void addParentEntry(final MetaEntry<?, ? super D> entry)
	{
		if (!this.ownEntries.contains(entry.id))
			this.addEntry(entry);
	}
	
	// Checked...
	@Override
	@SuppressWarnings("unchecked")
	public final <T> MetaEntry<T, ? super D> getEntry(final MetaID<T> metaID)
	{
		if (this.type != metaID.type)
			return null;
		
		final int id = metaID.getId();
		
		return id < this.entries.size() ? (MetaEntry<T, ? super D>) this.entries.get(id) : null;
	}
	
	@Override
	public final boolean canProvide(final MetaID<?> metaID)
	{
		if (this.type != metaID.type)
			return false;
		
		final int id = metaID.getId();
		
		return id < this.entries.size() ? this.entries.get(id) != null : false;
	}
	
	@Override
	public final <T> T getData(final MetaID<T> metaID, final Parser parser, final D parserData)
	{
		final MetaEntry<T, ? super D> entry = this.getEntry(metaID);
		
		if (entry == null)
			return null;
		
		return entry.get(parser, parserData);
	}
	
	@Override
	public final void addEntry(final MetaEntry<?, ? super D> entry)
	{
		if (this.type != entry.id.type)
			throw new IllegalArgumentException("Metadata of type " + this.type.name + " can't store entries of type " + entry.id.type.name);
		
		final int id = entry.id.getId();
		
		if (this.ownEntries != null)
			this.ownEntries.add(entry.id);
		
		if (this.childNodes != null)
			for (final Metadata<? extends D> child : this.childNodes)
				child.addParentEntry(entry);
		
		if (id < this.entries.size())
		{
			this.entries.set(id, entry);
			return;
		}
		
		for (int i = this.entries.size(); i < id; ++i)
			this.entries.add(null);
		
		this.entries.add(entry);
	}
}
