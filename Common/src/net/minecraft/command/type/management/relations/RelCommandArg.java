package net.minecraft.command.type.management.relations;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.type.management.Convertable;
import net.minecraft.command.type.management.Converter;

public class RelCommandArg extends Relation
{
	public RelCommandArg()
	{
		super("CommandArg");
	}
	
	public final <T> void registerPair(final Convertable<T, ?, ?> base, final Convertable<CommandArg<T>, ?, ?> rel)
	{
		base.addAttribute(new Att<>(rel));
	}
	
	public class Att<T, E extends CommandException> extends Attribute
	{
		public final Convertable<CommandArg<T>, ?, E> rel;
		
		private Att(final Convertable<CommandArg<T>, ?, E> rel)
		{
			this.rel = rel;
		}
		
		public final <F> void apply(final Convertable<CommandArg<F>, ?, ?> source, final Converter<F, T, ?> converter, final int dist)
		{
			source.addConverter(this.rel, new Converter<CommandArg<F>, CommandArg<T>, E>()
			{
				@Override
				public CommandArg<T> convert(final CommandArg<F> toConvert)
				{
					return new CommandArg<T>()
					{
						@Override
						public T eval(final ICommandSender sender) throws CommandException
						{
							return converter.convert(toConvert.eval(sender));
						}
					};
				}
			}, dist);
		}
	}
}
