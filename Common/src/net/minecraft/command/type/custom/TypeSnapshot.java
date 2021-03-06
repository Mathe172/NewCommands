package net.minecraft.command.type.custom;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CTypeParse;
import net.minecraft.command.type.IDataType;

public class TypeSnapshot<T> extends CTypeParse<T>
{
	private final IDataType<ArgWrapper<T>> target;
	
	public TypeSnapshot(final IDataType<ArgWrapper<T>> target)
	{
		this.target = target;
	}
	
	@Override
	public ArgWrapper<T> iParse(final Parser parser, final Context context) throws SyntaxErrorException
	{
		final boolean suppressEx = parser.suppressEx;
		parser.suppressEx = true;
		
		try
		{
			return this.target.parseSnapshot(parser, context);
		} catch (final SyntaxErrorException ex)
		{
		} finally
		{
			parser.suppressEx = suppressEx;
		}
		
		return null;
	}
}
