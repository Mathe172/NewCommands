package net.minecraft.command.type.custom;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletionData;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.CTypeCompletable;
import net.minecraft.entity.EntityList;

public class TypeEntityId extends CTypeCompletable<String>
{
	public static final CDataType<String> type = new TypeEntityId();
	
	@Override
	public ArgWrapper<String> iParse(final Parser parser) throws SyntaxErrorException, CompletionException
	{
		return ParserName.parser.parse(parser);
	}
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
	{
		TabCompletionData.addToSet(tcDataSet, parser.toParse, startIndex, cData, EntityList.completions);
	}
}
