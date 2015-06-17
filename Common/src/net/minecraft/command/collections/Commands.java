package net.minecraft.command.collections;

import net.minecraft.command.IPermission;
import net.minecraft.command.commands.CommandActivate;
import net.minecraft.command.commands.CommandBlockData;
import net.minecraft.command.commands.CommandBreak;
import net.minecraft.command.commands.CommandClone;
import net.minecraft.command.commands.CommandEntityData;
import net.minecraft.command.commands.CommandExecuteAt;
import net.minecraft.command.commands.CommandExplain;
import net.minecraft.command.commands.CommandFill;
import net.minecraft.command.commands.CommandFor;
import net.minecraft.command.commands.CommandGameMode;
import net.minecraft.command.commands.CommandGameRule;
import net.minecraft.command.commands.CommandIf;
import net.minecraft.command.commands.CommandKill;
import net.minecraft.command.commands.CommandMove;
import net.minecraft.command.commands.CommandParticle;
import net.minecraft.command.commands.CommandSay;
import net.minecraft.command.commands.CommandScoreboard;
import net.minecraft.command.commands.CommandSetBlock;
import net.minecraft.command.commands.CommandStats;
import net.minecraft.command.commands.CommandSummon;
import net.minecraft.command.commands.CommandTarget;
import net.minecraft.command.commands.CommandTeleport;
import net.minecraft.command.commands.CommandTry;
import net.minecraft.command.commands.CommandUseItem;
import net.minecraft.command.commands.dedicated.CommandDeOp;
import net.minecraft.command.commands.dedicated.CommandOp;
import net.minecraft.command.commands.dedicated.CommandStop;
import net.minecraft.command.construction.RegistrationHelper;
import net.minecraft.command.type.custom.ParserDouble;
import net.minecraft.command.type.custom.ParserInt;
import net.minecraft.command.type.custom.ParserLazyString;
import net.minecraft.command.type.custom.ParserName;
import net.minecraft.command.type.custom.TypeAlternatives;
import net.minecraft.command.type.custom.TypeBlockReplaceFilter;
import net.minecraft.command.type.custom.TypeBoolean;
import net.minecraft.command.type.custom.TypeLabelDeclaration;
import net.minecraft.command.type.custom.TypeLabelDeclaration.ProvideLastLabel;
import net.minecraft.command.type.custom.TypeNull;
import net.minecraft.command.type.custom.TypeSayString;
import net.minecraft.command.type.custom.TypeScoreObjective;
import net.minecraft.command.type.custom.TypeSnapshot;
import net.minecraft.command.type.custom.TypeStringLiteral;
import net.minecraft.command.type.custom.command.TypeCommand;
import net.minecraft.command.type.custom.coordinate.TypeBlockPos;
import net.minecraft.command.type.custom.coordinate.TypeCoordinate;
import net.minecraft.command.type.custom.coordinate.TypeCoordinates;
import net.minecraft.command.type.custom.nbt.TypeNBTArg;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumParticleTypes;

public final class Commands extends RegistrationHelper
{
	private Commands()
	{
	}
	
	public static final void init()
	{
		register(command(CommandFor.ignoreErrorConstructable, IPermission.level2, usage("commands.for.usage"), "for")
			.optional(
				command(CommandFor.constructable, "safe"))
			.then(TypeLabelDeclaration.intNonInstant)
			.then(ParserInt.parser)
			.then(ParserInt.parser)
			.optional(
				command("step")
					.then(ParserInt.parser))
			.then(ProvideLastLabel.parser)
			.then(TypeCommand.parserSingleCmd));
		
		register(command(CommandSay.constructable, IPermission.unrestricted, usage("commands.say.usage"), "say")
			.then(TypeSayString.sayStringType));
		
		register(command(CommandTry.constructable, IPermission.unrestricted, usage("commands.try.usage"), "try")
			.then(TypeCommand.parserSingleCmd)
			.then(TypeCommand.parserSingleCmd));
		
		register(command(CommandIf.constructable, IPermission.unrestricted, usage("commands.if.usage"), "if")
			.then(TypeBoolean.parser)
			.then(TypeCommand.parserSingleCmd)
			.optional(
				command(CommandIf.constructableElse, "else")
					.then(TypeCommand.parserSingleCmd)));
		
		register(command(CommandBreak.constructable, IPermission.unrestricted, usage("commands.break.usage"), "break"));
		
		register(command(CommandSummon.constructable, IPermission.level2, usage("commands.summon.usage"), "summon")
			.optional(
				command(CommandSummon.labelConstructable, "label")
					.then(TypeLabelDeclaration.entityNonInstant)
			)
			.then(Types.entityID)
			.optional(TypeCoordinates.nonCentered)
			.optional(TypeNBTArg.parserEntity));
		
		register(command(CommandKill.constructable, IPermission.level2, usage("commands.kill.usage"), "kill")
			.optional(Types.entityList));
		
		register(command(CommandGameMode.constructable, IPermission.level2, usage("commands.gamemode.usage"), "gamemode")
			.then(CommandGameMode.gamemodeParser)
			.optional(Types.entityList));
		
		register(command(CommandExecuteAt.constructable, IPermission.level2, usage("commands.execute.usage"), "execute")
			.then(Types.iCmdSenderList)
			.then(new TypeSnapshot<>(TypeCoordinates.shiftNC))
			.optional(command(CommandExecuteAt.constructableDetect, "detect")
				.then(TypeCoordinates.shiftNC)
				.then(Types.blockID)
				.then(new TypeSnapshot<>(ParserInt.parser))
				.then(new TypeSnapshot<>(TypeNBTArg.parserBlock)))
			.then(TypeCommand.parserSingleCmd));
		
		register(command(CommandActivate.constructable, IPermission.level2, usage("commands.activate.usage"), "activate")
			.optional(ParserInt.parser, CommandActivate.constructableDelayed)
			.optional(TypeBlockPos.parser, CommandActivate.constructablePos)
			.optional(TypeBlockPos.parser, CommandActivate.constructableBox));
		
		register(command(IPermission.level2, usage("commands.scoreboard.usage"), "scoreboard")
			.sub(
				command(usage("commands.scoreboard.objectives.usage"), "objectives")
					.sub(
						command(CommandScoreboard.objectivesAdd, usage("commands.scoreboard.objectives.add.usage"), "add")
							.then(ParserName.parser)
							.then(CommandScoreboard.typeCriterion)
							.optional(ParserName.parser),
						command(CommandScoreboard.objectivesSetDisplay, usage("commands.scoreboard.objectives.setdisplay.usage"), "setdisplay")
							.then(CommandScoreboard.typeSlot)
							.optional(TypeScoreObjective.type),
						command(CommandScoreboard.objectivesList, "list"),
						command(CommandScoreboard.objectivesRemove, usage("commands.scoreboard.objectives.remove.usage"), "remove")
							.then(TypeScoreObjective.type)),
				command(usage("commands.scoreboard.players.usage"), "players")
					.sub(
						command(CommandScoreboard.playersEnable, "enable")
							.then(Types.scoreHolderListWC)
							.then(TypeScoreObjective.parserTrigger),
						group(
							command(CommandScoreboard.playersSetAddRemove, usage("commands.scoreboard.players.set.usage"), "set"),
							command(CommandScoreboard.playersSetAddRemove, usage("commands.scoreboard.players.add.usage"), "add"),
							command(CommandScoreboard.playersSetAddRemove, usage("commands.scoreboard.players.remove.usage"), "remove")
						)
							.then(Types.scoreHolderListWC)
							.then(TypeScoreObjective.typeWriteable)
							.then(ParserInt.parser)
							.optional(TypeNBTArg.parserEntity),
						command(CommandScoreboard.playersOperation, "operation")
							.then(Types.scoreHolderListWC)
							.then(TypeScoreObjective.typeWriteable)
							.then(CommandScoreboard.typeOperation)
							.then(Types.scoreHolderListWC)
							.then(TypeScoreObjective.type),
						command(CommandScoreboard.playersTest, "test")
							.then(Types.scoreHolderListWC)
							.then(TypeScoreObjective.type)
							.then(ParserInt.Defaulted.parserMin)
							.optional(ParserInt.Defaulted.parserMax),
						command(CommandScoreboard.playersList, usage("commands.scoreboard.players.list.usage"), "list")
							.optional(Types.scoreHolderListWC),
						command(CommandScoreboard.playersReset, usage("commands.scoreboard.players.reset.usage"), "reset")
							.then(Types.scoreHolderListWC)
							.optional(TypeScoreObjective.type)),
				command(usage("commands.scoreboard.teams.usage"), "teams")
					.sub(
						command(CommandScoreboard.teamsAdd, usage("commands.scoreboard.teams.add.usage"), "add")
							.then(ParserName.parser)
							.optional(ParserName.parser),
						command(CommandScoreboard.teamsOption, usage("commands.scoreboard.teams.option.usage"), "option")
							.then(Types.teamName)
							.sub(
								command("color")
									.optional(CommandScoreboard.typeColor),
								command("deathMessageVisibility", "nameTagVisibility")
									.optional(CommandScoreboard.typeVisibility),
								command("friendlyFire", "seeFriendlyInvisibles")
									.optional(Types.stringBoolean)),
						command(CommandScoreboard.teamsJoin, usage("commands.scoreboard.teams.join.usage"), "join")
							.then(Types.teamName)
							.optional(Types.scoreHolderListWC),
						command(CommandScoreboard.teamsLeave, usage("commands.scoreboard.teams.leave.usage"), "leave")
							.optional(Types.scoreHolderListWC),
						command(CommandScoreboard.teamsEmpty, usage("commands.scoreboard.teams.empty.usage"), "empty")
							.then(Types.teamName),
						command(CommandScoreboard.teamsList, usage("commands.scoreboard.teams.list.usage"), "list")
							.optional(Types.teamName),
						command(CommandScoreboard.teamsRemove, usage("commands.scoreboard.teams.remove.usage"), "remove")
							.then(Types.teamName)
					)));
		
		register(command(CommandGameRule.constructable, IPermission.level2, usage("commands.gamerule.usage"), "gamerule")
			.optional(CommandGameRule.typeRules)
			.optional(Types.stringBoolean));
		
		register(command(CommandEntityData.constructable, IPermission.level2, usage("commands.entitydata.usage"), "entitydata")
			.then(Types.entityList)
			.then(TypeNBTArg.parserEntity));
		
		register(command(CommandTeleport.constructable, IPermission.level2, usage("commands.tp.usage"), "tp")
			.then(new TypeSnapshot<>(Types.entityList))
			.then(new TypeAlternatives<>(
				TypeCoordinates.shiftC,
				Types.ICmdSender,
				TypeNull.parser))
			.optional(TypeCoordinate.typeShiftNC)
			.then(TypeCoordinate.typeShiftNC));
		
		register(command(CommandBlockData.constructable, IPermission.level2, usage("commands.blockdata.usage"), "blockdata")
			.then(TypeBlockPos.parser)
			.then(TypeNBTArg.parserBlock));
		
		register(command(IPermission.level2, usage("commands.stats.usage"), "stats")
			.sub(command("entity")
				.then(Types.entityList),
				command("block")
					.then(TypeBlockPos.parser))
			.sub(
				command(CommandStats.constructableClear, CommandStats.usageClear, "clear")
					.then(CommandStats.typeStatName),
				command(CommandStats.constructableSet, CommandStats.usageSet, "set")
					.then(CommandStats.typeStatName)
					.then(ParserLazyString.parser)
					.then(TypeScoreObjective.typeWriteableString)));
		
		register(command(CommandSetBlock.constructable, IPermission.level2, usage("commands.setblock.usage"), "setblock")
			.then(TypeBlockPos.parser)
			.then(Types.blockID)
			.optional(ParserInt.parser)
			.optional(
				command("replace", "destroy", "keep")
					.optional(TypeNBTArg.parserBlock)));
		
		register(command(CommandFill.constructable, IPermission.level2, usage("commands.fill.usage"), "fill")
			.optional(
				command(CommandFill.fastConstructable, "fast"))
			.then(TypeBlockPos.parser)
			.then(TypeBlockPos.parser)
			.then(Types.blockID)
			.optional(ParserInt.parser)
			.optional(
				command("replace")
					.then(TypeBlockReplaceFilter.parser),
				command("destroy", "keep", "hollow", "outline")
					.optional(TypeNBTArg.parserBlock)));
		
		register(command(CommandClone.constructable, IPermission.level2, usage("commands.clone.usage"), "clone")
			.optional(
				command(CommandClone.constructableFast, "fast"))
			.then(TypeBlockPos.parser)
			.then(TypeBlockPos.parser)
			.then(TypeBlockPos.parser)
			.optional(
				command("replace", "masked")
					.optional(
						command("normal", "force", "move")),
				command("filtered")
					.optional(
						command("normal", "force", "move"))
					.then(Types.blockID)
					.optional(ParserInt.parser)));
		
		register(command(CommandExplain.constructable, IPermission.unrestricted, usage("commands.explain.usage"), "explain", "dafuq")
			.optional(
				command(CommandExplain.constructableAll, "all", "extended"))
			.optional(
				command(IPermission.level2, "")
					.then(
						TypeBlockPos.parser)));
		
		register(command(CommandParticle.constructable, IPermission.level2, usage("commands.particle.usage"), "particle")
			.sub(
				command("")
					.then(new TypeStringLiteral(EnumParticleTypes.func_179349_a())),
				command("blockcrack", "blockdust")
					.then(Types.blockID)
					.then(ParserInt.parser),
				command("iconcrack")
					.then(Types.itemID)
					.then(ParserInt.parser))
			.then(TypeCoordinates.shiftNC)
			.then(ParserDouble.parser)
			.then(ParserDouble.parser)
			.then(ParserDouble.parser)
			.then(ParserDouble.parser)
			.optional(ParserInt.parser)
			.optional(new TypeStringLiteral("force", "normal"))
			.optional(Types.iCmdSenderList));
		
		register(command(CommandMove.constructable, IPermission.level2, usage("commands.move.usage"), "move")
			.then(Types.entityList)
			.optional(TypeCoordinates.shiftNC));
		
		register(command(CommandTarget.constructable, IPermission.level2, usage("commands.target.usage"), "target")
			.then(Types.entityList)
			.optional(Types.entity));
		
		register(command(CommandUseItem.constructable, IPermission.level2, usage("commands.useitem.usage"), "useitem")
			.then(Types.entityList));
		
		if (MinecraftServer.getServer().isDedicatedServer())
		{
			register(command(CommandOp.constructable, IPermission.level3, usage("commands.op.usage"), "op")
				.then(Types.nonOppedOnline));
			
			register(command(CommandDeOp.constructable, IPermission.level3, usage("commands.deop.usage"), "deop")
				.then(Types.opName));
			
			register(command(CommandStop.constructable, IPermission.level4, usage("commands.stop.usage"), "stop"));
		}
	}
}
