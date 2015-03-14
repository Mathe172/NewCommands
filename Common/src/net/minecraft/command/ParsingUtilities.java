package net.minecraft.command;

import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IDataType;
import net.minecraft.command.type.IParse;
import net.minecraft.command.type.TypeID;
import net.minecraft.command.type.base.CustomParse;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

public class ParsingUtilities
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
	
	public static final SyntaxErrorException SEE(final String message)
	{
		return new SyntaxErrorException(message, new Object[0]);
	}
	
	public static final WrongUsageException WUE(final String message)
	{
		return new WrongUsageException(message, new Object[0]);
	}
	
	public static final <R> ArgWrapper<R> generalParse(final Parser parser, final TypeID<R> target) throws SyntaxErrorException, CompletionException
	{
		final Matcher m = parser.generalMatcher;
		
		if (!parser.findInc(m))
			return null;
		
		parser.terminateCompletion();
		
		if ("@".equals(m.group(1)))
			return target.selectorParser.parse(parser);
		
		return target.labelParser.parse(parser);
		
	}
	
	public static final Entity entiyFromIdentifier(String identifier)
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
	
	public static final <R> IParse<CommandArg<R>> unwrap(final IDataType<ArgWrapper<R>> toUnwrap)
	{
		return new CustomParse<CommandArg<R>>()
		{
			@Override
			public CommandArg<R> parse(Parser parser) throws SyntaxErrorException, CompletionException
			{
				return toUnwrap.parse(parser).arg;
			}
		};
	}
	
	public static final IChatComponent join(List<IChatComponent> toJoin)
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
	
	public static final String getEntityIdentifier(Entity entity)
	{
		return entity instanceof EntityPlayerMP ? entity.getName() : entity.getUniqueID().toString();
	}
}
