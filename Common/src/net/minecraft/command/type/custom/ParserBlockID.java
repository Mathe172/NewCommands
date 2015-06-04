package net.minecraft.command.type.custom;

import net.minecraft.block.Block;
import net.minecraft.command.CommandUtilities;
import net.minecraft.command.CommandException;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.management.CConverter;

public class ParserBlockID
{
	public static final CConverter<String, Block> stringToBlock = new CConverter<String, Block>()
	{
		@Override
		public Block convert(final String toConvert) throws CommandException
		{
			return CommandUtilities.getBlockByText(toConvert);
		}
	};
	
	public static final CDataType<Block> parser = new ParserName.CustomType<>("BlockID", TypeIDs.BlockID, stringToBlock, true);
	
	private ParserBlockID()
	{
	}
}
