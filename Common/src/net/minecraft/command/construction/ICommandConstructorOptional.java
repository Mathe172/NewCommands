package net.minecraft.command.construction;

import java.util.Set;

import net.minecraft.command.descriptors.CommandDescriptor;

import org.apache.commons.lang3.tuple.Pair;

public class ICommandConstructorOptional extends CommandConstructor
{
	protected final CommandConstructor base;
	
	public ICommandConstructorOptional(final CommandConstructor base)
	{
		super("");
		this.base = base;
	}
	
	@Override
	public Pair<Set<String>, CommandDescriptor> executes(final CommandConstructable constructable)
	{
		final Pair<Set<String>, CommandDescriptor> descriptor = super.executes(constructable);
		this.base.subCommands.add(descriptor);
		
		return descriptor;
	}
}
