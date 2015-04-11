package net.minecraft.command.type;

import java.util.Set;

import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.parser.Parser;

public abstract class CListProvider
{
	public abstract Set<ITabCompletion> getList(Parser parser);
}
