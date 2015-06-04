package net.minecraft.command.construction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.command.IPermission;
import net.minecraft.command.descriptors.CommandDescriptor;
import net.minecraft.command.descriptors.CommandDescriptor.CParserData;
import net.minecraft.command.descriptors.CommandDescriptor.WUEProvider;
import net.minecraft.command.type.IExParse;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public abstract class CommandProtoDescriptor
{
	public final Set<String> names;
	private final WUEProvider usage;
	public final IPermission permission;
	
	public boolean useEmptyConstructable = false;
	
	public final List<IExParse<Void, ? super CParserData>> args = new ArrayList<>();
	public final List<CommandProtoDescriptor> subCommands = new ArrayList<>();
	
	public CommandProtoDescriptor(final IPermission permission, final WUEProvider usage, final String name, final String... aliases)
	{
		this.usage = usage;
		this.permission = permission;
		
		this.names = new HashSet<>(1 + aliases.length);
		
		this.names.add(name);
		
		for (final String alias : aliases)
			this.names.add(alias);
	}
	
	public Pair<Set<String>, CommandDescriptor<CParserData>> construct(final CommandConstructable constructable)
	{
		final Map<String, CommandDescriptor<? super CParserData>> subCommands = new HashMap<>(this.subCommands.size());
		
		for (final CommandProtoDescriptor subCommand : this.subCommands)
		{
			final Pair<Set<String>, CommandDescriptor<CParserData>> data = subCommand.construct(constructable);
			for (final String name : data.getLeft())
				subCommands.put(name, data.getRight());
		}
		
		return new ImmutablePair<Set<String>, CommandDescriptor<CParserData>>(
			this.names,
			new CommandDescriptorConstructable(
				this.useEmptyConstructable ? CommandConstructable.emptyConstructable : constructable,
				this.permission, this.usage, this.args, subCommands));
	}
	
	public static class Constructable extends CommandProtoDescriptor
	{
		public final CommandConstructable constructable;
		private Pair<Set<String>, CommandDescriptor<CParserData>> ret = null;
		
		public Constructable(final CommandConstructable constructable, final IPermission permission, final WUEProvider usage, final String name, final String... aliases)
		{
			super(permission, usage, name, aliases);
			this.constructable = constructable;
		}
		
		public Constructable(final CommandConstructable constructable, final WUEProvider usage, final String name, final String... aliases)
		{
			this(constructable, null, usage, name, aliases);
		}
		
		@Override
		public Pair<Set<String>, CommandDescriptor<CParserData>> construct(final CommandConstructable constructable)
		{
			if (this.ret == null)
				this.ret = super.construct(this.constructable);
			
			return this.ret;
		}
	}
	
	public static class NoConstructable extends CommandProtoDescriptor
	{
		private final Map<CommandConstructable, Pair<Set<String>, CommandDescriptor<CParserData>>> cachedRet = new HashMap<>();
		
		public NoConstructable(final IPermission permission, final WUEProvider usage, final String name, final String... aliases)
		{
			super(permission, usage, name, aliases);
		}
		
		public NoConstructable(final WUEProvider usage, final String name, final String... aliases)
		{
			this(null, usage, name, aliases);
		}
		
		@Override
		public Pair<Set<String>, CommandDescriptor<CParserData>> construct(final CommandConstructable constructable)
		{
			Pair<Set<String>, CommandDescriptor<CParserData>> ret = this.cachedRet.get(constructable);
			
			if (ret == null)
			{
				ret = super.construct(constructable);
				this.cachedRet.put(constructable, ret);
			}
			
			return ret;
		}
	}
}
