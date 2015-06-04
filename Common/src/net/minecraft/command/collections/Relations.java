package net.minecraft.command.collections;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.type.management.Convertable;
import net.minecraft.command.type.management.Converter;
import net.minecraft.command.type.management.relations.RelCommandArg;
import net.minecraft.command.type.management.relations.RelDefault;
import net.minecraft.command.type.management.relations.RelIdentity;
import net.minecraft.command.type.management.relations.RelList;
import net.minecraft.command.type.management.relations.RelSuper;
import net.minecraft.command.type.management.relations.Relation.Attribute;
import net.minecraft.command.type.management.relations.Relation.Provider;

public final class Relations
{
	public static final RelIdentity id = new RelIdentity();
	public static final Attribute idAttribute = id.new Att();
	
	public static final RelCommandArg commandArg = new RelCommandArg();
	
	public static final RelList list = new RelList();
	
	public static final RelDefault relDefault = new RelDefault();
	
	public static final RelSuper relSuper = new RelSuper();
	
	private Relations()
	{
	}
	
	public static final void init()
	{
		commandArg.addProvider(commandArg, new Provider()
		{
			// Checked...
			@SuppressWarnings("unchecked")
			@Override
			public <F, T, E extends CommandException> void apply(final Convertable<F, ?, E> source, final Convertable<T, ?, ? extends E> target, final Converter<F, T, ? extends E> converter, final int dist, final Attribute attSource, final Attribute attTarget)
			{
				final Convertable<CommandArg<T>, ?, SyntaxErrorException> relTarget = ((RelCommandArg.Att<T>) attTarget).rel;
				
				((RelCommandArg.Att<F>) attSource).apply(relTarget, converter, dist);
			}
		});
		
		id.addProvider(relDefault, new Provider()
		{
			// Checked...
			@SuppressWarnings("unchecked")
			@Override
			public <F, T, E extends CommandException> void apply(final Convertable<F, ?, E> source, final Convertable<T, ?, ? extends E> target, final Converter<F, T, ? extends E> converter, final int dist, final Attribute attSource, final Attribute attTarget)
			{
				final RelDefault.Att<T, ?, ? extends E> att = (RelDefault.Att<T, ?, ? extends E>) attTarget;
				RelDefault.apply(source, att, converter, dist);
			}
		});
		
		relSuper.addProvider(id, new Provider()
		{
			// Checked...
			@SuppressWarnings("unchecked")
			@Override
			public <F, T, E extends CommandException> void apply(final Convertable<F, ?, E> source, final Convertable<T, ?, ? extends E> target, final Converter<F, T, ? extends E> converter, final int dist, final Attribute attSource, final Attribute attTarget)
			{
				final RelSuper.Att<?, F, ? super E> att = (RelSuper.Att<?, F, ? super E>) attSource;
				att.apply(target, converter, dist);
			}
		});
		
		list.addProvider(list, new Provider()
		{
			// Checked...
			@SuppressWarnings("unchecked")
			@Override
			public <F, T, E extends CommandException> void apply(final Convertable<F, ?, E> source, final Convertable<T, ?, ? extends E> target, final Converter<F, T, ? extends E> converter, final int dist, final Attribute attSource, final Attribute attTarget)
			{
				final Convertable<List<F>, ?, E> relSource = ((RelList.Att<F, E>) attSource).rel;
				final Convertable<List<T>, ?, ? extends E> relTarget = ((RelList.Att<T, ? extends E>) attTarget).rel;
				
				relSource.addConverter(relTarget, new Converter<List<F>, List<T>, E>()
				{
					@Override
					public List<T> convert(final List<F> toConvert) throws E
					{
						final List<T> list = new ArrayList<>(toConvert.size());
						
						for (final F item : toConvert)
							list.add(converter.convert(item));
						
						return list;
					}
					
				}, dist + 1);
			}
		});
	}
}
