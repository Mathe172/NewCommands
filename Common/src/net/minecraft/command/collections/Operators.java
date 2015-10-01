package net.minecraft.command.collections;

import java.util.Collections;
import java.util.Set;

import com.google.common.math.DoubleMath;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.construction.OperatorConstructable;
import net.minecraft.command.construction.RegistrationHelper;
import net.minecraft.command.descriptors.OperatorDescriptor.ListOperands;
import net.minecraft.command.operators.OperatorItems;
import net.minecraft.command.type.custom.ParserName;
import net.minecraft.command.type.custom.coordinate.TypeCoordinates;
import net.minecraft.command.type.custom.nbt.TypeNBTBase;
import net.minecraft.command.type.management.TypeID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class Operators extends RegistrationHelper
{
	public static void init()
	{
		final Set<TypeID<?>> resTypeDouble = Collections.<TypeID<?>> singleton(TypeIDs.Double);
		final Set<TypeID<?>> resTypeInt = Collections.<TypeID<?>> singleton(TypeIDs.Integer);
		final Set<TypeID<?>> resTypeString = Collections.<TypeID<?>> singleton(TypeIDs.String);
		final Set<TypeID<?>> resTypeEntity = Collections.<TypeID<?>> singleton(TypeIDs.Entity);
		final Set<TypeID<?>> resTypeVec3 = Collections.<TypeID<?>> singleton(TypeIDs.Coordinates);
		final Set<TypeID<?>> resTypeNBT = Collections.<TypeID<?>> singleton(TypeIDs.NBTBase);
		final Set<TypeID<?>> resTypeBoolean = Collections.<TypeID<?>> singleton(TypeIDs.Boolean);
		
		register("+", operator(level(0), resTypeDouble)
			.then(Parsers.dbl)
			.then(Parsers.dbl)
			.construct(
				new OperatorConstructable()
				{
					@Override
					public ArgWrapper<?> construct(final ListOperands operands)
					{
						return TypeIDs.Double.wrap(new CommandArg<Double>()
						{
							private final CommandArg<Double> arg1 = operands.get(TypeIDs.Double);
							private final CommandArg<Double> arg2 = operands.get(TypeIDs.Double);
							
							@Override
							public Double eval(final ICommandSender sender) throws CommandException
							{
								return this.arg1.eval(sender) + this.arg2.eval(sender);
							}
						});
					}
				}));
		
		register("*", operator(level(0), resTypeDouble)
			.then(Parsers.dbl)
			.then(Parsers.dbl)
			.construct(
				new OperatorConstructable()
				{
					@Override
					public ArgWrapper<?> construct(final ListOperands operands)
					{
						return TypeIDs.Double.wrap(new CommandArg<Double>()
						{
							private final CommandArg<Double> arg1 = operands.get(TypeIDs.Double);
							private final CommandArg<Double> arg2 = operands.get(TypeIDs.Double);
							
							@Override
							public Double eval(final ICommandSender sender) throws CommandException
							{
								return this.arg1.eval(sender) * this.arg2.eval(sender);
							}
						});
					}
				}));
		
		register("/", operator(level(0), resTypeDouble)
			.then(Parsers.dbl)
			.then(Parsers.dbl)
			.construct(
				new OperatorConstructable()
				{
					@Override
					public ArgWrapper<?> construct(final ListOperands operands)
					{
						return TypeIDs.Double.wrap(new CommandArg<Double>()
						{
							private final CommandArg<Double> arg1 = operands.get(TypeIDs.Double);
							private final CommandArg<Double> arg2 = operands.get(TypeIDs.Double);
							
							@Override
							public Double eval(final ICommandSender sender) throws CommandException
							{
								final double divisor = this.arg2.eval(sender);
								
								if (divisor == 0.0)
									throw new NumberInvalidException("Can't divide by 0");
								
								return this.arg1.eval(sender) / divisor;
							}
						});
					}
				}));
		
		register("-", operator(level(0), resTypeDouble).then(Parsers.dbl).then(Parsers.dbl).construct(
			new OperatorConstructable()
			{
				@Override
				public ArgWrapper<?> construct(final ListOperands operands)
				{
					return TypeIDs.Double.wrap(new CommandArg<Double>()
					{
						private final CommandArg<Double> arg1 = operands.get(TypeIDs.Double);
						private final CommandArg<Double> arg2 = operands.get(TypeIDs.Double);
						
						@Override
						public Double eval(final ICommandSender sender) throws CommandException
						{
							return this.arg1.eval(sender) - this.arg2.eval(sender);
						}
					});
				}
			}));
		
		register("sqrt", operator(level(0), resTypeDouble).then(Parsers.dbl).construct(
			new OperatorConstructable()
			{
				@Override
				public ArgWrapper<?> construct(final ListOperands operands)
				{
					return TypeIDs.Double.wrap(new CommandArg<Double>()
					{
						private final CommandArg<Double> arg = operands.get(TypeIDs.Double);
						
						@Override
						public Double eval(final ICommandSender sender) throws CommandException
						{
							final double arg = this.arg.eval(sender);
							
							if (arg < 0)
								throw new NumberInvalidException("Can't take square-root of negative numbers");
							
							return Math.sqrt(arg);
						}
					});
				}
			}));
		
		register("sq", operator(level(0), resTypeDouble).then(Parsers.dbl).construct(
			new OperatorConstructable()
			{
				@Override
				public ArgWrapper<?> construct(final ListOperands operands)
				{
					return TypeIDs.Double.wrap(new CommandArg<Double>()
					{
						private final CommandArg<Double> arg = operands.get(TypeIDs.Double);
						
						@Override
						public Double eval(final ICommandSender sender) throws CommandException
						{
							final double toSq = this.arg.eval(sender);
							
							return toSq * toSq;
						}
					});
				}
			}));
		
		register("-0", operator(level(0), resTypeDouble).then(Parsers.dbl).construct(
			new OperatorConstructable()
			{
				@Override
				public ArgWrapper<?> construct(final ListOperands operands)
				{
					return TypeIDs.Double.wrap(new CommandArg<Double>()
					{
						private final CommandArg<Double> arg = operands.get(TypeIDs.Double);
						
						@Override
						public Double eval(final ICommandSender sender) throws CommandException
						{
							return -this.arg.eval(sender);
						}
					});
				}
			}));
		
		register("floor", operator(level(0), resTypeInt).then(Parsers.dbl).construct(
			new OperatorConstructable()
			{
				@Override
				public ArgWrapper<?> construct(final ListOperands operands)
				{
					return TypeIDs.Integer.wrap(new CommandArg<Integer>()
					{
						private final CommandArg<Double> arg = operands.get(TypeIDs.Double);
						
						@Override
						public Integer eval(final ICommandSender sender) throws CommandException
						{
							return MathHelper.floor_double(this.arg.eval(sender));
						}
					});
				}
			}));
		
		register("ceil", operator(level(0), resTypeInt).then(Parsers.dbl).construct(
			new OperatorConstructable()
			{
				@Override
				public ArgWrapper<?> construct(final ListOperands operands)
				{
					return TypeIDs.Integer.wrap(new CommandArg<Integer>()
					{
						private final CommandArg<Double> arg = operands.get(TypeIDs.Double);
						
						@Override
						public Integer eval(final ICommandSender sender) throws CommandException
						{
							return MathHelper.ceiling_double_int(this.arg.eval(sender));
						}
					});
				}
			}));
		
		register("sin", operator(level(0), resTypeDouble).then(Parsers.dbl).construct(
			new OperatorConstructable()
			{
				@Override
				public ArgWrapper<?> construct(final ListOperands operands)
				{
					return TypeIDs.Double.wrap(new CommandArg<Double>()
					{
						private final CommandArg<Double> arg = operands.get(TypeIDs.Double);
						
						@Override
						public Double eval(final ICommandSender sender) throws CommandException
						{
							return Double.valueOf(MathHelper.sin((float) Math.toRadians(this.arg.eval(sender))));
						}
					});
				}
			}));
		
		register("cos", operator(level(0), resTypeDouble).then(Parsers.dbl).construct(
			new OperatorConstructable()
			{
				@Override
				public ArgWrapper<?> construct(final ListOperands operands)
				{
					
					return TypeIDs.Double.wrap(new CommandArg<Double>()
					{
						private final CommandArg<Double> arg = operands.get(TypeIDs.Double);
						
						@Override
						public Double eval(final ICommandSender sender) throws CommandException
						{
							return Double.valueOf(MathHelper.cos((float) Math.toRadians(this.arg.eval(sender))));
						}
					});
				}
			}));
		
		register("x", operator(level(2), resTypeDouble).then(Types.ICmdSender).construct(
			new OperatorConstructable()
			{
				@Override
				public ArgWrapper<?> construct(final ListOperands operands)
				{
					return TypeIDs.Double.wrap(new CommandArg<Double>()
					{
						private final CommandArg<ICommandSender> arg = operands.get(TypeIDs.ICmdSender);
						
						@Override
						public Double eval(final ICommandSender sender) throws CommandException
						{
							return new Double(this.arg.eval(sender).getPositionVector().xCoord);
						}
					});
				}
			}));
		
		register("y", operator(level(2), resTypeDouble).then(Types.ICmdSender).construct(
			new OperatorConstructable()
			{
				@Override
				public ArgWrapper<?> construct(final ListOperands operands)
				{
					return TypeIDs.Double.wrap(new CommandArg<Double>()
					{
						private final CommandArg<ICommandSender> arg = operands.get(TypeIDs.ICmdSender);
						
						@Override
						public Double eval(final ICommandSender sender) throws CommandException
						{
							return new Double(this.arg.eval(sender).getPositionVector().yCoord);
						}
					});
				}
			}));
		
		register("z", operator(level(2), resTypeDouble).then(Types.ICmdSender).construct(
			new OperatorConstructable()
			{
				@Override
				public ArgWrapper<?> construct(final ListOperands operands)
				{
					return TypeIDs.Double.wrap(new CommandArg<Double>()
					{
						private final CommandArg<ICommandSender> arg = operands.get(TypeIDs.ICmdSender);
						
						@Override
						public Double eval(final ICommandSender sender) throws CommandException
						{
							return new Double(this.arg.eval(sender).getPositionVector().zCoord);
						}
					});
				}
			}));
		
		register("pos", operator(level(2), resTypeVec3).then(Types.ICmdSender).construct(
			new OperatorConstructable()
			{
				@Override
				public ArgWrapper<?> construct(final ListOperands operands)
				{
					return TypeIDs.Coordinates.wrap(new CommandArg<Vec3>()
					{
						private final CommandArg<ICommandSender> arg = operands.get(TypeIDs.ICmdSender);
						
						@Override
						public Vec3 eval(final ICommandSender sender) throws CommandException
						{
							return this.arg.eval(sender).getPositionVector();
						}
					});
				}
			}));
		
		register("rx", operator(level(2), resTypeDouble).then(Types.entity).construct(
			new OperatorConstructable()
			{
				@Override
				public ArgWrapper<?> construct(final ListOperands operands)
				{
					return TypeIDs.Double.wrap(new CommandArg<Double>()
					{
						private final CommandArg<Entity> arg = operands.get(TypeIDs.Entity);
						
						@Override
						public Double eval(final ICommandSender sender) throws CommandException
						{
							return new Double(this.arg.eval(sender).rotationPitch);
						}
					});
				}
			}));
		
		register("ry", operator(level(2), resTypeDouble).then(Types.entity).construct(
			new OperatorConstructable()
			{
				@Override
				public ArgWrapper<?> construct(final ListOperands operands)
				{
					return TypeIDs.Double.wrap(new CommandArg<Double>()
					{
						private final CommandArg<Entity> arg = operands.get(TypeIDs.Entity);
						
						@Override
						public Double eval(final ICommandSender sender) throws CommandException
						{
							return new Double(this.arg.eval(sender).rotationYaw);
						}
					});
				}
			}));
		
		register("slot", operator(level(2), resTypeInt).then(Types.entity).construct(
			new OperatorConstructable()
			{
				@Override
				public ArgWrapper<?> construct(final ListOperands operands)
				{
					return TypeIDs.Integer.wrap(new CommandArg<Integer>()
					{
						private final CommandArg<Entity> arg = operands.get(TypeIDs.Entity);
						
						@Override
						public Integer eval(final ICommandSender sender) throws CommandException
						{
							final Entity entity = this.arg.eval(sender);
							
							if (!(entity instanceof EntityPlayerMP))
								throw new PlayerNotFoundException();
							
							return ((EntityPlayerMP) entity).inventory.currentItem;
						}
					});
				}
			}));
		
		register("rd", operator(level(2), resTypeEntity).then(Types.entity).construct(
			new OperatorConstructable()
			{
				@Override
				public ArgWrapper<?> construct(final ListOperands operands)
				{
					return TypeIDs.Entity.wrap(new CommandArg<Entity>()
					{
						private final CommandArg<Entity> arg = operands.get(TypeIDs.Entity);
						
						@Override
						public Entity eval(final ICommandSender sender) throws CommandException
						{
							final Entity ret = this.arg.eval(sender).ridingEntity;
							
							if (ret == null)
								throw new CommandException("This entity does not ride any entity");
							
							return ret;
						}
					});
				}
			}));
		
		register("+v", operator(level(0), resTypeVec3)
			.then(TypeCoordinates.nonCentered)
			.then(TypeCoordinates.nonCentered)
			.construct(new OperatorConstructable()
			{
				@Override
				public ArgWrapper<?> construct(final ListOperands operands) throws SyntaxErrorException
				{
					return TypeIDs.Coordinates.wrap(new CommandArg<Vec3>()
					{
						private final CommandArg<Vec3> arg1 = operands.get(TypeIDs.Coordinates);
						private final CommandArg<Vec3> arg2 = operands.get(TypeIDs.Coordinates);
						
						@Override
						public Vec3 eval(final ICommandSender sender) throws CommandException
						{
							return this.arg1.eval(sender).add(this.arg2.eval(sender));
						}
					});
				}
			}));
		
		register("-v", operator(level(0), resTypeVec3)
			.then(TypeCoordinates.nonCentered)
			.then(TypeCoordinates.nonCentered)
			.construct(new OperatorConstructable()
			{
				@Override
				public ArgWrapper<?> construct(final ListOperands operands) throws SyntaxErrorException
				{
					return TypeIDs.Coordinates.wrap(new CommandArg<Vec3>()
					{
						private final CommandArg<Vec3> arg1 = operands.get(TypeIDs.Coordinates);
						private final CommandArg<Vec3> arg2 = operands.get(TypeIDs.Coordinates);
						
						@Override
						public Vec3 eval(final ICommandSender sender) throws CommandException
						{
							return this.arg1.eval(sender).subtract(this.arg2.eval(sender));
						}
					});
				}
			}));
		
		register("*v", operator(level(0), resTypeVec3)
			.then(Parsers.dbl)
			.then(TypeCoordinates.nonCentered)
			.construct(new OperatorConstructable()
			{
				@Override
				public ArgWrapper<?> construct(final ListOperands operands) throws SyntaxErrorException
				{
					return TypeIDs.Coordinates.wrap(new CommandArg<Vec3>()
					{
						private final CommandArg<Double> arg1 = operands.get(TypeIDs.Double);
						private final CommandArg<Vec3> arg2 = operands.get(TypeIDs.Coordinates);
						
						@Override
						public Vec3 eval(final ICommandSender sender) throws CommandException
						{
							final Vec3 base = this.arg2.eval(sender);
							final double fac = this.arg1.eval(sender);
							return new Vec3(base.xCoord * fac, base.yCoord * fac, base.zCoord * fac);
						}
					});
				}
			}));
		
		register("/v", operator(level(0), resTypeVec3)
			.then(Parsers.dbl)
			.then(TypeCoordinates.nonCentered)
			.construct(new OperatorConstructable()
			{
				@Override
				public ArgWrapper<?> construct(final ListOperands operands) throws SyntaxErrorException
				{
					return TypeIDs.Coordinates.wrap(new CommandArg<Vec3>()
					{
						private final CommandArg<Double> arg1 = operands.get(TypeIDs.Double);
						private final CommandArg<Vec3> arg2 = operands.get(TypeIDs.Coordinates);
						
						@Override
						public Vec3 eval(final ICommandSender sender) throws CommandException
						{
							final Vec3 base = this.arg2.eval(sender);
							final double divisor = this.arg1.eval(sender);
							
							if (divisor == 0.0)
								throw new NumberInvalidException("Can't divide by 0");
							
							return new Vec3(base.xCoord / divisor, base.yCoord / divisor, base.zCoord / divisor);
						}
					});
				}
			}));
		
		register("v0", operator(level(0), resTypeVec3)
			.then(TypeCoordinates.nonCentered)
			.construct(new OperatorConstructable()
			{
				@Override
				public ArgWrapper<?> construct(final ListOperands operands) throws SyntaxErrorException
				{
					return TypeIDs.Coordinates.wrap(new CommandArg<Vec3>()
					{
						private final CommandArg<Vec3> arg = operands.get(TypeIDs.Coordinates);
						
						@Override
						public Vec3 eval(final ICommandSender sender) throws CommandException
						{
							final Vec3 vec = this.arg.eval(sender);
							
							final double len = vec.lengthVector();
							
							if (len == 0.0)
								throw new CommandException("Can't normalize vector with zero length");
							
							return new Vec3(vec.xCoord / len, vec.yCoord / len, vec.zCoord / len);
						}
					});
				}
			}));
		
		register("cv", operator(level(0), resTypeVec3)
			.then(TypeCoordinates.nonCentered)
			.construct(new OperatorConstructable()
			{
				@Override
				public ArgWrapper<?> construct(final ListOperands operands) throws SyntaxErrorException
				{
					return TypeIDs.Coordinates.wrap(new CommandArg<Vec3>()
					{
						private final CommandArg<Vec3> arg = operands.get(TypeIDs.Coordinates);
						
						@Override
						public Vec3 eval(final ICommandSender sender) throws CommandException
						{
							final Vec3 vec = this.arg.eval(sender);
							
							return new Vec3(Math.floor(vec.xCoord) + 0.5, Math.floor(vec.yCoord), Math.floor(vec.zCoord) + 0.5);
						}
					});
				}
			}));
		
		register("abs", operator(level(0), resTypeDouble)
			.then(TypeCoordinates.nonCentered)
			.construct(new OperatorConstructable()
			{
				@Override
				public ArgWrapper<?> construct(final ListOperands operands) throws SyntaxErrorException
				{
					return TypeIDs.Double.wrap(new CommandArg<Double>()
					{
						private final CommandArg<Vec3> arg = operands.get(TypeIDs.Coordinates);
						
						@Override
						public Double eval(final ICommandSender sender) throws CommandException
						{
							return this.arg.eval(sender).lengthVector();
						}
					});
				}
			}));
		
		register(".", operator(level(0), resTypeDouble)
			.then(TypeCoordinates.nonCentered)
			.then(TypeCoordinates.nonCentered)
			.construct(new OperatorConstructable()
			{
				@Override
				public ArgWrapper<?> construct(final ListOperands operands) throws SyntaxErrorException
				{
					return TypeIDs.Double.wrap(new CommandArg<Double>()
					{
						private final CommandArg<Vec3> arg1 = operands.get(TypeIDs.Coordinates);
						private final CommandArg<Vec3> arg2 = operands.get(TypeIDs.Coordinates);
						
						@Override
						public Double eval(final ICommandSender sender) throws CommandException
						{
							return this.arg1.eval(sender).dotProduct(this.arg2.eval(sender));
						}
					});
				}
			}));
		
		register("^v", operator(level(0), resTypeVec3)
			.then(TypeCoordinates.nonCentered)
			.then(TypeCoordinates.nonCentered)
			.construct(new OperatorConstructable()
			{
				@Override
				public ArgWrapper<?> construct(final ListOperands operands) throws SyntaxErrorException
				{
					return TypeIDs.Coordinates.wrap(new CommandArg<Vec3>()
					{
						private final CommandArg<Vec3> arg1 = operands.get(TypeIDs.Coordinates);
						private final CommandArg<Vec3> arg2 = operands.get(TypeIDs.Coordinates);
						
						@Override
						public Vec3 eval(final ICommandSender sender) throws CommandException
						{
							return this.arg1.eval(sender).crossProduct(this.arg2.eval(sender));
						}
					});
				}
			}));
		
		register("rxv", operator(level(0), resTypeDouble)
			.then(TypeCoordinates.nonCentered)
			.construct(new OperatorConstructable()
			{
				@Override
				public ArgWrapper<?> construct(final ListOperands operands) throws SyntaxErrorException
				{
					return TypeIDs.Double.wrap(new CommandArg<Double>()
					{
						private final CommandArg<Vec3> arg = operands.get(TypeIDs.Coordinates);
						
						@Override
						public Double eval(final ICommandSender sender) throws CommandException
						{
							final Vec3 vec = this.arg.eval(sender);
							final double ret = Math.toDegrees(-Math.atan(vec.yCoord / Math.sqrt(vec.xCoord * vec.xCoord + vec.zCoord * vec.zCoord)));
							
							if (Double.isNaN(ret))
								throw new NumberInvalidException("Invalid argument for 'rxv'");
							
							return ret;
						}
					});
				}
			}));
		
		register("ryv", operator(level(0), resTypeDouble)
			.then(TypeCoordinates.nonCentered)
			.construct(new OperatorConstructable()
			{
				@Override
				public ArgWrapper<?> construct(final ListOperands operands) throws SyntaxErrorException
				{
					return TypeIDs.Double.wrap(new CommandArg<Double>()
					{
						private final CommandArg<Vec3> arg = operands.get(TypeIDs.Coordinates);
						
						@Override
						public Double eval(final ICommandSender sender) throws CommandException
						{
							final Vec3 vec = this.arg.eval(sender);
							final double ret = Math.toDegrees(-Math.atan2(vec.xCoord, vec.zCoord));
							
							if (Double.isNaN(ret))
								throw new NumberInvalidException("Invalid argument for 'ryv'");
							
							return ret;
						}
					});
				}
			}));
		
		register("fv", operator(level(2), resTypeVec3)
			.then(Types.entity)
			.construct(new OperatorConstructable()
			{
				@Override
				public ArgWrapper<?> construct(final ListOperands operands) throws SyntaxErrorException
				{
					return TypeIDs.Coordinates.wrap(new CommandArg<Vec3>()
					{
						private final CommandArg<Entity> arg = operands.get(TypeIDs.Entity);
						
						@Override
						public Vec3 eval(final ICommandSender sender) throws CommandException
						{
							final Entity entity = this.arg.eval(sender);
							
							final Vec3 ret = entity.getLookVec();
							
							if (ret == null)
								throw new CommandException("Entity is has no facing direction");
							
							return ret;
						}
					});
				}
			}));
		
		register("xv", operator(level(0), resTypeVec3)
			.then(TypeCoordinates.nonCentered)
			.construct(new OperatorConstructable()
			{
				@Override
				public ArgWrapper<?> construct(final ListOperands operands) throws SyntaxErrorException
				{
					return TypeIDs.Double.wrap(new CommandArg<Double>()
					{
						private final CommandArg<Vec3> arg = operands.get(TypeIDs.Coordinates);
						
						@Override
						public Double eval(final ICommandSender sender) throws CommandException
						{
							return this.arg.eval(sender).xCoord;
						}
					});
				}
			}));
		
		register("yv", operator(level(0), resTypeVec3)
			.then(TypeCoordinates.nonCentered)
			.construct(new OperatorConstructable()
			{
				@Override
				public ArgWrapper<?> construct(final ListOperands operands) throws SyntaxErrorException
				{
					return TypeIDs.Double.wrap(new CommandArg<Double>()
					{
						private final CommandArg<Vec3> arg = operands.get(TypeIDs.Coordinates);
						
						@Override
						public Double eval(final ICommandSender sender) throws CommandException
						{
							return this.arg.eval(sender).yCoord;
						}
					});
				}
			}));
		
		register("zv", operator(level(0), resTypeVec3)
			.then(TypeCoordinates.nonCentered)
			.construct(new OperatorConstructable()
			{
				@Override
				public ArgWrapper<?> construct(final ListOperands operands) throws SyntaxErrorException
				{
					return TypeIDs.Double.wrap(new CommandArg<Double>()
					{
						private final CommandArg<Vec3> arg = operands.get(TypeIDs.Coordinates);
						
						@Override
						public Double eval(final ICommandSender sender) throws CommandException
						{
							return this.arg.eval(sender).zCoord;
						}
					});
				}
			}));
		
		register("ex", constant(new Vec3(1, 0, 0), TypeIDs.Coordinates));
		register("ey", constant(new Vec3(0, 1, 0), TypeIDs.Coordinates));
		register("ez", constant(new Vec3(0, 0, 1), TypeIDs.Coordinates));
		
		register("items", operator(level(0), resTypeNBT)
			.then(TypeNBTBase.parserDefault)
			.then(Parsers.integerList)
			.construct(OperatorItems.constructable));
		
		register("isAir", operator(level(0), resTypeBoolean)
			.then(Types.blockID)
			.construct(new OperatorConstructable()
			{
				@Override
				public ArgWrapper<Boolean> construct(final ListOperands operands) throws SyntaxErrorException
				{
					return TypeIDs.Boolean.wrap(new CommandArg<Boolean>()
					{
						private final CommandArg<Block> blockID = operands.get(TypeIDs.BlockID);
						
						@Override
						public Boolean eval(final ICommandSender sender) throws CommandException
						{
							return this.blockID.eval(sender) == Blocks.air;
						}
					});
				}
			}));
		
		register("meta", operator(level(0), resTypeInt)
			.then(Types.blockState)
			.construct(new OperatorConstructable()
			{
				@Override
				public ArgWrapper<Integer> construct(final ListOperands operands) throws SyntaxErrorException
				{
					return TypeIDs.Integer.wrap(new CommandArg<Integer>()
					{
						private final CommandArg<IBlockState> state = operands.get(TypeIDs.BlockState);
						
						@Override
						public Integer eval(final ICommandSender sender) throws CommandException
						{
							final IBlockState state = this.state.eval(sender);
							return state.getBlock().getMetaFromState(state);
						}
					});
				}
			}));
		
		register("!", alias("not"), operator(level(0), resTypeBoolean)
			.then(Parsers.bool)
			.construct(new OperatorConstructable()
			{
				@Override
				public ArgWrapper<Boolean> construct(final ListOperands operands) throws SyntaxErrorException
				{
					return TypeIDs.Boolean.wrap(new CommandArg<Boolean>()
					{
						private final CommandArg<Boolean> arg = operands.get(TypeIDs.Boolean);
						
						@Override
						public Boolean eval(final ICommandSender sender) throws CommandException
						{
							return !this.arg.eval(sender);
						}
					});
				}
			}));
		
		register("&&", operator(level(0), resTypeBoolean)
			.then(Parsers.bool)
			.then(Parsers.bool)
			.construct(new OperatorConstructable()
			{
				@Override
				public ArgWrapper<Boolean> construct(final ListOperands operands) throws SyntaxErrorException
				{
					return TypeIDs.Boolean.wrap(new CommandArg<Boolean>()
					{
						private final CommandArg<Boolean> arg1 = operands.get(TypeIDs.Boolean);
						private final CommandArg<Boolean> arg2 = operands.get(TypeIDs.Boolean);
						
						@Override
						public Boolean eval(final ICommandSender sender) throws CommandException
						{
							return this.arg1.eval(sender).booleanValue() && this.arg2.eval(sender).booleanValue();
						}
					});
				}
			}));
		
		register("&", alias("and"), operator(level(0), resTypeBoolean)
			.then(Parsers.bool)
			.then(Parsers.bool)
			.construct(new OperatorConstructable()
			{
				@Override
				public ArgWrapper<Boolean> construct(final ListOperands operands) throws SyntaxErrorException
				{
					return TypeIDs.Boolean.wrap(new CommandArg<Boolean>()
					{
						private final CommandArg<Boolean> arg1 = operands.get(TypeIDs.Boolean);
						private final CommandArg<Boolean> arg2 = operands.get(TypeIDs.Boolean);
						
						@Override
						public Boolean eval(final ICommandSender sender) throws CommandException
						{
							return this.arg1.eval(sender).booleanValue() & this.arg2.eval(sender).booleanValue();
						}
					});
				}
			}));
		
		register("||", operator(level(0), resTypeBoolean)
			.then(Parsers.bool)
			.then(Parsers.bool)
			.construct(new OperatorConstructable()
			{
				@Override
				public ArgWrapper<Boolean> construct(final ListOperands operands) throws SyntaxErrorException
				{
					return TypeIDs.Boolean.wrap(new CommandArg<Boolean>()
					{
						private final CommandArg<Boolean> arg1 = operands.get(TypeIDs.Boolean);
						private final CommandArg<Boolean> arg2 = operands.get(TypeIDs.Boolean);
						
						@Override
						public Boolean eval(final ICommandSender sender) throws CommandException
						{
							return this.arg1.eval(sender).booleanValue() || this.arg2.eval(sender).booleanValue();
						}
					});
				}
			}));
		
		register("|", alias("or"), operator(level(0), resTypeBoolean)
			.then(Parsers.bool)
			.then(Parsers.bool)
			.construct(new OperatorConstructable()
			{
				@Override
				public ArgWrapper<Boolean> construct(final ListOperands operands) throws SyntaxErrorException
				{
					return TypeIDs.Boolean.wrap(new CommandArg<Boolean>()
					{
						private final CommandArg<Boolean> arg1 = operands.get(TypeIDs.Boolean);
						private final CommandArg<Boolean> arg2 = operands.get(TypeIDs.Boolean);
						
						@Override
						public Boolean eval(final ICommandSender sender) throws CommandException
						{
							return this.arg1.eval(sender).booleanValue() | this.arg2.eval(sender).booleanValue();
						}
					});
				}
			}));
		
		register("i", primitiveOperator(level(0), Parsers.integer, resTypeInt));
		
		register("d", primitiveOperator(level(0), Parsers.dbl, resTypeDouble));
		
		register("s", primitiveOperator(level(0), ParserName.parser, resTypeString));
		
		register("e", primitiveOperator(level(0), Types.entity, resTypeEntity));
		
		register("v", primitiveOperator(level(0), TypeCoordinates.nonCentered, resTypeVec3));
		
		register("exp", operator(level(0), resTypeDouble)
			.then(Parsers.dbl)
			.construct(new OperatorConstructable()
			{
				@Override
				public ArgWrapper<Double> construct(final ListOperands operands) throws SyntaxErrorException
				{
					return TypeIDs.Double.wrap(new CommandArg<Double>()
					{
						private final CommandArg<Double> arg = operands.get(TypeIDs.Double);
						
						@Override
						public Double eval(final ICommandSender sender) throws CommandException
						{
							return Math.exp(this.arg.eval(sender));
						}
					});
				}
			}));
		
		register("ln", operator(level(0), resTypeDouble)
			.then(Parsers.dbl)
			.construct(new OperatorConstructable()
			{
				@Override
				public ArgWrapper<Double> construct(final ListOperands operands) throws SyntaxErrorException
				{
					return TypeIDs.Double.wrap(new CommandArg<Double>()
					{
						private final CommandArg<Double> arg = operands.get(TypeIDs.Double);
						
						@Override
						public Double eval(final ICommandSender sender) throws CommandException
						{
							final double arg = this.arg.eval(sender);
							
							if (arg <= 0)
								throw new NumberInvalidException("Can't take logarithm of non-positive numbers");
							
							return Math.log(arg);
						}
					});
				}
			}));
		
		register("^", alias("pow"), operator(level(0), resTypeDouble)
			.then(Parsers.dbl)
			.then(Parsers.dbl)
			.construct(new OperatorConstructable()
			{
				@Override
				public ArgWrapper<Double> construct(final ListOperands operands) throws SyntaxErrorException
				{
					return TypeIDs.Double.wrap(new CommandArg<Double>()
					{
						private final CommandArg<Double> arg1 = operands.get(TypeIDs.Double);
						private final CommandArg<Double> arg2 = operands.get(TypeIDs.Double);
						
						@Override
						public Double eval(final ICommandSender sender) throws CommandException
						{
							final double arg1 = this.arg1.eval(sender);
							final double arg2 = this.arg2.eval(sender);
							
							if (arg1 < 0 && !DoubleMath.isMathematicalInteger(arg2))
								throw new NumberInvalidException("Can't take non-integer power of negative number");
							
							return Math.pow(arg1, arg2);
						}
					});
				}
			}));
		
		register("%", operator(level(0), resTypeDouble)
			.then(Parsers.integer)
			.then(Parsers.integer)
			.construct(new OperatorConstructable()
			{
				@Override
				public ArgWrapper<Integer> construct(final ListOperands operands) throws SyntaxErrorException
				{
					return TypeIDs.Integer.wrap(new CommandArg<Integer>()
					{
						private final CommandArg<Integer> arg1 = operands.get(TypeIDs.Integer);
						private final CommandArg<Integer> arg2 = operands.get(TypeIDs.Integer);
						
						@Override
						public Integer eval(final ICommandSender sender) throws CommandException
						{
							return this.arg1.eval(sender) % this.arg2.eval(sender);
						}
					});
				}
			}));
		
		register("pi", constant(Math.PI, TypeIDs.Double));
		register("e_", constant(Math.E, TypeIDs.Double));
		
		register(">", operator(level(0), resTypeBoolean)
			.then(Parsers.dbl)
			.then(Parsers.dbl)
			.construct(new OperatorConstructable()
			{
				@Override
				public ArgWrapper<Boolean> construct(final ListOperands operands) throws SyntaxErrorException
				{
					return TypeIDs.Boolean.wrap(new CommandArg<Boolean>()
					{
						private final CommandArg<Double> arg1 = operands.get(TypeIDs.Double);
						private final CommandArg<Double> arg2 = operands.get(TypeIDs.Double);
						
						@Override
						public Boolean eval(final ICommandSender sender) throws CommandException
						{
							return this.arg1.eval(sender) > this.arg2.eval(sender);
						}
					});
				}
			}));
		
		register(">=", operator(level(0), resTypeBoolean)
			.then(Parsers.dbl)
			.then(Parsers.dbl)
			.construct(new OperatorConstructable()
			{
				@Override
				public ArgWrapper<Boolean> construct(final ListOperands operands) throws SyntaxErrorException
				{
					return TypeIDs.Boolean.wrap(new CommandArg<Boolean>()
					{
						private final CommandArg<Double> arg1 = operands.get(TypeIDs.Double);
						private final CommandArg<Double> arg2 = operands.get(TypeIDs.Double);
						
						@Override
						public Boolean eval(final ICommandSender sender) throws CommandException
						{
							return this.arg1.eval(sender) >= this.arg2.eval(sender);
						}
					});
				}
			}));
		
		register("<", operator(level(0), resTypeBoolean)
			.then(Parsers.dbl)
			.then(Parsers.dbl)
			.construct(new OperatorConstructable()
			{
				@Override
				public ArgWrapper<Boolean> construct(final ListOperands operands) throws SyntaxErrorException
				{
					return TypeIDs.Boolean.wrap(new CommandArg<Boolean>()
					{
						private final CommandArg<Double> arg1 = operands.get(TypeIDs.Double);
						private final CommandArg<Double> arg2 = operands.get(TypeIDs.Double);
						
						@Override
						public Boolean eval(final ICommandSender sender) throws CommandException
						{
							return this.arg1.eval(sender) < this.arg2.eval(sender);
						}
					});
				}
			}));
		
		register("<=", operator(level(0), resTypeBoolean)
			.then(Parsers.dbl)
			.then(Parsers.dbl)
			.construct(new OperatorConstructable()
			{
				@Override
				public ArgWrapper<Boolean> construct(final ListOperands operands) throws SyntaxErrorException
				{
					return TypeIDs.Boolean.wrap(new CommandArg<Boolean>()
					{
						private final CommandArg<Double> arg1 = operands.get(TypeIDs.Double);
						private final CommandArg<Double> arg2 = operands.get(TypeIDs.Double);
						
						@Override
						public Boolean eval(final ICommandSender sender) throws CommandException
						{
							return this.arg1.eval(sender) <= this.arg2.eval(sender);
						}
					});
				}
			}));
		
		register("==", operator(level(0), resTypeBoolean)
			.then(Parsers.dbl)
			.then(Parsers.dbl)
			.construct(new OperatorConstructable()
			{
				@Override
				public ArgWrapper<Boolean> construct(final ListOperands operands) throws SyntaxErrorException
				{
					return TypeIDs.Boolean.wrap(new CommandArg<Boolean>()
					{
						private final CommandArg<Double> arg1 = operands.get(TypeIDs.Double);
						private final CommandArg<Double> arg2 = operands.get(TypeIDs.Double);
						
						@Override
						public Boolean eval(final ICommandSender sender) throws CommandException
						{
							return this.arg1.eval(sender).doubleValue() == this.arg2.eval(sender).doubleValue();
						}
					});
				}
			}));
		
		register("!=", operator(level(0), resTypeBoolean)
			.then(Parsers.dbl)
			.then(Parsers.dbl)
			.construct(new OperatorConstructable()
			{
				@Override
				public ArgWrapper<Boolean> construct(final ListOperands operands) throws SyntaxErrorException
				{
					return TypeIDs.Boolean.wrap(new CommandArg<Boolean>()
					{
						private final CommandArg<Double> arg1 = operands.get(TypeIDs.Double);
						private final CommandArg<Double> arg2 = operands.get(TypeIDs.Double);
						
						@Override
						public Boolean eval(final ICommandSender sender) throws CommandException
						{
							return this.arg1.eval(sender).doubleValue() != this.arg2.eval(sender).doubleValue();
						}
					});
				}
			}));
		
		register("rnd", constant(TypeIDs.Double.wrap(new CommandArg<Double>()
		{
			@Override
			public Double eval(final ICommandSender sender) throws CommandException
			{
				return Math.random();
			}
		}), resTypeDouble));
		
		register("rndI", operator(level(0), resTypeInt)
			.then(Parsers.integer)
			.then(Parsers.integer)
			.construct(new OperatorConstructable()
			{
				@Override
				public ArgWrapper<?> construct(final ListOperands operands) throws SyntaxErrorException
				{
					return TypeIDs.Integer.wrap(new CommandArg<Integer>()
					{
						private final CommandArg<Integer> arg1 = operands.get(TypeIDs.Integer);
						private final CommandArg<Integer> arg2 = operands.get(TypeIDs.Integer);
						
						@Override
						public Integer eval(final ICommandSender sender) throws CommandException
						{
							final int arg1 = this.arg1.eval(sender);
							final int arg2 = this.arg2.eval(sender);
							return (int) Math.floor(arg1 + (arg2 - arg1 + 1) * Math.random());
						}
					});
				}
			}));
	}
}
