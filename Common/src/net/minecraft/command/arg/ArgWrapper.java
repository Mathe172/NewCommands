package net.minecraft.command.arg;

import net.minecraft.command.CommandException;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.type.management.Convertable;
import net.minecraft.command.type.management.TypeID;

public class ArgWrapper<R>
{
	public final TypeID<R> type;
	public final CommandArg<R> arg;
	
	public ArgWrapper(final TypeID<R> type, final CommandArg<R> arg)
	{
		this.type = type;
		this.arg = arg;
	}
	
	public ArgWrapper(final TypeID<R> type, final R arg)
	{
		this.type = type;
		this.arg = new PrimitiveParameter<>(arg);
	}
	
	// This is checked...
	@SuppressWarnings("unchecked")
	public <T> CommandArg<T> get(final TypeID<T> type)
	{
		if (type != this.type)
			throw new IllegalArgumentException("Incompatible TypeIDs: " + type.name + " & " + this.type.name);
		
		return (CommandArg<T>) this.arg;
	}
	
	public static <T> CommandArg<T> get(final TypeID<T> type, final ArgWrapper<?> wrapper)
	{
		if (wrapper == null)
			return null;
		
		return wrapper.get(type);
	}
	
	public final <T, E extends CommandException> T convertTo(final Convertable<?, T, E> target) throws E, SyntaxErrorException
	{
		return target.convertFrom(this);
	}
	
	public final <T, E extends CommandException> T iConvertTo(final Convertable<T, ?, E> target) throws E, SyntaxErrorException
	{
		return this.type.convertTo(this.arg, target);
	}
	
	public ArgWrapper<R> cachedWrapper()
	{
		return this.type.wrap(this.arg.cached());
	}
}
