package net.minecraft.command.type.custom;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.EntityNotFoundException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.type.Converter;
import net.minecraft.command.type.TypeID;
import net.minecraft.command.type.custom.coordinate.Coordinate.CoordValueDec;
import net.minecraft.command.type.custom.coordinate.Coordinate.CoordValueInt;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.Vec3;

public class TypeIDs
{
	
	private TypeIDs()
	{
	};
	
	public static final TypeID<Byte> Byte = new TypeID<>("Byte");
	public static final TypeID<Short> Short = new TypeID<>("Short");
	public static final TypeID<Integer> Integer = new TypeID<>("Integer");
	public static final TypeID<Long> Long = new TypeID<>("Long");
	public static final TypeID<Float> Float = new TypeID<>("Float");
	public static final TypeID<Double> Double = new TypeID<>("Double");
	
	public static final TypeID<Double> CoordValue = new TypeID<>("CoordValue");
	public static final TypeID<Vec3> Coordinates = new TypeID<>("Coordinates");
	
	public static final TypeID<String> String = new TypeID<>("String");
	
	public static final TypeID<NBTBase> NBTBase = new TypeID<>("NBTBase");
	public static final TypeID<NBTTagCompound> NBTCompound = new TypeID<>("NBTCompound");
	
	public static final TypeID<ICommandSender> ICmdSender = new TypeID<>("ICommandSender");
	
	public static final TypeID<Entity> Entity = new TypeID<>("Entity");
	public static final TypeID<String> UUID = new TypeID<>("UUID");
	
	public static final TypeID<List<Entity>> EntityList = new TypeID<>("EntityList");
	public static final TypeID<List<String>> UUIDList = new TypeID<>("UUIDList");
	
	public static final void initConverters()
	{
		TypeIDs.Integer.addConverter(TypeIDs.String, new Converter<Integer, String>()
		{
			@Override
			public CommandArg<String> convert(final CommandArg<Integer> toConvert)
			{
				return new CommandArg<String>()
				{
					@Override
					public String eval(final ICommandSender sender) throws CommandException
					{
						return toConvert.eval(sender).toString();
					}
				};
			}
		});
		
		TypeIDs.Double.addConverter(TypeIDs.String, new Converter<Double, String>()
		{
			@Override
			public CommandArg<String> convert(final CommandArg<Double> toConvert)
			{
				return new CommandArg<String>()
				{
					@Override
					public String eval(final ICommandSender sender) throws CommandException
					{
						return toConvert.eval(sender).toString();
					}
				};
			}
		});
		
		TypeIDs.String.addConverter(TypeIDs.Integer, new Converter<String, Integer>()
		{
			@Override
			public CommandArg<Integer> convert(final CommandArg<String> toConvert)
			{
				return new CommandArg<Integer>()
				{
					@Override
					public Integer eval(final ICommandSender sender) throws CommandException
					{
						String res = "";
						try
						{
							res = toConvert.eval(sender);
							return new Integer(res);
						} catch (final NumberFormatException e)
						{
							throw new NumberInvalidException("Cannot convert " + res + " to int", new Object[0]);
						}
						
					}
				};
			}
		});
		
		TypeIDs.String.addConverter(TypeIDs.Double, new Converter<String, Double>()
		{
			@Override
			public CommandArg<Double> convert(final CommandArg<String> toConvert)
			{
				return new CommandArg<Double>()
				{
					@Override
					public Double eval(final ICommandSender sender) throws CommandException
					{
						String res = "";
						try
						{
							res = toConvert.eval(sender);
							return new Double(res);
						} catch (final NumberFormatException e)
						{
							throw new NumberInvalidException("Cannot convert " + res + " to double", new Object[0]);
						}
						
					}
				};
			}
		});
		
		TypeIDs.Integer.addConverter(TypeIDs.Double, new Converter<Integer, Double>()
		{
			@Override
			public CommandArg<Double> convert(final CommandArg<Integer> toConvert)
			{
				return new CommandArg<Double>()
				{
					@Override
					public Double eval(final ICommandSender sender) throws CommandException
					{
						return new Double(toConvert.eval(sender));
					}
				};
			}
		});
		
		TypeIDs.Double.addConverter(TypeIDs.Integer, new Converter<Double, Integer>()
		{
			@Override
			public CommandArg<Integer> convert(final CommandArg<Double> toConvert)
			{
				return new CommandArg<Integer>()
				{
					@Override
					public Integer eval(final ICommandSender sender) throws CommandException
					{
						return new Integer(toConvert.eval(sender).intValue());
					}
				};
			}
		});
		
		TypeIDs.Integer.addConverter(TypeIDs.NBTBase, new Converter<Integer, NBTBase>()
		{
			@Override
			public CommandArg<NBTBase> convert(final CommandArg<Integer> toConvert)
			{
				return new CommandArg<NBTBase>()
				{
					@Override
					public NBTTagInt eval(final ICommandSender sender) throws CommandException
					{
						return new NBTTagInt(toConvert.eval(sender));
					}
				};
			}
		});
		
		TypeIDs.Double.addConverter(TypeIDs.NBTBase, new Converter<Double, NBTBase>()
		{
			@Override
			public CommandArg<NBTBase> convert(final CommandArg<Double> toConvert)
			{
				return new CommandArg<NBTBase>()
				{
					@Override
					public NBTTagDouble eval(final ICommandSender sender) throws CommandException
					{
						return new NBTTagDouble(toConvert.eval(sender));
					}
				};
			}
		});
		
		TypeIDs.String.addConverter(TypeIDs.NBTBase, new Converter<String, NBTBase>()
		{
			@Override
			public CommandArg<NBTBase> convert(final CommandArg<String> toConvert)
			{
				return new CommandArg<NBTBase>()
				{
					@Override
					public NBTTagString eval(final ICommandSender sender) throws CommandException
					{
						return new NBTTagString(toConvert.eval(sender));
					}
				};
			}
		});
		
		TypeIDs.NBTBase.addConverter(TypeIDs.Integer, new Converter<NBTBase, Integer>()
		{
			@Override
			public CommandArg<Integer> convert(final CommandArg<NBTBase> toConvert)
			{
				return new CommandArg<Integer>()
				{
					@Override
					public Integer eval(final ICommandSender sender) throws CommandException
					{
						final NBTBase res = toConvert.eval(sender);
						if (!(res instanceof NBTBase.NBTPrimitive))
							throw new CommandException("Cannot convert from this NBT to int", new Object[0]);
						
						return ((NBTBase.NBTPrimitive) res).getInt();
					}
				};
			}
		});
		
		TypeIDs.NBTBase.addConverter(TypeIDs.Double, new Converter<NBTBase, Double>()
		{
			@Override
			public CommandArg<Double> convert(final CommandArg<NBTBase> toConvert)
			{
				return new CommandArg<Double>()
				{
					@Override
					public Double eval(final ICommandSender sender) throws CommandException
					{
						final NBTBase res = toConvert.eval(sender);
						if (!(res instanceof NBTBase.NBTPrimitive))
							throw new CommandException("Cannot convert from this NBT to double", new Object[0]);
						
						return ((NBTBase.NBTPrimitive) res).getDouble();
					}
				};
			}
		});
		
		TypeIDs.NBTBase.addConverter(TypeIDs.String, new Converter<NBTBase, String>()
		{
			@Override
			public CommandArg<String> convert(final CommandArg<NBTBase> toConvert)
			{
				return new CommandArg<String>()
				{
					@Override
					public String eval(final ICommandSender sender) throws CommandException
					{
						return toConvert.eval(sender).toString();
					}
				};
			}
		});
		
		TypeIDs.ICmdSender.addConverter(TypeIDs.String, new Converter<ICommandSender, String>()
		{
			@Override
			public CommandArg<java.lang.String> convert(final CommandArg<ICommandSender> toConvert)
			{
				return new CommandArg<String>()
				{
					@Override
					public String eval(final ICommandSender sender) throws CommandException
					{
						return toConvert.eval(sender).getName();
					}
				};
			}
		});
		
		TypeIDs.Integer.addConverter(TypeIDs.CoordValue, new Converter<Integer, Double>()
		{
			@Override
			public CoordValueInt convert(final CommandArg<Integer> toConvert)
			{
				return new CoordValueInt(new CommandArg<Double>()
				{
					@Override
					public Double eval(final ICommandSender sender) throws CommandException
					{
						return new Double(toConvert.eval(sender));
					}
				});
			}
		});
		
		TypeIDs.Double.addConverter(TypeIDs.CoordValue, new Converter<Double, Double>()
		{
			@Override
			public CoordValueDec convert(final CommandArg<Double> toConvert)
			{
				return new CoordValueDec(toConvert);
			}
		});
		
		TypeIDs.EntityList.addConverter(TypeIDs.UUIDList, new Converter<List<Entity>, List<String>>()
		{
			@Override
			public CommandArg<List<String>> convert(final CommandArg<List<Entity>> toConvert)
			{
				return new CommandArg<List<String>>()
				{
					@Override
					public List<String> eval(final ICommandSender sender) throws CommandException
					{
						final List<Entity> entityList = toConvert.eval(sender);
						final List<String> uuidList = new ArrayList<>(entityList.size());
						
						for (final Entity entity : entityList)
							uuidList.add(ParsingUtilities.getEntityIdentifier(entity));
						
						return uuidList;
					}
				};
			}
		});
		
		TypeIDs.ICmdSender.addConverter(TypeIDs.UUIDList, new Converter<ICommandSender, List<String>>()
		{
			
			@Override
			public CommandArg<List<String>> convert(final CommandArg<ICommandSender> toConvert)
			{
				return new CommandArg<List<String>>()
				{
					@Override
					public List<String> eval(final ICommandSender sender) throws CommandException
					{
						final ICommandSender iCmdSender = toConvert.eval(sender);
						
						if (!(iCmdSender instanceof Entity))
							throw new CommandException("Unable to convert " + iCmdSender.getName() + " to Entity", new Object[0]);
						
						final List<String> uuidList = new ArrayList<>(1);
						
						uuidList.add(ParsingUtilities.getEntityIdentifier((Entity) iCmdSender));
						
						return uuidList;
					}
				};
			}
		});
		
		TypeIDs.String.addConverter(TypeIDs.UUIDList, new Converter<String, List<String>>()
		{
			@Override
			public CommandArg<List<String>> convert(final CommandArg<String> toConvert)
			{
				return new CommandArg<List<String>>()
				{
					@Override
					public List<String> eval(final ICommandSender sender) throws CommandException
					{
						final List<String> uuidList = new ArrayList<>();
						
						uuidList.add(toConvert.eval(sender));
						
						return uuidList;
					}
				};
			}
		});
		
		TypeIDs.String.addConverter(TypeIDs.Entity, new Converter<String, Entity>()
		{
			@Override
			public CommandArg<Entity> convert(final CommandArg<String> toConvert)
			{
				return new CommandArg<Entity>()
				{
					@Override
					public Entity eval(ICommandSender sender) throws CommandException
					{
						final Entity ret = ParsingUtilities.entiyFromIdentifier(toConvert.eval(sender));
						
						if (ret == null)
							throw new EntityNotFoundException("commands.generic.entity.invalidUuid", new Object[0]);
						
						return ret;
					}
				};
			}
		});
		
		TypeIDs.UUID.addConverter(TypeIDs.Entity, new Converter<String, Entity>()
		{
			@Override
			public CommandArg<Entity> convert(final CommandArg<String> toConvert)
			{
				return new CommandArg<Entity>()
				{
					@Override
					public Entity eval(ICommandSender sender) throws CommandException
					{
						final Entity ret = ParsingUtilities.entiyFromIdentifier(toConvert.eval(sender));
						
						if (ret == null)
							throw new EntityNotFoundException("commands.generic.entity.invalidUuid", new Object[0]);
						
						return ret;
					}
				};
			}
		});
		
		TypeIDs.Entity.addConverter(TypeIDs.EntityList, new Converter<Entity, List<Entity>>()
		{
			@Override
			public CommandArg<List<Entity>> convert(final CommandArg<Entity> toConvert)
			{
				return new CommandArg<List<Entity>>()
				{
					@Override
					public List<Entity> eval(final ICommandSender sender) throws CommandException
					{
						final List<Entity> entityList = new ArrayList<>();
						
						entityList.add(toConvert.eval(sender));
						
						return entityList;
					}
				};
			}
		});
		
		TypeIDs.Entity.addConverter(TypeIDs.UUID, new Converter<Entity, String>()
		{
			@Override
			public CommandArg<String> convert(final CommandArg<Entity> toConvert)
			{
				return new CommandArg<String>()
				{
					@Override
					public String eval(final ICommandSender sender) throws CommandException
					{
						return ParsingUtilities.getEntityIdentifier(toConvert.eval(sender));
					}
				};
			}
		});
		
		new Converter.Chained<>(TypeIDs.String, TypeIDs.Entity).chain(TypeIDs.EntityList).register();
		new Converter.Chained<>(TypeIDs.UUID, TypeIDs.Entity).chain(TypeIDs.EntityList).register();
		
		TypeIDs.EntityList.addConverter(TypeIDs.Entity, new Converter<List<Entity>, Entity>()
		{
			@Override
			public CommandArg<Entity> convert(final CommandArg<List<Entity>> toConvert)
			{
				return new CommandArg<Entity>()
				{
					@Override
					public Entity eval(ICommandSender sender) throws CommandException
					{
						final List<Entity> list = toConvert.eval(sender);
						
						if (list.size() != 1)
							throw new EntityNotFoundException();
						
						return list.get(0);
					}
				};
			}
		});
		
		new Converter.Chained<>(TypeIDs.EntityList, TypeIDs.Entity).chain(TypeIDs.UUID).register();
	}
}
