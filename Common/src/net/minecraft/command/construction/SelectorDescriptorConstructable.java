package net.minecraft.command.construction;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.descriptors.SelectorDescriptorDefault;
import net.minecraft.command.type.IDataType;
import net.minecraft.command.type.TypeID;

public class SelectorDescriptorConstructable extends SelectorDescriptorDefault
{
	private final SelectorConstructable constructable;
	
	public SelectorDescriptorConstructable(final List<IDataType<?>> unnamedTypes, final Map<String, IDataType<?>> namedTypes, final SelectorConstructable constructable, final Set<TypeID<?>> resultTypes)
	{
		super(unnamedTypes, namedTypes, resultTypes);
		this.constructable = constructable;
	}
	
	@Override
	public ArgWrapper<?> construct(final List<ArgWrapper<?>> unnamedParams, final Map<String, ArgWrapper<?>> namedParams) throws SyntaxErrorException
	{
		return this.constructable.construct(unnamedParams, namedParams);
	}
}
