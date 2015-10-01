package net.minecraft.command.construction;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.command.IPermission;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.PrimitiveParameter;
import net.minecraft.command.construction.ICommandConstructor.C;
import net.minecraft.command.construction.ICommandConstructor.CP;
import net.minecraft.command.construction.ICommandConstructor.CPU;
import net.minecraft.command.construction.ICommandConstructor.CU;
import net.minecraft.command.construction.ICommandConstructor.P;
import net.minecraft.command.construction.ICommandConstructor.PU;
import net.minecraft.command.construction.ICommandConstructor.U;
import net.minecraft.command.descriptors.CommandDescriptor;
import net.minecraft.command.descriptors.ICommandDescriptor.UsageProvider;
import net.minecraft.command.descriptors.OperatorDescriptor;
import net.minecraft.command.descriptors.OperatorDescriptor.ListOperands;
import net.minecraft.command.descriptors.SelectorDescriptor;
import net.minecraft.command.descriptors.SelectorDescriptorNoContent;
import net.minecraft.command.descriptors.SelectorDescriptorNoContent.PrimitiveConstructable;
import net.minecraft.command.descriptors.SelectorDescriptorSingleArg;
import net.minecraft.command.descriptors.SelectorDescriptorSingleArg.SingleArgConstructable;
import net.minecraft.command.type.IDataType;
import net.minecraft.command.type.management.TypeID;

public class RegistrationHelper
{
	protected RegistrationHelper()
	{
	}
	
	public static final SelectorDescriptor<?> selector(final IPermission permission, final PrimitiveConstructable constructable, final TypeID<?>... resultTypes)
	{
		return new SelectorDescriptorNoContent(new HashSet<>(Arrays.asList(resultTypes)), permission, constructable);
	}
	
	public static final SelectorDescriptor<?> selector(final String key, final IDataType<?> arg, final SingleArgConstructable constructable, final IPermission permission, final TypeID<?>... resultTypes)
	{
		return new SelectorDescriptorSingleArg(new HashSet<>(Arrays.asList(resultTypes)), permission, key, arg, constructable);
	}
	
	public static final SelectorDescriptor<?> selector(final IDataType<?> arg, final SingleArgConstructable constructable, final IPermission permission, final TypeID<?>... resultTypes)
	{
		return new SelectorDescriptorSingleArg(new HashSet<>(Arrays.asList(resultTypes)), permission, arg, constructable);
	}
	
	public static final SelectorConstructor selector(final IPermission permission, final TypeID<?>... resultTypes)
	{
		return new SelectorConstructor(permission, resultTypes);
	}
	
	public static final void register(final String name, final SelectorDescriptor<?> descriptor)
	{
		SelectorDescriptor.registerSelector(name, descriptor);
	}
	
	public static final void register(final String name, final List<String> aliases, final SelectorDescriptor<?> descriptor)
	{
		SelectorDescriptor.registerSelector(name, aliases, descriptor);
	}
	
	public static final OperatorConstructor operator(final IPermission permission, final TypeID<?>... resultTypes)
	{
		return new OperatorConstructor(permission, resultTypes);
	}
	
	public static final OperatorConstructor operator(final IPermission permission, final Set<TypeID<?>> resultTypes)
	{
		return new OperatorConstructor(permission, resultTypes);
	}
	
	public static final OperatorDescriptor constant(final ArgWrapper<?> constant, final Set<TypeID<?>> resultType)
	{
		return operator(IPermission.unrestricted, resultType)
			.construct(new OperatorConstructable()
			{
				@Override
				public ArgWrapper<?> construct(final ListOperands operands) throws SyntaxErrorException
				{
					return constant;
				}
			});
	}
	
	public static final OperatorDescriptor constant(final ArgWrapper<?> constant, final TypeID<?> resultType)
	{
		return constant(constant, Collections.<TypeID<?>> singleton(resultType));
	}
	
	public static final <T> OperatorDescriptor constant(final T constant, final TypeID<T> resultType)
	{
		return constant(new PrimitiveParameter<>(constant).wrap(resultType), Collections.<TypeID<?>> singleton(resultType));
	}
	
	public static final OperatorDescriptor primitiveOperator(final IPermission permission, final IDataType<?> operand, final Set<TypeID<?>> resultTypes)
	{
		return new OperatorDescriptor.Primitive(resultTypes, permission, operand);
	}
	
	public static final OperatorDescriptor primitiveOperator(final IPermission permission, final IDataType<?> operand, final TypeID<?>... resultTypes)
	{
		return new OperatorDescriptor.Primitive(new HashSet<>(Arrays.asList(resultTypes)), permission, operand);
	}
	
	public static final void register(final String name, final OperatorDescriptor descriptor)
	{
		OperatorDescriptor.register(name, descriptor);
	}
	
	public static final void register(final String name, final List<String> aliases, final OperatorDescriptor descriptor)
	{
		OperatorDescriptor.register(name, descriptor);
		
		for (final String alias : aliases)
			OperatorDescriptor.register(alias, descriptor);
	}
	
	public static final List<String> alias(final String... aliases)
	{
		return Arrays.asList(aliases);
	}
	
	public static final UsageProvider usage(final String usage, final Object... args)
	{
		return new UsageProviderDefault()
		{
			@Override
			protected <R> R create(final List<String> path, final AbstractCreator<R> creator)
			{
				return creator.create(usage, args);
			}
		};
	}
	
	/**
	 * Passes the last keyword as first argument to the exception. This means that it should NOT be used in the root command (no last keyword)
	 */
	public static final UsageProvider usageAliasAware(final String usage, final Object... args)
	{
		return new UsageProviderDefault()
		{
			@Override
			protected <R> R create(final List<String> path, final AbstractCreator<R> creator)
			{
				return creator.create(usage, ArrayUtils.addAll(new Object[] { path.get(path.size() - 1) }, args));
			}
		};
	}
	
	public static final CPU command(final String name, final List<String> aliases, final CommandConstructable constructable, final IPermission permission, final UsageProvider usage)
	{
		return new CommandConstructor(new CommandProtoDescriptor.Constructable(name, aliases, constructable, permission, usage));
	}
	
	public static final CU command(final String name, final List<String> aliases, final CommandConstructable constructable, final UsageProvider usage)
	{
		return new CommandConstructor(new CommandProtoDescriptor.Constructable(name, aliases, constructable, usage));
	}
	
	public static final PU command(final String name, final List<String> aliases, final IPermission permission, final UsageProvider usage)
	{
		return new CommandConstructor(new CommandProtoDescriptor.NoConstructable(name, aliases, permission, usage));
	}
	
	public static final U command(final String name, final List<String> aliases, final UsageProvider usage)
	{
		return new CommandConstructor(new CommandProtoDescriptor.NoConstructable(name, aliases, usage));
	}
	
	public static final CP command(final String name, final List<String> aliases, final CommandConstructable constructable, final IPermission permission)
	{
		return new CommandConstructor(new CommandProtoDescriptor.Constructable(name, aliases, constructable, permission, null));
	}
	
	public static final C command(final String name, final List<String> aliases, final CommandConstructable constructable)
	{
		return new CommandConstructor(new CommandProtoDescriptor.Constructable(name, aliases, constructable, null));
	}
	
	public static final P command(final String name, final List<String> aliases, final IPermission permission)
	{
		return new CommandConstructor(new CommandProtoDescriptor.NoConstructable(name, aliases, permission, null));
	}
	
	public static final ICommandConstructor command(final String name, final List<String> aliases)
	{
		return new CommandConstructor(new CommandProtoDescriptor.NoConstructable(name, aliases, null));
	}
	
	public static final CPU command(final String name, final CommandConstructable constructable, final IPermission permission, final UsageProvider usage)
	{
		return new CommandConstructor(new CommandProtoDescriptor.Constructable(name, Collections.<String> emptyList(), constructable, permission, usage));
	}
	
	public static final CU command(final String name, final CommandConstructable constructable, final UsageProvider usage)
	{
		return new CommandConstructor(new CommandProtoDescriptor.Constructable(name, Collections.<String> emptyList(), constructable, usage));
	}
	
	public static final PU command(final String name, final IPermission permission, final UsageProvider usage)
	{
		return new CommandConstructor(new CommandProtoDescriptor.NoConstructable(name, Collections.<String> emptyList(), permission, usage));
	}
	
	public static final U command(final String name, final UsageProvider usage)
	{
		return new CommandConstructor(new CommandProtoDescriptor.NoConstructable(name, Collections.<String> emptyList(), usage));
	}
	
	public static final CP command(final String name, final CommandConstructable constructable, final IPermission permission)
	{
		return new CommandConstructor(new CommandProtoDescriptor.Constructable(name, Collections.<String> emptyList(), constructable, permission, null));
	}
	
	public static final C command(final String name, final CommandConstructable constructable)
	{
		return new CommandConstructor(new CommandProtoDescriptor.Constructable(name, Collections.<String> emptyList(), constructable, null));
	}
	
	public static final P command(final String name, final IPermission permission)
	{
		return new CommandConstructor(new CommandProtoDescriptor.NoConstructable(name, Collections.<String> emptyList(), permission, null));
	}
	
	public static final ICommandConstructor command(final String name, final String... aliases)
	{
		return new CommandConstructor(new CommandProtoDescriptor.NoConstructable(name, alias(aliases), null));
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
	
	private static final IPermission[] levels = new IPermission[] { IPermission.unrestricted, IPermission.level1, IPermission.level2, IPermission.level3, IPermission.level4 };
	
	public static final IPermission level(final int i)
	{
		return levels[i];
	}
}
