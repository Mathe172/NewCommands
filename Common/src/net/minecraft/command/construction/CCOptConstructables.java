package net.minecraft.command.construction;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.command.descriptors.CommandDescriptor;

import org.apache.commons.lang3.tuple.Pair;

public class CCOptConstructables extends CCO
{
	private final Set<Pair<CommandConstructor, CommandConstructable>> toConstruct;
	
	@SafeVarargs
	public CCOptConstructables(final CommandConstructor base, final Pair<CommandConstructor, CommandConstructable>... commands)
	{
		super(base);
		
		this.toConstruct = new HashSet<>(commands.length);
		
		for (final Pair<CommandConstructor, CommandConstructable> command : commands)
			this.toConstruct.add(command);
	}
	
	@Override
	public Pair<Set<String>, CommandDescriptor> executes(final CommandConstructable constructable)
	{
		for (final Pair<CommandConstructor, CommandConstructable> command : this.toConstruct)
		{
			command.getLeft().args.addAll(this.args);
			command.getLeft().subCommands.addAll(this.subCommands);
			this.base.subCommands.add(command.getLeft().executes(command.getRight()));
		}
		
		return super.executes(constructable);
	}
}
