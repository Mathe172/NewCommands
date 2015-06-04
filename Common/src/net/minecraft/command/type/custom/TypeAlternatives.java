package net.minecraft.command.type.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.IDataType;
import net.minecraft.command.type.TypeParse;

public class TypeAlternatives<T extends ArgWrapper<?>> extends TypeParse<T>
{
	private final List<IDataType<? extends T>> alternatives;
	
	public TypeAlternatives(final List<IDataType<? extends T>> alternatives)
	{
		this.alternatives = alternatives;
	}
	
	@SafeVarargs
	public TypeAlternatives(final IDataType<? extends T>... alternatives)
	{
		this.alternatives = Arrays.asList(alternatives);
	}
	
	@Override
	public T parse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
	{
		if (parser.debug)
		{
			final List<SyntaxErrorException> suppressed = new ArrayList<>(this.alternatives.size());
			
			for (final IDataType<? extends T> alternative : this.alternatives)
			{
				try
				{
					return alternative.parseSnapshot(parser, context);
				} catch (final SyntaxErrorException ex)
				{
					suppressed.add(ex);
				}
			}
			
			final SyntaxErrorException ex = parser.SEE("Unable to parse argument ");
			
			for (final SyntaxErrorException e : suppressed)
				if (ex != e)
					ex.addSuppressed(e);
			
			throw ex;
		}
		
		final boolean suppressEx = parser.suppressEx;
		parser.suppressEx = true;
		
		try
		{
			for (final IDataType<? extends T> alternative : this.alternatives)
			{
				try
				{
					return alternative.parseSnapshot(parser, context);
				} catch (final SyntaxErrorException ex)
				{
				}
			}
		} finally
		{
			parser.suppressEx = suppressEx;
		}
		
		throw parser.SEE("Unable to parse argument ");
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
	}
}
