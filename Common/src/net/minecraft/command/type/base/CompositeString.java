package net.minecraft.command.type.base;

import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.arg.CommandArg;

public class CompositeString extends CommandArg<String>
{
	
	List<CommandArg<String>> parts;
	
	public CompositeString(List<CommandArg<String>> parts)
	{
		this.parts = parts;
	}
	
	@Override
	public String eval(ICommandSender sender) throws CommandException
	{
		final StringBuilder sb = new StringBuilder();
		for (final CommandArg<String> part : this.parts)
		{
			sb.append(part.eval(sender));
		}
		return sb.toString();
	}
	
}
