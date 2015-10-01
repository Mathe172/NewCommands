package net.minecraft.command.construction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections4.trie.PatriciaTrie;

import net.minecraft.command.IPermission;
import net.minecraft.command.descriptors.SelectorDescriptor;
import net.minecraft.command.type.IDataType;
import net.minecraft.command.type.management.TypeID;

public class SelectorConstructor
{
	private final ArrayList<IDataType<?>> unnamedTypes = new ArrayList<>();
	private final PatriciaTrie<IDataType<?>> namedTypes = new PatriciaTrie<>();
	
	private final ArrayList<String> keyMapping = new ArrayList<>();
	
	private final Set<TypeID<?>> resultTypes;
	private final IPermission permission;
	
	public SelectorConstructor(final IPermission permission, final TypeID<?>... resultTypes)
	{
		this.resultTypes = new HashSet<>(Arrays.asList(resultTypes));
		this.permission = permission;
	}
	
	public final SelectorConstructor then(final IDataType<?> dataType)
	{
		this.unnamedTypes.add(dataType);
		this.keyMapping.add(null);
		
		return this;
	}
	
	public final SelectorConstructor named(final String name, final IDataType<?> dataType)
	{
		this.namedTypes.put(name, dataType);
		return this;
	}
	
	public final SelectorConstructor then(final String name, final IDataType<?> dataType)
	{
		this.unnamedTypes.add(dataType);
		this.namedTypes.put(name, dataType);
		this.keyMapping.add(name.toLowerCase());
		
		return this;
	}
	
	public SelectorDescriptor<?> construct(final SelectorConstructable constructable)
	{
		this.unnamedTypes.trimToSize();
		this.keyMapping.trimToSize();
		return new SelectorDescriptorConstructable(this.unnamedTypes, this.namedTypes, this.keyMapping, constructable, this.resultTypes, this.permission);
	}
}
