package net.minecraft.command.arg;

import net.minecraft.command.type.management.TypeID;

public class AbstractWrapper<R>
{
	public final TypeID<R> type;
	
	public AbstractWrapper(final TypeID<R> type)
	{
		this.type = type;
	}
	
	public void checkTypes(final TypeID<?> type) throws IllegalArgumentException
	{
		if (this.type != type)
			throw new IllegalArgumentException("Incompatible TypeIDs: " + type.name + " & " + this.type.name);
	}
}