package net.minecraft.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;

import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.arg.CompositeString;
import net.minecraft.command.arg.PrimitiveParameter;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IParse;
import net.minecraft.command.type.base.CustomParse;
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
	public static final MatcherRegistry aKeyMatcher = new MatcherRegistry("\\G\\s*+([\\w-]++)\\s*+\\=");
	public static final MatcherRegistry listEndMatcher = new MatcherRegistry("\\G\\s*+([,\\]}])");
	public static final MatcherRegistry nameMatcher = new MatcherRegistry("\\G[\\w-]*+");
	public static final MatcherRegistry keyMatcher = new MatcherRegistry("\\G\\s*+([\\w-]++)");
	public static final MatcherRegistry endingMatcher = new MatcherRegistry("\\G(\\s*+)([,;)\\]]|\\z)");
	public static final MatcherRegistry endingMatcherCompletion = new MatcherRegistry("\\G(\\s*+)([,;)\\]])");
	public static final MatcherRegistry idMatcher = new MatcherRegistry("\\G\\s*+([/@])");
	public static final MatcherRegistry oParenthMatcher = new MatcherRegistry("\\G\\s*+\\(");
	public static final MatcherRegistry generalMatcher = new MatcherRegistry("\\G\\s*+([@\\$])");
	public static final MatcherRegistry spaceMatcher = new MatcherRegistry("\\G\\s");
	
	public static final MatcherRegistry stringMatcher = new MatcherRegistry("\\G\\s*+([\\w\\.:-]++)");
	
	public static final MatcherRegistry escapedMatcher = new MatcherRegistry("\\G([^\\\\\"]*+)(?:\"|\\\\(.))");
	public static final MatcherRegistry quoteMatcher = new MatcherRegistry("\\G\\s*+\"");
	
	private ParsingUtilities()
	{
	}
	
	public static SyntaxErrorException SEE(final String message)
	{
		return new SyntaxErrorException(message);
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
	
	public static <R> IParse<CommandArg<R>> unwrap(final IParse<? extends ArgWrapper<R>> toUnwrap)
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
	
	public static String joinNiceString(final Object... elements)
	{
		final StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < elements.length; ++i)
		{
			final String item = elements[i].toString();
			
			if (i > 0)
			{
				if (i == elements.length - 1)
				{
					sb.append(" and ");
				}
				else
				{
					sb.append(", ");
				}
			}
			
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
		if (sender instanceof EntityPlayerMP)
		{
			return (EntityPlayerMP) sender;
		}
		else
		{
			throw new PlayerNotFoundException("You must specify which player you wish to perform this action on.");
		}
	}
	
	/**
	 * Note: Does not call generalParse
	 */
	public static CommandArg<String> parseString(final Parser parser, final Matcher stringMatcher) throws SyntaxErrorException, CompletionException
	{
		if (parser.findInc(stringMatcher))
			return new PrimitiveParameter<>(stringMatcher.group(1));
		
		if (parser.findInc(quoteMatcher))
			return parseQuotedString(parser);
		
		return null;
	}
	
	/**
	 * Note: Does not call generalParse
	 */
	public static CommandArg<String> parseString(final Parser parser) throws SyntaxErrorException, CompletionException
	{
		return parseString(parser, stringMatcher);
	}
	
	/**
	 * Note: Does not call generalParse
	 */
	public static CommandArg<String> parseString(final Parser parser, final MatcherRegistry m) throws SyntaxErrorException, CompletionException
	{
		return parseString(parser, parser.getMatcher(m));
	}
	
	public static <T> ArgWrapper<T> parseString(final Parser parser, final Context context, final TypeID<T> target, final Converter<String, T, ?> converter, final Matcher stringMatcher, final boolean immediate) throws SyntaxErrorException, CompletionException
	{
		final ArgWrapper<T> ret = context.generalParse(parser, target);
		
		if (ret != null)
			return ret;
		
		final CommandArg<String> ret2 = ParsingUtilities.parseString(parser, stringMatcher);
		
		if (ret2 != null)
		{
			if (immediate && ret2 instanceof PrimitiveParameter<?>)
			{
				try
				{
					return target.wrap(new PrimitiveParameter<>(converter.convert(ret2.eval(null))));
				} catch (final SyntaxErrorException e)
				{
					throw e;
				} catch (final CommandException e)
				{
					throw new SyntaxErrorException(e.getMessage(), e.getErrorOjbects());
				}
			}
			else
			{
				return target.wrap(new CommandArg<T>()
				{
					@Override
					public T eval(final ICommandSender sender) throws CommandException
					{
						return converter.convert(ret2.eval(sender));
					}
				});
			}
		}
		
		return null;
	}
	
	public static <T> ArgWrapper<T> parseString(final Parser parser, final Context context, final TypeID<T> target, final Converter<String, T, ?> converter, final Matcher stringMatcher) throws SyntaxErrorException, CompletionException
	{
		return parseString(parser, context, target, converter, stringMatcher, false);
	}
	
	public static <T> ArgWrapper<T> parseString(final Parser parser, final Context context, final TypeID<T> target, final Converter<String, T, ?> converter) throws SyntaxErrorException, CompletionException
	{
		return parseString(parser, context, target, converter, stringMatcher);
	}
	
	public static <T> ArgWrapper<T> parseString(final Parser parser, final Context context, final TypeID<T> target, final Converter<String, T, ?> converter, final MatcherRegistry m) throws SyntaxErrorException, CompletionException
	{
		return parseString(parser, context, target, converter, parser.getMatcher(m));
	}
	
	public static <T> ArgWrapper<T> parseString(final Parser parser, final Context context, final TypeID<T> target, final Converter<String, T, ?> converter, final MatcherRegistry m, final boolean immediate) throws SyntaxErrorException, CompletionException
	{
		return parseString(parser, context, target, converter, parser.getMatcher(m), immediate);
	}
	
	public static ArgWrapper<String> parseString(final Parser parser, final Context context, final TypeID<String> target, final Matcher stringMatcher) throws SyntaxErrorException, CompletionException
	{
		final ArgWrapper<String> ret = context.generalParse(parser, target);
		
		if (ret != null)
			return ret;
		
		final CommandArg<String> ret2 = parseString(parser, stringMatcher);
		
		if (ret2 != null)
			return target.wrap(ret2);
		
		return null;
	}
	
	public static ArgWrapper<String> parseString(final Parser parser, final Context context, final TypeID<String> target) throws SyntaxErrorException, CompletionException
	{
		return parseString(parser, context, target, stringMatcher);
	}
	
	public static ArgWrapper<String> parseString(final Parser parser, final Context context, final TypeID<String> target, final MatcherRegistry m) throws SyntaxErrorException, CompletionException
	{
		return parseString(parser, context, target, parser.getMatcher(m));
	}
	
	/**
	 * Note: Does not call generalParse
	 */
	public static String parseLiteralString(final Parser parser, final Matcher m) throws SyntaxErrorException, CompletionException
	{
		if (parser.findInc(m))
			return m.group(1);
		
		throw parser.SEE("Expected identifier around index ");
	}
	
	/**
	 * Note: Does not call generalParse
	 */
	public static String parseLiteralString(final Parser parser, final MatcherRegistry m) throws SyntaxErrorException, CompletionException
	{
		return parseLiteralString(parser, parser.getMatcher(m));
	}
	
	/**
	 * Note: Does not call generalParse
	 */
	public static String parseLiteralString(final Parser parser) throws SyntaxErrorException, CompletionException
	{
		return parseLiteralString(parser, parser.getMatcher(stringMatcher));
	}
	
	/**
	 * Requires the opening quotation mark to be parsed!
	 */
	public static CommandArg<String> parseQuotedString(final Parser parser) throws SyntaxErrorException, CompletionException
	{
		final Matcher m = parser.getMatcher(escapedMatcher);
		
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
			case '!': // '\!' represents 'nothing', useful for stuff like "@s-text" => use "@s\!-text"
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
	
	@SafeVarargs
	public static <T> Set<T> toSet(final T... items)
	{
		final Set<T> ret = new HashSet<>(items.length);
		for (final T typeID : ret)
			ret.add(typeID);
		
		return ret;
	}
	
	public static boolean isTrue(final String toCheck)
	{
		return "true".equalsIgnoreCase(toCheck) || "t".equalsIgnoreCase(toCheck) || "1".equalsIgnoreCase(toCheck);
	}
	
	public static boolean isFalse(final String toCheck)
	{
		return "false".equalsIgnoreCase(toCheck) || "f".equalsIgnoreCase(toCheck) || "0".equalsIgnoreCase(toCheck);
	}
}
