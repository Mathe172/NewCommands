package net.minecraft.command.type.custom;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.descriptors.OperatorDescriptor;
import net.minecraft.command.type.management.TypeID;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

public class Operators
{
	public static void init()
	{
		final Set<TypeID<?>> resTypeDouble = resType(TypeIDs.Double);
		final Set<TypeID<?>> resTypeInt = resType(TypeIDs.Integer);
		final Set<TypeID<?>> resTypeString = resType(TypeIDs.String);
		final Set<TypeID<?>> resTypeEntity = resType(TypeIDs.Entity);
		final Set<TypeID<?>> resTypeVec3 = resType(TypeIDs.Coordinates);
		
		OperatorDescriptor.register("+", new OperatorDescriptor(resTypeDouble, ParserDouble.parser, ParserDouble.parser)
		{
			@Override
			public ArgWrapper<?> construct(final List<ArgWrapper<?>> operands)
			{
				final CommandArg<Double> arg1 = operands.get(0).get(TypeIDs.Double);
				final CommandArg<Double> arg2 = operands.get(1).get(TypeIDs.Double);
				
				return TypeIDs.Double.wrap(new CommandArg<Double>()
				{
					@Override
					public Double eval(final ICommandSender sender) throws CommandException
					{
						return Double.valueOf(arg1.eval(sender) + arg2.eval(sender));
					}
				});
			}
		});
		
		OperatorDescriptor.register("*", new OperatorDescriptor(resTypeDouble, ParserDouble.parser, ParserDouble.parser)
		{
			@Override
			public ArgWrapper<?> construct(final List<ArgWrapper<?>> operands)
			{
				final CommandArg<Double> arg1 = operands.get(0).get(TypeIDs.Double);
				final CommandArg<Double> arg2 = operands.get(1).get(TypeIDs.Double);
				
				return TypeIDs.Double.wrap(new CommandArg<Double>()
				{
					@Override
					public Double eval(final ICommandSender sender) throws CommandException
					{
						return Double.valueOf(arg1.eval(sender) * arg2.eval(sender));
					}
				});
			}
		});
		
		OperatorDescriptor.register("-", new OperatorDescriptor(resTypeDouble, ParserDouble.parser, ParserDouble.parser)
		{
			@Override
			public ArgWrapper<?> construct(final List<ArgWrapper<?>> operands)
			{
				final CommandArg<Double> arg1 = operands.get(0).get(TypeIDs.Double);
				final CommandArg<Double> arg2 = operands.get(1).get(TypeIDs.Double);
				
				return TypeIDs.Double.wrap(new CommandArg<Double>()
				{
					@Override
					public Double eval(final ICommandSender sender) throws CommandException
					{
						return Double.valueOf(arg1.eval(sender) - arg2.eval(sender));
					}
				});
			}
		});
		
		OperatorDescriptor.register("sqrt", new OperatorDescriptor(resTypeDouble, ParserDouble.parser)
		{
			@Override
			public ArgWrapper<?> construct(final List<ArgWrapper<?>> operands)
			{
				final CommandArg<Double> arg = operands.get(0).get(TypeIDs.Double);
				
				return TypeIDs.Double.wrap(new CommandArg<Double>()
				{
					@Override
					public Double eval(final ICommandSender sender) throws CommandException
					{
						return Math.sqrt(arg.eval(sender));
					}
				});
			}
		});
		
		OperatorDescriptor.register("sq", new OperatorDescriptor(resTypeDouble, ParserDouble.parser)
		{
			@Override
			public ArgWrapper<?> construct(final List<ArgWrapper<?>> operands)
			{
				final CommandArg<Double> arg = operands.get(0).get(TypeIDs.Double);
				
				return TypeIDs.Double.wrap(new CommandArg<Double>()
				{
					@Override
					public Double eval(final ICommandSender sender) throws CommandException
					{
						final double toSq = arg.eval(sender);
						
						return toSq * toSq;
					}
				});
			}
		});
		
		OperatorDescriptor.register("-0", new OperatorDescriptor(resTypeDouble, ParserDouble.parser)
		{
			@Override
			public ArgWrapper<?> construct(final List<ArgWrapper<?>> operands)
			{
				final CommandArg<Double> arg = operands.get(0).get(TypeIDs.Double);
				
				return TypeIDs.Double.wrap(new CommandArg<Double>()
				{
					@Override
					public Double eval(final ICommandSender sender) throws CommandException
					{
						return -arg.eval(sender);
					}
				});
			}
		});
		
		OperatorDescriptor.register("sin", new OperatorDescriptor(resTypeDouble, ParserDouble.parser)
		{
			@Override
			public ArgWrapper<?> construct(final List<ArgWrapper<?>> operands)
			{
				final CommandArg<Double> arg = operands.get(0).get(TypeIDs.Double);
				
				return TypeIDs.Double.wrap(new CommandArg<Double>()
				{
					@Override
					public Double eval(final ICommandSender sender) throws CommandException
					{
						return Math.sin(Math.toRadians(arg.eval(sender)));
					}
				});
			}
		});
		
		OperatorDescriptor.register("cos", new OperatorDescriptor(resTypeDouble, ParserDouble.parser)
		{
			@Override
			public ArgWrapper<?> construct(final List<ArgWrapper<?>> operands)
			{
				final CommandArg<Double> arg = operands.get(0).get(TypeIDs.Double);
				
				return TypeIDs.Double.wrap(new CommandArg<Double>()
				{
					@Override
					public Double eval(final ICommandSender sender) throws CommandException
					{
						return Math.cos(Math.toRadians(arg.eval(sender)));
					}
				});
			}
		});
		
		OperatorDescriptor.register("x", new OperatorDescriptor(resTypeDouble, Types.entity)
		{
			@Override
			public ArgWrapper<?> construct(final List<ArgWrapper<?>> operands)
			{
				final CommandArg<Entity> arg = operands.get(0).get(TypeIDs.Entity);
				
				return TypeIDs.Double.wrap(new CommandArg<Double>()
				{
					@Override
					public Double eval(final ICommandSender sender) throws CommandException
					{
						return new Double(arg.eval(sender).posX);
					}
				});
			}
		});
		
		OperatorDescriptor.register("y", new OperatorDescriptor(resTypeDouble, Types.entity)
		{
			@Override
			public ArgWrapper<?> construct(final List<ArgWrapper<?>> operands)
			{
				final CommandArg<Entity> arg = operands.get(0).get(TypeIDs.Entity);
				
				return TypeIDs.Double.wrap(new CommandArg<Double>()
				{
					@Override
					public Double eval(final ICommandSender sender) throws CommandException
					{
						return new Double(arg.eval(sender).posY);
					}
				});
			}
		});
		
		OperatorDescriptor.register("z", new OperatorDescriptor(resTypeDouble, Types.entity)
		{
			@Override
			public ArgWrapper<?> construct(final List<ArgWrapper<?>> operands)
			{
				final CommandArg<Entity> arg = operands.get(0).get(TypeIDs.Entity);
				
				return TypeIDs.Double.wrap(new CommandArg<Double>()
				{
					@Override
					public Double eval(final ICommandSender sender) throws CommandException
					{
						return new Double(arg.eval(sender).posZ);
					}
				});
			}
		});
		
		OperatorDescriptor.register("pos", new OperatorDescriptor(resTypeVec3, Types.entity)
		{
			@Override
			public ArgWrapper<?> construct(final List<ArgWrapper<?>> operands)
			{
				final CommandArg<Entity> arg = operands.get(0).get(TypeIDs.Entity);
				
				return TypeIDs.Coordinates.wrap(new CommandArg<Vec3>()
				{
					@Override
					public Vec3 eval(final ICommandSender sender) throws CommandException
					{
						return arg.eval(sender).getPositionVector();
					}
				});
			}
		});
		
		OperatorDescriptor.register("rx", new OperatorDescriptor(resTypeDouble, Types.entity)
		{
			@Override
			public ArgWrapper<?> construct(final List<ArgWrapper<?>> operands)
			{
				final CommandArg<Entity> arg = operands.get(0).get(TypeIDs.Entity);
				
				return TypeIDs.Double.wrap(new CommandArg<Double>()
				{
					@Override
					public Double eval(final ICommandSender sender) throws CommandException
					{
						return new Double(arg.eval(sender).rotationYaw);
					}
				});
			}
		});
		
		OperatorDescriptor.register("ry", new OperatorDescriptor(resTypeDouble, Types.entity)
		{
			@Override
			public ArgWrapper<?> construct(final List<ArgWrapper<?>> operands)
			{
				final CommandArg<Entity> arg = operands.get(0).get(TypeIDs.Entity);
				
				return TypeIDs.Double.wrap(new CommandArg<Double>()
				{
					@Override
					public Double eval(final ICommandSender sender) throws CommandException
					{
						return new Double(arg.eval(sender).rotationPitch);
					}
				});
			}
		});
		
		OperatorDescriptor.register("i", new OperatorDescriptor.Primitive(resTypeInt, ParserInt.parser));
		
		OperatorDescriptor.register("s", new OperatorDescriptor.Primitive(resTypeString, ParserName.parser));
		
		OperatorDescriptor.register("e", new OperatorDescriptor.Primitive(resTypeEntity, Types.entity));
	}
	
	private static Set<TypeID<?>> resType(final TypeID<?>... resTypes)
	{
		final Set<TypeID<?>> ret = new HashSet<>(resTypes.length);
		for (final TypeID<?> typeID : ret)
			ret.add(typeID);
		
		return ret;
	}
}
