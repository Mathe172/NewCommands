package net.minecraft.command.construction;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.command.descriptors.CommandDescriptor;

import org.apache.commons.lang3.tuple.Pair;

public class CCOptionals extends CCO
{
	private final Set<CommandConstructor> toConstruct = new HashSet<>();
	
	public CCOptionals(final CommandConstructor base, final CommandConstructor... commands)
	{
		super(base);
		
		for (final CommandConstructor command : commands)
			this.toConstruct.add(command);
	}
	
	@Override
	public Pair<Set<String>, CommandDescriptor> executes(final CommandConstructable constructable)
	{
		for (final CommandConstructor command : this.toConstruct)
		{
			command.args.addAll(this.args);
			command.subCommands.addAll(this.subCommands);
			this.base.subCommands.add(command.executes(constructable));
		}
		
		return super.executes(constructable);
	}
}
