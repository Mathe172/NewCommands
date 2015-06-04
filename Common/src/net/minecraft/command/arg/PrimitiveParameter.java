package net.minecraft.command.arg;

import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.arg.CommandArg.Primitive;
import net.minecraft.command.arg.ExArgWrapper.GetterWrapper;
import net.minecraft.command.type.management.TypeID;

public class PrimitiveParameter<T> extends Primitive<T>
{
	public final T value;
	
	public PrimitiveParameter(final T value)
	{
		this.value = value;
	}
	
	@Override
	public final T eval(final ICommandSender sender) throws CommandException
	{
		return get();
	}
	
	@Override
	public T get()
	{
		return this.value;
	}
	
	@Override
	public CommandArg<T> commandArg()
	{
		return this;
	}
	
	@Override
	public GetterWrapper<T> wrap(final TypeID<T> type)
	{
		return new GetterWrapper<T>(type, this)
		{
			@Override
			public TypedWrapper<T> addToProcess(final List<Processable> toProcess)
			{
				return this;
			}
		};
	}
}
