package net.minecraft.command.construction;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.command.CommandBase;
import net.minecraft.command.IPermission;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.descriptors.CommandDescriptor;
import net.minecraft.command.type.IDataType;

import org.apache.commons.lang3.tuple.Pair;

public class CommandDescriptorConstructable extends CommandDescriptor
{
	private final CommandConstructable constructable;
	
	public CommandDescriptorConstructable(final CommandConstructable constructable, final IPermission permission, final String usage, final List<IDataType<?>> paramTypes)
	{
		super(permission, usage, paramTypes);
		this.constructable = constructable;
	}
	
	public CommandDescriptorConstructable(final CommandConstructable constructable, final IPermission permission, final String usage, final List<IDataType<?>> paramTypes, final Map<String, CommandDescriptor> keywords)
	{
		super(permission, usage, paramTypes, keywords);
		this.constructable = constructable;
	}
	
	public CommandDescriptorConstructable(final CommandConstructable constructable, final IPermission permission, final String usage, final List<IDataType<?>> paramTypes, final Set<Pair<Set<String>, CommandDescriptor>> descriptors)
	{
		super(permission, usage, paramTypes, descriptors);
		this.constructable = constructable;
	}
	
	@Override
	public CommandBase construct(final List<ArgWrapper<?>> params, final IPermission permission)
	{
		return this.constructable.construct(params, permission);
	}
	
}
