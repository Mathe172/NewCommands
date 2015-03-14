package net.minecraft.command.construction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.command.IPermission;
import net.minecraft.command.descriptors.CommandDescriptor;
import net.minecraft.command.type.IDataType;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class CommandConstructor
{
	protected final Set<String> names = new HashSet<>();
	
	protected final List<IDataType<?>> args = new ArrayList<>();
	
	protected final Set<Pair<Set<String>, CommandDescriptor>> subCommands = new HashSet<>();
	
	protected final IPermission permission;
	
	public CommandConstructor(final String name, final String... aliases)
	{
		this(null, name, aliases);
	}
	
	public CommandConstructor(IPermission permission, final String name, final String... aliases)
	{
		this.permission = permission;
		this.names.add(name);
		
		for (final String alias : aliases)
			this.names.add(alias);
	}
	
	public final CommandConstructor then(final IDataType<?> arg)
	{
		if (!this.subCommands.isEmpty())
			return new CCO(this).then(arg);
		
		this.args.add(arg);
		return this;
	}
	
	@SafeVarargs
	public final CommandConstructor sub(final Pair<Set<String>, CommandDescriptor>... subCommands)
	{
		for (final Pair<Set<String>, CommandDescriptor> subCommand : subCommands)
			this.subCommands.add(subCommand);
		
		return this;
	}
	
	public final CommandConstructor optional(final IDataType<?> arg)
	{
		return new CCOptional(this).then(arg);
	}
	
	public final CommandConstructor optional(final IDataType<?> arg, final CommandConstructable constructable)
	{
		return new CCOptConstructable(this, constructable).then(arg);
	}
	
	public final CommandConstructor optional(final CommandConstructor constructor, final CommandConstructable constructable)
	{
		return new CCOptConstructables(this, new ImmutablePair<>(constructor, constructable));
	}
	
	@SafeVarargs
	public final CommandConstructor optinal(final Pair<CommandConstructor, CommandConstructable>... commands)
	{
		return new CCOptConstructables(this, commands);
	}
	
	public final CommandConstructor optional(final CommandConstructor... commands)
	{
		return new CCOptionals(this, commands);
	}
	
	public Pair<Set<String>, CommandDescriptor> executes(final CommandConstructable constructable)
	{
		final CommandDescriptor descriptor = new CommandDescriptorConstructable(constructable, this.permission, null, this.args, this.subCommands);
		
		return new ImmutablePair<>(this.names, descriptor);
	}
}
