package net.minecraft.command.type.custom;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.type.management.Convertable;
import net.minecraft.command.type.management.Converter;
import net.minecraft.command.type.management.relations.RelCommandArg;
import net.minecraft.command.type.management.relations.RelDefault;
import net.minecraft.command.type.management.relations.RelIdentity;
import net.minecraft.command.type.management.relations.RelList;
import net.minecraft.command.type.management.relations.Relation.Attribute;
import net.minecraft.command.type.management.relations.Relation.Provider;

public class Relations
{
	public static final RelIdentity id = new RelIdentity();
	public static final Attribute idAttribute = id.new Att();
	
	public static final RelCommandArg commandArg = new RelCommandArg();
	
	public static final RelList list = new RelList();
	
	public static final RelDefault relDefault = new RelDefault();
	
	public static final void initRelations()
	{
		commandArg.addProvider(commandArg, new Provider()
		{
			// Checked...
			@SuppressWarnings("unchecked")
			@Override
			public <F, T, E extends CommandException> void apply(final Convertable<F, ?, ?> source, final Convertable<T, ?, E> target, final Converter<F, T, ? extends E> converter, final int dist, final Attribute attSource, final Attribute attTarget)
			{
				final Convertable<CommandArg<F>, ?, ?> relSource = ((RelCommandArg.Att<F, ?>) attSource).rel;
				
				((RelCommandArg.Att<T, ?>) attTarget).apply(relSource, converter, dist);
			}
		});
		
		relDefault.addProvider(id, new Provider()
		{
			// Checked...
			@SuppressWarnings("unchecked")
			@Override
			public <F, T, E extends CommandException> void apply(final Convertable<F, ?, ?> source, final Convertable<T, ?, E> target, final Converter<F, T, ? extends E> converter, final int dist, final Attribute attSource, final Attribute attTarget)
			{
				((RelDefault.Att<T, ?, E>) attTarget).apply(source, converter, dist);
			}
		});
		
		list.addProvider(list, new Provider()
		{
			// Checked...
			@SuppressWarnings("unchecked")
			@Override
			public <F, T, E extends CommandException> void apply(final Convertable<F, ?, ?> source, final Convertable<T, ?, E> target, final Converter<F, T, ? extends E> converter, final int dist, final Attribute attSource, final Attribute attTarget)
			{
				final Convertable<List<F>, ?, ?> relSource = ((RelList.Att<F, ?>) attSource).rel;
				final Convertable<List<T>, ?, ? super E> relTarget = ((RelList.Att<T, ? super E>) attTarget).rel;
				
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
