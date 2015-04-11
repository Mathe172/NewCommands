package net.minecraft.command.arg;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class CounterArg extends CommandArg<Integer> implements Processable
{
	private Integer index = 0;
	private final CommandArg<Integer> startArg;
	private final CommandArg<Integer> endArg;
	
	public CounterArg(final CommandArg<Integer> startArg, final CommandArg<Integer> endArg)
	{
		this.startArg = startArg;
		this.endArg = endArg;
	}
	
	private int end = 0;
	
	@Override
	public Integer eval(final ICommandSender sender) throws CommandException
	{
		return this.index;
	}
	
	public void inc()
	{
		++this.index;
	}
	
	public boolean endNotReached()
	{
		return this.index <= this.end;
	}
	
	@Override
	public void process(final ICommandSender sender) throws CommandException
	{
		this.index = this.startArg.eval(sender);
		this.end = this.endArg.eval(sender);
	}
	
}
