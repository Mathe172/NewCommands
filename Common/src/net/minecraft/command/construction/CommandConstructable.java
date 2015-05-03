package net.minecraft.command.construction;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.descriptors.CommandDescriptor.ParserData;
import net.minecraft.command.type.management.TypeID;

public abstract class CommandConstructable
{
	public abstract CommandArg<Integer> construct(final ParserData data) throws SyntaxErrorException;
	
	public static CommandConstructable emptyConstructable = primitiveConstructable(null);
	
	public static CommandConstructable primitiveConstructable(final CommandArg<Integer> command)
	{
		return new CommandConstructable()
		{
			@Override
			public final CommandArg<Integer> construct(final ParserData data)
			{
				return command;
			}
		};
	}
	
	public static final <T> CommandArg<T> getParam(final TypeID<T> type, final int index, final ParserData data)
	{
		if (index < data.params.size())
			return ArgWrapper.get(type, data.get(index));
		
		return null;
	}
	
	public static final <T> CommandArg<T> getParam(final TypeID<T> type, final ParserData data)
	{
		if (data.index < data.params.size())
			return ArgWrapper.get(type, data.get());
		
		return null;
	}
}
