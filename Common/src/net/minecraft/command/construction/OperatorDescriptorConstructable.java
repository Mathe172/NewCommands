package net.minecraft.command.construction;

import java.util.List;
import java.util.Set;

import net.minecraft.command.IPermission;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.descriptors.OperatorDescriptor;
import net.minecraft.command.type.IDataType;
import net.minecraft.command.type.management.TypeID;

public class OperatorDescriptorConstructable extends OperatorDescriptor
{
	private final OperatorConstructable constructable;
	
	public OperatorDescriptorConstructable(final List<IDataType<?>> operands, final OperatorConstructable constructable, final Set<TypeID<?>> resultTypes, final IPermission permission)
	{
		super(resultTypes, permission, operands);
		this.constructable = constructable;
	}
	
	@Override
	public ArgWrapper<?> construct(final List<ArgWrapper<?>> operands) throws SyntaxErrorException
	{
		return this.constructable.construct(operands);
	}
}
