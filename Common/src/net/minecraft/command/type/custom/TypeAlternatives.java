package net.minecraft.command.type.custom;

import java.util.Arrays;
import java.util.List;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Parser;
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
	public ArgWrapper<?> parse(final Parser parser) throws SyntaxErrorException, CompletionException
	{
		for (final IDataType<?> alternative : this.alternatives)
		{
			try
			{
				return alternative.parseSnapshot(parser);
			} catch (final SyntaxErrorException ex)
			{
			}
		}
		
		throw parser.SEE("Unable to parse argument around index ");
	}
	
}
