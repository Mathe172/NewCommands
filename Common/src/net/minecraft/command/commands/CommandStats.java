package net.minecraft.command.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.CommandResultStats.Type;
import net.minecraft.command.CommandUtilities;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.construction.CommandConstructable;
import net.minecraft.command.construction.CommandDescriptorDefault.CParserData;
import net.minecraft.command.construction.UsageProviderDefault;
import net.minecraft.command.descriptors.ICommandDescriptor.UsageProvider;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.custom.TypeStringLiteral;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.BlockPos;

public abstract class CommandStats extends CommandArg<Integer>
{
	public static final CDataType<String> typeStatName = new TypeStringLiteral(CommandResultStats.Type.func_179634_c());
	
	private final CommandArg<List<CommandResultStats>> stats;
	private final CommandArg<String> statName;
	
	public CommandStats(final CommandArg<List<CommandResultStats>> stats, final CommandArg<String> statName)
	{
		this.stats = stats;
		this.statName = statName;
	}
	
	public static final UsageProvider usageSet = usage("commands.stats.block.set.usage", "commands.stats.entity.set.usage");
	public static final UsageProvider usageClear = usage("commands.stats.block.clear.usage", "commands.stats.entity.clear.usage");
	
	public static final CommandConstructable constructableSet = new CommandConstructable()
	{
		@Override
		public CommandArg<Integer> construct(final CParserData data) throws SyntaxErrorException
		{
			return new Set(
				"block".equals(data.path.get(0)) ? new Block(data.get(TypeIDs.BlockPos)) : new Entities(data.get(TypeIDs.EntityList)),
				data.get(TypeIDs.String),
				data.get(TypeIDs.String),
				data.get(TypeIDs.String));
		}
	};
	
	public static final CommandConstructable constructableClear = new CommandConstructable()
	{
		@Override
		public CommandArg<Integer> construct(final CParserData data) throws SyntaxErrorException
		{
			return new Clear(
				"block".equals(data.path.get(0)) ? new Block(data.get(TypeIDs.BlockPos)) : new Entities(data.get(TypeIDs.EntityList)),
				data.get(TypeIDs.String));
		}
	};
	
	private static final UsageProvider usage(final String block, final String entity)
	{
		return new UsageProviderDefault()
		{
			@Override
			protected <R> R create(final List<String> path, final AbstractCreator<R> creator)
			{
				final String target = path.get(0);
				
				if ("block".equals(target))
					return creator.create(block);
				if ("entity".equals(target))
					return creator.create(entity);
				
				return creator.create("commands.stats.usage");
			}
		};
	}
	
	@Override
	public Integer eval(final ICommandSender sender) throws CommandException
	{
		final CommandResultStats.Type statType = CommandResultStats.Type.func_179635_a(this.statName.eval(sender));
		
		if (statType == null)
			throw new CommandException("commands.stats.failed");
		
		final List<CommandResultStats> stats = this.stats.eval(sender);
		
		this.eval(sender, stats, statType);
		
		return stats.size();
	}
	
	protected abstract void eval(final ICommandSender sender, final List<CommandResultStats> stats, final CommandResultStats.Type statType) throws CommandException;
	
	private static class Set extends CommandStats
	{
		private final CommandArg<String> scoreholders;
		private final CommandArg<String> objective;
		
		public Set(final CommandArg<List<CommandResultStats>> stats, final CommandArg<String> statName, final CommandArg<String> scoreholders, final CommandArg<String> objective)
		{
			super(stats, statName);
			this.scoreholders = scoreholders;
			this.objective = objective;
		}
		
		@Override
		protected void eval(final ICommandSender sender, final List<CommandResultStats> stats, final Type statType) throws CommandException
		{
			final String scoreholders = this.scoreholders.eval(sender);
			final String objectives = this.objective.eval(sender);
			
			if (scoreholders.length() == 0 || objectives.length() == 0)
				throw new CommandException("commands.stats.failed");
			
			for (final CommandResultStats stat : stats)
			{
				CommandResultStats.func_179667_a(stat, statType, scoreholders, objectives);
				CommandUtilities.notifyOperators(sender, "commands.stats.success", statType.func_179637_b(), objectives, scoreholders);
			}
		}
	}
	
	private static class Clear extends CommandStats
	{
		public Clear(final CommandArg<List<CommandResultStats>> stats, final CommandArg<String> statName)
		{
			super(stats, statName);
		}
		
		@Override
		protected void eval(final ICommandSender sender, final List<CommandResultStats> stats, final Type statType) throws CommandException
		{
			for (final CommandResultStats stat : stats)
			{
				CommandResultStats.func_179667_a(stat, statType, null, null);
				CommandUtilities.notifyOperators(sender, "commands.stats.cleared", statType.func_179637_b());
			}
		}
	}
	
	private static class Block extends CommandArg<List<CommandResultStats>>
	{
		private final CommandArg<BlockPos> pos;
		
		public Block(final CommandArg<BlockPos> pos)
		{
			this.pos = pos;
		}
		
		@Override
		public List<CommandResultStats> eval(final ICommandSender sender) throws CommandException
		{
			final BlockPos pos = this.pos.eval(sender);
			
			final TileEntity tileEntity = sender.getEntityWorld().getTileEntity(pos);
			
			if (tileEntity == null)
				throw new CommandException("commands.stats.noCompatibleBlock", pos.getX(), pos.getY(), pos.getZ());
			
			CommandResultStats stats;
			
			if (tileEntity instanceof TileEntityCommandBlock)
				stats = ((TileEntityCommandBlock) tileEntity).func_175124_c();
			else if (tileEntity instanceof TileEntitySign)
				stats = ((TileEntitySign) tileEntity).func_174880_d();
			else
				throw new CommandException("commands.stats.noCompatibleBlock", pos.getX(), pos.getY(), pos.getZ());
			
			tileEntity.markDirty();
			
			sender.func_174794_a(CommandResultStats.Type.AFFECTED_BLOCKS, 1);
			
			return Collections.singletonList(stats);
		}
	}
	
	private static class Entities extends CommandArg<List<CommandResultStats>>
	{
		private final CommandArg<List<Entity>> entities;
		
		public Entities(final CommandArg<List<Entity>> entities)
		{
			this.entities = entities;
		}
		
		@Override
		public List<CommandResultStats> eval(final ICommandSender sender) throws CommandException
		{
			final List<Entity> entities = this.entities.eval(sender);
			
			final List<CommandResultStats> stats = new ArrayList<>(entities.size());
			
			for (final Entity entity : entities)
				stats.add(entity.func_174807_aT());
			
			sender.func_174794_a(CommandResultStats.Type.AFFECTED_ENTITIES, entities.size());
			
			return stats;
		}
	}
}
