package net.minecraft.command.construction;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.descriptors.OperatorDescriptor.ListOperands;

public interface OperatorConstructable
{
	public ArgWrapper<?> construct(ListOperands operands) throws SyntaxErrorException;
}
