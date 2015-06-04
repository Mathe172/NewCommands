package net.minecraft.command.type.custom;

import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.collections.Completers;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.parser.MatcherRegistry;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.base.CompoundType;
import net.minecraft.command.type.management.CConverter;

public final class ParserInt
{
	public static final MatcherRegistry intMatcher = new MatcherRegistry("\\G\\s*+([+-]?+\\d++)");
	
	public static final CConverter<String, Integer> stringToInt = new CConverter<String, Integer>()
	{
		@Override
		public Integer convert(final String toConvert) throws CommandException
		{
			try
			{
				return new Integer(toConvert);
			} catch (final NumberFormatException e)
			{
				throw new NumberInvalidException("Cannot convert " + toConvert + " to int");
			}
		}
	};
	
	public static final CDataType<Integer> parser = new ParserName.CustomType<>(intMatcher, "int", TypeIDs.Integer, stringToInt, true);
	public static final CDataType<List<Integer>> parserList = new TypeList.GParsed<>(TypeIDs.IntList, parser);
	
	private ParserInt()
	{
	}
	
	public final static class Defaulted extends CompoundType<Integer>
	{
		public static final MatcherRegistry intDefMatcher = new MatcherRegistry("\\G\\s*+(\\*|[+-]?+\\d++)");
		
		public static final CDataType<Integer> parserMin = new Defaulted(Integer.MIN_VALUE);
		public static final CDataType<Integer> parserMax = new Defaulted(Integer.MAX_VALUE);
		
		public Defaulted(final int def)
		{
			super(new ParserName.CustomType<>(intMatcher, "int", TypeIDs.Integer, new CConverter<String, Integer>()
			{
				@Override
				public Integer convert(final String toConvert) throws CommandException
				{
					try
					{
						return "*".equals(toConvert) ? def : new Integer(toConvert);
					} catch (final NumberFormatException e)
					{
						throw new NumberInvalidException("Cannot convert " + toConvert + " to int");
					}
				}
			}, true), Completers.wildcardCompleter);
		}
	}
}
