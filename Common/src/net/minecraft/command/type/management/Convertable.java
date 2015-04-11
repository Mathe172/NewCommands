package net.minecraft.command.type.management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.command.CommandException;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.type.custom.Relations;
import net.minecraft.command.type.management.relations.Relation.Attribute;

public abstract class Convertable<T, W, E extends CommandException>
{
	private static final Map<String, Convertable<?, ?, ?>> convertables = new HashMap<>();
	
	private final Map<Convertable<?, ?, ?>, Converter<?, T, ? extends E>> convertableFrom = new HashMap<>();
	protected final Set<Convertable<?, ?, ?>> convertableTo = new HashSet<>();
	
	private final List<Attribute> attributes = new ArrayList<>();
	
	/** Converters from these convertables (to this convertable) may be overridden */
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
	
	/**
	 * Calling this method for 'random' types is not advised. Only call if the type is intended to have this functionality since it retroactively changes the behavior of this type and the behavior of future types
	 */
	/*
	 * public <R> void addDefaultConverter(final Convertable<R, ?> fallbackTo, final Converter<T, R> fallbackToConverter) { this.addConverter(fallbackTo, fallbackToConverter); this.registerFallbackTo(fallbackTo); }
	 */
	
	private final void setDist(final Convertable<?, ?, ?> source, final int dist)
	{
		if (dist == 0)
			this.dist.remove(source);
		else
			this.dist.put(source, dist);
	}
	
	public final int getDist(final Convertable<?, ?, ?> source)
	{
		final Integer dist = this.dist.get(source);
		
		return dist == null ? 0 : dist;
	}
	
	/*
	 * private final <R> void registerFallbackTo(final Convertable<R, ?> fallbackTo) { this.fallbacksTo.add(fallbackTo);
	 * 
	 * for (final Entry<Convertable<?, ?>, Converter<?, T>> sourceData : this.convertableFrom.entrySet()) this.addAsFallbackToSource(sourceData.getKey(), fallbackTo, sourceData.getValue()); }
	 */
	
	/** NEVER EVER call this. Only for use in {@link #registerFallbackTo(Convertable)} */
	/*
	 * @SuppressWarnings("unchecked") private final <R, F> void addAsFallbackToSource(final Convertable<R, ?> source, final Convertable<F, ?> fallbackTo, final Converter<?, T> converter) { this.addChainedConverter(source, fallbackTo, (Converter<R, T>) converter, this.getDist(source)); }
	 */
	
	private final <F> void addConverterFrom(final Convertable<F, ?, ?> source, Converter<F, T, ? extends E> converter, final int dist)
	{
		if (this == source)
		{
			if (dist == 0)
				throw new IllegalArgumentException("Cannot register identity converter (for convertable '" + this.name + "')");
			
			return;
		}
		
		if (this.convertableFrom.containsKey(source))
		{
			final int oldFallbackLevel = this.getDist(source);
			
			if (oldFallbackLevel < dist)
				return;
			
			if (oldFallbackLevel == dist)
			{
				if (dist == 0)
					throw new IllegalArgumentException("Converter from '" + source.name + "' to '" + this.name + "' already registered");
				
				converter = createFailingConverter(source, this, dist);
			}
		}
		else
		{
			source.convertableTo.add(this);
			
			source.adjustCompletions(this);
		}
		
		this.setDist(source, dist);
		
		this.convertableFrom.put(source, converter);
		
		for (final Attribute attTarget : this.attributes)
		{
			for (final Attribute attSource : source.attributes)
				attTarget.apply(source, this, converter, dist, attSource);
			
			attTarget.apply(source, this, converter, dist, Relations.idAttribute);
		}
		
		for (final Attribute attSource : source.attributes)
			Relations.idAttribute.apply(source, this, converter, dist, attSource);
	}
	
	private static final <F, R, E extends CommandException> Converter<F, R, E> createFailingConverter(final Convertable<F, ?, ?> source, final Convertable<R, ?, E> target, final int dist)
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
	
	public final <R, U extends CommandException> void addConverter(final Convertable<R, ?, U> target, final Converter<T, R, ? extends U> converter)
	{
		target.addConverterFrom(this, converter, 0);
	}
	
	public final <R, U extends CommandException> void addConverter(final Convertable<R, ?, U> target, final Converter<T, R, ? extends U> converter, final int dist)
	{
		target.addConverterFrom(this, converter, dist);
	}
	
	// Checked...
	@SuppressWarnings("unchecked")
	public final <R, U extends CommandException> R convertTo(final T toConvert, final Convertable<R, ?, U> target) throws U, SyntaxErrorException
	{
		if (this == target)
			return (R) toConvert;
		
		final Converter<T, R, ? extends U> converter = this.getConverter(target);
		
		if (converter == null)
			throw new SyntaxErrorException("Cannot convert " + this.name + " to " + target.name + ".");
		
		return converter.convert(toConvert);
	}
	
	public final boolean convertableFrom(final Convertable<?, ?, ?> source)
	{
		return this == source || this.convertableFrom.containsKey(source);
	}
	
	public abstract W convertFrom(ArgWrapper<?> toConvert) throws E, SyntaxErrorException;
	
	// Checked...
	@SuppressWarnings("unchecked")
	public final <R, U extends CommandException> Converter<T, R, ? extends U> getConverter(final Convertable<R, ?, U> target)
	{
		return (Converter<T, R, ? extends U>) target.convertableFrom.get(this);
	}
	
	public static final Convertable<?, ?, ?> get(final String name)
	{
		return convertables.get(name);
	}
	
	public static final void clearAll()
	{
		for (final Convertable<?, ?, ?> convertable : convertables.values())
			convertable.clear();
		
		convertables.clear();
	}
	
	public final void addAttribute(final Attribute attribute)
	{
		this.attributes.add(attribute);
		attribute.init(this);
	}
	
	public final void initAtt(final Attribute attribute)
	{
		for (final Entry<Convertable<?, ?, ?>, Converter<?, T, ? extends E>> data : this.convertableFrom.entrySet())
			this.initAttSource(data.getKey(), data.getValue(), this.getDist(data.getKey()), attribute);
		
		for (final Convertable<?, ?, ?> target : this.convertableTo)
			this.initAttTarget(target, attribute);
	}
	
	// Checked...
	@SuppressWarnings("unchecked")
	private final <F> void initAttSource(final Convertable<F, ?, ?> source, final Converter<?, T, ? extends E> converter, final int dist, final Attribute attTarget)
	{
		final Converter<F, T, ? extends E> newConverter = (Converter<F, T, ? extends E>) converter;
		
		for (final Attribute attSource : source.attributes)
			attTarget.apply(source, this, newConverter, dist, attSource);
		
		attTarget.apply(source, this, newConverter, dist, Relations.idAttribute);
	}
	
	private final void initAttTarget(final Convertable<?, ?, ?> target, final Attribute attSource)
	{
		for (final Attribute attTarget : target.attributes)
			attTarget.apply(this, target, attSource);
		
		Relations.idAttribute.apply(this, target, attSource);
	}
	
	@SuppressWarnings("unused")
	public void addPossibleSelector(final ITabCompletion tc)
	{
	}
	
	@SuppressWarnings("unused")
	public void addPossibleOperator(final ITabCompletion tc)
	{
	}
	
	@SuppressWarnings("unused")
	public void adjustCompletions(final Convertable<?, ?, ?> target)
	{
	}
}
