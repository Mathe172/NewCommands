package net.minecraft.command.type;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.completion.TabCompletion;
import net.minecraft.command.type.custom.ParserLabel;
import net.minecraft.command.type.custom.ParserSelector;

public final class TypeID<T>
{
	private final Map<TypeID<?>, Converter<T, ?>> convertableTo;
	public final Set<TypeID<?>> convertableFrom;
	
	public final ParserSelector<T> selectorParser = new ParserSelector<T>(this);
	public final ParserLabel<T> labelParser = new ParserLabel<T>(this);
	
	private final Set<TabCompletion> directSelectors = new HashSet<>();
	private final Set<TabCompletion> possibleSelectors = new HashSet<>();
	
	public final String name;
	
	private static final Set<TypeID<?>> typeIDs = new HashSet<>();
	
	public TypeID(final String name)
	{
		this.name = name;
		this.convertableTo = new HashMap<>();
		this.convertableFrom = new HashSet<>();
		this.convertableFrom.add(this);
		
		typeIDs.add(this);
	}
	
	public static void clear()
	{
		for (final TypeID<?> typeID : typeIDs)
		{
			typeID.convertableTo.clear();
			typeID.convertableFrom.clear();
			typeID.directSelectors.clear();
			typeID.possibleSelectors.clear();
		}
	}
	
	public <R> void addConverter(final TypeID<R> target, final Converter<T, R> converter)
	{
		if (this.convertableTo.put(target, converter) != null)
			throw new IllegalArgumentException("Converter from '" + this.name + "' to '" + target.name + "' already registered");
		
		target.convertableFrom.add(this);
		
		for (final TabCompletion selector : this.directSelectors)
			target.possibleSelectors.add(selector);
	}
	
	public void addSelector(final TabCompletion selector)
	{
		this.directSelectors.add(selector);
		
		for (final TypeID<?> type : this.convertableTo.keySet())
			type.possibleSelectors.add(selector);
	}
	
	@SuppressWarnings("unchecked")
	public <R> CommandArg<R> convertTo(final CommandArg<?> toConvert, final TypeID<R> target) throws SyntaxErrorException
	{
		return this.iConvertTo((CommandArg<T>) toConvert, target);
	}
	
	@SuppressWarnings("unchecked")
	public <R> CommandArg<R> iConvertTo(final CommandArg<T> toConvert, final TypeID<R> target) throws SyntaxErrorException
	{
		if (this == target)
			return (CommandArg<R>) toConvert;
		
		final Converter<T, ?> converter = this.convertableTo.get(target);
		
		if (converter == null)
			throw new SyntaxErrorException("Cannot convert " + this.name + " to " + target.name + ".", new Object[0]);
		
		return (CommandArg<R>) converter.convert(toConvert);
	}
	
	public Set<TabCompletion> getCompletions()
	{
		return this.possibleSelectors;
	}
	
	// Checked...
	@SuppressWarnings("unchecked")
	public <R> Converter<T, R> getConverter(final TypeID<R> target)
	{
		return (Converter<T, R>) this.convertableTo.get(target);
	}
}
