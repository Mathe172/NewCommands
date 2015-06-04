package net.minecraft.command.arg;

import net.minecraft.command.CommandException;
import net.minecraft.command.type.management.TypeID;

public interface TypedWrapper<T>
{
	public TypeID<T> type();
	
	public Getter<T> get();
	
	public <U> Getter<U> get(TypeID<U> type);
	
	public static interface SimpleGetter<T>
	{
		public T get() throws CommandException;
	}
	
	public static interface Getter<T> extends SimpleGetter<T>
	{
		public CommandArg<T> commandArg();
	}
}
