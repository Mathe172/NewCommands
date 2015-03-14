package net.minecraft.command.arg;

import java.util.Iterator;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class CompoundArg<T> extends CommandArg<T>
{
	
	final List<Processable> toProcess;
	final List<Boolean> ignoreErrors;
	final CommandArg<T> trailing;
	
	public CompoundArg(final List<Processable> toProcess, final List<Boolean> ignoreErrors, final CommandArg<T> trailing)
	{
		this.toProcess = toProcess;
		this.ignoreErrors = ignoreErrors;
		this.trailing = trailing;
	}
	
	@Override
	public T eval(final ICommandSender sender) throws CommandException
	{
		final Iterator<Boolean> it = this.ignoreErrors.iterator();
		for (final Processable toProc : this.toProcess)
		{
			if (it.next())
			{
				try
				{
					toProc.process(sender);
				} catch (final CommandException ex)
				{
				}
			}
			else
				toProc.process(sender);
		}
		
		return this.trailing.eval(sender);
	}
}
