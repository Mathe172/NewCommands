package net.minecraft.command.type.custom;

import net.minecraft.command.CommandUtilities;
import net.minecraft.command.CommandException;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.management.CConverter;
import net.minecraft.item.Item;

public class ParserItemID
{
	public static final CConverter<String, Item> stringToItem = new CConverter<String, Item>()
	{
		@Override
		public Item convert(final String toConvert) throws CommandException
		{
			return CommandUtilities.getItemByText(toConvert);
		}
	};
	
	public static final CDataType<Item> parser = new ParserName.CustomType<>("ItemID", TypeIDs.ItemID, stringToItem, true);
	
	private ParserItemID()
	{
	}
}
