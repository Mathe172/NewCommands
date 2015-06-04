package net.minecraft.command.construction;

import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.TypedWrapper;
import net.minecraft.command.arg.TypedWrapper.Getter;
import net.minecraft.command.descriptors.SelectorDescriptorDefault.DefaultParserData;
import net.minecraft.command.type.management.TypeID;

public abstract class SelectorConstructable
{
	public abstract ArgWrapper<?> construct(DefaultParserData parserData) throws SyntaxErrorException;
	
	public static TypedWrapper<?> getParam(final int index, final DefaultParserData parserData)
	{
		return index < parserData.unnamedParams.size() ? parserData.unnamedParams.get(index) : null;
	}
	
	public static TypedWrapper<?> getParam(final int index, final String name, final DefaultParserData parserData)
	{
		if (name != null)
		{
			final TypedWrapper<?> ret = parserData.namedParams.get(name.toLowerCase());
			if (ret != null)
				return ret;
		}
		
		return getParam(index, parserData);
	}
	
	public static <T> Getter<T> getParam(final TypeID<T> type, final int index, final String name, final DefaultParserData parserData)
	{
		return ParsingUtilities.get(type, getParam(index, name, parserData));
	}
	
	public static <T> Getter<T> getParam(final TypeID<T> type, final int index, final DefaultParserData parserData)
	{
		return ParsingUtilities.get(type, getParam(index, parserData));
	}
	
	public static <T> Getter<T> getParam(final TypeID<T> type, final String name, final DefaultParserData parserData)
	{
		return ParsingUtilities.get(type, parserData.namedParams.get(name.toLowerCase()));
	}
	
	public static TypedWrapper<?> getRequiredParam(final int index, final String name, final DefaultParserData parserData) throws SyntaxErrorException
	{
		final TypedWrapper<?> ret = getParam(index, name, parserData);
		
		if (ret != null)
			return ret;
		
		throw parserData.parser.SEE("Missing parameter for selector: " + (name != null ? name : index), false);
	}
	
	public static TypedWrapper<?> getRequiredParam(final int index, final DefaultParserData parserData) throws SyntaxErrorException
	{
		final TypedWrapper<?> ret = getParam(index, parserData);
		
		if (ret != null)
			return ret;
		
		throw parserData.parser.SEE("Missing parameter for selector: " + index, false);
	}
	
	public static TypedWrapper<?> getRequiredParam(final String name, final DefaultParserData parserData) throws SyntaxErrorException
	{
		final TypedWrapper<?> ret = parserData.namedParams.get(name.toLowerCase());
		
		if (ret != null)
			return ret;
		
		throw parserData.parser.SEE("Missing parameter for selector: " + name, false);
	}
	
	public static <T> Getter<T> getRequiredParam(final TypeID<T> type, final int index, final String name, final DefaultParserData parserData) throws SyntaxErrorException
	{
		return getRequiredParam(index, name, parserData).get(type);
	}
	
	public static <T> Getter<T> getRequiredParam(final TypeID<T> type, final int index, final DefaultParserData parserData) throws SyntaxErrorException
	{
		return getRequiredParam(index, parserData).get(type);
	}
	
	public static <T> Getter<T> getRequiredParam(final TypeID<T> type, final String name, final DefaultParserData parserData) throws SyntaxErrorException
	{
		return getRequiredParam(name, parserData).get(type);
	}
}
