package net.minecraft.command.type;

import java.util.Set;

import net.minecraft.command.completion.TabCompletion;
import net.minecraft.command.parser.Parser;

public abstract class CListProvider
{
	public abstract Set<TabCompletion> getList(Parser parser);
}
