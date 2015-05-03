package net.minecraft.command.type.custom.nbt;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.arg.PermissionWrapper;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IParse;
import net.minecraft.command.type.base.CustomCompletable;
import net.minecraft.command.type.custom.TypeUntypedSelector;
import net.minecraft.nbt.NBTBase;

public final class ParserNBTSelector extends CustomCompletable<CommandArg<NBTBase>>
{
	public static final IParse<CommandArg<NBTBase>> parser = new ParserNBTSelector();
	
	private ParserNBTSelector()
	{
	}
	
	@Override
	public CommandArg<NBTBase> iParse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
	{
		return NBTUtilities.procIdentifier(parser, TypeUntypedSelector.parseName(parser).parse(parser));
	}
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
	{
		PermissionWrapper.complete(tcDataSet, startIndex, cData, TypeIDs.NBTBase.getSelectorCompletions());
	}
}
