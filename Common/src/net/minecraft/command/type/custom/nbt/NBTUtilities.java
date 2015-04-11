package net.minecraft.command.type.custom.nbt;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletion;
import net.minecraft.command.completion.TabCompletionData;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IComplete;
import net.minecraft.command.type.custom.TypeIDs;
import net.minecraft.command.type.management.TypeID;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;

public class NBTUtilities
{
	public final static Pattern numberIDPattern = Pattern.compile("\\G\\s*+([bsilfd])", Pattern.CASE_INSENSITIVE);
	
	private static Set<TypeID<? extends Number>> primitiveTypes = new HashSet<TypeID<? extends Number>>(Arrays.asList(TypeIDs.Byte, TypeIDs.Short, TypeIDs.Integer, TypeIDs.Long, TypeIDs.Float, TypeIDs.Double));
	
	public static final ITabCompletion braceCompletion = new TabCompletion(Pattern.compile("\\A(\\s*+)\\{?+"), "{}", "{}")
	{
		@Override
		public boolean complexFit()
		{
			return false;
		}
		
		@Override
		public int getCursorOffset(final Matcher m, final CompletionData cData)
		{
			return -1;
		};
		
		@Override
		public double weightOffset(final Matcher m, final CompletionData cData)
		{
			return 1.0;
		}
		
		@Override
		public boolean fullMatch(final Matcher m, final CompletionData cData, final String replacement)
		{
			return false;
		}
	};
	
	public static final IComplete braceCompleter = new IComplete()
	{
		@Override
		public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
		{
			TabCompletionData.addToSet(tcDataSet, startIndex, cData, braceCompletion);
		}
	};
	
	public static final ITabCompletion bracketCompletion = new TabCompletion(Pattern.compile("\\A(\\s*+)\\[?+\\z"), "[]", "[]")
	{
		@Override
		public boolean complexFit()
		{
			return false;
		}
		
		@Override
		public int getCursorOffset(final Matcher m, final CompletionData cData)
		{
			return -1;
		};
		
		@Override
		public double weightOffset(final Matcher m, final CompletionData cData)
		{
			return 1.0;
		}
		
		@Override
		public boolean fullMatch(final Matcher m, final CompletionData cData, final String replacement)
		{
			return false;
		}
	};
	
	public static final IComplete bracketCompleter = new IComplete()
	{
		@Override
		public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
		{
			TabCompletionData.addToSet(tcDataSet, startIndex, cData, bracketCompletion);
		}
	};
	
	public static CommandArg<NBTBase> getTagByte(final ArgWrapper<?> toConvert)
	{
		if (primitiveTypes.contains(toConvert.type))
			return new CommandArg<NBTBase>()
			{
				@Override
				public NBTTagByte eval(final ICommandSender sender) throws CommandException
				{
					return new NBTTagByte(((Number) toConvert.arg.eval(sender)).byteValue());
				}
			};
		
		return new CommandArg<NBTBase>()
		{
			@Override
			public NBTTagByte eval(final ICommandSender sender) throws CommandException
			{
				return new NBTTagByte(toConvert.iConvertTo(TypeIDs.Byte).eval(sender));
			}
		};
	}
	
	public static CommandArg<NBTBase> getTagShort(final ArgWrapper<?> toConvert)
	{
		if (primitiveTypes.contains(toConvert.type))
			return new CommandArg<NBTBase>()
			{
				@Override
				public NBTTagShort eval(final ICommandSender sender) throws CommandException
				{
					return new NBTTagShort(((Number) toConvert.arg.eval(sender)).shortValue());
				}
			};
		
		return new CommandArg<NBTBase>()
		{
			@Override
			public NBTTagShort eval(final ICommandSender sender) throws CommandException
			{
				return new NBTTagShort(toConvert.iConvertTo(TypeIDs.Short).eval(sender));
			}
		};
	}
	
	public static CommandArg<NBTBase> getTagInt(final ArgWrapper<?> toConvert)
	{
		if (primitiveTypes.contains(toConvert.type))
			return new CommandArg<NBTBase>()
			{
				@Override
				public NBTTagInt eval(final ICommandSender sender) throws CommandException
				{
					return new NBTTagInt(((Number) toConvert.arg.eval(sender)).intValue());
				}
			};
		
		return new CommandArg<NBTBase>()
		{
			@Override
			public NBTTagInt eval(final ICommandSender sender) throws CommandException
			{
				return new NBTTagInt(toConvert.iConvertTo(TypeIDs.Integer).eval(sender));
			}
		};
	}
	
	public static CommandArg<NBTBase> getTagLong(final ArgWrapper<?> toConvert)
	{
		if (primitiveTypes.contains(toConvert.type))
			return new CommandArg<NBTBase>()
			{
				@Override
				public NBTTagLong eval(final ICommandSender sender) throws CommandException
				{
					return new NBTTagLong(((Number) toConvert.arg.eval(sender)).longValue());
				}
			};
		
		return new CommandArg<NBTBase>()
		{
			@Override
			public NBTTagLong eval(final ICommandSender sender) throws CommandException
			{
				return new NBTTagLong(toConvert.iConvertTo(TypeIDs.Long).eval(sender));
			}
		};
	}
	
	public static CommandArg<NBTBase> getTagFloat(final ArgWrapper<?> toConvert)
	{
		if (primitiveTypes.contains(toConvert.type))
			return new CommandArg<NBTBase>()
			{
				@Override
				public NBTTagFloat eval(final ICommandSender sender) throws CommandException
				{
					return new NBTTagFloat(((Number) toConvert.arg.eval(sender)).floatValue());
				}
			};
		
		return new CommandArg<NBTBase>()
		{
			@Override
			public NBTTagFloat eval(final ICommandSender sender) throws CommandException
			{
				return new NBTTagFloat(toConvert.iConvertTo(TypeIDs.Float).eval(sender));
			}
		};
	}
	
	public static CommandArg<NBTBase> getTagDouble(final ArgWrapper<?> toConvert)
	{
		if (primitiveTypes.contains(toConvert.type))
			return new CommandArg<NBTBase>()
			{
				@Override
				public NBTTagDouble eval(final ICommandSender sender) throws CommandException
				{
					return new NBTTagDouble(((Number) toConvert.arg.eval(sender)).doubleValue());
				}
			};
		
		return new CommandArg<NBTBase>()
		{
			@Override
			public NBTTagDouble eval(final ICommandSender sender) throws CommandException
			{
				return new NBTTagDouble(toConvert.iConvertTo(TypeIDs.Double).eval(sender));
			}
		};
	}
	
	public static abstract class NBTData
	{
		public abstract void put(NBTBase data);
		
		public abstract void put(CommandArg<NBTBase> data);
	}
	
	public static CommandArg<NBTBase> procIdentifier(final Parser parser, final ArgWrapper<?> toConvert) throws SyntaxErrorException
	{
		if (parser.findInc(parser.numberIDMatcher))
		{
			switch (parser.numberIDMatcher.group(1).charAt(0))
			{
			case 'b':
				return getTagByte(toConvert);
			case 's':
				return getTagShort(toConvert);
			case 'i':
				return getTagInt(toConvert);
			case 'l':
				return getTagLong(toConvert);
			case 'f':
				return getTagFloat(toConvert);
			case 'd':
				return getTagDouble(toConvert);
			}
		}
		
		return toConvert.iConvertTo(TypeIDs.NBTBase);
	}
}
