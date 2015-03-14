package net.minecraft.command;

import java.util.Iterator;

import net.minecraft.command.commands.CommandFor;
import net.minecraft.command.commands.CommandSay;
import net.minecraft.command.commands.CommandSummon;
import net.minecraft.command.commands.dedicated.CommandDeOp;
import net.minecraft.command.commands.dedicated.CommandOp;
import net.minecraft.command.commands.dedicated.CommandStop;
import net.minecraft.command.construction.CommandConstructor;
import net.minecraft.command.construction.CommandConstructorU;
import net.minecraft.command.construction.NBTConstructor;
import net.minecraft.command.construction.NBTConstructorList;
import net.minecraft.command.construction.SelectorConstructor;
import net.minecraft.command.descriptors.CommandDescriptor;
import net.minecraft.command.descriptors.SelectorDescriptor;
import net.minecraft.command.selectors.SelectorSelf;
import net.minecraft.command.selectors.SelectorTiming;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.command.type.custom.CounterType;
import net.minecraft.command.type.custom.TypeCommand;
import net.minecraft.command.type.custom.TypeEntityId;
import net.minecraft.command.type.custom.TypeIDs;
import net.minecraft.command.type.custom.TypeSayString;
import net.minecraft.command.type.custom.Types;
import net.minecraft.command.type.custom.coordinate.ParserCoordinates;
import net.minecraft.command.type.custom.nbt.NBTArg;
import net.minecraft.command.type.custom.nbt.NBTDescriptor;
import net.minecraft.command.type.custom.nbt.NBTUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;

public class ServerCommandManager extends CommandHandler implements IAdminCommand
{
	@SuppressWarnings("unused")
	private static final String __OBFID = "CL_00000922";
	
	public ServerCommandManager()
	{
		TypeIDs.initConverters();
		
		CommandBase.setAdminCommander(this);
		
		CommandDescriptor.registerCommand(
			new CommandConstructorU(IPermission.PermissionLevel2, "commands.for.usage", "for")
				.optional(new CommandConstructor("safe"), CommandFor.constructable)
				.then(CounterType.counterType)
				.then(TypeCommand.parser)
				.executes(CommandFor.ignoreErrorConstructable));
		CommandDescriptor.registerCommand(
			new CommandConstructorU("commands.say.usage", "say")
				.then(TypeSayString.sayStringType)
				.executes(CommandSay.constructable));
		CommandDescriptor.registerCommand(
			new CommandConstructorU(IPermission.PermissionLevel2, "commands.summon.usage", "summon")
				.then(TypeEntityId.type)
				.optional(ParserCoordinates.centered)
				.optional(NBTArg.nbtArg)
				.executes(CommandSummon.constructable));
		
		SelectorDescriptor.registerSelector("s",
			new SelectorConstructor(TypeIDs.ICmdSender)
				.construct(SelectorSelf.constructable));
		
		SelectorDescriptor.registerSelector("t",
			new SelectorConstructor(TypeIDs.Integer)
				.then("cmd", TypeCommand.parserSingleCmd)
				.construct(SelectorTiming.constructable));
		
		if (MinecraftServer.getServer().isDedicatedServer())
		{
			CommandDescriptor.registerCommand(
				new CommandConstructorU(IPermission.PermissionLevel3, "commands.op.usage", "op")
					.then(Types.nonOppedOnline)
					.executes(CommandOp.constructable));
			CommandDescriptor.registerCommand(
				new CommandConstructorU(IPermission.PermissionLevel3, "commands.deop.usage", "deop")
					.then(Types.opName)
					.executes(CommandDeOp.constructable));
			CommandDescriptor.registerCommand(
				new CommandConstructorU(IPermission.PermissionLevel4, "commands.stop.usage", "stop")
					.executes(CommandStop.constructable));
		}
		
		final NBTConstructor base = NBTUtilities.baseDescriptor;
		
		base.sKey("Air")
			.key("CommandStats", new NBTConstructorList(new NBTConstructor()
				.key("AffectedBlocksName",
					"AffectedBlocksObjective",
					"AffectedEntitiesName",
					"AffectedEntitiesObjective",
					"AffectedItemsName",
					"AffectedItemsObjective",
					"QueryResultName",
					"QueryResultObjective",
					"SuccessCountName",
					"SuccessCountObjective")))
			.key("CustomName",
				"Invulnerable")
			.sKey("CustomNameVisible",
				"Dimension",
				"FallDistance",
				"Fire",
				"id")
			
			.key("Motion", NBTDescriptor.defaultTagList)
			.key("Rotation", NBTDescriptor.defaultTagList)
			.key("Pos", NBTDescriptor.defaultTagList)
			
			.sKey("OnGround",
				"PortalCooldown")
			.key("Riding", base)
			.sKey("UUID",
				"UUIDLeast",
				"UUIDMost")
			.sKey("AbsorptionAmount")
			
			.key("ActiveEffects",
				new NBTConstructorList(new NBTConstructor()
					.key("Ambient",
						"Amplifier",
						"Duration",
						"Id")))
			.sKey("AttackTime")
			.key("Attributes", new NBTConstructorList(new NBTConstructor()
				.key("Name",
					"Base")
				.key("Modifiers", new NBTConstructorList(new NBTConstructor()
					.key("Amount",
						"Name",
						"Operation")
					.sKey("UUIDLeast",
						"UUIDMost")))))
			
			.sKey("CanPickUpLoot",
				"DeathTime",
				"DropChances",
				"Equipment",
				"HealF",
				"Health",
				"HurtByTimestamp",
				"HurtTime")
			.sKey("Leash", new NBTConstructor()
				.key("UUIDLeast",
					"UUIDMost",
					"X",
					"Y",
					"Z"))
			.sKey("Leashed")
			
			.key("NoAI",
				"PersistenceRequired",
				"Silent");
	}
	
	@Override
	public void notifyOperators(final ICommandSender sender, final IPermission permission, final int p_152372_3_, final String msgFormat, final Object... msgParams)
	{
		final MinecraftServer var7 = MinecraftServer.getServer();
		
		final ChatComponentTranslation var8 = new ChatComponentTranslation("chat.type.admin", new Object[] { sender.getName(), new ChatComponentTranslation(msgFormat, msgParams) });
		var8.getChatStyle().setColor(EnumChatFormatting.GRAY);
		var8.getChatStyle().setItalic(Boolean.valueOf(true));
		
		if (sender.sendCommandFeedback())
		{
			@SuppressWarnings("rawtypes")
			final Iterator var9 = var7.getConfigurationManager().playerEntityList.iterator();
			
			while (var9.hasNext())
			{
				final EntityPlayer var10 = (EntityPlayer) var9.next();
				
				if (var10 != sender && var7.getConfigurationManager().canSendCommands(var10.getGameProfile()) && permission.canCommandSenderUseCommand(sender))
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
		
		if ((p_152372_3_ & 1) != 1 && var11)
		{
			sender.addChatMessage(new ChatComponentTranslation(msgFormat, msgParams));
		}
	}
}
