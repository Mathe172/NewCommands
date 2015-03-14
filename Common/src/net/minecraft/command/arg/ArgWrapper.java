package net.minecraft.command.arg;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.type.TypeID;

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
	
	// Checked...
	@SuppressWarnings("unchecked")
	public <T> ArgWrapper<T> convertTo(final TypeID<T> target) throws SyntaxErrorException
	{
		if (target == this.type)
			return (ArgWrapper<T>) this;
		
		return new ArgWrapper<T>(target, this.type.convertTo(this.arg, target));
	}
	
	public ArgWrapper<R> cachedWrapper()
	{
		return new ArgWrapper<>(this.type, this.arg.cached());
	}
	
}
