package net.minecraft.command;

import net.minecraft.command.arg.CommandArg;

public abstract class IFutureCommand
{
	private String commandStored;
	
	public IFutureCommand(final String commandStored)
	{
		this.commandStored = commandStored;
	}
	
	public final String get()
	{
		return this.commandStored;
	}
	
	public void set(final String command)
	{
		this.commandStored = command;
	}
	
	public abstract CommandArg<Integer> getCommand();
	
}