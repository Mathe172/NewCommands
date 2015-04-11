package net.minecraft.command;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.arg.CompositeString;
import net.minecraft.command.arg.PrimitiveParameter;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IDataType;
import net.minecraft.command.type.IParse;
import net.minecraft.command.type.base.CustomParse;
import net.minecraft.command.type.custom.TypeIDs;
import net.minecraft.command.type.custom.TypeSelectorContent.ParserData;
import net.minecraft.command.type.management.CConvertable;
import net.minecraft.command.type.management.Converter;
import net.minecraft.command.type.management.TypeID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

public final class ParsingUtilities
{
	public static final Pattern assignedKeyPattern = Pattern.compile("\\G\\s*+([\\w-]++)\\s*+\\=");
	public static final Pattern listEndPattern = Pattern.compile("\\G\\s*+([,\\]}])");
	public static final Pattern namePattern = Pattern.compile("\\G[\\w-]*+");
	public static final Pattern keyPattern = Pattern.compile("\\G\\s*+([\\w-]++)");
	public static final Pattern endingPattern = Pattern.compile("\\G(\\s*+)([,;)\\]]|\\z)");
	public static final Pattern endingPatternCompletion = Pattern.compile("\\G(\\s*+)([,;)\\]])");
	public static final Pattern identifierPattern = Pattern.compile("\\G\\s*+([/@])");
	public static final Pattern oParenthPattern = Pattern.compile("\\G\\s*+\\(");
	public static final Pattern generalPattern = Pattern.compile("\\G\\s*+([@\\$])");
	public static final Pattern spacePattern = Pattern.compile("\\G\\s");
	
	public static final Pattern stringPattern = Pattern.compile("\\G\\s*+([\\w\\.:-]++)");
	
	public static final Pattern escapedPattern = Pattern.compile("\\G([^\\\\\"]*+)(?:\"|\\\\(.))");
	public static final Pattern quotePattern = Pattern.compile("\\G\\s*+\"");
	
	public static SyntaxErrorException SEE(final String message)
	{
		return new SyntaxErrorException(message);
	}
	
	public static WrongUsageException WUE(final String message)
	{
		return new WrongUsageException(message);
	}
	
	public static <R> R generalParse(final Parser parser, final CConvertable<?, R> target, final Matcher m) throws SyntaxErrorException, CompletionException
	{
		if (!parser.findInc(m))
			return null;
		
		parser.terminateCompletion();
		
		return "@".equals(m.group(1)) ? target.selectorParser.parse(parser) : target.labelParser.parse(parser);
	}
	
	public static Entity entiyFromIdentifier(final String identifier)
	{
		final MinecraftServer server = MinecraftServer.getServer();
		
		Entity ret = server.getConfigurationManager().getPlayerByUsername(identifier);
		
		if (ret != null)
			return ret;
		
		try
		{
			final UUID uuid = UUID.fromString(identifier);
			ret = server.getEntityFromUuid(uuid);
			
			if (ret != null)
				return ret;
			// getPlayerFromUuid
			return server.getConfigurationManager().func_177451_a(uuid);
			
		} catch (final IllegalArgumentException ex)
		{
			return null;
		}
	}
	
	public static <R> IParse<CommandArg<R>> unwrap(final IDataType<ArgWrapper<R>> toUnwrap)
	{
		return new CustomParse<CommandArg<R>>()
		{
			@Override
			public CommandArg<R> parse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
			{
				return toUnwrap.parse(parser, context).arg;
			}
		};
	}
	
	public static IChatComponent join(final List<IChatComponent> toJoin)
	{
		final ChatComponentText ret = new ChatComponentText("");
		
		for (int i = 0; i < toJoin.size(); ++i)
		{
			if (i > 0)
			{
				if (i == toJoin.size() - 1)
				{
					ret.appendText(" and ");
				}
				else if (i > 0)
				{
					ret.appendText(", ");
				}
			}
			
			ret.appendSibling(toJoin.get(i));
		}
		
		return ret;
	}
	
	public static String getEntityIdentifier(final Entity entity)
	{
		return entity instanceof EntityPlayerMP ? entity.getName() : entity.getUniqueID().toString();
	}
	
	public static ArgWrapper<?> getParam(final int index, final ParserData parserData)
	{
		return index < parserData.unnamedParams.size() ? parserData.unnamedParams.get(index) : null;
	}
	
	public static ArgWrapper<?> getParam(final int index, final String name, final ParserData parserData)
	{
		if (name != null)
		{
			final ArgWrapper<?> ret = parserData.namedParams.get(name);
			if (ret != null)
				return ret;
		}
		
		return getParam(index, parserData);
	}
	
	public static <T> CommandArg<T> getParam(final TypeID<T> type, final int index, final String name, final ParserData parserData)
	{
		return ArgWrapper.get(type, getParam(index, name, parserData));
	}
	
	public static <T> CommandArg<T> getParam(final TypeID<T> type, final int index, final ParserData parserData)
	{
		return ArgWrapper.get(type, getParam(index, parserData));
	}
	
	public static <T> CommandArg<T> getParam(final TypeID<T> type, final String name, final ParserData parserData)
	{
		return ArgWrapper.get(type, parserData.namedParams.get(name));
	}
	
	public static ArgWrapper<?> getRequiredParam(final int index, final String name, final ParserData parserData) throws SyntaxErrorException
	{
		final ArgWrapper<?> ret = getParam(index, name, parserData);
		
		if (ret != null)
			return ret;
		
		throw SEE("Missing parameter for selector: " + (name != null ? name : index));
	}
	
	public static ArgWrapper<?> getRequiredParam(final int index, final ParserData parserData) throws SyntaxErrorException
	{
		final ArgWrapper<?> ret = getParam(index, parserData);
		
		if (ret != null)
			return ret;
		
		throw SEE("Missing parameter for selector: " + index);
	}
	
	public static ArgWrapper<?> getRequiredParam(final String name, final ParserData parserData) throws SyntaxErrorException
	{
		final ArgWrapper<?> ret = parserData.namedParams.get(name);
		
		if (ret != null)
			return ret;
		
		throw SEE("Missing parameter for selector: " + name);
	}
	
	public static <T> CommandArg<T> getRequiredParam(final TypeID<T> type, final int index, final String name, final ParserData parserData) throws SyntaxErrorException
	{
		return getRequiredParam(index, name, parserData).get(type);
	}
	
	public static <T> CommandArg<T> getRequiredParam(final TypeID<T> type, final int index, final ParserData parserData) throws SyntaxErrorException
	{
		return getRequiredParam(index, parserData).get(type);
	}
	
	public static <T> CommandArg<T> getRequiredParam(final TypeID<T> type, final String name, final ParserData parserData) throws SyntaxErrorException
	{
		return getRequiredParam(name, parserData).get(type);
	}
	
	/**
	 * Returns the given ICommandSender as a EntityPlayer or throw an exception.
	 */
	public static EntityPlayerMP getCommandSenderAsPlayer(final ICommandSender sender) throws PlayerNotFoundException
	{
		if (sender instanceof EntityPlayerMP)
		{
			return (EntityPlayerMP) sender;
		}
		else
		{
			throw new PlayerNotFoundException("You must specify which player you wish to perform this action on.", new Object[0]);
		}
	}
	
	/**
	 * Note: Does not call generalParse
	 */
	public static CommandArg<String> parseString(final Parser parser) throws SyntaxErrorException, CompletionException
	{
		final Matcher m = parser.stringMatcher;
		
		if (parser.findInc(m))
			return new PrimitiveParameter<>(m.group(1));
		
		if (parser.findInc(parser.quoteMatcher))
			return parseQuotedString(parser);
		
		return null;
	}
	
	public static <T> ArgWrapper<T> parseString(final Parser parser, final Context context, final TypeID<T> target, final Converter<String, T, ?> converter) throws SyntaxErrorException, CompletionException
	{
		final ArgWrapper<T> ret = context.generalParse(parser, target);
		
		if (ret != null)
			return ret;
		
		final CommandArg<String> ret2 = ParsingUtilities.parseString(parser);
		
		if (ret2 != null)
			return target.wrap(new CommandArg<T>()
			{
				@Override
				public T eval(final ICommandSender sender) throws CommandException
				{
					return converter.convert(ret2.eval(sender));
				}
			});
		
		return null;
	}
	
	public static ArgWrapper<String> parseString(final Parser parser, final Context context, final TypeID<String> target) throws SyntaxErrorException, CompletionException
	{
		final ArgWrapper<String> ret = context.generalParse(parser, target);
		
		if (ret != null)
			return ret;
		
		final CommandArg<String> ret2 = ParsingUtilities.parseString(parser);
		
		if (ret2 != null)
			return target.wrap(ret2);
		
		return null;
	}
	
	/**
	 * Note: Does not call generalParse
	 */
	public static String parseLiteralString(final Parser parser) throws SyntaxErrorException, CompletionException
	{
		final Matcher m = parser.stringMatcher;
		
		if (parser.findInc(m))
			return m.group(1);
		
		throw parser.SEE("Expected identifier around index ");
	}
	
	/**
	 * Requires the opening quotation mark to be parsed!
	 */
	public static CommandArg<String> parseQuotedString(final Parser parser) throws SyntaxErrorException, CompletionException
	{
		final Matcher m = parser.escapedMatcher;
		
		StringBuilder sb = new StringBuilder();
		
		final List<CommandArg<String>> parts = new ArrayList<>();
		
		final IParse<ArgWrapper<String>> selectorParser = TypeIDs.String.selectorParser;
		final IParse<ArgWrapper<String>> labelParser = TypeIDs.String.labelParser;
		
		while (parser.findInc(m))
		{
			sb.append(m.group(1));
			
			if (m.group(2) == null)
			{
				if (parts.isEmpty())
					return new PrimitiveParameter<>(sb.toString());
				else
				{
					if (sb.length() != 0)
						parts.add(new PrimitiveParameter<>(sb.toString()));
					
					return new CompositeString(parts);
				}
			}
			
			switch (m.group(2).charAt(0))
			{
			case '@':
				sb = resetSB(parts, sb);
				
				parts.add(selectorParser.parse(parser).arg);
				continue;
			case '$':
				sb = resetSB(parts, sb);
				
				parts.add(labelParser.parse(parser).arg);
				continue;
			case '"':
				sb.append('"');
				continue;
			case '\\':
				sb.append('\\');
				continue;
			case '!':
				continue;
			default:
				sb.append(m.group(2));
			}
		}
		
		throw parser.SEE("Missing '\"' around index ");
	}
	
	private static StringBuilder resetSB(final List<CommandArg<String>> parts, final StringBuilder sb)
	{
		if (sb.length() != 0)
		{
			parts.add(new PrimitiveParameter<>(sb.toString()));
			return new StringBuilder();
		}
		
		return sb;
	}
}
