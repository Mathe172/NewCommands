package net.minecraft.command.construction;

import java.util.Set;

import net.minecraft.command.descriptors.CommandDescriptor;

import org.apache.commons.lang3.tuple.Pair;

public class CCOptConstructable extends ICommandConstructorOptional
{
	
	private final CommandConstructable constructable;
	
	public CCOptConstructable(final CommandConstructor base, final CommandConstructable constructable)
	{
		super(base);
		this.constructable = constructable;
	}
	
	@Override
	public Pair<Set<String>, CommandDescriptor> executes(final CommandConstructable constructable)
	{
		super.executes(this.constructable);
		
		return this.base.executes(constructable);
	}
	
}
