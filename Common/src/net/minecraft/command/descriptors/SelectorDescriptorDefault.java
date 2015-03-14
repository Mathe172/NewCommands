package net.minecraft.command.descriptors;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.command.completion.TabCompletion;
import net.minecraft.command.type.IDataType;
import net.minecraft.command.type.TypeID;

public abstract class SelectorDescriptorDefault extends SelectorDescriptor
{
	private final List<IDataType<?>> unnamedTypes;
	private final Map<String, IDataType<?>> namedTypes;
	private final Set<TabCompletion> keyCompletions;
	
	public SelectorDescriptorDefault(final List<IDataType<?>> unnamedTypes, final Map<String, IDataType<?>> namedTypes, final Set<TypeID<?>> resultTypes)
	{
		super(resultTypes);
		this.unnamedTypes = unnamedTypes;
		this.namedTypes = namedTypes;
		this.keyCompletions = new HashSet<>();
		
		for (final String key : namedTypes.keySet())
		{
			final String s = key + "=";
			this.keyCompletions.add(new TabCompletion(s, s, key));
		}
	}
	
	@Override
	public IDataType<?> getRequiredType(final int index)
	{
		return this.unnamedTypes.size() > index ? this.unnamedTypes.get(index) : null;
	}
	
	@Override
	public IDataType<?> getRequiredType(final String key)
	{
		return this.namedTypes.get(key);
	}
	
	@Override
	public Set<TabCompletion> getKeyCompletions()
	{
		return this.keyCompletions;
	}
}
