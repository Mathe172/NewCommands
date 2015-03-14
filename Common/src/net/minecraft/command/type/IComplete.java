package net.minecraft.command.type;

import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Parser;

public interface IComplete
{
	public void complete(TCDSet tcDataSet, Parser parser, int startIndex, CompletionData cData);
}
