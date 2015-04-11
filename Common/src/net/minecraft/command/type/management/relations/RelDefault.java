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
	
	public final <T, U, E extends CommandException> void registerPair(final Convertable<T, ?, ? extends E> base, final Convertable<U, ?, E> rel, final Converter<T, U, ? extends E> converter)
	{
		base.addAttribute(new Att<>(rel, converter));
		base.addConverter(rel, converter);
	}
	
	public class Att<T, U, E extends CommandException> extends Attribute
	{
		public final Convertable<U, ?, E> rel;
		public final Converter<T, U, ? extends E> relConverter;
		
		private Att(final Convertable<U, ?, E> rel, final Converter<T, U, ? extends E> relConverter)
		{
			this.rel = rel;
			this.relConverter = relConverter;
		}
		
		public final <R> void apply(final Convertable<R, ?, ?> source, final Converter<R, T, ? extends E> converter, final int dist)
		{
			source.addConverter(this.rel, Converter.chain(converter, this.relConverter), dist + 1);
		}
	}
}
