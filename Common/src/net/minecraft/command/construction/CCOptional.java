package net.minecraft.command.construction;

import java.util.Set;

import net.minecraft.command.descriptors.CommandDescriptor;

import org.apache.commons.lang3.tuple.Pair;

public class CCOptional extends ICommandConstructorOptional
{
	public CCOptional(final CommandConstructor base)
	{
		super(base);
	}
	
	@Override
	public Pair<Set<String>, CommandDescriptor> executes(final CommandConstructable constructable)
	{
		super.executes(constructable);
		
		return this.base.executes(constructable);
	}
}
