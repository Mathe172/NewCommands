package net.minecraft.command;

import java.util.Iterator;

import net.minecraft.command.collections.Commands;
import net.minecraft.command.collections.Matchers;
import net.minecraft.command.collections.NBTDescriptors;
import net.minecraft.command.collections.Operators;
import net.minecraft.command.collections.Relations;
import net.minecraft.command.collections.Selectors;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.commands.CommandScoreboard;
import net.minecraft.command.descriptors.CommandDescriptor;
import net.minecraft.command.descriptors.OperatorDescriptor;
import net.minecraft.command.descriptors.SelectorDescriptor;
import net.minecraft.command.parser.MatcherRegistry;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.command.type.management.Convertable;
import net.minecraft.command.type.management.TypeID;
import net.minecraft.command.type.management.relations.Relation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;

public final class ServerCommandManager extends CommandHandler
{
	@SuppressWarnings("unused")
	private static final String __OBFID = "CL_00000922";
	
	private ServerCommandManager()
	{
	}
	
	public static void init()
	{
		Relation.clearAll();
		Convertable.clearAll();
		TypeID.clearAll();
		CommandDescriptor.clear();
		SelectorDescriptor.clear();
		OperatorDescriptor.clear();
		
		MatcherRegistry.clear();
		
		Relations.init();
		TypeIDs.init();
		
		Matchers.init();
		
		Commands.init();
		Selectors.init();
		Operators.init();
		
		// TODO:.........
		for (final Object criterion : IScoreObjectiveCriteria.INSTANCES.keySet())
			CommandScoreboard.completerCriterion.registerResource(criterion.toString());
	}
	
	static
	{
		NBTDescriptors.init();
	}
	
	public static void notifyOperators(final ICommandSender sender, final int flags, final String msgFormat, final Object... msgParams)
	{
		final MinecraftServer var7 = MinecraftServer.getServer();
		
		final ChatComponentTranslation var8 = new ChatComponentTranslation("chat.type.admin", sender.getName(), new ChatComponentTranslation(msgFormat, msgParams));
		var8.getChatStyle().setColor(EnumChatFormatting.GRAY);
		var8.getChatStyle().setItalic(Boolean.valueOf(true));
		
		if (sender.sendCommandFeedback())
		{
			@SuppressWarnings("rawtypes")
			final Iterator var9 = var7.getConfigurationManager().playerEntityList.iterator();
			
			while (var9.hasNext())
			{
				final EntityPlayer var10 = (EntityPlayer) var9.next();
				
				if (var10 != sender && var7.getConfigurationManager().canSendCommands(var10.getGameProfile()))
				{
					var10.addChatMessage(var8);
				}
			}
		}
		
		if (sender != var7 && var7.worldServers[0].getGameRules().getGameRuleBooleanValue("logAdminCommands"))
		{
			var7.addChatMessage(var8);
		}
		
		boolean var11 = var7.worldServers[0].getGameRules().getGameRuleBooleanValue("sendCommandFeedback");
		
		if (sender instanceof CommandBlockLogic)
		{
			var11 = ((CommandBlockLogic) sender).func_175571_m();
		}
		
		if ((flags & 1) != 1 && var11)
		{
			sender.addChatMessage(new ChatComponentTranslation(msgFormat, msgParams));
		}
	}
}
