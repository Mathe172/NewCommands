package net.minecraft.command.arg;

import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CachedArg.Initialized;
import net.minecraft.command.arg.ExArgWrapper.GetterWrapper;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.management.Convertable;
import net.minecraft.command.type.management.TypeID;

public abstract class ArgWrapper<R> extends AbstractWrapper<R>
{
	public ArgWrapper(final TypeID<R> type)
	{
		super(type);
	}
	
	public static <R> ArgWrapper<R> create(final TypeID<R> type, final CommandArg<R> arg)
	{
		return new ExArgWrapper<>(type, arg);
	}
	
	public abstract CommandArg<R> arg();
	
	// This is checked...
	@SuppressWarnings("unchecked")
	public <T> CommandArg<T> get(final TypeID<T> type)
	{
		checkTypes(type);
		
		return (CommandArg<T>) this.arg();
	}
	
	public static <T> CommandArg<T> get(final TypeID<T> type, final ArgWrapper<?> wrapper)
	{
		if (wrapper == null)
			return null;
		
		return wrapper.get(type);
	}
	
	public final <T> T convertTo(final Parser parser, final Convertable<?, T, ?> target) throws SyntaxErrorException
	{
		return target.convertFrom(parser, this);
	}
	
	public final <T> T iConvertTo(final Parser parser, final Convertable<T, ?, ?> target) throws SyntaxErrorException
	{
		return this.type.convertTo(parser, this.arg(), target);
	}
	
	public final <T> T iConvertTo(final Convertable<T, ?, ?> target) throws SyntaxErrorException
	{
		return this.type.convertTo(this.arg(), target);
	}
	
	public final ArgWrapper<R> linkSetter(final Setter<R> setter)
	{
		return this.type.wrap(new CommandArg<R>()
		{
			CommandArg<R> arg = arg();
			
			@Override
			public R eval(final ICommandSender sender) throws CommandException
			{
				final R value = this.arg.eval(sender);
				setter.set(value);
				return value;
			}
		});
	}
	
	public TypedWrapper<R> addToProcess(final List<Processable> toProcess)
	{
		final Initialized<R> ret = new Initialized<>(arg());
		
		toProcess.add(ret);
		
		return new GetterWrapper<R>(this.type, ret);
	}
}
