package net.minecraft.command.type.custom;

import java.util.Arrays;
import java.util.List;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CTypeParse;
import net.minecraft.command.type.IDataType;
import net.minecraft.command.type.TypeParse;

public class TypeAlternatives extends TypeParse<ArgWrapper<?>>
{
	private final List<IDataType<?>> alternatives;
	
	public TypeAlternatives(final List<IDataType<?>> alternatives)
	{
		this.alternatives = alternatives;
	}
	
	public TypeAlternatives(final IDataType<?>... alternatives)
	{
		this.alternatives = Arrays.asList(alternatives);
	}
	
	@Override
	public ArgWrapper<?> parse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
	{
		for (final IDataType<?> alternative : this.alternatives)
		{
			try
			{
				return alternative.parseSnapshot(parser, context);
			} catch (final SyntaxErrorException ex)
			{
			}
		}
		
		throw parser.SEE("Unable to parse argument around index ");
	}
	
	public static class Typed<T> extends CTypeParse<T>
	{
		private final List<IDataType<ArgWrapper<T>>> alternatives;
		
		public Typed(final List<IDataType<ArgWrapper<T>>> alternatives)
		{
			this.alternatives = alternatives;
		}
		
		@SafeVarargs
		public Typed(final IDataType<ArgWrapper<T>>... alternatives)
		{
			this.alternatives = Arrays.asList(alternatives);
		}
		
		@Override
		public ArgWrapper<T> parse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
		{
			for (final IDataType<ArgWrapper<T>> alternative : this.alternatives)
			{
				try
				{
					return alternative.parseSnapshot(parser, context);
				} catch (final SyntaxErrorException ex)
				{
				}
			}
			
			throw parser.SEE("Unable to parse argument around index ");
		}
	}
}
