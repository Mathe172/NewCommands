package net.minecraft.command.type.management.relations;

import net.minecraft.command.CommandException;
import net.minecraft.command.type.management.Convertable;
import net.minecraft.command.type.management.Converter;

public class RelDefault extends Relation
{
	public RelDefault()
	{
		super("Default");
	}
	
	public final <T, U, E extends CommandException> void registerPair(final Convertable<T, ?, E> base, final Convertable<U, ?, ? extends E> rel, final Converter<T, U, ? extends E> converter)
	{
		base.addAttribute(new Att<>(rel, converter));
		base.addConverter(rel, converter);
	}
	
	public class Att<T, U, E extends CommandException> extends Attribute
	{
		public final Convertable<U, ?, ? extends E> rel;
		public final Converter<T, U, ? extends E> relConverter;
		
		private Att(final Convertable<U, ?, ? extends E> rel, final Converter<T, U, ? extends E> relConverter)
		{
			this.rel = rel;
			this.relConverter = relConverter;
		}
	}
	
	public static final <F, T, FT, E extends CommandException> void apply(final Convertable<F, ?, E> source, final Att<T, FT, ? extends E> att, final Converter<F, T, ? extends E> converter, final int dist)
	{
		source.addConverter(att.rel, Converter.chain(converter, att.relConverter), dist + 1);
	}
}
