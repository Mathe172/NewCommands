package net.minecraft.command.construction;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.command.IPermission;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.WrongUsageException;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.construction.ICommandConstructor.C;
import net.minecraft.command.construction.ICommandConstructor.CP;
import net.minecraft.command.construction.ICommandConstructor.CPU;
import net.minecraft.command.construction.ICommandConstructor.CU;
import net.minecraft.command.construction.ICommandConstructor.P;
import net.minecraft.command.construction.ICommandConstructor.PU;
import net.minecraft.command.construction.ICommandConstructor.U;
import net.minecraft.command.descriptors.CommandDescriptor;
import net.minecraft.command.descriptors.CommandDescriptor.ParserData;
import net.minecraft.command.descriptors.CommandDescriptor.WUEProvider;
import net.minecraft.command.descriptors.OperatorDescriptor;
import net.minecraft.command.descriptors.SelectorDescriptor;
import net.minecraft.command.type.IDataType;
import net.minecraft.command.type.management.TypeID;

import org.apache.commons.lang3.ArrayUtils;

public class RegistrationHelper
{
	public static final SelectorConstructor selector(final IPermission permission, final TypeID<?>... resultTypes)
	{
		return new SelectorConstructor(permission, resultTypes);
	}
	
	public static final void register(final String name, final SelectorDescriptor<?> descriptor)
	{
		SelectorDescriptor.registerSelector(name, descriptor);
	}
	
	public static final void register(final SelectorDescriptor<?> descriptor, final String... names)
	{
		SelectorDescriptor.registerSelector(descriptor, names);
	}
	
	public static final OperatorConstructor operator(final IPermission permission, final TypeID<?>... resultTypes)
	{
		return new OperatorConstructor(permission, resultTypes);
	}
	
	public static final OperatorConstructor operator(final IPermission permission, final Set<TypeID<?>> resultTypes)
	{
		return new OperatorConstructor(permission, resultTypes);
	}
	
	public static final OperatorDescriptor primitiveOperator(final IPermission permission, final IDataType<?> operand, final Set<TypeID<?>> resultTypes)
	{
		return new OperatorDescriptor.Primitive(resultTypes, permission, operand);
	}
	
	public static final OperatorDescriptor primitiveOperator(final IPermission permission, final IDataType<?> operand, final TypeID<?>... resultTypes)
	{
		return new OperatorDescriptor.Primitive(ParsingUtilities.toSet(resultTypes), permission, operand);
	}
	
	public static final void register(final String name, final OperatorDescriptor descriptor)
	{
		OperatorDescriptor.register(name, descriptor);
	}
	
	public static final void register(final String name, final ITabCompletion completion, final OperatorDescriptor descriptor)
	{
		OperatorDescriptor.register(name, completion, descriptor);
	}
	
	public static final WUEProvider usage(final String usage, final Object... args)
	{
		return new WUEProvider()
		{
			@Override
			public WrongUsageException create(final ParserData data)
			{
				return new WrongUsageException(usage, args);
			}
		};
	}
	
	/**
	 * Passes the last keyword as first argument to the exception. This means that it should NOT be used in the root command (no last keyword)
	 */
	public static final WUEProvider usageAliasAware(final String usage, final Object... args)
	{
		return new WUEProvider()
		{
			@Override
			public WrongUsageException create(final ParserData data)
			{
				return new WrongUsageException(usage, ArrayUtils.addAll(new Object[] { data.path.get(data.size() - 1) }, args));
			}
		};
	}
	
	public static final CPU command(final CommandConstructable constructable, final IPermission permission, final WUEProvider usage, final String name, final String... aliases)
	{
		return new CommandConstructor(new CommandProtoDescriptor.Constructable(constructable, permission, usage, name, aliases));
	}
	
	public static final CU command(final CommandConstructable constructable, final WUEProvider usage, final String name, final String... aliases)
	{
		return new CommandConstructor(new CommandProtoDescriptor.Constructable(constructable, usage, name, aliases));
	}
	
	public static final PU command(final IPermission permission, final WUEProvider usage, final String name, final String... aliases)
	{
		return new CommandConstructor(new CommandProtoDescriptor.NoConstructable(permission, usage, name, aliases));
	}
	
	public static final U command(final WUEProvider usage, final String name, final String... aliases)
	{
		return new CommandConstructor(new CommandProtoDescriptor.NoConstructable(usage, name, aliases));
	}
	
	public static final CP command(final CommandConstructable constructable, final IPermission permission, final String name, final String... aliases)
	{
		return new CommandConstructor(new CommandProtoDescriptor.Constructable(constructable, permission, null, name, aliases));
	}
	
	public static final C command(final CommandConstructable constructable, final String name, final String... aliases)
	{
		return new CommandConstructor(new CommandProtoDescriptor.Constructable(constructable, null, name, aliases));
	}
	
	public static final P command(final IPermission permission, final String name, final String... aliases)
	{
		return new CommandConstructor(new CommandProtoDescriptor.NoConstructable(permission, null, name, aliases));
	}
	
	public static final ICommandConstructor command(final String name, final String... aliases)
	{
		return new CommandConstructor(new CommandProtoDescriptor.NoConstructable(null, name, aliases));
	}
	
	public static final void register(final CPU toRegister)
	{
		for (final CommandProtoDescriptor descriptor : toRegister.baseCommands())
			CommandDescriptor.registerCommand(descriptor.construct(null));
	}
	
	@SuppressWarnings("unchecked")
	@SafeVarargs
	public static final <T extends ICommandConstructor> T group(final T... elements)
	{
		final Set<CommandProtoDescriptor> base = new HashSet<>(elements.length);
		final Set<CommandProtoDescriptor> ends = new HashSet<>(elements.length);
		
		for (final T element : elements)
		{
			base.addAll(element.baseCommands());
			ends.addAll(element.ends());
		}
		
		return (T) new CommandConstructor(base, ends);
	}
	
	public static final CP group(final CommandConstructable constructable, final IPermission permission, final String... names)
	{
		final Set<CommandProtoDescriptor> base = new HashSet<>(names.length);
		final Set<CommandProtoDescriptor> ends = new HashSet<>(names.length);
		
		for (final String name : names)
		{
			final CommandProtoDescriptor descriptor = new CommandProtoDescriptor.Constructable(constructable, permission, null, name);
			base.add(descriptor);
			ends.add(descriptor);
		}
		
		return new CommandConstructor(base, ends);
	}
	
	public static final C group(final CommandConstructable constructable, final String... names)
	{
		return group(constructable, null, names);
	}
	
	public static final P group(final IPermission permission, final String... names)
	{
		final Set<CommandProtoDescriptor> base = new HashSet<>(names.length);
		final Set<CommandProtoDescriptor> ends = new HashSet<>(names.length);
		
		for (final String name : names)
		{
			final CommandProtoDescriptor descriptor = new CommandProtoDescriptor.NoConstructable(permission, null, name);
			base.add(descriptor);
			ends.add(descriptor);
		}
		
		return new CommandConstructor(base, ends);
	}
	
	public static final ICommandConstructor group(final String... names)
	{
		return group((IPermission) null, names);
	}
}