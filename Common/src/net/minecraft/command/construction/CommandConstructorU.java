package net.minecraft.command.construction;

import java.util.Set;

import net.minecraft.command.IPermission;
import net.minecraft.command.descriptors.CommandDescriptor;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class CommandConstructorU extends CommandConstructor
{
	private final String usage;
	
	public CommandConstructorU(String usage, String name, String... aliases)
	{
		this(null, usage, name, aliases);
	}
	
	public CommandConstructorU(IPermission permission, String usage, String name, String... aliases)
	{
		super(permission, name, aliases);
		this.usage = usage;
	}
	
	@Override
	public Pair<Set<String>, CommandDescriptor> executes(final CommandConstructable constructable)
	{
		final CommandDescriptor descriptor = new CommandDescriptorConstructable(constructable, this.permission, this.usage, this.args, this.subCommands);
		
		return new ImmutablePair<>(this.names, descriptor);
	}
}
