package net.minecraft.command.type.custom;

import java.util.Collection;

import net.minecraft.command.completion.ProviderCompleter;
import net.minecraft.command.parser.MatcherRegistry;
import net.minecraft.command.type.base.CompoundType;

public class TypeStringLiteral extends CompoundType<String>
{
	public TypeStringLiteral(final String... literals)
	{
		super(ParserName.parser, new ProviderCompleter(literals));
	}
	
	public TypeStringLiteral(final Collection<String> literals)
	{
		super(ParserName.parser, ProviderCompleter.create(literals));
	}
	
	public static class Escaped extends CompoundType<String>
	{
		public Escaped(final MatcherRegistry m, final String... literals)
		{
			super(new ParserName(m), ProviderCompleter.createEscaped(literals));
		}
		
		public Escaped(final String... literals)
		{
			super(ParserName.parser, ProviderCompleter.createEscaped(literals));
		}
	}
	
	public static class ResourcePath extends CompoundType<String>
	{
		public ResourcePath(final String... resources)
		{
			super(ParserName.parser, new CompleterResourcePath().registerResource(resources));
		}
	}
}
