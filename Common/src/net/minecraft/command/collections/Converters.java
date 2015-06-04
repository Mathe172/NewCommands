package net.minecraft.command.collections;

import net.minecraft.block.Block;
import net.minecraft.command.CommandException;
import net.minecraft.command.type.management.CConverter;

public class Converters
{
	public static final CConverter<Block, String> blockToString = new CConverter<Block, String>()
	{
		@Override
		public String convert(final Block toConvert) throws CommandException
		{
			return toConvert.getName();
		}
	};
}
