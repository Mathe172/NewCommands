package net.minecraft.command.collections;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.command.EntityNotFoundException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.type.custom.ParserBlockID;
import net.minecraft.command.type.custom.ParserDouble;
import net.minecraft.command.type.custom.ParserEntity;
import net.minecraft.command.type.custom.ParserICmdSender;
import net.minecraft.command.type.custom.ParserInt;
import net.minecraft.command.type.custom.ParserItemID;
import net.minecraft.command.type.custom.TypeBoolean;
import net.minecraft.command.type.custom.TypeScoreObjective;
import net.minecraft.command.type.custom.coordinate.Coordinate;
import net.minecraft.command.type.custom.coordinate.Coordinate.CoordValue;
import net.minecraft.command.type.custom.coordinate.TypeCoordinate;
import net.minecraft.command.type.custom.coordinate.TypeCoordinates;
import net.minecraft.command.type.custom.coordinate.TypeCoordinates.Shift;
import net.minecraft.command.type.management.CConverter;
import net.minecraft.command.type.management.Converter;
import net.minecraft.command.type.management.SConverter;
import net.minecraft.command.type.management.TypeID;
import net.minecraft.command.type.management.TypeID.ExceptionProvider;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Vec3;

public final class TypeIDs
{
	public static final TypeID<Byte> Byte = new TypeID<>("Byte");
	public static final TypeID<Short> Short = new TypeID<>("Short");
	public static final TypeID<Long> Long = new TypeID<>("Long");
	public static final TypeID<Float> Float = new TypeID<>("Float");
	public static final TypeID<Double> Double = new TypeID<>("Double");
	
	public static final TypeID<Integer> Integer = new TypeID<>("Integer");
	public static final TypeID<List<Integer>> IntList = TypeIDs.Integer.addList();
	
	public static final TypeID<Boolean> Boolean = new TypeID<>("Boolean");
	
	public static final TypeID<Vec3> Coordinates = new TypeID<>("Coordinates");
	public static final TypeID<TypeCoordinate.SingleShift> SingleShift = new TypeID<>("RelativeCoordinate");
	public static final TypeID<TypeCoordinates.Shift> Shift = new TypeID<>("RelativeCoordinates");
	public static final TypeID<BlockPos> BlockPos = new TypeID<>("BlockPos");
	
	public static final TypeID<String> String = new TypeID<>("String");
	
	public static final TypeID<IChatComponent> IChatComponent = new TypeID<IChatComponent>("IChatComponent");
	
	public static final TypeID<NBTBase> NBTBase = new TypeID<>("NBTBase");
	public static final TypeID<NBTTagCompound> NBTCompound = new TypeID<>("NBTCompound");
	
	public static final TypeID<ICommandSender> ICmdSender = new TypeID<>("ICommandSender");
	public static final TypeID<List<ICommandSender>> ICmdSenderList = TypeIDs.ICmdSender.addList();
	
	public static final TypeID<Entity> Entity = new TypeID<>("Entity");
	public static final TypeID<String> UUID = new TypeID<>("UUID");
	
	public static final TypeID<IBlockState> BlockState = new TypeID<>("BlockState");
	public static final TypeID<Block> BlockID = new TypeID<>("BlockID");
	
	public static final TypeID<Item> ItemID = new TypeID<>("ItemID");
	
	public static final TypeID<List<Entity>> EntityList = TypeIDs.Entity.addList(new ExceptionProvider()
	{
		@Override
		public CommandException create()
		{
			return new EntityNotFoundException();
		}
	});
	
	public static final TypeID<List<String>> UUIDList = TypeIDs.UUID.addList();
	public static final TypeID<List<String>> StringList = TypeIDs.String.addList();
	
	public static final TypeID<ScoreObjective> ScoreObjective = new TypeID<>("ScoreObjective");
	
	private TypeIDs()
	{
	}
	
	public static final void init()
	{
		Coordinate.typeCoord.init();
		Byte.init();
		Coordinates.init();
		BlockPos.init();
		SingleShift.init();
		Shift.init();
		Double.init();
		Boolean.init();
		Entity.init();
		EntityList.init();
		Float.init();
		IChatComponent.init();
		ICmdSender.init();
		ICmdSenderList.init();
		Integer.init();
		IntList.init();
		Long.init();
		NBTBase.init();
		NBTCompound.init();
		ScoreObjective.init();
		Short.init();
		String.init();
		StringList.init();
		UUID.init();
		UUIDList.init();
		BlockState.init();
		BlockID.init();
		ItemID.init();
		
		TypeIDs.String.addDefaultConverter(TypeIDs.IChatComponent, new CConverter<String, IChatComponent>()
		{
			@Override
			public IChatComponent convert(final String toConvert)
			{
				return new ChatComponentText(toConvert);
			}
		});
		
		TypeIDs.Integer.addPrimitiveConverter(TypeIDs.String, new CConverter<Integer, String>()
		{
			@Override
			public String convert(final Integer toConvert)
			{
				return toConvert.toString();
			}
		});
		
		TypeIDs.Double.addPrimitiveConverter(TypeIDs.String, new CConverter<Double, String>()
		{
			@Override
			public String convert(final Double toConvert)
			{
				return toConvert.toString();
			}
		});
		
		TypeIDs.String.addPrimitiveConverter(TypeIDs.Integer, ParserInt.stringToInt);
		
		TypeIDs.String.addPrimitiveConverter(TypeIDs.Double, ParserDouble.stringToDouble);
		
		TypeIDs.String.addPrimitiveConverter(TypeIDs.Boolean, TypeBoolean.stringToBoolean);
		
		TypeIDs.String.addPrimitiveConverter(TypeIDs.Short, new CConverter<String, Short>()
		{
			@Override
			public Short convert(final String toConvert) throws CommandException
			{
				try
				{
					return java.lang.Short.parseShort(toConvert);
				} catch (final NumberFormatException ex)
				{
					throw new NumberInvalidException(ex.getMessage());
				}
			}
		});
		
		TypeIDs.String.addPrimitiveConverter(TypeIDs.Long, new CConverter<String, Long>()
		{
			@Override
			public Long convert(final String toConvert) throws CommandException
			{
				try
				{
					return java.lang.Long.parseLong(toConvert);
				} catch (final NumberFormatException ex)
				{
					throw new NumberInvalidException(ex.getMessage());
				}
			}
		});
		
		TypeIDs.String.addPrimitiveConverter(TypeIDs.Byte, new CConverter<String, Byte>()
		{
			@Override
			public Byte convert(final String toConvert) throws CommandException
			{
				try
				{
					return java.lang.Byte.parseByte(toConvert);
				} catch (final NumberFormatException ex)
				{
					throw new NumberInvalidException(ex.getMessage());
				}
			}
		});
		
		TypeIDs.String.addPrimitiveConverter(TypeIDs.Float, new CConverter<String, Float>()
		{
			@Override
			public Float convert(final String toConvert) throws CommandException
			{
				try
				{
					return java.lang.Float.parseFloat(toConvert);
				} catch (final NumberFormatException ex)
				{
					throw new NumberInvalidException(ex.getMessage());
				}
			}
		});
		
		TypeIDs.Integer.addPrimitiveConverter(TypeIDs.Double, new CConverter<Integer, Double>()
		{
			@Override
			public Double convert(final Integer toConvert)
			{
				return new Double(toConvert);
			}
			
		});
		
		TypeIDs.Integer.addDefaultConverter(TypeIDs.Boolean, new CConverter<Integer, Boolean>()
		{
			@Override
			public Boolean convert(final Integer toConvert) throws CommandException
			{
				return toConvert.intValue() != 0;
			}
		});
		
		TypeIDs.Double.addPrimitiveConverter(TypeIDs.Integer, new CConverter<Double, Integer>()
		{
			@Override
			public Integer convert(final Double toConvert)
			{
				
				return new Integer(toConvert.intValue());
			}
			
		});
		
		TypeIDs.Integer.addDefaultConverter(TypeIDs.NBTBase, new CConverter<Integer, NBTBase>()
		{
			@Override
			public NBTBase convert(final Integer toConvert)
			{
				return new NBTTagInt(toConvert);
			}
		});
		
		TypeIDs.Double.addDefaultConverter(TypeIDs.NBTBase, new CConverter<Double, NBTBase>()
		{
			@Override
			public NBTBase convert(final Double toConvert)
			{
				return new NBTTagDouble(toConvert);
			}
		});
		
		TypeIDs.String.addDefaultConverter(TypeIDs.NBTBase, new CConverter<String, NBTBase>()
		{
			@Override
			public NBTBase convert(final String toConvert)
			{
				return new NBTTagString(toConvert);
			}
		});
		
		TypeIDs.NBTBase.addPrimitiveConverter(TypeIDs.Integer, new CConverter<NBTBase, Integer>()
		{
			@Override
			public Integer convert(final NBTBase toConvert) throws CommandException
			{
				if (!(toConvert instanceof NBTBase.NBTPrimitive))
					throw new CommandException("Cannot convert from this NBT to int");
				
				return ((NBTBase.NBTPrimitive) toConvert).getInt();
			}
		});
		
		TypeIDs.NBTBase.addPrimitiveConverter(TypeIDs.Double, new CConverter<NBTBase, Double>()
		{
			@Override
			public Double convert(final NBTBase toConvert) throws CommandException
			{
				if (!(toConvert instanceof NBTBase.NBTPrimitive))
					throw new CommandException("Cannot convert from this NBT to double");
				
				return ((NBTBase.NBTPrimitive) toConvert).getDouble();
			}
		});
		
		TypeIDs.NBTBase.addPrimitiveConverter(TypeIDs.String, new CConverter<NBTBase, String>()
		{
			@Override
			public String convert(final NBTBase toConvert)
			{
				switch (toConvert.getId())
				{
				case 8:
					return ((NBTTagString) toConvert).getString();
				default:
					return toConvert.toString();
				}
			}
		});
		
		TypeIDs.ICmdSender.addPrimitiveConverter(TypeIDs.String, new CConverter<ICommandSender, String>()
		{
			@Override
			public String convert(final ICommandSender toConvert)
			{
				return toConvert.getName();
			}
		});
		
		TypeIDs.ICmdSender.addPrimitiveConverter(TypeIDs.UUID, new CConverter<ICommandSender, String>()
		{
			@Override
			public String convert(final ICommandSender toConvert) throws CommandException
			{
				if (toConvert.getCommandSenderEntity() == null)
					throw new CommandException(toConvert.getName() + " is not an entity");
				
				return ParsingUtilities.getEntityIdentifier(toConvert.getCommandSenderEntity());
			}
		});
		
		TypeIDs.UUID.addPrimitiveConverter(TypeIDs.Entity, ParserEntity.UUIDToEntity);
		
		TypeIDs.Entity.addPrimitiveConverter(TypeIDs.UUID, new CConverter<Entity, String>()
		{
			@Override
			public String convert(final Entity toConvert)
			{
				return ParsingUtilities.getEntityIdentifier(toConvert);
			}
		});
		
		TypeIDs.Entity.addDefaultConverter(TypeIDs.ICmdSender, ParserICmdSender.EntityToICmdSender);
		
		TypeIDs.ICmdSender.addPrimitiveConverter(TypeIDs.Entity, new CConverter<ICommandSender, Entity>()
		{
			@Override
			public Entity convert(final ICommandSender toConvert) throws CommandException
			{
				final Entity ret = toConvert.getCommandSenderEntity();
				
				if (ret != null)
					return ret;
				
				throw new EntityNotFoundException("'" + toConvert.getName() + "' is not an entity");
			}
		});
		
		TypeIDs.ICmdSender.addPrimitiveConverter(TypeIDs.IChatComponent, new CConverter<ICommandSender, IChatComponent>()
		{
			@Override
			public IChatComponent convert(final ICommandSender toConvert) throws CommandException
			{
				return toConvert.getDisplayName();
			}
		});
		
		TypeIDs.Entity.addPrimitiveConverter(TypeIDs.String, new CConverter<Entity, String>()
		{
			@Override
			public String convert(final Entity toConvert)
			{
				return toConvert.getName();
			}
		});
		
		TypeIDs.Entity.addPrimitiveConverter(TypeIDs.IChatComponent, new CConverter<Entity, IChatComponent>()
		{
			@Override
			public IChatComponent convert(final Entity toConvert)
			{
				return toConvert.getDisplayName();
			}
		});
		
		TypeIDs.EntityList.addPrimitiveConverter(TypeIDs.IChatComponent, new CConverter<List<Entity>, IChatComponent>()
		{
			@Override
			public IChatComponent convert(final List<Entity> toConvert)
			{
				final List<IChatComponent> names = new ArrayList<>(toConvert.size());
				
				for (final Entity entity : toConvert)
					names.add(entity.getDisplayName());
				
				return ParsingUtilities.join(names);
			}
		});
		
		new Converter.Chained<>(TypeIDs.EntityList, TypeIDs.Entity).chain(TypeIDs.UUID).register();
		new Converter.Chained<>(TypeIDs.EntityList, TypeIDs.Entity).chain(TypeIDs.String).register();
		
		TypeIDs.NBTCompound.addDefaultConverter(TypeIDs.NBTBase, CConverter.<NBTTagCompound, NBTBase> primitiveConverter());
		
		TypeIDs.NBTBase.addPrimitiveConverter(TypeIDs.NBTCompound, new CConverter<NBTBase, NBTTagCompound>()
		{
			@Override
			public NBTTagCompound convert(final NBTBase toConvert) throws CommandException
			{
				if (toConvert instanceof NBTTagCompound)
					return (NBTTagCompound) toConvert;
				
				throw new CommandException("Can't convert this NBTBase to NBTTagCompound");
			}
		});
		
		TypeIDs.ScoreObjective.addPrimitiveConverter(TypeIDs.String, new CConverter<ScoreObjective, String>()
		{
			@Override
			public String convert(final ScoreObjective toConvert)
			{
				return toConvert.getDisplayName();
			}
		});
		
		TypeIDs.String.addPrimitiveConverter(TypeIDs.ScoreObjective, TypeScoreObjective.StringToObjective);
		
		TypeIDs.Integer.addConverter(Coordinate.typeCoord, new SConverter<CommandArg<Integer>, CoordValue>()
		{
			@Override
			public CoordValue convert(final CommandArg<Integer> toConvert)
			{
				return new CoordValue.Dynamic(new CommandArg<Double>()
				{
					@Override
					public Double eval(final ICommandSender sender) throws CommandException
					{
						return new Double(toConvert.eval(sender));
					}
				}, false);
			}
		});
		
		TypeIDs.Double.addConverter(Coordinate.typeCoord, new SConverter<CommandArg<Double>, CoordValue>()
		{
			@Override
			public CoordValue convert(final CommandArg<Double> toConvert)
			{
				return new CoordValue.Dynamic(toConvert, true);
			}
		});
		
		TypeIDs.String.addPrimitiveConverter(TypeIDs.BlockID, ParserBlockID.stringToBlock);
		
		TypeIDs.BlockID.addChild(TypeIDs.BlockState, new CConverter<IBlockState, Block>()
		{
			@Override
			public Block convert(final IBlockState toConvert) throws CommandException
			{
				return toConvert.getBlock();
			}
		});
		
		TypeIDs.BlockID.addPrimitiveConverter(TypeIDs.String, Converters.blockToString);
		
		TypeIDs.BlockID.addPrimitiveConverter(TypeIDs.IChatComponent, new CConverter<Block, IChatComponent>()
		{
			@Override
			public IChatComponent convert(final Block toConvert) throws CommandException
			{
				return new ChatComponentTranslation(toConvert.getLocalizedName());
			}
		});
		
		TypeIDs.String.addPrimitiveConverter(TypeIDs.ItemID, ParserItemID.stringToItem);
		
		TypeIDs.BlockPos.addDefaultConverter(TypeIDs.Coordinates, new SConverter<BlockPos, Vec3>()
		{
			@Override
			public Vec3 convert(final BlockPos toConvert) throws SyntaxErrorException
			{
				return new Vec3(toConvert.getX(), toConvert.getY(), toConvert.getZ());
			}
		});
		
		TypeIDs.Coordinates.addDefaultConverter(TypeIDs.BlockPos, new SConverter<Vec3, BlockPos>()
		{
			@Override
			public BlockPos convert(final Vec3 toConvert) throws SyntaxErrorException
			{
				return new BlockPos(toConvert);
			}
		});
		
		TypeIDs.Coordinates.addDefaultConverter(TypeIDs.Shift, Converter.<Vec3, Shift> primitiveConverter());
	}
}
