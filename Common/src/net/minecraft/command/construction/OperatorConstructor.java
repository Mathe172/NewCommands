package net.minecraft.command.construction;

import java.util.ArrayList;
import java.util.Set;

import net.minecraft.command.IPermission;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.descriptors.OperatorDescriptor;
import net.minecraft.command.type.IDataType;
import net.minecraft.command.type.management.TypeID;

public class OperatorConstructor
{
	private final ArrayList<IDataType<?>> operands = new ArrayList<>();
	
	private final Set<TypeID<?>> resultTypes;
	private final IPermission permission;
	
	public OperatorConstructor(final IPermission permission, final TypeID<?>... resultTypes)
	{
		this.permission = permission;
		this.resultTypes = ParsingUtilities.toSet(resultTypes);
		
	}
	
	public OperatorConstructor(final IPermission permission, final Set<TypeID<?>> resultTypes)
	{
		this.permission = permission;
		this.resultTypes = resultTypes;
	}
	
	public final OperatorConstructor then(final IDataType<?> dataType)
	{
		this.operands.add(dataType);
		return this;
	}
	
	public OperatorDescriptor construct(final OperatorConstructable constructable)
	{
		this.operands.trimToSize();
		return new OperatorDescriptorConstructable(this.operands, constructable, this.resultTypes, this.permission);
	}
}
