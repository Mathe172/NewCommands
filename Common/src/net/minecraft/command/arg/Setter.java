package net.minecraft.command.arg;

import net.minecraft.command.CommandException;
import net.minecraft.command.type.management.TypeID;

public interface Setter<T>
{
	public void set(final T value) throws CommandException;
	
	public static interface SetterProvider<R>
	{
		public <T> Setter<T> getSetter(final TypeID<T> type);
	}
}
