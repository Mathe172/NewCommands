package net.minecraft.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;

import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.arg.CompositeString;
import net.minecraft.command.arg.PrimitiveParameter;
import net.minecraft.command.arg.TypedWrapper;
import net.minecraft.command.arg.TypedWrapper.Getter;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.parser.CompletionParser;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.MatcherRegistry;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IParse;
import net.minecraft.command.type.custom.ParserName;
import net.minecraft.command.type.management.CConvertable;
import net.minecraft.command.type.management.Converter;
import net.minecraft.command.type.management.SConverter;
import net.minecraft.command.type.management.TypeID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public final class ParsingUtilities
{
	public static final MatcherRegistry aKeyMatcher = new MatcherRegistry("\\G\\s*+([\\w-]++)\\s*+\\=");
	public static final MatcherRegistry listEndMatcher = new MatcherRegistry("\\G\\s*+([,\\]}])");
	public static final MatcherRegistry nameMatcher = new MatcherRegistry("\\G[\\w-]++");
	public static final MatcherRegistry keyMatcher = new MatcherRegistry("\\G\\s*+([\\w-]++)");
	public static final MatcherRegistry endingMatcher = new MatcherRegistry("\\G(\\s*+)([,;)\\]]|\\z)");
	public static final MatcherRegistry endingMatcherCompletion = new MatcherRegistry("\\G(\\s*+)([,;)\\]])");
	public static final MatcherRegistry idMatcher = new MatcherRegistry("\\G\\s*+([/@])");
	public static final MatcherRegistry oParenthMatcher = new MatcherRegistry("\\G\\s*+\\(");
	public static final MatcherRegistry generalMatcher = new MatcherRegistry("\\G\\s*+([@\\$])");
	public static final MatcherRegistry spaceMatcher = new MatcherRegistry("\\G\\s");
	
	public static final MatcherRegistry stringMatcher = new MatcherRegistry("\\G\\s*+([\\w\\.:-]++)");
	
	public static final MatcherRegistry quoteMatcher = new MatcherRegistry("\\G\\s*+\"");
	
	public final static MatcherRegistry baseMatcher = new MatcherRegistry("\\G[^\\[\\s]*+(?:(\\[)|(?=\\s|\\z))");
	public final static MatcherRegistry stackedMatcher = new MatcherRegistry("\\G[^\\[\\]]*+(\\[|\\])");
	public static final MatcherRegistry whitespaceMatcher = new MatcherRegistry("\\G\\s*+");
	
	private ParsingUtilities()
	{
	}
	
	public static <R> R generalParse(final Parser parser, final CConvertable<?, R> target, final Matcher m) throws SyntaxErrorException
	{
		if (!parser.findInc(m))
			return null;
		
		terminateCompletion(parser);
		
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
	
	public static IChatComponent join(final List<IChatComponent> toJoin)
	{
		final ChatComponentText ret = new ChatComponentText("");
		
		for (int i = 0; i < toJoin.size(); ++i)
		{
			if (i > 0)
				if (i == toJoin.size() - 1)
					ret.appendText(" and ");
				else if (i > 0)
					ret.appendText(", ");
			
			ret.appendSibling(toJoin.get(i));
		}
		
		return ret;
	}
	
	public static String joinNiceString(final Object... elements)
	{
		final StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < elements.length; ++i)
		{
			final String item = elements[i].toString();
			
			if (i > 0)
				if (i == elements.length - 1)
					sb.append(" and ");
				else
					sb.append(", ");
			
			sb.append(item);
		}
		
		return sb.toString();
	}
	
	public static String joinNiceString(final Collection<?> strings)
	{
		return joinNiceString(strings.toArray());
	}
	
	public static String getEntityIdentifier(final Entity entity)
	{
		return entity instanceof EntityPlayerMP ? entity.getName() : entity.getUniqueID().toString();
	}
	
	/**
	 * Returns the given ICommandSender as a EntityPlayer or throw an exception.
	 */
	public static EntityPlayerMP getCommandSenderAsPlayer(final ICommandSender sender) throws PlayerNotFoundException
	{
		if (sender.getCommandSenderEntity() instanceof EntityPlayerMP)
			return (EntityPlayerMP) sender.getCommandSenderEntity();
		
		throw new PlayerNotFoundException("You must specify which player you wish to perform this action on.");
	}
	
	/**
	 * Note: Does not call generalParse
	 */
	public static <T> CommandArg<T> parseString(final Parser parser, final Matcher stringMatcher, final Converter<String, T, ?> converter, final PrimitiveCallback<T> callback) throws SyntaxErrorException
	{
		if (parser.findInc(stringMatcher))
			return callback.call(parser, stringMatcher.group(1));
		
		if (parser.findInc(quoteMatcher))
			return parseQuotedString(parser, converter, callback);
		
		return null;
	}
	
	public static <T> ArgWrapper<T> parseString(final Parser parser, final Context context, final TypeID<T> target, final Converter<String, T, ?> converter, final Matcher stringMatcher, final PrimitiveCallback<T> callback) throws SyntaxErrorException
	{
		final ArgWrapper<T> ret = context.generalParse(parser, target);
		
		if (ret != null)
			return ret;
		
		final CommandArg<T> ret2 = parseString(parser, stringMatcher, converter, callback);
		
		if (ret2 != null)
			return target.wrap(ret2);
		
		return null;
	}
	
	public static <T> ArgWrapper<T> parseString(final Parser parser, final Context context, final TypeID<T> target, final Converter<String, T, ?> converter, final MatcherRegistry m, final PrimitiveCallback<T> callback) throws SyntaxErrorException
	{
		return parseString(parser, context, target, converter, parser.getMatcher(m), callback);
	}
	
	public static ArgWrapper<String> parseString(final Parser parser, final Context context, final TypeID<String> target, final MatcherRegistry m, final PrimitiveCallback<String> callback) throws SyntaxErrorException
	{
		return parseString(parser, context, target, idStringConverter, parser.getMatcher(m), callback);
	}
	
	/**
	 * Note: Does not call generalParse
	 */
	public static String parseLiteralString(final Parser parser, final Matcher m, final String errorMessage) throws SyntaxErrorException
	{
		if (parser.findInc(m))
			return m.group(1);
		
		throw parser.SEE(errorMessage);
	}
	
	/**
	 * Note: Does not call generalParse
	 */
	public static String parseLiteralString(final Parser parser, final MatcherRegistry m, final String errorMessage) throws SyntaxErrorException
	{
		return parseLiteralString(parser, parser.getMatcher(m), errorMessage);
	}
	
	/**
	 * Note: Does not call generalParse
	 */
	public static String parseLiteralString(final Parser parser, final String errorMessage) throws SyntaxErrorException
	{
		return parseLiteralString(parser, parser.getMatcher(stringMatcher), errorMessage);
	}
	
	/**
	 * Requires the opening quotation mark to be parsed!
	 */
	public static String parseEscapedString(final Parser parser, final char endChar) throws SyntaxErrorException
	{
		final StringBuilder sb = new StringBuilder();
		
		int partStart = parser.getIndex();
		while (!parser.endReached())
		{
			final char nextChar = parser.consumeNextChar();
			
			if (nextChar == endChar)
				return sb.append(parser.toParse, partStart, parser.getIndex() - 1).toString();
			
			if (nextChar == '\\')
			{
				if (parser.endReached())
					throw parser.SEE("Unterminated string ");
				
				sb.append(parser.toParse, partStart, parser.getIndex() - 1);
				
				procCSequence(parser, parser.consumeNextChar(), sb);
				
				partStart = parser.getIndex();
			}
		}
		
		throw parser.SEE("Missing " + (endChar != '\'' ? "'" + endChar + "'" : "\"'\"") + " ");
	}
	
	/**
	 * Requires the opening quotation mark to be parsed!
	 */
	public static <T> CommandArg<T> parseQuotedString(final Parser parser, final Converter<String, T, ?> converter, final PrimitiveCallback<T> callback, final char endChar) throws SyntaxErrorException
	{
		StringBuilder sb = new StringBuilder();
		
		final List<CommandArg<String>> parts = new ArrayList<>();
		
		final IParse<ArgWrapper<String>> selectorParser = TypeIDs.String.selectorParser;
		final IParse<ArgWrapper<String>> labelParser = TypeIDs.String.labelParser;
		
		int partStart = parser.getIndex();
		while (!parser.endReached())
		{
			final char nextChar = parser.consumeNextChar();
			
			if (nextChar == endChar)
			{
				sb.append(parser.toParse, partStart, parser.getIndex() - 1);
				
				if (parts.isEmpty())
					return callback.call(parser, sb.toString());
				if (sb.length() != 0)
					parts.add(new PrimitiveParameter<>(sb.toString()));
				
				return converter.transform(new CompositeString(parts));
			}
			
			if (nextChar == '\\')
			{
				if (parser.endReached())
					throw parser.SEE("Unterminated string ");
				
				sb.append(parser.toParse, partStart, parser.getIndex() - 1);
				
				final char controlChar = parser.consumeNextChar();
				
				switch (controlChar)
				{
				case '@':
					sb = resetSB(parts, sb);
					
					parts.add(selectorParser.parse(parser).arg());
					break;
				case '$':
					sb = resetSB(parts, sb);
					
					parts.add(labelParser.parse(parser).arg());
					break;
				case '!': // '\!' represents 'nothing', useful for stuff like "@s-text" => use "@s\!-text"
					break;
				default:
					procCSequence(parser, controlChar, sb);
				}
				
				partStart = parser.getIndex();
			}
		}
		
		throw parser.SEE("Missing " + (endChar != '\'' ? "'" + endChar + "'" : "\"'\"") + " ");
	}
	
	public static final void procCSequence(final Parser parser, final char controlChar, final StringBuilder sb) throws SyntaxErrorException
	{
		switch (controlChar)
		{
		case 'b':
			sb.append('\b');
			return;
		case 'f':
			sb.append('\f');
			return;
		case 'n':
			sb.append('\n');
			return;
		case 'r':
			sb.append('\r');
			return;
		case 't':
			sb.append('\t');
			return;
		case 'u':
			if (parser.getIndex() + 4 > parser.len)
				throw parser.SEE("Unterminated escape sequence: \\u" + parser.toParse.substring(parser.getIndex()) + " ");
			
			char result = 0;
			for (int i = 1; i <= 4; ++i)
			{
				final char c = parser.consumeNextChar();
				
				result = (char) (result << 4);
				if ((c >= '0') && (c <= '9'))
					result = (char) (result + c - '0');
				else if ((c >= 'a') && (c <= 'f'))
					result = (char) (result + c - 'a' + 10);
				else if ((c >= 'A') && (c <= 'F'))
					result = (char) (result + c - 'A' + 10);
				else
					throw parser.SEE("Invalid escape sequence: \\u" + parser.toParse.substring(parser.getIndex() - i, parser.getIndex() - i + 4) + " ");
			}
			
			sb.append(result);
			return;
		default:
			sb.append(controlChar);
		}
	}
	
	/**
	 * Requires the opening quotation mark to be parsed!
	 */
	public static <T> CommandArg<T> parseQuotedString(final Parser parser, final Converter<String, T, ?> converter, final PrimitiveCallback<T> callback) throws SyntaxErrorException
	{
		return parseQuotedString(parser, converter, callback, '"');
	}
	
	/**
	 * Requires the opening quotation mark to be parsed!
	 */
	public static CommandArg<String> parseQuotedString(final Parser parser, final PrimitiveCallback<String> callback) throws SyntaxErrorException
	{
		return parseQuotedString(parser, idStringConverter, callback);
	}
	
	/**
	 * Requires the opening quotation mark to be parsed!
	 */
	public static CommandArg<String> parseQuotedString(final Parser parser) throws SyntaxErrorException
	{
		return parseQuotedString(parser, ParserName.callback);
	}
	
	/**
	 * Requires the opening quotation mark to be parsed!
	 */
	public static CommandArg<String> parseQuotedString(final Parser parser, final PrimitiveCallback<String> callback, final char endChar) throws SyntaxErrorException
	{
		return parseQuotedString(parser, idStringConverter, callback, endChar);
	}
	
	/**
	 * Requires the opening quotation mark to be parsed!
	 */
	public static CommandArg<String> parseQuotedString(final Parser parser, final char endChar) throws SyntaxErrorException
	{
		return parseQuotedString(parser, ParserName.callback, endChar);
	}
	
	public static interface PrimitiveCallback<T>
	{
		public CommandArg<T> call(final Parser parser, final String s) throws SyntaxErrorException;
	}
	
	public static final SConverter<String, String> idStringConverter = new SConverter<String, String>()
	{
		@Override
		public String convert(final String toConvert) throws SyntaxErrorException
		{
			return toConvert;
		}
		
		@Override
		public CommandArg<String> transform(final CommandArg<String> toTransform)
		{
			return toTransform;
		};
	};
	
	public static final <T> PrimitiveCallback<T> callbackImmediate(final Converter<String, T, ? super SyntaxErrorException> converter)
	{
		return new PrimitiveCallback<T>()
		{
			@Override
			public CommandArg<T> call(final Parser parser, final String s) throws SyntaxErrorException
			{
				try
				{
					return new PrimitiveParameter<>(converter.convert(s));
				} catch (final SyntaxErrorException e)
				{
					throw e;
				} catch (final CommandException e)
				{
					throw parser.SEE(e.getMessage(), false, e.getErrorOjbects());
				}
			}
		};
	}
	
	public static final <T> PrimitiveCallback<T> callbackNonImmediate(final Converter<String, T, ?> converter)
	{
		return new PrimitiveCallback<T>()
		{
			@Override
			public CommandArg<T> call(final Parser parser, final String s) throws SyntaxErrorException
			{
				return converter.transform(new PrimitiveParameter<>(s));
			}
		};
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
	
	public static boolean isTrue(final String toCheck)
	{
		return "true".equalsIgnoreCase(toCheck) || "t".equalsIgnoreCase(toCheck) || "1".equalsIgnoreCase(toCheck);
	}
	
	public static boolean isFalse(final String toCheck)
	{
		return "false".equalsIgnoreCase(toCheck) || "f".equalsIgnoreCase(toCheck) || "0".equalsIgnoreCase(toCheck);
	}
	
	public static final String parseLazyString(final Parser parser, final MatcherRegistry baseMatcher) throws SyntaxErrorException
	{
		final int startIndex = parser.getIndex();
		int level = 0;
		
		final Matcher bm = parser.getMatcher(baseMatcher);
		final Matcher sm = parser.getMatcher(ParsingUtilities.stackedMatcher);
		
		parser.findInc(whitespaceMatcher);
		
		while (true)
			if (level == 0)
			{
				if (!parser.findInc(bm))
					throw parser.SEE("Missing ']', '}' or ',' ");
				
				if (bm.group(1) == null)
					return parser.toParse.substring(startIndex, parser.getIndex()).trim();
				
				level = 1;
			}
			else
			{
				if (!parser.findInc(sm))
					throw parser.SEE("Missing ']' ");
				
				if ("[".equals(sm.group(1)))
					++level;
				else
					--level;
			}
	}
	
	public static <T> Getter<T> get(final TypeID<T> type, final TypedWrapper<?> wrapper)
	{
		if (wrapper == null)
			return null;
		
		return wrapper.get(type);
	}
	
	@SafeVarargs
	public static final <T> Set<T> setOrNull(final T... elements)
	{
		return elements == null || elements.length == 0 ? null : elements.length == 1 ? Collections.singleton(elements[0]) : new HashSet<>(Arrays.asList(elements));
	}
	
	public static void proposeCompletion(final Parser parser)
	{
		parser.supplyHint(CompletionParser.propose);
	}
	
	public static void terminateCompletion(final Parser parser)
	{
		parser.supplyHint(CompletionParser.terminate);
	}
	
	public static IChatComponent location(final Parser parser)
	{
		final String toParse = parser.toParse;
		final int index = parser.getIndex();
		
		final int start = index > 19 ? index - 20 : 0;
		
		final int end = index < toParse.length() - 20 ? index + 19 : toParse.length();
		
		final IChatComponent prefix = new ChatComponentText((start > 0 ? "�" : "") + toParse.substring(start, index));
		
		final IChatComponent cursor = new ChatComponentText("|");
		
		final IChatComponent postfix = new ChatComponentText(toParse.substring(index, end) + (end < toParse.length() ? "�" : ""));
		
		prefix.getChatStyle().setColor(EnumChatFormatting.GRAY);
		cursor.getChatStyle().setColor(EnumChatFormatting.RED);
		postfix.getChatStyle().setColor(EnumChatFormatting.GRAY);
		
		final IChatComponent message = prefix.appendSibling(cursor).appendSibling(postfix);
		return message;
	}
}
