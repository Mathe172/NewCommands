package net.minecraft.command.construction;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.command.IPermission;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.descriptors.CommandDescriptor;
import net.minecraft.command.type.IExParse;

import org.apache.commons.lang3.tuple.Pair;

public class CommandDescriptorConstructable extends CommandDescriptorDefault
{
	private final CommandConstructable constructable;
	
	public CommandDescriptorConstructable(final CommandConstructable constructable, final IPermission permission, final WUEProvider usage, final List<IExParse<Void, ? super CParserData>> paramTypes)
	{
		super(permission, usage, paramTypes);
		this.constructable = constructable;
	}
	
	public CommandDescriptorConstructable(final CommandConstructable constructable, final IPermission permission, final WUEProvider usage, final List<IExParse<Void, ? super CParserData>> paramTypes, final Map<String, CommandDescriptor<? super CParserData>> keywords)
	{
		super(permission, usage, keywords, paramTypes);
		this.constructable = constructable;
	}
	
	public CommandDescriptorConstructable(final CommandConstructable constructable, final IPermission permission, final WUEProvider usage, final List<IExParse<Void, ? super CParserData>> paramTypes, final Set<Pair<Set<String>, CommandDescriptor<? super CParserData>>> descriptors)
	{
		super(permission, usage, descriptors, paramTypes);
		this.constructable = constructable;
	}
	
	@Override
	public CommandArg<Integer> construct(final CParserData data) throws SyntaxErrorException
	{
		return this.constructable.construct(data);
	}
}
