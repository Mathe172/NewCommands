package net.minecraft.command.type.custom.nbt;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletionData;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IParse;
import net.minecraft.command.type.base.CustomCompletable;
import net.minecraft.command.type.custom.TypeIDs;
import net.minecraft.command.type.custom.TypeUntypedLabel;
import net.minecraft.nbt.NBTBase;

public class ParserNBTLabel extends CustomCompletable<CommandArg<NBTBase>>
{
	public static final IParse<CommandArg<NBTBase>> parser = new ParserNBTLabel();
	
	@Override
	public CommandArg<NBTBase> iParse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
	{
		final ArgWrapper<?> label = TypeUntypedLabel.parseLabel(parser);
		
		return NBTUtilities.procIdentifier(parser, label);
	}
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
	{
		TabCompletionData.addToSet(tcDataSet, startIndex, cData, parser.getLabelCompletions(TypeIDs.NBTBase));
	}
}
