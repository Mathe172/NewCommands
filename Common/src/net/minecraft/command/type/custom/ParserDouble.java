package net.minecraft.command.type.custom;

import net.minecraft.command.CommandException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.parser.MatcherRegistry;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.management.CConverter;

public final class ParserDouble
{
	public static final MatcherRegistry doubleMatcher = new MatcherRegistry("\\G\\s*+([+-]?+(?=\\.?+\\d)\\d*+\\.?+\\d*+)");
	
	public static final CConverter<String, Double> stringToDouble = new CConverter<String, Double>()
	{
		@Override
		public Double convert(final String toConvert) throws CommandException
		{
			try
			{
				return new Double(toConvert);
			} catch (final NumberFormatException e)
			{
				throw new NumberInvalidException("Cannot convert " + toConvert + " to double");
			}
		}
	};
	
	public static final CDataType<Double> parser = new ParserName.CustomType<>(doubleMatcher, "double", TypeIDs.Double, stringToDouble, true);
	
	private ParserDouble()
	{
	}
}
