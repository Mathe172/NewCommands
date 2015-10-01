package net.minecraft.command.type;

import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Parser;

public interface IExComplete<D>
{
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData, D parserData);
}
