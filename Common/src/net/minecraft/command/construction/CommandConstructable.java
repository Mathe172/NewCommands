package net.minecraft.command.construction;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.construction.CommandDescriptorDefault.CParserData;

public abstract class CommandConstructable
{
	public abstract CommandArg<Integer> construct(final CParserData data) throws SyntaxErrorException;
	
	public static CommandConstructable emptyConstructable = primitiveConstructable(null);
	
	public static CommandConstructable primitiveConstructable(final CommandArg<Integer> command)
	{
		return new CommandConstructable()
		{
			@Override
			public final CommandArg<Integer> construct(final CParserData data)
			{
				return command;
			}
		};
	}
}
