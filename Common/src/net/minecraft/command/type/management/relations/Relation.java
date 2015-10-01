package net.minecraft.command.type.management.relations;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.trie.PatriciaTrie;

import net.minecraft.command.CommandException;
import net.minecraft.command.type.management.Convertable;
import net.minecraft.command.type.management.Converter;

public class Relation
{
	private static final PatriciaTrie<Relation> relations = new PatriciaTrie<>();
	
	private final Map<Relation, Provider> providers = new HashMap<>();
	
	final String name;
	
	public Relation(final String name)
	{
		this.name = name;
		
		if (relations.put(name, this) != null)
			throw new IllegalArgumentException("Relation with name '" + name + "' already registered");
	}
	
	public abstract class Attribute
	{
		public final <F, T, E extends CommandException> void apply(final Convertable<F, ?, E> source, final Convertable<T, ?, ? extends E> target, final Converter<F, T, ? extends E> converter, final int dist, final Attribute attTarget)
		{
			final Provider provider = Relation.this.providers.get(attTarget.relation());
			
			if (provider == null)
				return;
			
			provider.apply(source, target, converter, dist, this, attTarget);
		}
		
		public final <F, T, E extends CommandException> void apply(final Convertable<F, ?, E> source, final Convertable<T, ?, ? extends E> target, final Converter<F, T, ? extends E> converter, final Attribute attTarget)
		{
			final Provider provider = Relation.this.providers.get(attTarget.relation());
			
			if (provider == null)
				return;
			
			provider.apply(source, target, converter, source.getDist(target), this, attTarget);
		}
		
		private final Relation relation()
		{
			return Relation.this;
		}
	}
	
	public static abstract class Provider
	{
		public abstract <F, T, E extends CommandException> void apply(Convertable<F, ?, E> source, Convertable<T, ?, ? extends E> target, Converter<F, T, ? extends E> converter, int dist, Attribute attSource, Attribute attTarget);
	}
	
	public static final Relation get(final String name)
	{
		return relations.get(name);
	}
	
	public final void addProvider(final Relation rel, final Provider provider)
	{
		if (this.providers.put(rel, provider) != null)
			throw new IllegalArgumentException("Relation-Provider from '" + this.name + "' to '" + rel.name + "' already registered");
	}
	
	public void clear()
	{
		this.providers.clear();
	}
	
	public static void clearAll()
	{
		for (final Relation relation : relations.values())
			relation.clear();
	}
}
