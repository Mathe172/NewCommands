package net.minecraft.command.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.CommandResultStats.Type;
import net.minecraft.command.EntityNotFoundException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.MatcherRegistry;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.construction.CommandConstructable;
import net.minecraft.command.descriptors.CommandDescriptor.ParserData;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.base.CompoundType;
import net.minecraft.command.type.custom.CompleterResourcePath;
import net.minecraft.command.type.custom.ParserName;
import net.minecraft.command.type.custom.TypeStringLiteral;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;

public final class CommandScoreboard
{
	private CommandScoreboard()
	{
	}
	
	public static final Collection<String> colors = initColors();
	
	@SuppressWarnings("unchecked")
	private static Collection<String> initColors()
	{
		return EnumChatFormatting.getValidValues(true, false);
	}
	
	public static final CompleterResourcePath completerCriterion = new CompleterResourcePath();
	public static final CDataType<String> typeCriterion = new CompoundType<>(new ParserName("objective criterion"), completerCriterion);
	
	public static final CDataType<String> typeSlot = new TypeStringLiteral.Escaped(Scoreboard.func_178821_h());
	
	public static final MatcherRegistry operationMatcher = new MatcherRegistry("\\G\\s*+([-+*/%]=|[<>]|><)");
	
	public static final CDataType<String> typeOperation = new TypeStringLiteral.Escaped(operationMatcher, "+=", "-=", "*=", "/=", "%=", "=", ">", "<", "><");
	
	public static final CDataType<String> typeColor = new TypeStringLiteral(colors);
	
	public static final CDataType<String> typeVisibility = new TypeStringLiteral(Team.EnumVisible.func_178825_a());
	
	public static final CommandConstructable objectivesList = CommandConstructable.primitiveConstructable(ObjectivesList.command);
	
	public static final CommandConstructable objectivesAdd = new CommandConstructable()
	{
		@Override
		public ObjectivesAdd construct(final ParserData data) throws SyntaxErrorException
		{
			return new ObjectivesAdd(
				getParam(TypeIDs.String, data),
				getParam(TypeIDs.String, data),
				getParam(TypeIDs.String, data));
		}
	};
	
	public static final CommandConstructable objectivesSetDisplay = new CommandConstructable()
	{
		@Override
		public ObjectivesSetDisplay construct(final ParserData data) throws SyntaxErrorException
		{
			return new ObjectivesSetDisplay(
				getParam(TypeIDs.String, data),
				getParam(TypeIDs.ScoreObjective, data));
		}
	};
	
	public static final CommandConstructable objectivesRemove = new CommandConstructable()
	{
		@Override
		public ObjectivesRemove construct(final ParserData data) throws SyntaxErrorException
		{
			return new ObjectivesRemove(getParam(TypeIDs.ScoreObjective, data));
		}
	};
	
	public static final CommandConstructable playersEnable = new CommandConstructable()
	{
		@Override
		public PlayersEnable construct(final ParserData data) throws SyntaxErrorException
		{
			return new PlayersEnable(
				getParam(TypeIDs.UUIDList, data),
				getParam(TypeIDs.ScoreObjective, data));
		}
	};
	
	public static final CommandConstructable playersSetAddRemove = new CommandConstructable()
	{
		@Override
		public PlayersSetAddRemove construct(final ParserData data) throws SyntaxErrorException
		{
			return new PlayersSetAddRemove(
				data.path.get(1),
				getParam(TypeIDs.UUIDList, data),
				getParam(TypeIDs.ScoreObjective, data),
				getParam(TypeIDs.Integer, data),
				getParam(TypeIDs.NBTCompound, data));
		}
	};
	
	public static final CommandConstructable playersOperation = new CommandConstructable()
	{
		@Override
		public PlayersOperation construct(final ParserData data) throws SyntaxErrorException
		{
			return new PlayersOperation(
				getParam(TypeIDs.UUIDList, data),
				getParam(TypeIDs.ScoreObjective, data),
				getParam(TypeIDs.String, data),
				getParam(TypeIDs.UUIDList, data),
				getParam(TypeIDs.ScoreObjective, data));
		}
	};
	
	public static final CommandConstructable playersTest = new CommandConstructable()
	{
		@Override
		public PlayersTest construct(final ParserData data) throws SyntaxErrorException
		{
			return new PlayersTest(
				getParam(TypeIDs.UUIDList, data),
				getParam(TypeIDs.ScoreObjective, data),
				getParam(TypeIDs.Integer, data),
				getParam(TypeIDs.Integer, data));
		}
	};
	
	public static final CommandConstructable playersList = new CommandConstructable()
	{
		@Override
		public CommandArg<Integer> construct(final ParserData data) throws SyntaxErrorException
		{
			return data.isEmpty() ? PlayersList.NoPlayer.command : new PlayersList.Player(getParam(TypeIDs.UUIDList, data));
		}
	};
	
	public static final CommandConstructable playersReset = new CommandConstructable()
	{
		@Override
		public PlayersReset construct(final ParserData data) throws SyntaxErrorException
		{
			return new PlayersReset(
				getParam(TypeIDs.UUIDList, data),
				getParam(TypeIDs.ScoreObjective, data));
		}
	};
	
	public static final CommandConstructable teamsAdd = new CommandConstructable()
	{
		@Override
		public TeamsAdd construct(final ParserData data) throws SyntaxErrorException
		{
			return new TeamsAdd(
				getParam(TypeIDs.String, data),
				getParam(TypeIDs.String, data));
		}
	};
	
	public static final CommandConstructable teamsJoin = new CommandConstructable()
	{
		@Override
		public TeamsJoin construct(final ParserData data) throws SyntaxErrorException
		{
			return new TeamsJoin(
				getParam(TypeIDs.String, data),
				getParam(TypeIDs.UUIDList, data));
		}
	};
	
	public static final CommandConstructable teamsLeave = new CommandConstructable()
	{
		@Override
		public TeamsLeave construct(final ParserData data) throws SyntaxErrorException
		{
			return new TeamsLeave(getParam(TypeIDs.UUIDList, data));
		}
	};
	
	public static final CommandConstructable teamsEmpty = new CommandConstructable()
	{
		@Override
		public TeamsEmpty construct(final ParserData data) throws SyntaxErrorException
		{
			return new TeamsEmpty(getParam(TypeIDs.String, data));
		}
	};
	
	public static final CommandConstructable teamsList = new CommandConstructable()
	{
		@Override
		public CommandArg<Integer> construct(final ParserData data) throws SyntaxErrorException
		{
			return data.isEmpty() ? TeamsList.NoTeam.command : new TeamsList.Team(getParam(TypeIDs.String, data));
		}
	};
	
	public static final CommandConstructable teamsRemove = new CommandConstructable()
	{
		@Override
		public TeamsRemove construct(final ParserData data) throws SyntaxErrorException
		{
			return new TeamsRemove(getParam(TypeIDs.String, data));
		}
	};
	
	public static final CommandConstructable teamsOption = new CommandConstructable()
	{
		@Override
		public TeamsOption construct(final ParserData data) throws SyntaxErrorException
		{
			return data.size() == 2 ?
				new TeamsOption.NoValue(
					getParam(TypeIDs.String, data),
					data.path.get(2)) :
				new TeamsOption.Value(
					getParam(TypeIDs.String, data),
					data.path.get(2),
					getParam(TypeIDs.String, data));
		}
	};
	
	private static Scoreboard getScoreboard()
	{
		return MinecraftServer.getServer().worldServerForDimension(0).getScoreboard();
	}
	
	// objectiveNames is obviously of type Collection<String>...
	@SuppressWarnings("unchecked")
	private static Collection<String> scoreholderCollection(final List<String> toIterate, final boolean copy)
	{
		if (toIterate.size() == 1 && toIterate.get(0).equals("*"))
			return copy ? new ArrayList<>(getScoreboard().getObjectiveNames()) : getScoreboard().getObjectiveNames();
		
		return toIterate;
	}
	
	private static Collection<String> scoreholderCollection(final CommandArg<List<String>> toIterate, final ICommandSender sender) throws CommandException
	{
		return scoreholderCollection(toIterate.eval(sender), false);
	}
	
	private static Collection<String> scoreholderCollectionCopy(final CommandArg<List<String>> toIterate, final ICommandSender sender) throws CommandException
	{
		return scoreholderCollection(toIterate.eval(sender), true);
	}
	
	private static void assertWriteable(final ScoreObjective objective) throws CommandException
	{
		if (objective.getCriteria().isReadOnly())
			throw new CommandException("commands.scoreboard.objectiveReadOnly", objective.getName());
	}
	
	private static ScorePlayerTeam getTeam(final String name) throws CommandException
	{
		final ScorePlayerTeam team = getScoreboard().getTeam(name);
		
		if (team == null)
			throw new CommandException("commands.scoreboard.teamNotFound", name);
		
		return team;
	}
	
	private static class ObjectivesList extends CommandArg<Integer>
	{
		private static final ObjectivesList command = new ObjectivesList();
		
		private ObjectivesList()
		{
		}
		
		@Override
		public Integer eval(final ICommandSender sender) throws CommandException
		{
			final Scoreboard scoreboard = getScoreboard();
			final Collection<?> objectives = scoreboard.getScoreObjectives();
			
			if (objectives.isEmpty())
				throw new CommandException("commands.scoreboard.objectives.list.empty");
			
			CommandBase.message(sender, EnumChatFormatting.DARK_GREEN, "commands.scoreboard.objectives.list.count", objectives.size());
			
			for (final Object item : objectives)
			{
				final ScoreObjective objective = (ScoreObjective) item;
				sender.addChatMessage(
					new ChatComponentTranslation("commands.scoreboard.objectives.list.entry",
						objective.getName(),
						objective.getDisplayName(),
						objective.getCriteria().getName()));
			}
			
			return objectives.size();
		}
	}
	
	private static class ObjectivesAdd extends CommandArg<Integer>
	{
		private final CommandArg<String> name;
		private final CommandArg<String> criterion;
		private final CommandArg<String> displayName;
		
		private ObjectivesAdd(final CommandArg<String> name, final CommandArg<String> criterion, final CommandArg<String> displayName)
		{
			this.name = name;
			this.criterion = criterion;
			this.displayName = displayName;
		}
		
		@Override
		public Integer eval(final ICommandSender sender) throws CommandException
		{
			final String name = this.name.eval(sender);
			final String criterionName = this.criterion.eval(sender);
			final Scoreboard scoreboard = getScoreboard();
			
			final IScoreObjectiveCriteria criterion = (IScoreObjectiveCriteria) IScoreObjectiveCriteria.INSTANCES.get(criterionName);
			
			if (criterion == null)
				throw new WrongUsageException("commands.scoreboard.objectives.add.wrongType", criterionName);
			
			if (scoreboard.getObjective(name) != null)
				throw new CommandException("commands.scoreboard.objectives.add.alreadyExists", name);
			
			if (name.length() > 16)
				throw new SyntaxErrorException("commands.scoreboard.objectives.add.tooLong", name, 16);
			
			if (name.length() == 0)
				throw new WrongUsageException("commands.scoreboard.objectives.add.usage");
			
			if (this.displayName == null)
				scoreboard.addScoreObjective(name, criterion);
			else
			{
				final String displayName = this.displayName.eval(sender);
				
				if (displayName.length() > 32)
					throw new SyntaxErrorException("commands.scoreboard.objectives.add.displayTooLong", displayName, 32);
				
				if (displayName.length() > 0)
					scoreboard.addScoreObjective(name, criterion).setDisplayName(displayName);
				else
					scoreboard.addScoreObjective(name, criterion);
			}
			
			CommandBase.notifyOperators(sender, "commands.scoreboard.objectives.add.success", name);
			
			return 1;
		}
	}
	
	private static class ObjectivesRemove extends CommandArg<Integer>
	{
		private final CommandArg<ScoreObjective> objective;
		
		private ObjectivesRemove(final CommandArg<ScoreObjective> objective)
		{
			this.objective = objective;
		}
		
		@Override
		public Integer eval(final ICommandSender sender) throws CommandException
		{
			final ScoreObjective objective = this.objective.eval(sender);
			getScoreboard().func_96519_k(objective);
			CommandBase.notifyOperators(sender, "commands.scoreboard.objectives.remove.success", objective.getName());
			
			return 1;
		}
	}
	
	private static class ObjectivesSetDisplay extends CommandArg<Integer>
	{
		private final CommandArg<String> slot;
		private final CommandArg<ScoreObjective> objective;
		
		private ObjectivesSetDisplay(final CommandArg<String> slot, final CommandArg<ScoreObjective> objective)
		{
			this.slot = slot;
			this.objective = objective;
		}
		
		@Override
		public Integer eval(final ICommandSender sender) throws CommandException
		{
			final String slot = this.slot.eval(sender);
			final int slotIndex = Scoreboard.getObjectiveDisplaySlotNumber(slot);
			
			if (slotIndex < 0)
				throw new CommandException("commands.scoreboard.objectives.setdisplay.invalidSlot", slot);
			
			final ScoreObjective objective = CommandArg.eval(this.objective, sender);
			
			getScoreboard().setObjectiveInDisplaySlot(slotIndex, objective);
			
			if (objective == null)
				CommandBase.notifyOperators(sender, "commands.scoreboard.objectives.setdisplay.successCleared", Scoreboard.getObjectiveDisplaySlot(slotIndex));
			else
				CommandBase.notifyOperators(sender, "commands.scoreboard.objectives.setdisplay.successSet", Scoreboard.getObjectiveDisplaySlot(slotIndex), objective.getName());
			
			return 1;
		}
	}
	
	private static class PlayersList
	{
		private static class NoPlayer extends CommandArg<Integer>
		{
			private static final NoPlayer command = new NoPlayer();
			
			@Override
			public Integer eval(final ICommandSender sender) throws CommandException
			{
				final Collection<?> scoreholders = getScoreboard().getObjectiveNames();
				sender.func_174794_a(CommandResultStats.Type.QUERY_RESULT, scoreholders.size());
				
				if (scoreholders.isEmpty())
					throw new CommandException("commands.scoreboard.players.list.empty");
				
				CommandBase.message(sender, EnumChatFormatting.DARK_GREEN, "commands.scoreboard.players.list.count", scoreholders.size());
				sender.addChatMessage(new ChatComponentText(ParsingUtilities.joinNiceString(scoreholders)));
				
				return scoreholders.size();
			}
		}
		
		private static class Player extends CommandArg<Integer>
		{
			private final CommandArg<List<String>> scoreholders;
			
			private Player(final CommandArg<List<String>> scoreholders)
			{
				this.scoreholders = scoreholders;
			}
			
			@Override
			public Integer eval(final ICommandSender sender) throws CommandException
			{
				final Scoreboard scoreboard = getScoreboard();
				int queryResult = 0;
				
				for (final String scoreholder : scoreholderCollection(this.scoreholders, sender))
				{
					final Map<ScoreObjective, Score> scores = scoreboard.getScores(scoreholder);
					
					if (scores == null || scores.isEmpty())
					{
						CommandBase.errorMessage(sender, "commands.scoreboard.players.list.player.empty", scoreholder);
						continue;
					}
					
					queryResult += scores.size();
					
					CommandBase.message(sender, EnumChatFormatting.DARK_GREEN, "commands.scoreboard.players.list.player.count", scores.size(), scoreholder);
					
					for (final Score score : scores.values())
						sender.addChatMessage(new ChatComponentTranslation("commands.scoreboard.players.list.player.entry", score.getScorePoints(), score.getObjective().getDisplayName(), score.getObjective().getName()));
				}
				
				sender.func_174794_a(CommandResultStats.Type.QUERY_RESULT, queryResult);
				
				return queryResult;
			}
			
		}
	}
	
	private static class PlayersSetAddRemove extends CommandArg<Integer>
	{
		private enum Operation
		{
			set, add, remove;
		}
		
		private final Operation operation;
		private final CommandArg<List<String>> scoreholders;
		private final CommandArg<ScoreObjective> objective;
		private final CommandArg<Integer> amount;
		private final CommandArg<NBTTagCompound> nbt;
		
		private PlayersSetAddRemove(final String operation, final CommandArg<List<String>> scoreholders, final CommandArg<ScoreObjective> objective, final CommandArg<Integer> amount, final CommandArg<NBTTagCompound> nbt)
		{
			this.operation = Operation.valueOf(operation);
			this.scoreholders = scoreholders;
			this.objective = objective;
			this.amount = amount;
			this.nbt = nbt;
		}
		
		@Override
		public Integer eval(final ICommandSender sender) throws CommandException
		{
			final ScoreObjective objective = this.objective.eval(sender);
			
			assertWriteable(objective);
			
			final int amount = this.amount.eval(sender);
			
			final NBTTagCompound nbt = CommandArg.eval(this.nbt, sender);
			
			int affectedEntities = 0;
			
			for (final String scoreholder : scoreholderCollectionCopy(this.scoreholders, sender))
			{
				if (nbt != null)
				{
					final Entity entity = ParsingUtilities.entiyFromIdentifier(scoreholder);
					
					if (entity == null)
					{
						CommandBase.errorMessage(sender, scoreholder + " is not an entity");
						continue;
					}
					
					final NBTTagCompound entityNBT = new NBTTagCompound();
					entity.writeToNBT(entityNBT);
					
					if (!NBTBase.compareTags(nbt, entityNBT, true))
					{
						CommandBase.errorMessage(sender, "commands.scoreboard.players.set.tagMismatch", scoreholder);
						continue;
					}
				}
				
				final Score score = getScoreboard().getValueFromObjective(scoreholder, objective);
				
				switch (this.operation)
				{
				case set:
					score.setScorePoints(amount);
					break;
				case add:
					score.increseScore(amount);
					break;
				case remove:
					score.decreaseScore(amount);
				}
				
				++affectedEntities;
				CommandBase.notifyOperators(sender, "commands.scoreboard.players.set.success", objective.getName(), scoreholder, score.getScorePoints());
			}
			
			sender.func_174794_a(CommandResultStats.Type.AFFECTED_ENTITIES, affectedEntities);
			
			return affectedEntities;
		}
	}
	
	private static class PlayersReset extends CommandArg<Integer>
	{
		private final CommandArg<List<String>> scoreholders;
		private final CommandArg<ScoreObjective> objective;
		
		private PlayersReset(final CommandArg<List<String>> scoreholders, final CommandArg<ScoreObjective> objective)
		{
			this.scoreholders = scoreholders;
			this.objective = objective;
		}
		
		@Override
		public Integer eval(final ICommandSender sender) throws CommandException
		{
			final Scoreboard scoreboard = getScoreboard();
			
			final ScoreObjective objective = CommandArg.eval(this.objective, sender);
			
			final Collection<String> scoreholders = scoreholderCollectionCopy(this.scoreholders, sender);
			for (final String scoreholder : scoreholders)
			{
				scoreboard.func_178822_d(scoreholder, objective);
				
				if (objective == null)
					CommandBase.notifyOperators(sender, "commands.scoreboard.players.reset.success", scoreholder);
				else
					CommandBase.notifyOperators(sender, "commands.scoreboard.players.resetscore.success", objective.getName(), scoreholder);
			}
			
			sender.func_174794_a(Type.AFFECTED_ENTITIES, scoreholders.size());
			
			return scoreholders.size();
		}
	}
	
	private static class PlayersEnable extends CommandArg<Integer>
	{
		private final CommandArg<List<String>> scoreholders;
		private final CommandArg<ScoreObjective> objective;
		
		private PlayersEnable(final CommandArg<List<String>> scoreholders, final CommandArg<ScoreObjective> objective)
		{
			this.scoreholders = scoreholders;
			this.objective = objective;
		}
		
		@Override
		public Integer eval(final ICommandSender sender) throws CommandException
		{
			final ScoreObjective objective = this.objective.eval(sender);
			
			if (objective.getCriteria() != IScoreObjectiveCriteria.field_178791_c)
				throw new CommandException("commands.scoreboard.players.enable.noTrigger", objective.getName());
			
			final Collection<String> scoreholders = scoreholderCollectionCopy(this.scoreholders, sender);
			for (final String scoreholder : scoreholders)
			{
				final Score score = getScoreboard().getValueFromObjective(scoreholder, objective);
				score.func_178815_a(false);
				CommandBase.notifyOperators(sender, "commands.scoreboard.players.enable.success", objective.getName(), scoreholder);
			}
			
			sender.func_174794_a(Type.AFFECTED_ENTITIES, scoreholders.size());
			return scoreholders.size();
		}
	}
	
	private static class PlayersTest extends CommandArg<Integer>
	{
		private final CommandArg<List<String>> scoreholders;
		private final CommandArg<ScoreObjective> objective;
		private final CommandArg<Integer> min;
		private final CommandArg<Integer> max;
		
		public PlayersTest(final CommandArg<List<String>> scoreholders, final CommandArg<ScoreObjective> objective, final CommandArg<Integer> min, final CommandArg<Integer> max)
		{
			this.scoreholders = scoreholders;
			this.objective = objective;
			this.min = min;
			this.max = max;
		}
		
		@Override
		public Integer eval(final ICommandSender sender) throws CommandException
		{
			final Scoreboard scoreboard = getScoreboard();
			
			final ScoreObjective objective = this.objective.eval(sender);
			
			final Collection<String> scoreholders = scoreholderCollection(this.scoreholders, sender);
			
			int successCount = 0;
			
			final int min = this.min.eval(sender);
			final int max = this.max == null ? Integer.MAX_VALUE : CommandBase.parseInt(this.max.eval(sender), min);
			
			for (final String scoreholder : scoreholders)
			{
				final Score score = scoreboard.getScore(scoreholder, objective);
				
				if (score == null)
				{
					CommandBase.errorMessage(sender, "commands.scoreboard.players.test.notFound", objective.getName(), scoreholder);
					continue;
				}
				
				if (score.getScorePoints() >= min && score.getScorePoints() <= max)
				{
					CommandBase.notifyOperators(sender, "commands.scoreboard.players.test.success", score.getScorePoints(), min, max);
					++successCount;
				}
				else
					CommandBase.errorMessage(sender, "commands.scoreboard.players.test.failed", score.getScorePoints(), min, max);
			}
			
			sender.func_174794_a(Type.AFFECTED_ENTITIES, scoreholders.size());
			sender.func_174794_a(Type.QUERY_RESULT, successCount);
			return successCount;
		}
	}
	
	private static class PlayersOperation extends CommandArg<Integer>
	{
		private final CommandArg<List<String>> targetScoreholders;
		private final CommandArg<ScoreObjective> targetObjective;
		private final CommandArg<String> operation;
		private final CommandArg<List<String>> sourceScoreholders;
		private final CommandArg<ScoreObjective> sourceObjective;
		
		private PlayersOperation(final CommandArg<List<String>> targetScoreholders, final CommandArg<ScoreObjective> targetObjective, final CommandArg<String> operation, final CommandArg<List<String>> sourceScoreholders, final CommandArg<ScoreObjective> sourceObjective)
		{
			this.targetScoreholders = targetScoreholders;
			this.targetObjective = targetObjective;
			this.operation = operation;
			this.sourceScoreholders = sourceScoreholders;
			this.sourceObjective = sourceObjective;
		}
		
		@Override
		public Integer eval(final ICommandSender sender) throws CommandException
		{
			final Scoreboard scoreboard = getScoreboard();
			
			final Collection<String> targetScoreholders = scoreholderCollection(this.targetScoreholders, sender);
			final ScoreObjective targetObjective = this.targetObjective.eval(sender);
			assertWriteable(targetObjective);
			
			final String operationName = this.operation.eval(sender);
			
			final Collection<String> sourceScoreholders = scoreholderCollectionCopy(this.sourceScoreholders, sender);
			final ScoreObjective sourceObjective = this.sourceObjective.eval(sender);
			if (operationName.equals("><"))
				assertWriteable(sourceObjective);
			
			final Operation operation = Operation.get(operationName);
			
			final List<Score> targetScores = new ArrayList<>();
			
			for (final String targetScoreholder : targetScoreholders)
				targetScores.add(scoreboard.getValueFromObjective(targetScoreholder, targetObjective));
			
			for (final String sourceScoreholder : sourceScoreholders)
			{
				final Score sourceScore = scoreboard.getScore(sourceScoreholder, sourceObjective);
				
				if (sourceScore == null)
					CommandBase.errorMessage(sender, "commands.scoreboard.players.operation.notFound", sourceObjective.getName(), sourceScoreholder);
				else
					for (final Score targetScore : targetScores)
					{
						operation.proc(targetScore, sourceScore);
						
						CommandBase.notifyOperators(sender, "commands.scoreboard.players.operation.success");
					}
			}
			
			sender.func_174794_a(Type.AFFECTED_ENTITIES, targetScoreholders.size());
			
			return targetScoreholders.size() * sourceScoreholders.size();
		}
		
		private static abstract class Operation
		{
			public abstract void proc(Score target, Score source);
			
			private static final Map<String, Operation> operations = new HashMap<>(9);
			
			private static Operation get(final String operation) throws CommandException
			{
				final Operation ret = operations.get(operation);
				
				if (ret == null)
					throw new CommandException("commands.scoreboard.players.operation.invalidOperation", operation);
				
				return ret;
			}
			
			static
			{
				operations.put("+=", new Operation()
				{
					@Override
					public void proc(final Score target, final Score source)
					{
						target.setScorePoints(target.getScorePoints() + source.getScorePoints());
					}
				});
				operations.put("-=", new Operation()
				{
					@Override
					public void proc(final Score target, final Score source)
					{
						target.setScorePoints(target.getScorePoints() - source.getScorePoints());
					}
				});
				operations.put("*=", new Operation()
				{
					@Override
					public void proc(final Score target, final Score source)
					{
						target.setScorePoints(target.getScorePoints() * source.getScorePoints());
					}
				});
				operations.put("/=", new Operation()
				{
					@Override
					public void proc(final Score target, final Score source)
					{
						if (source.getScorePoints() != 0)
							target.setScorePoints(target.getScorePoints() / source.getScorePoints());
					}
				});
				operations.put("%=", new Operation()
				{
					@Override
					public void proc(final Score target, final Score source)
					{
						if (source.getScorePoints() != 0)
							target.setScorePoints(target.getScorePoints() % source.getScorePoints());
					}
				});
				operations.put("=", new Operation()
				{
					@Override
					public void proc(final Score target, final Score source)
					{
						target.setScorePoints(source.getScorePoints());
					}
				});
				operations.put("<", new Operation()
				{
					@Override
					public void proc(final Score target, final Score source)
					{
						target.setScorePoints(Math.min(target.getScorePoints(), source.getScorePoints()));
					}
				});
				operations.put(">", new Operation()
				{
					@Override
					public void proc(final Score target, final Score source)
					{
						target.setScorePoints(Math.max(target.getScorePoints(), source.getScorePoints()));
					}
				});
				operations.put("><", new Operation()
				{
					@Override
					public void proc(final Score target, final Score source)
					{
						final int tempScore = target.getScorePoints();
						target.setScorePoints(source.getScorePoints());
						source.setScorePoints(tempScore);
					}
				});
			}
		}
	}
	
	private static class TeamsList
	{
		private static class NoTeam extends CommandArg<Integer>
		{
			private static final NoTeam command = new NoTeam();
			
			@Override
			public Integer eval(final ICommandSender sender) throws CommandException
			{
				final Collection<?> teams = getScoreboard().getTeams();
				sender.func_174794_a(CommandResultStats.Type.QUERY_RESULT, teams.size());
				
				if (teams.isEmpty())
					throw new CommandException("commands.scoreboard.teams.list.empty");
				
				CommandBase.message(sender, EnumChatFormatting.DARK_GREEN, "commands.scoreboard.teams.list.count", teams.size());
				
				for (final Object item : teams)
				{
					final ScorePlayerTeam team = (ScorePlayerTeam) item;
					sender.addChatMessage(new ChatComponentTranslation("commands.scoreboard.teams.list.entry", team.getRegisteredName(), team.func_96669_c(), team.getMembershipCollection().size()));
				}
				
				return teams.size();
			}
		}
		
		private static class Team extends CommandArg<Integer>
		{
			private final CommandArg<String> teamName;
			
			private Team(final CommandArg<String> teamName)
			{
				this.teamName = teamName;
			}
			
			@Override
			public Integer eval(final ICommandSender sender) throws CommandException
			{
				final ScorePlayerTeam team = getTeam(this.teamName.eval(sender));
				
				final Collection<?> members = team.getMembershipCollection();
				sender.func_174794_a(CommandResultStats.Type.QUERY_RESULT, members.size());
				
				if (members.isEmpty())
					throw new CommandException("commands.scoreboard.teams.list.player.empty", team.getRegisteredName());
				
				CommandBase.message(sender, EnumChatFormatting.DARK_GREEN, "commands.scoreboard.teams.list.player.count", members.size(), team.getRegisteredName());
				sender.addChatMessage(new ChatComponentText(ParsingUtilities.joinNiceString(members)));
				
				return members.size();
			}
		}
	}
	
	private static class TeamsAdd extends CommandArg<Integer>
	{
		private final CommandArg<String> teamName;
		private final CommandArg<String> displayName;
		
		private TeamsAdd(final CommandArg<String> teamName, final CommandArg<String> displayName)
		{
			this.teamName = teamName;
			this.displayName = displayName;
		}
		
		@Override
		public Integer eval(final ICommandSender sender) throws CommandException
		{
			final String teamName = this.teamName.eval(sender);
			final Scoreboard scoreboard = getScoreboard();
			
			if (scoreboard.getTeam(teamName) != null)
				throw new CommandException("commands.scoreboard.teams.add.alreadyExists", teamName);
			
			if (teamName.length() > 16)
				throw new SyntaxErrorException("commands.scoreboard.teams.add.tooLong", teamName, 16);
			
			if (teamName.length() == 0)
				throw new WrongUsageException("commands.scoreboard.teams.add.usage");
			
			if (this.displayName == null)
				scoreboard.createTeam(teamName);
			else
			{
				final String displayName = this.displayName.eval(sender);
				
				if (displayName.length() > 32)
					throw new SyntaxErrorException("commands.scoreboard.teams.add.displayTooLong", displayName, 32);
				
				if (displayName.length() == 0)
					scoreboard.createTeam(teamName);
				else
					scoreboard.createTeam(teamName).setTeamName(displayName);
			}
			
			CommandBase.notifyOperators(sender, "commands.scoreboard.teams.add.success", teamName);
			
			return 1;
		}
	}
	
	private static class TeamsRemove extends CommandArg<Integer>
	{
		private final CommandArg<String> teamName;
		
		private TeamsRemove(final CommandArg<String> teamName)
		{
			this.teamName = teamName;
		}
		
		@Override
		public Integer eval(final ICommandSender sender) throws CommandException
		{
			final ScorePlayerTeam team = getTeam(this.teamName.eval(sender));
			
			getScoreboard().removeTeam(team);
			CommandBase.notifyOperators(sender, "commands.scoreboard.teams.remove.success", team.getRegisteredName());
			
			return 1;
		}
	}
	
	private static class TeamsEmpty extends CommandArg<Integer>
	{
		private final CommandArg<String> teamName;
		
		private TeamsEmpty(final CommandArg<String> teamName)
		{
			this.teamName = teamName;
		}
		
		@Override
		public Integer eval(final ICommandSender sender) throws CommandException
		{
			final ScorePlayerTeam team = getTeam(this.teamName.eval(sender));
			
			final Collection<?> members = team.getMembershipCollection();
			
			if (members.isEmpty())
				throw new CommandException("commands.scoreboard.teams.empty.alreadyEmpty", team.getRegisteredName());
			
			for (final Object member : members)
				getScoreboard().removePlayerFromTeam((String) member, team);
			
			CommandBase.notifyOperators(sender, "commands.scoreboard.teams.empty.success", members.size(), team.getRegisteredName());
			
			sender.func_174794_a(Type.AFFECTED_ENTITIES, members.size());
			return members.size();
		}
	}
	
	private static class TeamsJoin extends CommandArg<Integer>
	{
		private final CommandArg<String> teamName;
		private final CommandArg<List<String>> scoreholders;
		
		private TeamsJoin(final CommandArg<String> teamName, final CommandArg<List<String>> scoreholders)
		{
			this.teamName = teamName;
			this.scoreholders = scoreholders;
		}
		
		@Override
		public Integer eval(final ICommandSender sender) throws CommandException
		{
			final Scoreboard scoreboard = getScoreboard();
			final String teamName = this.teamName.eval(sender);
			
			final Set<String> newMembers = new HashSet<>();
			final Set<String> failedMembers = new HashSet<>();
			
			if (this.scoreholders == null && !(sender instanceof Entity))
				throw new EntityNotFoundException();
			
			final Collection<String> scoreholders = this.scoreholders == null ?
				Collections.singleton(ParsingUtilities.getEntityIdentifier((Entity) sender)) :
				scoreholderCollection(this.scoreholders, sender);
			
			for (final String scoreholder : scoreholders)
			{
				if (scoreboard.func_151392_a(scoreholder, teamName))
					newMembers.add(scoreholder);
				else
					failedMembers.add(scoreholder);
			}
			
			if (!newMembers.isEmpty())
			{
				sender.func_174794_a(CommandResultStats.Type.AFFECTED_ENTITIES, newMembers.size());
				CommandBase.notifyOperators(sender, "commands.scoreboard.teams.join.success", newMembers.size(), teamName, ParsingUtilities.joinNiceString(newMembers));
			}
			
			if (!failedMembers.isEmpty())
				throw new CommandException("commands.scoreboard.teams.join.failure", failedMembers.size(), teamName, ParsingUtilities.joinNiceString(failedMembers));
			
			return newMembers.size();
		}
	}
	
	private static class TeamsLeave extends CommandArg<Integer>
	{
		private final CommandArg<List<String>> scoreholders;
		
		private TeamsLeave(final CommandArg<List<String>> scoreholders)
		{
			this.scoreholders = scoreholders;
		}
		
		@Override
		public Integer eval(final ICommandSender sender) throws CommandException
		{
			final Scoreboard scoreboard = getScoreboard();
			
			final Set<String> removedMembers = new HashSet<>();
			final Set<String> failedMembers = new HashSet<>();
			
			if (this.scoreholders == null && !(sender instanceof Entity))
				throw new EntityNotFoundException();
			
			final Collection<String> scoreholders = this.scoreholders == null ? Collections.singleton(ParsingUtilities.getEntityIdentifier((Entity) sender)) : scoreholderCollection(this.scoreholders, sender);
			
			for (final String scoreholder : scoreholders)
			{
				if (scoreboard.removePlayerFromTeams(scoreholder))
					removedMembers.add(scoreholder);
				else
					failedMembers.add(scoreholder);
			}
			
			if (!removedMembers.isEmpty())
			{
				sender.func_174794_a(CommandResultStats.Type.AFFECTED_ENTITIES, removedMembers.size());
				CommandBase.notifyOperators(sender, "commands.scoreboard.teams.leave.success", removedMembers.size(), ParsingUtilities.joinNiceString(removedMembers));
			}
			
			if (!failedMembers.isEmpty())
				throw new CommandException("commands.scoreboard.teams.leave.failure", failedMembers.size(), ParsingUtilities.joinNiceString(failedMembers));
			
			return removedMembers.size();
		}
	}
	
	private static abstract class TeamsOption extends CommandArg<Integer>
	{
		private enum Option
		{
			color, friendlyFire, seeFriendlyInvisibles, nameTagVisibility, deathMessageVisibility;
		}
		
		protected final CommandArg<String> teamName;
		protected final Option option;
		
		private TeamsOption(final CommandArg<String> teamName, final String option)
		{
			this.teamName = teamName;
			this.option = Option.valueOf(option);
		}
		
		private static class NoValue extends TeamsOption
		{
			private NoValue(final CommandArg<String> teamName, final String option)
			{
				super(teamName, option);
			}
			
			@Override
			public Integer eval(final ICommandSender sender) throws CommandException
			{
				final ScorePlayerTeam team = getTeam(this.teamName.eval(sender));
				
				switch (this.option)
				{
				case color:
					CommandBase.message(sender, EnumChatFormatting.DARK_GREEN, "The color of team '" + team.getRegisteredName() + "' is set to '" + team.func_178775_l().getFriendlyName() + "'");
					break;
				case friendlyFire:
					CommandBase.message(sender, EnumChatFormatting.DARK_GREEN, "friendlyFire for team '" + team.getRegisteredName() + "' is set to '" + (team.getAllowFriendlyFire() ? "true'" : "false'"));
					break;
				case seeFriendlyInvisibles:
					CommandBase.message(sender, EnumChatFormatting.DARK_GREEN, "seeFriendlyInvisibles for team '" + team.getRegisteredName() + "' is set to '" + (team.func_98297_h() ? "true'" : "false'"));
					break;
				case nameTagVisibility:
					CommandBase.message(sender, EnumChatFormatting.DARK_GREEN, "nameTagVisibility for team '" + team.getRegisteredName() + "' is set to '" + team.func_178770_i().field_178830_e + "'");
					break;
				case deathMessageVisibility:
					CommandBase.message(sender, EnumChatFormatting.DARK_GREEN, "deathMessageVisibility for team '" + team.getRegisteredName() + "' is set to '" + team.func_178771_j().field_178830_e + "'");
				}
				
				return 1;
			}
		}
		
		private static class Value extends TeamsOption
		{
			
			private final CommandArg<String> value;
			
			private Value(final CommandArg<String> teamName, final String option, final CommandArg<String> value)
			{
				super(teamName, option);
				this.value = value;
			}
			
			@Override
			public Integer eval(final ICommandSender sender) throws CommandException
			{
				final ScorePlayerTeam team = getTeam(this.teamName.eval(sender));
				
				final String value = this.value.eval(sender);
				
				switch (this.option)
				{
				case color:
					final EnumChatFormatting color = EnumChatFormatting.getValueByName(value);
					
					if (color == null || color.isFancyStyling())
						throw new WrongUsageException("commands.scoreboard.teams.option.noValue", "color", ParsingUtilities.joinNiceString(CommandScoreboard.colors));
					
					team.func_178774_a(color);
					team.setNamePrefix(color.toString());
					team.setNameSuffix(EnumChatFormatting.RESET.toString());
					break;
				case friendlyFire:
				case seeFriendlyInvisibles:
					final boolean isTrue = ParsingUtilities.isTrue(value);
					
					if (!isTrue && !ParsingUtilities.isFalse(value))
						throw new WrongUsageException("commands.scoreboard.teams.option.noValue", this.option.toString(), "true and false");
					
					if (this.option == Option.friendlyFire)
						team.setAllowFriendlyFire(isTrue);
					else
						team.setSeeFriendlyInvisiblesEnabled(isTrue);
					
					break;
				case nameTagVisibility:
				case deathMessageVisibility:
					final Team.EnumVisible visibility = Team.EnumVisible.func_178824_a(value);
					
					if (visibility == null)
						throw new WrongUsageException("commands.scoreboard.teams.option.noValue", this.option.toString(), ParsingUtilities.joinNiceString((Object[]) Team.EnumVisible.func_178825_a()));
					
					if (this.option == Option.nameTagVisibility)
						team.func_178772_a(visibility);
					else
						team.func_178773_b(visibility);
				}
				
				CommandBase.notifyOperators(sender, "commands.scoreboard.teams.option.success", this.option.toString(), team.getRegisteredName(), value);
				
				return 1;
			}
		}
	}
}