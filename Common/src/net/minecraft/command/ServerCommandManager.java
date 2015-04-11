package net.minecraft.command;

import java.util.Iterator;

import net.minecraft.command.commands.CommandActivate;
import net.minecraft.command.commands.CommandExecuteAt;
import net.minecraft.command.commands.CommandFor;
import net.minecraft.command.commands.CommandGameMode;
import net.minecraft.command.commands.CommandKill;
import net.minecraft.command.commands.CommandSay;
import net.minecraft.command.commands.CommandSummon;
import net.minecraft.command.commands.dedicated.CommandDeOp;
import net.minecraft.command.commands.dedicated.CommandOp;
import net.minecraft.command.commands.dedicated.CommandStop;
import net.minecraft.command.construction.CommandConstructor;
import net.minecraft.command.construction.CommandConstructorU;
import net.minecraft.command.construction.SelectorConstructor;
import net.minecraft.command.descriptors.CommandDescriptor;
import net.minecraft.command.descriptors.SelectorDescriptor;
import net.minecraft.command.selectors.PrimitiveWrapper;
import net.minecraft.command.selectors.SelectorNBT;
import net.minecraft.command.selectors.SelectorScore;
import net.minecraft.command.selectors.SelectorSelf;
import net.minecraft.command.selectors.SelectorTiming;
import net.minecraft.command.selectors.entity.SelectorDescriptorEntity;
import net.minecraft.command.selectors.entity.SelectorEntity.SelectorType;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.command.type.custom.CounterType;
import net.minecraft.command.type.custom.NBTDescriptors;
import net.minecraft.command.type.custom.Operators;
import net.minecraft.command.type.custom.ParserInt;
import net.minecraft.command.type.custom.ParserName;
import net.minecraft.command.type.custom.Relations;
import net.minecraft.command.type.custom.TypeAlternatives;
import net.minecraft.command.type.custom.TypeCommand;
import net.minecraft.command.type.custom.TypeIDs;
import net.minecraft.command.type.custom.TypeSayString;
import net.minecraft.command.type.custom.TypeScoreObjective;
import net.minecraft.command.type.custom.TypeSnapshot;
import net.minecraft.command.type.custom.TypeUntypedOperator;
import net.minecraft.command.type.custom.Types;
import net.minecraft.command.type.custom.coordinate.TypeCoordinates;
import net.minecraft.command.type.custom.nbt.TypeNBTArg;
import net.minecraft.command.type.custom.nbt.TypeNBTBase;
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
		Relations.initRelations();
		TypeIDs.init();
		
		CommandBase.setAdminCommander(this);
		
		CommandDescriptor.registerCommand(
			new CommandConstructorU(IPermission.PermissionLevel2, "commands.for.usage", "for")
				.optional(new CommandConstructor("safe"), CommandFor.constructable)
				.then(CounterType.counterType)
				.then(TypeCommand.parserSingleCmd)
				.executes(CommandFor.ignoreErrorConstructable));
		CommandDescriptor.registerCommand(
			new CommandConstructorU("commands.say.usage", "say")
				.then(TypeSayString.sayStringType)
				.executes(CommandSay.constructable));
		CommandDescriptor.registerCommand(
			new CommandConstructorU(IPermission.PermissionLevel2, "commands.summon.usage", "summon")
				.then(Types.type)
				.optional(TypeCoordinates.centered)
				.optional(TypeNBTArg.parserEntity)
				.executes(CommandSummon.constructable));
		CommandDescriptor.registerCommand(
			new CommandConstructorU(IPermission.PermissionLevel2, "commands.kill.usage", "kill")
				.optional(Types.entityList)
				.executes(CommandKill.constructable));
		CommandDescriptor.registerCommand(
			new CommandConstructorU(IPermission.PermissionLevel2, "commands.gamemode.usage", "gamemode")
				.then(CommandGameMode.gamemodeParser)
				.optional(Types.entityList)
				.executes(CommandGameMode.constructable));
		CommandDescriptor.registerCommand(
			new CommandConstructorU(IPermission.PermissionLevel2, "commands.execute.usage", "execute")
				.then(Types.iCmdSenderList)
				.then(new TypeSnapshot<>(TypeCoordinates.nonCentered))
				.optional(new CommandConstructor("detect")
					.then(TypeCoordinates.nonCentered)
					.then(Types.blockID)
					.then(new TypeSnapshot<>(ParserInt.parser))
					.then(new TypeSnapshot<>(TypeNBTArg.parserBlock)), CommandExecuteAt.constructableDetect)
				.then(TypeCommand.parserSingleCmd)
				.executes(CommandExecuteAt.constructable));
		CommandDescriptor.registerCommand(
			new CommandConstructorU(IPermission.PermissionLevel2, "commands.activate.usage", "activate")
				.optional(ParserInt.parser, CommandActivate.constructableDelayed)
				.optional(TypeCoordinates.nonCentered, CommandActivate.constructablePos)
				.optional(TypeCoordinates.nonCentered, CommandActivate.constructableBox)
				.executes(CommandActivate.constructable));
		
		SelectorDescriptor.registerSelector("s",
			new SelectorConstructor(TypeIDs.ICmdSender)
				.construct(SelectorSelf.constructable));
		
		SelectorDescriptor.registerSelector("t",
			new SelectorConstructor(TypeIDs.Integer)
				.then("cmd", TypeCommand.parserSingleCmd)
				.construct(SelectorTiming.constructable));
		
		SelectorDescriptor.registerSelector("c",
			new SelectorConstructor(TypeIDs.Double, TypeIDs.Integer)
				.then(TypeUntypedOperator.parser)
				.construct(PrimitiveWrapper.constructable));
		
		SelectorDescriptor.registerSelector("o",
			new SelectorConstructor(TypeIDs.ScoreObjective)
				.then(TypeScoreObjective.parser)
				.construct(PrimitiveWrapper.constructable));
		
		SelectorDescriptor.registerSelector("sc",
			new SelectorConstructor(TypeIDs.Integer)
				.then("objective", TypeScoreObjective.parser)
				.then("target", Types.UUID)
				.construct(SelectorScore.constructable));
		
		SelectorDescriptor.registerSelector("n",
			new SelectorConstructor(TypeIDs.NBTBase)
				.then(new TypeAlternatives(
					TypeCoordinates.nonCentered,
					Types.entity,
					TypeNBTBase.parserDefault,
					ParserName.parser))
				.then(ParserName.parser)
				.construct(SelectorNBT.constructable));
		
		SelectorDescriptor.registerSelector("p", new SelectorDescriptorEntity(SelectorType.p));
		SelectorDescriptor.registerSelector("a", new SelectorDescriptorEntity(SelectorType.a));
		SelectorDescriptor.registerSelector("r", new SelectorDescriptorEntity(SelectorType.r));
		SelectorDescriptor.registerSelector("e", new SelectorDescriptorEntity(SelectorType.e));
		
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
		Operators.init();
	}
	
	static
	{
		NBTDescriptors.init();
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
