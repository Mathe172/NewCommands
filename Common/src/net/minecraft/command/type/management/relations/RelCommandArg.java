package net.minecraft.command.type.management.relations;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.type.management.Convertable;
import net.minecraft.command.type.management.Converter;
import net.minecraft.command.type.management.SConverter;

public class RelCommandArg extends Relation
{
	public RelCommandArg()
	{
		super("CommandArg");
	}
	
	public final <T> void registerPair(final Convertable<T, ?, ?> base, final Convertable<CommandArg<T>, ?, SyntaxErrorException> rel)
	{
		base.addAttribute(new Att<>(rel));
	}
	
	public class Att<T> extends Attribute
	{
		public final Convertable<CommandArg<T>, ?, SyntaxErrorException> rel;
		
		private Att(final Convertable<CommandArg<T>, ?, SyntaxErrorException> rel)
		{
			this.rel = rel;
		}
		
		// Checked...
		public final <U> void apply(final Convertable<CommandArg<U>, ?, SyntaxErrorException> target, final Converter<T, U, ?> converter, final int dist)
		{
			this.rel.addConverter(target, new SConverter<CommandArg<T>, CommandArg<U>>()
			{
				@Override
				public CommandArg<U> convert(final CommandArg<T> toConvert)
				{
					return converter.transform(toConvert);
				}
			}, dist);
		}
	}
}
