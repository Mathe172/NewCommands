package net.minecraft.command.type.custom.nbt;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ParsingUtilities.PrimitiveCallback;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.parser.MatcherRegistry;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.management.TypeID;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;

public final class NBTUtilities
{
	public final static MatcherRegistry numberIDMatcher = new MatcherRegistry(Pattern.compile("\\G\\s*+([bsilfd])", Pattern.CASE_INSENSITIVE));
	public static Set<TypeID<? extends Number>> primitiveTypes = new HashSet<TypeID<? extends Number>>(Arrays.asList(TypeIDs.Byte, TypeIDs.Short, TypeIDs.Integer, TypeIDs.Long, TypeIDs.Float, TypeIDs.Double));
	
	private NBTUtilities()
	{
	}
	
	public static CommandArg<NBTBase> getTagByte(final Parser parser, final ArgWrapper<?> toConvert) throws SyntaxErrorException
	{
		if (primitiveTypes.contains(toConvert.type))
			return new CommandArg<NBTBase>()
			{
				@Override
				public NBTTagByte eval(final ICommandSender sender) throws CommandException
				{
					return new NBTTagByte(((Number) toConvert.arg().eval(sender)).byteValue());
				}
			};
		
		return new CommandArg<NBTBase>()
		{
			private final CommandArg<Byte> arg = toConvert.iConvertTo(parser, TypeIDs.Byte);
			
			@Override
			public NBTTagByte eval(final ICommandSender sender) throws CommandException
			{
				return new NBTTagByte(this.arg.eval(sender));
			}
		};
	}
	
	public static CommandArg<NBTBase> getTagShort(final Parser parser, final ArgWrapper<?> toConvert) throws SyntaxErrorException
	{
		if (primitiveTypes.contains(toConvert.type))
			return new CommandArg<NBTBase>()
			{
				@Override
				public NBTTagShort eval(final ICommandSender sender) throws CommandException
				{
					return new NBTTagShort(((Number) toConvert.arg().eval(sender)).shortValue());
				}
			};
		
		return new CommandArg<NBTBase>()
		{
			private final CommandArg<Short> arg = toConvert.iConvertTo(parser, TypeIDs.Short);
			
			@Override
			public NBTTagShort eval(final ICommandSender sender) throws CommandException
			{
				return new NBTTagShort(this.arg.eval(sender));
			}
		};
	}
	
	public static CommandArg<NBTBase> getTagInt(final Parser parser, final ArgWrapper<?> toConvert) throws SyntaxErrorException
	{
		if (primitiveTypes.contains(toConvert.type))
			return new CommandArg<NBTBase>()
			{
				@Override
				public NBTTagInt eval(final ICommandSender sender) throws CommandException
				{
					return new NBTTagInt(((Number) toConvert.arg().eval(sender)).intValue());
				}
			};
		
		return new CommandArg<NBTBase>()
		{
			private final CommandArg<Integer> arg = toConvert.iConvertTo(parser, TypeIDs.Integer);
			
			@Override
			public NBTTagInt eval(final ICommandSender sender) throws CommandException
			{
				return new NBTTagInt(this.arg.eval(sender));
			}
		};
	}
	
	public static CommandArg<NBTBase> getTagLong(final Parser parser, final ArgWrapper<?> toConvert) throws SyntaxErrorException
	{
		if (primitiveTypes.contains(toConvert.type))
			return new CommandArg<NBTBase>()
			{
				@Override
				public NBTTagLong eval(final ICommandSender sender) throws CommandException
				{
					return new NBTTagLong(((Number) toConvert.arg().eval(sender)).longValue());
				}
			};
		
		return new CommandArg<NBTBase>()
		{
			private final CommandArg<Long> arg = toConvert.iConvertTo(parser, TypeIDs.Long);
			
			@Override
			public NBTTagLong eval(final ICommandSender sender) throws CommandException
			{
				return new NBTTagLong(this.arg.eval(sender));
			}
		};
	}
	
	public static CommandArg<NBTBase> getTagFloat(final Parser parser, final ArgWrapper<?> toConvert) throws SyntaxErrorException
	{
		if (primitiveTypes.contains(toConvert.type))
			return new CommandArg<NBTBase>()
			{
				@Override
				public NBTTagFloat eval(final ICommandSender sender) throws CommandException
				{
					return new NBTTagFloat(((Number) toConvert.arg().eval(sender)).floatValue());
				}
			};
		
		return new CommandArg<NBTBase>()
		{
			private final CommandArg<Float> arg = toConvert.iConvertTo(parser, TypeIDs.Float);
			
			@Override
			public NBTTagFloat eval(final ICommandSender sender) throws CommandException
			{
				return new NBTTagFloat(this.arg.eval(sender));
			}
		};
	}
	
	public static CommandArg<NBTBase> getTagDouble(final Parser parser, final ArgWrapper<?> toConvert) throws SyntaxErrorException
	{
		if (primitiveTypes.contains(toConvert.type))
			return new CommandArg<NBTBase>()
			{
				@Override
				public NBTTagDouble eval(final ICommandSender sender) throws CommandException
				{
					return new NBTTagDouble(((Number) toConvert.arg().eval(sender)).doubleValue());
				}
			};
		
		return new CommandArg<NBTBase>()
		{
			private final CommandArg<Double> arg = toConvert.iConvertTo(parser, TypeIDs.Double);
			
			@Override
			public NBTTagDouble eval(final ICommandSender sender) throws CommandException
			{
				return new NBTTagDouble(this.arg.eval(sender));
			}
		};
	}
	
	public static abstract class NBTData implements PrimitiveCallback<String>
	{
		public abstract void put(NBTBase data);
		
		public abstract void add(CommandArg<NBTBase> data);
		
		@Override
		public CommandArg<String> call(final Parser parser, final String s) throws SyntaxErrorException
		{
			final Matcher m = parser.getMatcher(numberIDMatcher);
			
			if (parser.findInc(m))
				try
				{
					switch (m.group(1).charAt(0))
					{
					case 'b':
						this.put(new NBTTagByte(Byte.parseByte(s)));
						return null;
					case 's':
						this.put(new NBTTagShort(Short.parseShort(s)));
						return null;
					case 'i':
						this.put(new NBTTagInt(Integer.parseInt(s)));
						return null;
					case 'l':
						this.put(new NBTTagLong(Long.parseLong(s)));
						return null;
					case 'f':
						this.put(new NBTTagFloat(Float.parseFloat(s)));
						return null;
					case 'd':
						this.put(new NBTTagDouble(Double.parseDouble(s)));
						return null;
					}
				} catch (final NumberFormatException ex)
				{
					throw parser.SEE(ex.getMessage());
				}
			
			this.put(new NBTTagString(s));
			return null;
		}
	}
	
	public static CommandArg<NBTBase> procIdentifier(final Parser parser, final ArgWrapper<?> toConvert) throws SyntaxErrorException
	{
		final Matcher m = parser.getMatcher(numberIDMatcher);
		if (parser.findInc(m))
			switch (m.group(1).charAt(0))
			{
			case 'b':
				return getTagByte(parser, toConvert);
			case 's':
				return getTagShort(parser, toConvert);
			case 'i':
				return getTagInt(parser, toConvert);
			case 'l':
				return getTagLong(parser, toConvert);
			case 'f':
				return getTagFloat(parser, toConvert);
			case 'd':
				return getTagDouble(parser, toConvert);
			}
		
		return toConvert.iConvertTo(parser, TypeIDs.NBTBase);
	}
}
