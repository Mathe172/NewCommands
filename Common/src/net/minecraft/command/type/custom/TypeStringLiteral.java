package net.minecraft.command.type.custom;

import net.minecraft.command.type.ProviderCompleter;
import net.minecraft.command.type.base.CompoundType;

public class TypeStringLiteral extends CompoundType<String>
{
	public TypeStringLiteral(final String... literals)
	{
		super(ParserName.parser, new ProviderCompleter(literals));
	}
}
