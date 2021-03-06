package net.minecraft.command.construction;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.command.IPermission;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.descriptors.SelectorDescriptorDefault;
import net.minecraft.command.type.IDataType;
import net.minecraft.command.type.management.TypeID;

public class SelectorDescriptorConstructable extends SelectorDescriptorDefault
{
	private final SelectorConstructable constructable;
	
	public SelectorDescriptorConstructable(final List<IDataType<?>> unnamedTypes, final Map<String, IDataType<?>> namedTypes, final List<String> keyMapping, final SelectorConstructable constructable, final Set<TypeID<?>> resultTypes, final IPermission permission)
	{
		super(unnamedTypes, namedTypes, keyMapping, resultTypes, permission);
		this.constructable = constructable;
	}
	
	@Override
	public ArgWrapper<?> construct(final DefaultParserData parserData) throws SyntaxErrorException
	{
		return this.constructable.construct(parserData);
	}
}
