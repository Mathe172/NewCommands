package net.minecraft.command.construction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.command.descriptors.SelectorDescriptor;
import net.minecraft.command.type.IDataType;
import net.minecraft.command.type.management.TypeID;

public class SelectorConstructor
{
	private final List<IDataType<?>> unnamedTypes = new ArrayList<>();
	private final Map<String, IDataType<?>> namedTypes = new HashMap<>();
	
	private final Set<TypeID<?>> resultTypes;
	
	public SelectorConstructor(final TypeID<?>... resultTypes)
	{
		this.resultTypes = new HashSet<>(resultTypes.length);
		
		for (final TypeID<?> resultType : resultTypes)
			this.resultTypes.add(resultType);
	}
	
	public final SelectorConstructor then(final IDataType<?> dataType)
	{
		this.unnamedTypes.add(dataType);
		return this;
	}
	
	public final SelectorConstructor named(final String name, final IDataType<?> dataType)
	{
		this.namedTypes.put(name, dataType);
		return this;
	}
	
	public final SelectorConstructor then(final String name, final IDataType<?> dataType)
	{
		this.then(dataType);
		return this.named(name, dataType);
	}
	
	public SelectorDescriptor<?> construct(final SelectorConstructable constructable)
	{
		return new SelectorDescriptorConstructable(this.unnamedTypes, this.namedTypes, constructable, this.resultTypes);
	}
}
