package net.minecraft.command.type.management.relations;

import net.minecraft.command.CommandException;
import net.minecraft.command.type.management.Convertable;
import net.minecraft.command.type.management.Converter;

public class RelSuper extends Relation
{
	public RelSuper()
	{
		super("Super");
	}
	
	public final <T, U, E extends CommandException> void registerPair(final Convertable<T, ?, ? extends E> base, final Convertable<U, ?, E> rel, final Converter<U, T, ? extends E> converter)
	{
		base.addAttribute(new Att<>(rel, converter));
	}
	
	public class Att<T, U, E extends CommandException> extends Attribute
	{
		public final Convertable<T, ?, E> rel;
		public final Converter<T, U, ? extends E> relConverter;
		
		private Att(final Convertable<T, ?, E> rel, final Converter<T, U, ? extends E> relConverter)
		{
			this.rel = rel;
			this.relConverter = relConverter;
		}
		
		public final <FT> void apply(final Convertable<FT, ?, ? extends E> target, final Converter<U, FT, ? extends E> converter, final int dist)
		{
			this.rel.addConverter(target, Converter.chain(this.relConverter, converter), dist + 1);
		}
	}
}
