package net.minecraft.command.collections;

import java.util.List;
import java.util.Set;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.IPermission;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.construction.OperatorConstructable;
import net.minecraft.command.construction.RegistrationHelper;
import net.minecraft.command.operators.OperatorItems;
import net.minecraft.command.type.custom.ParserDouble;
import net.minecraft.command.type.custom.ParserInt;
import net.minecraft.command.type.custom.ParserName;
import net.minecraft.command.type.custom.nbt.TypeNBTBase;
import net.minecraft.command.type.management.TypeID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.Vec3;

public class Operators extends RegistrationHelper
{
	public static void init()
	{
		final Set<TypeID<?>> resTypeDouble = ParsingUtilities.<TypeID<?>> toSet(TypeIDs.Double);
		final Set<TypeID<?>> resTypeInt = ParsingUtilities.<TypeID<?>> toSet(TypeIDs.Integer);
		final Set<TypeID<?>> resTypeString = ParsingUtilities.<TypeID<?>> toSet(TypeIDs.String);
		final Set<TypeID<?>> resTypeEntity = ParsingUtilities.<TypeID<?>> toSet(TypeIDs.Entity);
		final Set<TypeID<?>> resTypeVec3 = ParsingUtilities.<TypeID<?>> toSet(TypeIDs.Coordinates);
		final Set<TypeID<?>> resTypeNBT = ParsingUtilities.<TypeID<?>> toSet(TypeIDs.NBTBase);
		
		register("+", operator(IPermission.unrestricted, resTypeDouble).then(ParserDouble.parser).then(ParserDouble.parser).construct(
			new OperatorConstructable()
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
			}));
		
		register("*", operator(IPermission.unrestricted, resTypeDouble).then(ParserDouble.parser).then(ParserDouble.parser).construct(
			new OperatorConstructable()
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
			}));
		
		register("-", operator(IPermission.unrestricted, resTypeDouble).then(ParserDouble.parser).then(ParserDouble.parser).construct(
			new OperatorConstructable()
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
			}));
		
		register("sqrt", operator(IPermission.unrestricted, resTypeDouble).then(ParserDouble.parser).construct(
			new OperatorConstructable()
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
			}));
		
		register("sq", operator(IPermission.unrestricted, resTypeDouble).then(ParserDouble.parser).construct(
			new OperatorConstructable()
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
			}));
		
		register("-0", operator(IPermission.unrestricted, resTypeDouble).then(ParserDouble.parser).construct(
			new OperatorConstructable()
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
			}));
		
		register("sin", operator(IPermission.unrestricted, resTypeDouble).then(ParserDouble.parser).construct(
			new OperatorConstructable()
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
			}));
		
		register("cos", operator(IPermission.unrestricted, resTypeDouble).then(ParserDouble.parser).construct(
			new OperatorConstructable()
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
			}));
		
		register("x", operator(IPermission.level2, resTypeDouble).then(Types.entity).construct(
			new OperatorConstructable()
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
			}));
		
		register("y", operator(IPermission.level2, resTypeDouble).then(Types.entity).construct(
			new OperatorConstructable()
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
			}));
		
		register("z", operator(IPermission.level2, resTypeDouble).then(Types.entity).construct(
			new OperatorConstructable()
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
			}));
		
		register("pos", operator(IPermission.level2, resTypeVec3).then(Types.entity).construct(
			new OperatorConstructable()
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
			}));
		
		register("rx", operator(IPermission.level2, resTypeDouble).then(Types.entity).construct(
			new OperatorConstructable()
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
			}));
		
		register("ry", operator(IPermission.level2, resTypeDouble).then(Types.entity).construct(
			new OperatorConstructable()
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
			}));
		
		register("slot", operator(IPermission.level2, resTypeInt).then(Types.entity).construct(
			new OperatorConstructable()
			{
				@Override
				public ArgWrapper<?> construct(final List<ArgWrapper<?>> operands)
				{
					final CommandArg<Entity> arg = operands.get(0).get(TypeIDs.Entity);
					
					return TypeIDs.Integer.wrap(new CommandArg<Integer>()
					{
						@Override
						public Integer eval(final ICommandSender sender) throws CommandException
						{
							final Entity entity = arg.eval(sender);
							
							if (!(entity instanceof EntityPlayerMP))
								throw new PlayerNotFoundException();
							
							return ((EntityPlayerMP) entity).inventory.currentItem;
						}
					});
				}
			}));
		
		register("items", operator(IPermission.unrestricted, resTypeNBT)
			.then(TypeNBTBase.parserDefault)
			.then(ParserInt.parserList)
			.construct(OperatorItems.constructable));
		
		register("i", primitiveOperator(IPermission.unrestricted, ParserInt.parser, resTypeInt));
		
		register("s", primitiveOperator(IPermission.unrestricted, ParserName.parser, resTypeString));
		
		register("e", primitiveOperator(IPermission.unrestricted, Types.entity, resTypeEntity));
	}
}
