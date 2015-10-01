package net.minecraft.command.arg;

import java.util.Iterator;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class CompoundArg<T> extends CommandArg<T>
{
	protected final List<Processable> toProcess;
	protected final CommandArg<T> trailing;
	
	public CompoundArg(final List<Processable> toProcess, final CommandArg<T> trailing)
	{
		this.toProcess = toProcess;
		this.trailing = trailing;
	}
	
	@Override
	public T eval(final ICommandSender sender) throws CommandException
	{
		for (final Processable toProc : this.toProcess)
			toProc.process(sender);
		
		return this.trailing.eval(sender);
	}
	
	public static <T> CommandArg<T> create(final List<Processable> toProcess, final List<Boolean> ignoreErrors, final CommandArg<T> trailing)
	{
		if (toProcess.isEmpty())
			return trailing;
		
		return new IgnoreErrors<>(toProcess, ignoreErrors, trailing);
	}
	
	public static <T> CommandArg<T> create(final List<Processable> toProcess, final CommandArg<T> trailing)
	{
		if (toProcess.isEmpty())
			return trailing;
		
		return new CompoundArg<>(toProcess, trailing);
	}
	
	public static <T> ArgWrapper<T> create(final List<Processable> toProcess, final ArgWrapper<T> trailing)
	{
		if (toProcess.isEmpty())
			return trailing;
		
		return new CompoundArg<>(toProcess, trailing.arg()).wrap(trailing.type);
	}
	
	public static class IgnoreErrors<T> extends CompoundArg<T>
	{
		final List<Boolean> ignoreErrors;
		
		private IgnoreErrors(final List<Processable> toProcess, final List<Boolean> ignoreErrors, final CommandArg<T> trailing)
		{
			super(toProcess, trailing);
			this.ignoreErrors = ignoreErrors;
		}
		
		@Override
		public T eval(final ICommandSender sender) throws CommandException
		{
			final Iterator<Boolean> it = this.ignoreErrors.iterator();
			for (final Processable toProc : this.toProcess)
				if (it.next())
					try
					{
						toProc.process(sender);
					} catch (final CommandException ex)
					{
					}
				else
					toProc.process(sender);
			
			return this.trailing.eval(sender);
		}
	}
	
}
