package net.minecraft.command.construction;

import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.type.custom.TypeSelectorContent.ParserData;
import net.minecraft.command.type.management.TypeID;

public abstract class SelectorConstructable
{
	public abstract ArgWrapper<?> construct(ParserData parserData) throws SyntaxErrorException;
	
	public static ArgWrapper<?> getParam(final int index, final ParserData parserData)
	{
		return index < parserData.unnamedParams.size() ? parserData.unnamedParams.get(index) : null;
	}
	
	public static ArgWrapper<?> getParam(final int index, final String name, final ParserData parserData)
	{
		if (name != null)
		{
			final ArgWrapper<?> ret = parserData.namedParams.get(name);
			if (ret != null)
				return ret;
		}
		
		return getParam(index, parserData);
	}
	
	public static <T> CommandArg<T> getParam(final TypeID<T> type, final int index, final String name, final ParserData parserData)
	{
		return ArgWrapper.get(type, getParam(index, name, parserData));
	}
	
	public static <T> CommandArg<T> getParam(final TypeID<T> type, final int index, final ParserData parserData)
	{
		return ArgWrapper.get(type, getParam(index, parserData));
	}
	
	public static <T> CommandArg<T> getParam(final TypeID<T> type, final String name, final ParserData parserData)
	{
		return ArgWrapper.get(type, parserData.namedParams.get(name));
	}
	
	public static ArgWrapper<?> getRequiredParam(final int index, final String name, final ParserData parserData) throws SyntaxErrorException
	{
		final ArgWrapper<?> ret = getParam(index, name, parserData);
		
		if (ret != null)
			return ret;
		
		throw ParsingUtilities.SEE("Missing parameter for selector: " + (name != null ? name : index));
	}
	
	public static ArgWrapper<?> getRequiredParam(final int index, final ParserData parserData) throws SyntaxErrorException
	{
		final ArgWrapper<?> ret = getParam(index, parserData);
		
		if (ret != null)
			return ret;
		
		throw ParsingUtilities.SEE("Missing parameter for selector: " + index);
	}
	
	public static ArgWrapper<?> getRequiredParam(final String name, final ParserData parserData) throws SyntaxErrorException
	{
		final ArgWrapper<?> ret = parserData.namedParams.get(name);
		
		if (ret != null)
			return ret;
		
		throw ParsingUtilities.SEE("Missing parameter for selector: " + name);
	}
	
	public static <T> CommandArg<T> getRequiredParam(final TypeID<T> type, final int index, final String name, final ParserData parserData) throws SyntaxErrorException
	{
		return getRequiredParam(index, name, parserData).get(type);
	}
	
	public static <T> CommandArg<T> getRequiredParam(final TypeID<T> type, final int index, final ParserData parserData) throws SyntaxErrorException
	{
		return getRequiredParam(index, parserData).get(type);
	}
	
	public static <T> CommandArg<T> getRequiredParam(final TypeID<T> type, final String name, final ParserData parserData) throws SyntaxErrorException
	{
		return getRequiredParam(name, parserData).get(type);
	}
}
