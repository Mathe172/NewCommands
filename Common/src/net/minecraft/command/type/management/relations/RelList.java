package net.minecraft.command.type.management.relations;

import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.type.management.Convertable;

public class RelList extends Relation
{
	public RelList()
	{
		super("List");
	}
	
	public final <T, E extends CommandException> void registerPair(final Convertable<T, ?, E> base, final Convertable<List<T>, ?, E> rel)
	{
		base.addAttribute(new Att<>(rel));
	}
	
	public class Att<T, E extends CommandException> extends Attribute
	{
		public final Convertable<List<T>, ?, E> rel;
		
		private Att(final Convertable<List<T>, ?, E> rel)
		{
			this.rel = rel;
		}
	}
}
