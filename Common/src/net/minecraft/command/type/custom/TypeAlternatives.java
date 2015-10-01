package net.minecraft.command.type.custom;

import java.util.Arrays;
import java.util.List;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.IDataType;
import net.minecraft.command.type.TypeParse;

public class TypeAlternatives<T extends ArgWrapper<?>> extends TypeParse<T>
{
	private final String error;
	private final List<IDataType<? extends T>> alternatives;
	
	public TypeAlternatives(final List<IDataType<? extends T>> alternatives)
	{
		this("argument", alternatives);
	}
	
	@SafeVarargs
	public TypeAlternatives(final IDataType<? extends T>... alternatives)
	{
		this("argument", alternatives);
	}
	
	public TypeAlternatives(final String name, final List<IDataType<? extends T>> alternatives)
	{
		this.alternatives = alternatives;
		this.error = "Unable to parse " + name + " ";
	}
	
	@SafeVarargs
	public TypeAlternatives(final String name, final IDataType<? extends T>... alternatives)
	{
		this(name, Arrays.asList(alternatives));
	}
	
	@Override
	public T iParse(final Parser parser, final Context context) throws SyntaxErrorException
	{
		final boolean suppressEx = parser.suppressEx;
		parser.suppressEx = true;
		
		try
		{
			for (final IDataType<? extends T> alternative : this.alternatives)
				try
				{
					return alternative.parseSnapshot(parser, context);
				} catch (final SyntaxErrorException ex)
				{
				}
		} finally
		{
			parser.suppressEx = suppressEx;
		}
		
		throw parser.SEE(this.error);
	}
	
	public static class Typed<T> extends TypeAlternatives<ArgWrapper<T>> implements CDataType<T>
	{
		public Typed(final List<IDataType<? extends ArgWrapper<T>>> alternatives)
		{
			super(alternatives);
		}
		
		@SafeVarargs
		public Typed(final IDataType<? extends ArgWrapper<T>>... alternatives)
		{
			super(alternatives);
		}
		
		public Typed(final String name, final List<IDataType<? extends ArgWrapper<T>>> alternatives)
		{
			super(name, alternatives);
		}
		
		@SafeVarargs
		public Typed(final String name, final IDataType<? extends ArgWrapper<T>>... alternatives)
		{
			super(name, alternatives);
		}
	}
}
