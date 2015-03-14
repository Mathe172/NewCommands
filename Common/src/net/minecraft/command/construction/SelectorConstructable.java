package net.minecraft.command.construction;

import java.util.List;
import java.util.Map;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;

public interface SelectorConstructable
{
	public ArgWrapper<?> construct(List<ArgWrapper<?>> unnamedParams, Map<String, ArgWrapper<?>> namedParams) throws SyntaxErrorException;
}
