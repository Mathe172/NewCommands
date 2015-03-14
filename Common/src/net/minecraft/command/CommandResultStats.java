package net.minecraft.command;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.parser.ParsingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;

public class CommandResultStats
{
	private static final int typeCount = CommandResultStats.Type.values().length;
	private static final String[] pseudoNull = new String[typeCount];
	private static final Target[] pseudoNullTarget = new Target[typeCount];
	private Target[] targets;
	private String[] objectiveNames;
	@SuppressWarnings("unused")
	private static final String __OBFID = "CL_00002364";
	
	public CommandResultStats()
	{
		this.targets = pseudoNullTarget;
		this.objectiveNames = pseudoNull;
	}
	
	/**
	 * main function (set stat)
	 */
	public void func_179672_a(final ICommandSender sender, final CommandResultStats.Type stat, final int amount)
	{
		final Target target = this.targets[stat.statId()];
		
		if (target == null)
			return;
		
		final String objectiveName = this.objectiveNames[stat.statId()];
		
		if (objectiveName == null)
			return;
		
		final Scoreboard scoreboard = sender.getEntityWorld().getScoreboard();
		final ScoreObjective objective = scoreboard.getObjective(objectiveName);
		
		if (objective == null)
			return;
		
		final List<String> entityIdentifiers;
		try
		{
			entityIdentifiers = target.getTarget().eval(sender);
		} catch (final CommandException e)
		{
			return;
		}
		
		// boolean scoreSet(...)
		// if (scoreboard.func_178819_b(entityIdentifiers, objective)) <-- set score even if not yet existing
		
		for (final String entityIdentifier : entityIdentifiers)
			scoreboard.getValueFromObjective(entityIdentifier, objective).setScorePoints(amount);
	}
	
	/**
	 * Read from NBT
	 */
	public void func_179668_a(final NBTTagCompound tag)
	{
		if (tag.hasKey("CommandStats", 10))
		{
			final NBTTagCompound statsCompound = tag.getCompoundTag("CommandStats");
			final CommandResultStats.Type[] types = CommandResultStats.Type.values();
			final int typeCount = types.length;
			
			for (int i = 0; i < typeCount; ++i)
			{
				final CommandResultStats.Type type = types[i];
				// typeIdentifier?
				final String targetTagName = type.func_179637_b() + "Name";
				final String objectiveTagName = type.func_179637_b() + "Objective";
				
				if (statsCompound.hasKey(targetTagName, 8) && statsCompound.hasKey(objectiveTagName, 8))
				{
					final String target = statsCompound.getString(targetTagName);
					final String objectiveName = statsCompound.getString(objectiveTagName);
					func_179667_a(this, type, target, objectiveName);
				}
			}
		}
	}
	
	/**
	 * Write to NBT
	 */
	public void func_179670_b(final NBTTagCompound tag)
	{
		final NBTTagCompound statsTag = new NBTTagCompound();
		final CommandResultStats.Type[] types = CommandResultStats.Type.values();
		
		for (final Type type : types)
		{
			final Target target = this.targets[type.statId()];
			final String objectiveName = this.objectiveNames[type.statId()];
			
			if (target != null && objectiveName != null)
			{
				statsTag.setString(type.func_179637_b() + "Name", target.get());
				statsTag.setString(type.func_179637_b() + "Objective", objectiveName);
			}
		}
		
		if (!statsTag.hasNoTags())
		{
			tag.setTag("CommandStats", statsTag);
		}
	}
	
	/**
	 * Set target-objective pair
	 */
	public static void func_179667_a(final CommandResultStats stats, final CommandResultStats.Type type, final String targetString, final String objectiveName)
	{
		if (targetString != null && targetString.length() != 0 && objectiveName != null && objectiveName.length() != 0)
		{
			if (stats.targets == pseudoNullTarget || stats.objectiveNames == pseudoNull)
			{
				stats.targets = new Target[typeCount];
				stats.objectiveNames = new String[typeCount];
			}
			
			stats.targets[type.statId()] = new Target(targetString);
			stats.objectiveNames[type.statId()] = objectiveName;
		}
		else
		{
			removeTypeData(stats, type);
		}
	}
	
	private static void removeTypeData(final CommandResultStats stats, final CommandResultStats.Type type)
	{
		if (stats.targets != pseudoNullTarget && stats.objectiveNames != pseudoNull)
		{
			stats.targets[type.statId()] = null;
			stats.objectiveNames[type.statId()] = null;
			boolean nowEmpty = true;
			final CommandResultStats.Type[] types = CommandResultStats.Type.values();
			final int typeCount = types.length;
			
			for (int i = 0; i < typeCount; ++i)
			{
				final CommandResultStats.Type curType = types[i];
				
				if (stats.targets[curType.statId()] != null && stats.objectiveNames[curType.statId()] != null)
				{
					nowEmpty = false;
					break;
				}
			}
			
			if (nowEmpty)
			{
				stats.targets = pseudoNullTarget;
				stats.objectiveNames = pseudoNull;
			}
		}
	}
	
	// Copy
	public void func_179671_a(final CommandResultStats stats)
	{
		final CommandResultStats.Type[] types = CommandResultStats.Type.values();
		
		for (final Type type : types)
		{
			final Target target = stats.targets[type.statId()];
			func_179667_a(this, type, target == null ? null : target.get(), stats.objectiveNames[type.statId()]);
		}
	}
	
	public static enum Type
	{
		SUCCESS_COUNT("SUCCESS_COUNT", 0, 0, "SuccessCount"),
		AFFECTED_BLOCKS("AFFECTED_BLOCKS", 1, 1, "AffectedBlocks"),
		AFFECTED_ENTITIES("AFFECTED_ENTITIES", 2, 2, "AffectedEntities"),
		AFFECTED_ITEMS("AFFECTED_ITEMS", 3, 3, "AffectedItems"),
		QUERY_RESULT("QUERY_RESULT", 4, 4, "QueryResult");
		/**
		 * StatID
		 */
		final int field_179639_f;
		/**
		 * Name
		 */
		final String field_179640_g;
		
		@SuppressWarnings("unused")
		private static final CommandResultStats.Type[] $VALUES = new CommandResultStats.Type[] { SUCCESS_COUNT, AFFECTED_BLOCKS, AFFECTED_ENTITIES, AFFECTED_ITEMS, QUERY_RESULT };
		@SuppressWarnings("unused")
		private static final String __OBFID = "CL_00002363";
		
		private Type(final String p_i46050_1_, final int p_i46050_2_, final int statID, final String name)
		{
			this.field_179639_f = statID;
			this.field_179640_g = name;
		}
		
		public int statId()
		{
			return this.field_179639_f;
		}
		
		/**
		 * getName
		 */
		public String func_179637_b()
		{
			return this.field_179640_g;
		}
		
		/**
		 * getNames
		 */
		public static String[] func_179634_c()
		{
			final String[] var0 = new String[values().length];
			int i2 = 0;
			final CommandResultStats.Type[] types = values();
			final int typeCount = types.length;
			
			for (int i = 0; i < typeCount; ++i)
			{
				final CommandResultStats.Type type = types[i];
				var0[i2++] = type.func_179637_b();
			}
			
			return var0;
		}
		
		/**
		 * getTypeByName
		 */
		public static CommandResultStats.Type func_179635_a(final String name)
		{
			final CommandResultStats.Type[] types = values();
			final int typeCount = types.length;
			
			for (int i = 0; i < typeCount; ++i)
			{
				final CommandResultStats.Type type = types[i];
				
				if (type.func_179637_b().equals(name))
				{
					return type;
				}
			}
			
			return null;
		}
	}
	
	public static class Target
	{
		private String targetString;
		private Future<CommandArg<List<String>>> fTarget;
		private CommandArg<List<String>> target;
		
		public Target()
		{
			this.targetString = "";
			this.fTarget = null;
			this.target = initTarget;
		}
		
		public Target(final String targetString)
		{
			this.targetString = targetString;
			this.fTarget = ParsingManager.submitTarget(this.targetString);
			this.target = null;
		}
		
		public final String get()
		{
			return this.targetString;
		}
		
		public final void set(final String targetString)
		{
			this.targetString = targetString;
			this.fTarget = ParsingManager.submitTarget(this.targetString);
			this.target = null;
		}
		
		public CommandArg<List<String>> getTarget()
		{
			if (this.target == null)
			{
				try
				{
					try
					{
						this.target = this.fTarget.get();
					} catch (final InterruptedException e)
					{
						this.target = Parser.parseStatsTarget(this.targetString);
					}
				} catch (final ExecutionException | SyntaxErrorException e)
				{
					this.target = initTarget;
				}
				
				this.fTarget = null;
			}
			
			return this.target;
		}
		
		private static final CommandArg<List<String>> initTarget = new CommandArg<List<String>>()
		{
			@Override
			public List<String> eval(final ICommandSender sender) throws CommandException
			{
				return Collections.emptyList();
			}
		};
	}
}
