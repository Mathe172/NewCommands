package net.minecraft.command.construction;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.construction.CommandDescriptorDefault.CParserData;
import net.minecraft.command.construction.ICommandConstructor.CPU;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IDataType;
import net.minecraft.command.type.IExParse;
import net.minecraft.command.type.base.ExCustomParse;

public class CommandConstructor implements CPU
{
	public final Set<CommandProtoDescriptor> baseCommands;
	
	public final Set<CommandProtoDescriptor> ends;
	
	public CommandConstructor(final Set<CommandProtoDescriptor> baseCommands, final Set<CommandProtoDescriptor> ends)
	{
		this.baseCommands = baseCommands;
		this.ends = ends;
	}
	
	public CommandConstructor(final Set<CommandProtoDescriptor> baseCommands)
	{
		this.baseCommands = baseCommands;
		this.ends = new HashSet<>();
		this.ends.addAll(baseCommands);
	}
	
	public CommandConstructor(final CommandProtoDescriptor baseCommand)
	{
		this.baseCommands = Collections.singleton(baseCommand);
		this.ends = new HashSet<>();
		this.ends.add(baseCommand);
	}
	
	@Override
	public final CommandConstructor then(final IExParse<Void, ? super CParserData> arg)
	{
		CommandProtoDescriptor newEnd = null;
		
		final Iterator<CommandProtoDescriptor> it = this.ends.iterator();
		
		while (it.hasNext())
		{
			final CommandProtoDescriptor end = it.next();
			
			if (end.subCommands.isEmpty())
				end.args.add(arg);
			else
			{
				if (newEnd == null)
				{
					newEnd = new CommandProtoDescriptor.NoConstructable("", Collections.<String> emptyList(), null);
					newEnd.args.add(arg);
				}
				
				end.subCommands.add(newEnd);
				end.useEmptyConstructable = true;
				it.remove();
			}
		}
		
		if (newEnd != null)
			this.ends.add(newEnd);
		
		return this;
	}
	
	@Override
	public final CommandConstructor then(final IDataType<?> arg)
	{
		return this.then(wrap(arg));
	}
	
	private static final IExParse<Void, ? super CParserData> wrap(final IDataType<?> arg)
	{
		return new ExCustomParse<Void, CParserData>()
		{
			@Override
			public Void iParse(final Parser parser, final CParserData parserData) throws SyntaxErrorException
			{
				parserData.add(arg.parse(parser));
				
				return null;
			}
		};
	}
	
	@Override
	public final CommandConstructor sub(final ICommandConstructor... subCommands)
	{
		for (final CommandProtoDescriptor end : this.ends)
		{
			end.useEmptyConstructable = true;
			for (final ICommandConstructor subCommand : subCommands)
				end.subCommands.addAll(subCommand.baseCommands());
		}
		
		this.ends.clear();
		
		for (final ICommandConstructor subCommand : subCommands)
			this.ends.addAll(subCommand.ends());
		
		return this;
	}
	
	/**
	 * Single optional argument. If {@code arg} is missing while parsing, the command is finished (the whole subcommand-tree is dependent on {@code arg})
	 */
	@Override
	public final CommandConstructor optional(final IExParse<Void, ? super CParserData> arg)
	{
		this.optional(arg, new CommandProtoDescriptor.NoConstructable("", Collections.<String> emptyList(), null));
		
		return this;
	}
	
	@Override
	public final CommandConstructor optional(final IDataType<?> arg)
	{
		return this.optional(wrap(arg));
	}
	
	/**
	 * Single optional argument. If {@code arg} is missing while parsing, the command is finished (the whole subcommand-tree is dependent on this {@code arg})
	 * 
	 * @param constructable
	 *            Used to construct the command that is terminated exactly after {@code arg}
	 */
	@Override
	public final CommandConstructor optional(final IExParse<Void, ? super CParserData> arg, final CommandConstructable constructable)
	{
		this.optional(arg, new CommandProtoDescriptor.Constructable("", Collections.<String> emptyList(), constructable, null));
		
		return this;
	}
	
	@Override
	public final CommandConstructor optional(final IDataType<?> arg, final CommandConstructable constructable)
	{
		return this.optional(wrap(arg), constructable);
	}
	
	private final void optional(final IExParse<Void, ? super CParserData> arg, final CommandProtoDescriptor descriptor)
	{
		descriptor.args.add(arg);
		
		for (final CommandProtoDescriptor end : this.ends)
			end.subCommands.add(descriptor);
		
		this.ends.clear();
		this.ends.add(descriptor);
	}
	
	/**
	 * Multiple optional subCommands. Everything added after this call gets added to all subCommands and to {@code this}
	 */
	@Override
	public final CommandConstructor optional(final ICommandConstructor... commands)
	{
		for (final CommandProtoDescriptor end : this.ends)
			for (final ICommandConstructor command : commands)
				end.subCommands.addAll(command.baseCommands());
		
		for (final ICommandConstructor command : commands)
			this.ends.addAll(command.ends());
		
		return this;
	}
	
	@Override
	public final Set<CommandProtoDescriptor> baseCommands()
	{
		return this.baseCommands;
	}
	
	@Override
	public final Set<CommandProtoDescriptor> ends()
	{
		return this.ends;
	}
	
	@Override
	public final CommandConstructor sub(final C... subCommands)
	{
		return this.sub((ICommandConstructor[]) subCommands);
	}
	
	@Override
	public final CommandConstructor sub(final P... subCommands)
	{
		return this.sub((ICommandConstructor[]) subCommands);
	}
	
	@Override
	public CommandConstructor sub(final CP... subCommands)
	{
		return this.sub((ICommandConstructor[]) subCommands);
	}
}
