package net.minecraft.command;

import net.minecraft.command.arg.CommandArg;

public interface ICommandManager
{
	public int executeCommand(ICommandSender sender, CommandArg<Integer> command);
	
	public int executeCommand(ICommandSender sender, String command);
	
	public int executeCommand(ICommandSender sender, String command, int startIndex);
}
