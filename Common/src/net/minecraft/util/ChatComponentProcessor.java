package net.minecraft.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.EntityNotFoundException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.parser.Parser;
import net.minecraft.entity.Entity;

public class ChatComponentProcessor
{
	private static final String __OBFID = "CL_00002310";
	
	public static IChatComponent func_179985_a(ICommandSender sender, IChatComponent toProcess, Entity target) throws CommandException
	{
		IChatComponent ret = null;
		
		if (toProcess instanceof ChatComponentScore)
		{
			final ChatComponentScore scoreToProc = (ChatComponentScore) toProcess;
			String playerName = scoreToProc.func_179995_g();
			
			if (target != null && playerName.equals("*"))
				playerName = ParsingUtilities.getEntityIdentifier(target);
			else
			{
				try
				{
					playerName = Parser.parseUUID(playerName).eval(sender);
				} catch (final EntityNotFoundException e)
				{
					throw e;
				} catch (final CommandException e)
				{
				}
			}
			
			ret = new ChatComponentScore(playerName, scoreToProc.func_179994_h());
			((ChatComponentScore) ret).func_179997_b(scoreToProc.getUnformattedTextForChat());
		}
		else if (toProcess instanceof ChatComponentSelector)
		{
			final String toParse = ((ChatComponentSelector) toProcess).func_179992_g();
			
			try
			{
				final List<Entity> entityList = Parser.parseEntityList(toParse).eval(sender);
				
				final List<IChatComponent> toJoin = new ArrayList<>(entityList.size());
				
				for (final Entity entity : entityList)
					toJoin.add(entity.getDisplayName());
				
				ret = ParsingUtilities.join(toJoin);
			} catch (final CommandException e)
			{
				ret = new ChatComponentText("");
			}
		}
		else if (toProcess instanceof ChatComponentText)
		{
			ret = new ChatComponentText(((ChatComponentText) toProcess).getChatComponentText_TextValue());
		}
		else
		{
			if (!(toProcess instanceof ChatComponentTranslation))
			{
				return toProcess;
			}
			
			final Object[] var8 = ((ChatComponentTranslation) toProcess).getFormatArgs();
			
			for (int var10 = 0; var10 < var8.length; ++var10)
			{
				final Object var12 = var8[var10];
				
				if (var12 instanceof IChatComponent)
				{
					var8[var10] = func_179985_a(sender, (IChatComponent) var12, target);
				}
			}
			
			ret = new ChatComponentTranslation(((ChatComponentTranslation) toProcess).getKey(), var8);
		}
		
		final ChatStyle var9 = toProcess.getChatStyle();
		
		if (var9 != null)
		{
			ret.setChatStyle(var9.createShallowCopy());
		}
		
		final Iterator var11 = toProcess.getSiblings().iterator();
		
		while (var11.hasNext())
		{
			final IChatComponent var13 = (IChatComponent) var11.next();
			ret.appendSibling(func_179985_a(sender, var13, target));
		}
		
		return ret;
	}
}
