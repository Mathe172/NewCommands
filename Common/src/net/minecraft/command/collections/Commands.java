package net.minecraft.command.collections;

import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.command.CommandClearInventory;
import net.minecraft.command.CommandCompare;
import net.minecraft.command.CommandDebug;
import net.minecraft.command.CommandDefaultGameMode;
import net.minecraft.command.CommandDifficulty;
import net.minecraft.command.CommandEffect;
import net.minecraft.command.CommandEnchant;
import net.minecraft.command.CommandGive;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.CommandPlaySound;
import net.minecraft.command.CommandServerKick;
import net.minecraft.command.CommandSetPlayerTimeout;
import net.minecraft.command.CommandSetSpawnpoint;
import net.minecraft.command.CommandShowSeed;
import net.minecraft.command.CommandSpreadPlayers;
import net.minecraft.command.CommandTime;
import net.minecraft.command.CommandTitle;
import net.minecraft.command.CommandToggleDownfall;
import net.minecraft.command.CommandTrigger;
import net.minecraft.command.CommandWeather;
import net.minecraft.command.CommandWorldBorder;
import net.minecraft.command.CommandXP;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.arg.PrimitiveParameter;
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
import net.minecraft.command.commands.CommandHelp;
import net.minecraft.command.commands.CommandIf;
import net.minecraft.command.commands.CommandKill;
import net.minecraft.command.commands.CommandLegacy;
import net.minecraft.command.commands.CommandMessageRaw;
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
import net.minecraft.command.common.CommandReplaceItem;
import net.minecraft.command.construction.CommandConstructable;
import net.minecraft.command.construction.CommandDescriptorDefault.CParserData;
import net.minecraft.command.construction.ICommandConstructor.CPU;
import net.minecraft.command.construction.RegistrationHelper;
import net.minecraft.command.descriptors.CommandDescriptor;
import net.minecraft.command.server.CommandAchievement;
import net.minecraft.command.server.CommandBanIp;
import net.minecraft.command.server.CommandBanPlayer;
import net.minecraft.command.server.CommandEmote;
import net.minecraft.command.server.CommandListBans;
import net.minecraft.command.server.CommandListPlayers;
import net.minecraft.command.server.CommandPardonIp;
import net.minecraft.command.server.CommandPardonPlayer;
import net.minecraft.command.server.CommandPublishLocalServer;
import net.minecraft.command.server.CommandSaveAll;
import net.minecraft.command.server.CommandSaveOff;
import net.minecraft.command.server.CommandSaveOn;
import net.minecraft.command.server.CommandSetDefaultSpawnpoint;
import net.minecraft.command.server.CommandTestFor;
import net.minecraft.command.server.CommandTestForBlock;
import net.minecraft.command.server.CommandWhitelist;
import net.minecraft.command.type.custom.ParserLazyString;
import net.minecraft.command.type.custom.ParserName;
import net.minecraft.command.type.custom.ParserSayString;
import net.minecraft.command.type.custom.TypeAlternatives;
import net.minecraft.command.type.custom.TypeBlockReplaceFilter;
import net.minecraft.command.type.custom.TypeCommandPath;
import net.minecraft.command.type.custom.TypeLabelDeclaration;
import net.minecraft.command.type.custom.TypeLabelDeclaration.ProvideLastLabel;
import net.minecraft.command.type.custom.TypeNull;
import net.minecraft.command.type.custom.TypeScoreObjective;
import net.minecraft.command.type.custom.TypeSnapshot;
import net.minecraft.command.type.custom.TypeStringLiteral;
import net.minecraft.command.type.custom.command.TypeCommand;
import net.minecraft.command.type.custom.coordinate.TypeBlockPos;
import net.minecraft.command.type.custom.coordinate.TypeCoordinate;
import net.minecraft.command.type.custom.coordinate.TypeCoordinates;
import net.minecraft.command.type.custom.json.TypeJsonText;
import net.minecraft.command.type.custom.nbt.TypeNBTArg;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumParticleTypes;

public final class Commands extends RegistrationHelper
{
	private Commands()
	{
	}
	
	public static final Pair<Set<String>, CommandDescriptor<CParserData>> helpDescriptor;
	
	static
	{
		final CPU helpConstructible = command("help", CommandHelp.constructable, level(0), usage("commands.help.usage"))
			.optional(TypeCommandPath.parser);
		
		helpDescriptor = helpConstructible.baseCommands().iterator().next().construct(null); //don't ask...
	}
	
	public static final void init()
	{
		//TODO: Constructable-identifiers
		
		register(command("for", CommandFor.ignoreErrorConstructable, level(2), usage("commands.for.usage"))
			.optional(
				command("safe", CommandFor.constructable))
			.then(TypeLabelDeclaration.intNonInstant)
			.then(Parsers.integer)
			.then(Parsers.integer)
			.optional(
				command("step")
					.then(Parsers.integer))
			.then(ProvideLastLabel.parser)
			.then(TypeCommand.parserSingleCmd));
		
		register(command("say", CommandSay.constructable, level(0), usage("commands.say.usage"))
			.then(ParserSayString.parser));
		
		register(command("try", CommandTry.constructable, level(0), usage("commands.try.usage"))
			.then(TypeCommand.parserSingleCmd)
			.then(TypeCommand.parserSingleCmd));
		
		register(command("if", CommandIf.constructable, level(0), usage("commands.if.usage"))
			.then(Parsers.bool)
			.then(TypeCommand.parserSingleCmd)
			.optional(
				command("else", CommandIf.constructableElse)
					.then(TypeCommand.parserSingleCmd)));
		
		register(command("break", CommandBreak.constructable, level(0), usage("commands.break.usage")));
		
		register(command("summon", CommandSummon.constructable, level(2), usage("commands.summon.usage"))
			.optional(
				command("label", CommandSummon.labelConstructable)
					.then(TypeLabelDeclaration.entityNonInstant)
			)
			.then(Types.entityID)
			.optional(TypeCoordinates.nonCentered)
			.optional(TypeNBTArg.parserEntity));
		
		register(command("kill", CommandKill.constructable, level(2), usage("commands.kill.usage"))
			.optional(Types.entityList));
		
		register(command("gamemode", CommandGameMode.constructable, level(2), usage("commands.gamemode.usage"))
			.then(CommandGameMode.gamemodeParser)
			.optional(Types.entityList));
		
		register(command("execute", CommandExecuteAt.constructable, level(2), usage("commands.execute.usage"))
			.then(Types.iCmdSenderList)
			.then(new TypeSnapshot<>(TypeCoordinates.shiftNC))
			.optional(command("detect", CommandExecuteAt.constructableDetect)
				.then(TypeCoordinates.shiftNC)
				.then(Types.blockID)
				.then(new TypeSnapshot<>(Parsers.integer))
				.then(new TypeSnapshot<>(TypeNBTArg.parserBlock)))
			.then(TypeCommand.parserSingleCmd));
		
		register(command("activate", CommandActivate.constructable, level(2), usage("commands.activate.usage"))
			.optional(Parsers.integer, CommandActivate.constructableDelayed)
			.optional(TypeBlockPos.parser, CommandActivate.constructablePos)
			.optional(TypeBlockPos.parser, CommandActivate.constructableBox));
		
		register(command("scoreboard", level(2), usage("commands.scoreboard.usage"))
			.sub(
				command("objectives", usage("commands.scoreboard.objectives.usage"))
					.sub(
						command("add", CommandScoreboard.objectivesAdd, usage("commands.scoreboard.objectives.add.usage"))
							.then(ParserName.parser)
							.then(CommandScoreboard.typeCriterion)
							.optional(ParserName.parser),
						command("setdisplay", CommandScoreboard.objectivesSetDisplay, usage("commands.scoreboard.objectives.setdisplay.usage"))
							.then(CommandScoreboard.typeSlot)
							.optional(TypeScoreObjective.type),
						command("list", CommandScoreboard.objectivesList),
						command("remove", CommandScoreboard.objectivesRemove, usage("commands.scoreboard.objectives.remove.usage"))
							.then(TypeScoreObjective.type)),
				command("players", usage("commands.scoreboard.players.usage"))
					.sub(
						command("enable", CommandScoreboard.playersEnable)
							.then(Types.scoreHolderListWC)
							.then(TypeScoreObjective.typeTrigger),
						group(
							command("set", CommandScoreboard.playersSetAddRemove, usage("commands.scoreboard.players.set.usage")),
							command("add", CommandScoreboard.playersSetAddRemove, usage("commands.scoreboard.players.add.usage")),
							command("remove", CommandScoreboard.playersSetAddRemove, usage("commands.scoreboard.players.remove.usage"))
						)
							.then(Types.scoreHolderListWC)
							.then(TypeScoreObjective.typeWriteable)
							.then(Parsers.integer)
							.optional(TypeNBTArg.parserEntity),
						command("operation", CommandScoreboard.playersOperation)
							.then(Types.scoreHolderListWC)
							.then(TypeScoreObjective.typeWriteable)
							.then(CommandScoreboard.typeOperation)
							.then(Types.scoreHolderListWC)
							.then(TypeScoreObjective.type),
						command("test", CommandScoreboard.playersTest)
							.then(Types.scoreHolderListWC)
							.then(TypeScoreObjective.type)
							.then(Parsers.defaultedIntMin)
							.optional(Parsers.defaultedIntMax),
						command("list", CommandScoreboard.playersList, usage("commands.scoreboard.players.list.usage"))
							.optional(Types.scoreHolderListWC),
						command("reset", CommandScoreboard.playersReset, usage("commands.scoreboard.players.reset.usage"))
							.then(Types.scoreHolderListWC)
							.optional(TypeScoreObjective.type)),
				command("teams", usage("commands.scoreboard.teams.usage"))
					.sub(
						command("add", CommandScoreboard.teamsAdd, usage("commands.scoreboard.teams.add.usage"))
							.then(ParserName.parser)
							.optional(ParserName.parser),
						command("option", CommandScoreboard.teamsOption, usage("commands.scoreboard.teams.option.usage"))
							.then(Types.teamName)
							.sub(
								command("color")
									.optional(CommandScoreboard.typeColor),
								command("deathMessageVisibility", "nameTagVisibility")
									.optional(CommandScoreboard.typeVisibility),
								command("friendlyFire", "seeFriendlyInvisibles")
									.optional(Types.stringBoolean)),
						command("join", CommandScoreboard.teamsJoin, usage("commands.scoreboard.teams.join.usage"))
							.then(Types.teamName)
							.optional(Types.scoreHolderListWC),
						command("leave", CommandScoreboard.teamsLeave, usage("commands.scoreboard.teams.leave.usage"))
							.optional(Types.scoreHolderListWC),
						command("empty", CommandScoreboard.teamsEmpty, usage("commands.scoreboard.teams.empty.usage"))
							.then(Types.teamName),
						command("list", CommandScoreboard.teamsList, usage("commands.scoreboard.teams.list.usage"))
							.optional(Types.teamName),
						command("remove", CommandScoreboard.teamsRemove, usage("commands.scoreboard.teams.remove.usage"))
							.then(Types.teamName)
					)));
		
		register(command("gamerule", CommandGameRule.constructable, level(2), usage("commands.gamerule.usage"))
			.optional(CommandGameRule.typeRules)
			.optional(Types.stringBoolean));
		
		register(command("entitydata", CommandEntityData.constructable, level(2), usage("commands.entitydata.usage"))
			.then(Types.entityList)
			.then(TypeNBTArg.parserEntity));
		
		register(command("tp", CommandTeleport.constructable, level(2), usage("commands.tp.usage"))
			.then(new TypeSnapshot<>(Types.entityList))
			.then(new TypeAlternatives<>(
				TypeCoordinates.shiftC,
				Types.ICmdSender,
				TypeNull.parser))
			.optional(TypeCoordinate.typeShiftNC)
			.then(TypeCoordinate.typeShiftNC));
		
		register(command("blockdata", CommandBlockData.constructable, level(2), usage("commands.blockdata.usage"))
			.then(TypeBlockPos.parser)
			.then(TypeNBTArg.parserBlock));
		
		register(command("stats", level(2), usage("commands.stats.usage"))
			.sub(command("entity")
				.then(Types.entityList),
				command("block")
					.then(TypeBlockPos.parser))
			.sub(
				command("clear", CommandStats.constructableClear, CommandStats.usageClear)
					.then(CommandStats.typeStatName),
				command("set", CommandStats.constructableSet, CommandStats.usageSet)
					.then(CommandStats.typeStatName)
					.then(ParserLazyString.parser)
					.then(TypeScoreObjective.typeWriteableString)));
		
		register(command("setblock", CommandSetBlock.constructable, level(2), usage("commands.setblock.usage"))
			.then(TypeBlockPos.parser)
			.then(Types.blockID)
			.optional(Parsers.integer)
			.optional(
				command("replace", "destroy", "keep")
					.optional(TypeNBTArg.parserBlock)));
		
		register(command("fill", CommandFill.constructable, level(2), usage("commands.fill.usage"))
			.optional(
				command("fast", CommandFill.fastConstructable))
			.then(TypeBlockPos.parser)
			.then(TypeBlockPos.parser)
			.then(Types.blockID)
			.optional(Parsers.integer)
			.optional(
				command("replace")
					.then(TypeBlockReplaceFilter.parser),
				command("destroy", "keep", "hollow", "outline")
					.optional(TypeNBTArg.parserBlock)));
		
		register(command("clone", CommandClone.constructable, level(2), usage("commands.clone.usage"))
			.optional(
				command("fast", CommandClone.constructableFast))
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
					.optional(Parsers.integer)));
		
		register(command("explain", alias("dafuq"), CommandExplain.constructable, level(0), usage("commands.explain.usage"))
			.optional(
				command("all", alias("extended"), CommandExplain.constructableAll))
			.optional(
				command("", level(2))
					.then(
						TypeBlockPos.parser)));
		
		register(command("particle", CommandParticle.constructable, level(2), usage("commands.particle.usage"))
			.sub(
				command("")
					.then(new TypeStringLiteral(EnumParticleTypes.func_179349_a())),
				command("blockcrack", "blockdust")
					.then(Types.blockID)
					.then(Parsers.integer),
				command("iconcrack")
					.then(Types.itemID)
					.then(Parsers.integer))
			.then(TypeCoordinates.shiftNC)
			.then(Parsers.dbl)
			.then(Parsers.dbl)
			.then(Parsers.dbl)
			.then(Parsers.dbl)
			.optional(Parsers.integer)
			.optional(new TypeStringLiteral("force", "normal"))
			.optional(Types.iCmdSenderList));
		
		register(command("move", CommandMove.constructable, level(2), usage("commands.move.usage"))
			.then(Types.entityList)
			.optional(TypeCoordinates.shiftNC));
		
		register(command("target", CommandTarget.constructable, level(2), usage("commands.target.usage"))
			.then(Types.entityList)
			.optional(Types.entity));
		
		register(command("useitem", CommandUseItem.constructable, level(2), usage("commands.useitem.usage"))
			.then(Types.entityList));
		
		register(command("tellraw", CommandMessageRaw.constructable, level(2), usage("commands.tellraw.usage"))
			.then(Types.entityList)
			.then(TypeJsonText.parser));
		
		register(command("nop", new CommandConstructable()
		{
			private final PrimitiveParameter<Integer> nopArg = new PrimitiveParameter<>(1);
			
			@Override
			public CommandArg<Integer> construct(final CParserData data) throws SyntaxErrorException
			{
				return this.nopArg;
			}
		}, level(0), usage("/nop")));
		
		register(command("legacy", alias("old"), CommandLegacy.constructable, level(0), usage("commands.legacy.usage"))
			.then(Types.commandName));
		
		CommandDescriptor.registerCommand(helpDescriptor);
		
		if (MinecraftServer.getServer().isDedicatedServer())
		{
			register(command("op", CommandOp.constructable, level(3), usage("commands.op.usage"))
				.then(Types.nonOppedOnline));
			
			register(command("deop", CommandDeOp.constructable, level(3), usage("commands.deop.usage"))
				.then(Types.opName));
			
			register(command("stop", CommandStop.constructable, level(4), usage("commands.stop.usage")));
			
			CommandHandler.registerCommand(new CommandBanIp());
			CommandHandler.registerCommand(new CommandBanPlayer());
			CommandHandler.registerCommand(new CommandListBans());
			CommandHandler.registerCommand(new CommandListPlayers());
			CommandHandler.registerCommand(new CommandPardonIp());
			CommandHandler.registerCommand(new CommandPardonPlayer());
			CommandHandler.registerCommand(new CommandWhitelist());
		}
		else
			CommandHandler.registerCommand(new CommandPublishLocalServer());
		
		CommandHandler.registerCommand(new CommandAchievement());
		CommandHandler.registerCommand(new CommandClearInventory());
		CommandHandler.registerCommand(new CommandCompare());
		CommandHandler.registerCommand(new CommandDebug());
		CommandHandler.registerCommand(new CommandDefaultGameMode());
		CommandHandler.registerCommand(new CommandDifficulty());
		CommandHandler.registerCommand(new CommandEffect());
		CommandHandler.registerCommand(new CommandEmote());
		CommandHandler.registerCommand(new CommandEnchant());
		CommandHandler.registerCommand(new CommandGive());
		CommandHandler.registerCommand(new CommandPlaySound());
		CommandHandler.registerCommand(new CommandReplaceItem());
		CommandHandler.registerCommand(new CommandSaveAll());
		CommandHandler.registerCommand(new CommandSaveOff());
		CommandHandler.registerCommand(new CommandSaveOn());
		CommandHandler.registerCommand(new CommandServerKick());
		CommandHandler.registerCommand(new CommandSetDefaultSpawnpoint());
		CommandHandler.registerCommand(new CommandSetPlayerTimeout());
		CommandHandler.registerCommand(new CommandSetSpawnpoint());
		CommandHandler.registerCommand(new CommandShowSeed());
		CommandHandler.registerCommand(new CommandSpreadPlayers());
		CommandHandler.registerCommand(new CommandTestFor());
		CommandHandler.registerCommand(new CommandTestForBlock());
		CommandHandler.registerCommand(new CommandTime());
		CommandHandler.registerCommand(new CommandTitle());
		CommandHandler.registerCommand(new CommandToggleDownfall());
		CommandHandler.registerCommand(new CommandTrigger());
		CommandHandler.registerCommand(new CommandWeather());
		CommandHandler.registerCommand(new CommandWorldBorder());
		CommandHandler.registerCommand(new CommandXP());
	}
}
