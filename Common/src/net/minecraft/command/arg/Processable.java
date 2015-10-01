package net.minecraft.command.arg;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public interface Processable
{
	public void process(ICommandSender sender) throws CommandException;
}
