package net.minecraft.command.type.custom;

import net.minecraft.command.CommandException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.completion.ProviderCompleter;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.base.CompoundType;
import net.minecraft.command.type.management.CConverter;

public class TypeBoolean
{
	private TypeBoolean()
	{
	}
	
	public static final CConverter<String, Boolean> stringToBoolean = new CConverter<String, Boolean>()
	{
		@Override
		public Boolean convert(final String toConvert) throws CommandException
		{
			if (ParsingUtilities.isTrue(toConvert))
				return true;
			if (ParsingUtilities.isFalse(toConvert))
				return false;
			
			throw new NumberInvalidException("'" + toConvert + "' cannot be converted to boolean");
		}
	};
	
	public static final CDataType<Boolean> parser = new CompoundType<>(
		new ParserName.CustomType<>("boolean", TypeIDs.Boolean, stringToBoolean, true),
		new ProviderCompleter("true", "false"));
}