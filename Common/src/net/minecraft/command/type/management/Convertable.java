package net.minecraft.command.type.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections4.trie.PatriciaTrie;

import net.minecraft.command.CommandException;
import net.minecraft.command.IPermission;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.collections.Relations;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.management.relations.Relation.Attribute;

public abstract class Convertable<T, W, E extends CommandException>
{
	private static final PatriciaTrie<Convertable<?, ?, ?>> convertables = new PatriciaTrie<>();
	
	private final Map<Convertable<?, ?, ?>, Converter<?, T, ?>> convertableFrom = new HashMap<>();
	protected final Map<Convertable<?, ?, ? extends E>, Converter<T, ?, ? extends E>> convertableTo = new HashMap<>();
	
	private final List<Attribute> attributes = new ArrayList<>();
	
	private final Map<Convertable<?, ?, ?>, Integer> dist = new HashMap<>();
	
	public final String name;
	
	public Convertable(final String name)
	{
		this.name = name;
	}
	
	public void init()
	{
		if (convertables.put(this.name, this) != null)
			throw new IllegalArgumentException("Convertable with name '" + this.name + "' already registered");
	}
	
	public Set<? extends Convertable<?, ?, ?>> convertableTo()
	{
		return Collections.unmodifiableSet(this.convertableTo.keySet());
	}
	
	private final void setDist(final Convertable<?, ?, ?> target, final int dist)
	{
		if (dist == 0)
			this.dist.remove(target);
		else
			this.dist.put(target, dist);
	}
	
	public final int getDist(final Convertable<?, ?, ?> target)
	{
		final Integer dist = this.dist.get(target);
		
		return dist == null ? 0 : dist;
	}
	
	private static final <F, R, E extends CommandException> Converter<F, R, E> createFailingConverter(final Convertable<F, ?, E> source, final Convertable<R, ?, ?> target, final int dist)
	{
		return new Converter<F, R, E>()
		{
			@Override
			public R convert(final F toConvert)
			{
				throw new IllegalStateException("Multiple converters from '" + source.name + "' to '" + target.name + "' with same fallback-level (" + dist + ") are registered: No well defined priority");
			}
		};
	}
	
	public void clear()
	{
		this.convertableFrom.clear();
		this.convertableTo.clear();
		
		this.attributes.clear();
		
		this.dist.clear();
	}
	
	public final <R> void addConverter(final Convertable<R, ?, ? extends E> target, final Converter<T, R, ? extends E> converter)
	{
		this.addConverter(target, converter, 0);
	}
	
	public final <R> void addConverter(final Convertable<R, ?, ? extends E> target, Converter<T, R, ? extends E> converter, final int dist)
	{
		if (this == target)
		{
			if (dist == 0)
				throw new IllegalArgumentException("Cannot register identity converter (for convertable '" + this.name + "')");
			
			return;
		}
		
		if (this.convertableTo.containsKey(target))
		{
			final int oldFallbackLevel = this.getDist(target);
			
			if (oldFallbackLevel < dist)
				return;
			
			if (oldFallbackLevel == dist)
			{
				if (dist == 0)
					throw new IllegalArgumentException("Converter from '" + this.name + "' to '" + target.name + "' already registered");
				
				converter = createFailingConverter(this, target, dist);
			}
		}
		else
			this.adjustCompletions(target);
		
		this.setDist(target, dist);
		
		this.convertableTo.put(target, converter);
		
		target.convertableFrom.put(this, converter);
		
		for (final Attribute attSource : this.attributes)
		{
			for (final Attribute attTarget : target.attributes)
				attSource.apply(this, target, converter, dist, attTarget);
			
			attSource.apply(this, target, converter, dist, Relations.idAttribute);
		}
		
		for (final Attribute attTarget : target.attributes)
			Relations.idAttribute.apply(this, target, converter, dist, attTarget);
	}
	
	// Checked...
	@SuppressWarnings("unchecked")
	public final <R> R convertTo(final T toConvert, final Convertable<R, ?, ?> target) throws E, SyntaxErrorException
	{
		if (this == target)
			return (R) toConvert;
		
		final Converter<T, R, ? extends E> converter = this.getConverter(target);
		
		if (converter == null)
			throw new SyntaxErrorException("Cannot convert " + this.name + " to " + target.name + ".");
		
		return converter.convert(toConvert);
	}
	
	// Checked...
	@SuppressWarnings("unchecked")
	public final <R> R convertTo(final Parser parser, final T toConvert, final Convertable<R, ?, ?> target) throws E, SyntaxErrorException
	{
		if (this == target)
			return (R) toConvert;
		
		final Converter<T, R, ? extends E> converter = this.getConverter(target);
		
		if (converter == null)
			throw parser.SEE("Cannot convert " + this.name + " to " + target.name + ".", false);
		
		return converter.convert(toConvert);
	}
	
	public final boolean convertableFrom(final Convertable<?, ?, ?> source)
	{
		return this == source || this.convertableFrom.containsKey(source);
	}
	
	public abstract W convertFrom(Parser parser, ArgWrapper<?> toConvert) throws SyntaxErrorException;
	
	// Checked...
	@SuppressWarnings("unchecked")
	public final <R> Converter<T, R, ? extends E> getConverter(final Convertable<R, ?, ?> target)
	{
		return (Converter<T, R, ? extends E>) this.convertableTo.get(target);
	}
	
	public static Convertable<?, ?, ?> get(final String name)
	{
		return convertables.get(name);
	}
	
	public static void clearAll()
	{
		for (final Convertable<?, ?, ?> convertable : convertables.values())
			convertable.clear();
		
		convertables.clear();
	}
	
	public final void addAttribute(final Attribute attribute)
	{
		this.attributes.add(attribute);
		this.initAtt(attribute);
	}
	
	public final void initAtt(final Attribute attribute)
	{
		for (final Entry<Convertable<?, ?, ?>, Converter<?, T, ?>> data : this.convertableFrom.entrySet())
			this.initAttSource(data.getKey(), data.getValue(), attribute);
		
		for (final Entry<Convertable<?, ?, ? extends E>, Converter<T, ?, ? extends E>> data : this.convertableTo.entrySet())
			this.initAttTarget(data.getKey(), data.getValue(), attribute);
	}
	
	// Checked...
	@SuppressWarnings("unchecked")
	private final <R, U extends CommandException> void initAttSource(final Convertable<R, ?, U> source, final Converter<?, T, ?> converterTmp, final Attribute attTarget)
	{
		final Converter<R, T, ? extends U> converter = (Converter<R, T, ? extends U>) converterTmp;
		
		for (final Attribute attSource : source.attributes)
			attSource.apply(source, (Convertable<T, ?, ? extends U>) this, converter, attTarget);
		
		Relations.idAttribute.apply(source, (Convertable<T, ?, ? extends U>) this, converter, attTarget);
	}
	
	// Checked...
	@SuppressWarnings("unchecked")
	private final <R> void initAttTarget(final Convertable<R, ?, ? extends E> target, final Converter<T, ?, ? extends E> converterTmp, final Attribute attSource)
	{
		final Converter<T, R, ? extends E> converter = (Converter<T, R, ? extends E>) converterTmp;
		
		for (final Attribute attTarget : target.attributes)
			attSource.apply(this, target, converter, attTarget);
		
		attSource.apply(this, target, converter, Relations.idAttribute);
	}
	
	@SuppressWarnings("unused")
	public void addPossibleSelector(final ITabCompletion tc, final IPermission permission)
	{
	}
	
	@SuppressWarnings("unused")
	public void addPossibleOperator(final ITabCompletion tc, final IPermission permission)
	{
	}
	
	@SuppressWarnings("unused")
	public void adjustCompletions(final Convertable<?, ?, ?> target)
	{
	}
}
