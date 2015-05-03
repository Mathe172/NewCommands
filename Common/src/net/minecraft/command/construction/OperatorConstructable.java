package net.minecraft.command.construction;

import java.util.List;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;

public interface OperatorConstructable
{
	public ArgWrapper<?> construct(List<ArgWrapper<?>> operands) throws SyntaxErrorException;
}
