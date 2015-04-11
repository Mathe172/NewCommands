package net.minecraft.command.construction;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.type.custom.TypeSelectorContent.ParserData;

public interface SelectorConstructable
{
	public ArgWrapper<?> construct(ParserData parserData) throws SyntaxErrorException;
}
