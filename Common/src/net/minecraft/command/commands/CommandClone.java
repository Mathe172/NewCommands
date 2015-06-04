package net.minecraft.command.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.CommandUtilities;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.construction.CommandConstructable;
import net.minecraft.command.descriptors.CommandDescriptor.CParserData;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;

public class CommandClone extends CommandArg<Integer>
{
	public static final CommandConstructable constructable = new CommandConstructable()
	{
		@Override
		public CommandArg<Integer> construct(final CParserData data) throws SyntaxErrorException
		{
			return CommandClone.construct(data.getPath(), data.getPath(), data, false);
		}
	};
	
	public static final CommandConstructable constructableFast = new CommandConstructable()
	{
		@Override
		public CommandArg<Integer> construct(final CParserData data) throws SyntaxErrorException
		{
			return CommandClone.construct(data.getPath(1), data.getPath(), data, true);
		}
	};
	
	private static final CommandArg<Integer> construct(final String filterMode, final String moveMode, final CParserData data, final boolean silent)
	{
		final Mode mode = moveMode == null ? Mode.normal :
			"normal".equals(moveMode) ? Mode.normal :
				"force".equals(moveMode) ? Mode.force :
					"move".equals(moveMode) ? Mode.move : null;
		
		if (filterMode == null)
			return new CommandClone(data, mode, silent);
		
		switch (filterMode)
		{
		case "replace":
			return new CommandClone(data, mode, silent);
		case "masked":
			return new Masked(data, mode, silent);
		case "filtered":
			return new Filtered(data, mode, silent);
		}
		
		return null;
	}
	
	private final CommandArg<BlockPos> pos1;
	private final CommandArg<BlockPos> pos2;
	private final CommandArg<BlockPos> posTarget;
	
	private final Mode mode;
	
	private final boolean fast;
	
	public CommandClone(final CommandArg<BlockPos> pos1, final CommandArg<BlockPos> pos2, final CommandArg<BlockPos> posTarget, final Mode mode, final boolean fast)
	{
		this.pos1 = pos1;
		this.pos2 = pos2;
		this.posTarget = posTarget;
		this.mode = mode;
		this.fast = fast;
	}
	
	protected CommandClone(final CParserData data, final Mode mode, final boolean fast)
	{
		this.pos1 = data.get(TypeIDs.BlockPos);
		this.pos2 = data.get(TypeIDs.BlockPos);
		this.posTarget = data.get(TypeIDs.BlockPos);
		this.mode = mode;
		this.fast = fast;
	}
	
	private static enum Mode
	{
		normal, force, move;
	}
	
	@Override
	public Integer eval(final ICommandSender sender) throws CommandException
	{
		final BlockPos pos1 = this.pos1.eval(sender);
		final BlockPos pos2 = this.pos2.eval(sender);
		final BlockPos pos3 = this.posTarget.eval(sender);
		
		this.evalArgs(sender);
		
		sender.func_174794_a(CommandResultStats.Type.AFFECTED_BLOCKS, 0);
		
		final StructureBoundingBox boxOrig = new StructureBoundingBox(pos1, pos2);
		final StructureBoundingBox boxTarget = new StructureBoundingBox(pos3, pos3.add(boxOrig.func_175896_b()));
		
		final int size = boxOrig.getXSize() * boxOrig.getYSize() * boxOrig.getZSize();
		
		if (size > 32768)
			throw new CommandException("commands.clone.tooManyBlocks", size, 32768);
		
		if (this.mode == Mode.normal && boxOrig.intersectsWith(boxTarget))
			throw new CommandException("commands.clone.noOverlap");
		
		if (boxOrig.minY < 0 || boxOrig.maxY >= 256 || boxTarget.minY < 0 || boxTarget.maxY >= 256)
			throw new CommandException("commands.clone.outOfWorld");
		
		final World world = sender.getEntityWorld();
		
		if (!world.isAreaLoaded(boxOrig) || !world.isAreaLoaded(boxTarget))
			throw new CommandException("commands.clone.outOfWorld");
		
		final BlockPos diff = new BlockPos(boxTarget.minX - boxOrig.minX, boxTarget.minY - boxOrig.minY, boxTarget.minZ - boxOrig.minZ);
		
		if (diff.getX() == 0 && diff.getY() == 0 && diff.getZ() == 0)
			throw new CommandException("commands.clone.failed");
		
		this.checkArgs();
		
		final List<?> tileTicks = world.func_175712_a(
			new StructureBoundingBox(
				boxOrig.minX, boxOrig.minY, boxOrig.minZ,
				boxOrig.maxX + 1, boxOrig.maxY + 1, boxOrig.maxZ + 1), false);
		
		int succ = 0;
		
		final boolean xReverse = diff.getX() > 0;
		final boolean yReverse = diff.getY() > 0;
		final boolean zReverse = diff.getZ() > 0;
		
		final int xIncrement = xReverse ? -1 : 1;
		final int yIncrement = yReverse ? -1 : 1;
		final int zIncrement = zReverse ? -1 : 1;
		
		final int xStart = xReverse ? boxOrig.maxX : boxOrig.minX;
		final int xEnd = xReverse ? boxOrig.minX : boxOrig.maxX + 1;
		final int yStart = yReverse ? boxOrig.maxY : boxOrig.minY;
		final int yEnd = yReverse ? boxOrig.minY : boxOrig.maxY + 1;
		final int zStart = zReverse ? boxOrig.maxZ : boxOrig.minZ;
		final int zEnd = zReverse ? boxOrig.minZ : boxOrig.maxZ + 1;
		
		if (this.fast)
		{
			for (int z = zStart; zReverse != z < zEnd; z += zIncrement)
				for (int y = yStart; yReverse != y < yEnd; y += yIncrement)
					for (int x = xStart; xReverse != x < xEnd; x += xIncrement)
					{
						final BlockPos sourcePos = new BlockPos(x, y, z);
						
						final IBlockState sourceState = world.getBlockState(sourcePos);
						
						if (this.filter(sourceState))
						{
							final BlockPos targetPos = sourcePos.add(diff);
							
							final boolean hasTe = sourceState.getBlock().hasTileEntity();
							
							if (world.setBlockState(targetPos, sourceState, 18) || hasTe)
								++succ;
							
							if (hasTe)
							{
								final TileEntity te = world.getTileEntity(sourcePos);
								
								if (te != null)
								{
									final NBTTagCompound nbt = new NBTTagCompound();
									
									te.writeToNBT(nbt);
									
									CommandUtilities.setNBT(world, targetPos, nbt);
								}
							}
						}
						
						if (this.mode == Mode.move && sourceState.getBlock() != Blocks.air && !boxTarget.func_175898_b(sourcePos))
							world.setBlockState(sourcePos, Blocks.air.getDefaultState(), 18);
					}
		}
		else
		{
			final List<BlockPos> sourcePositions = this.mode == Mode.move ? new ArrayList<BlockPos>(size - intersectionSize(boxTarget, boxOrig)) : null;
			final List<BlockPos> targetPositions = new ArrayList<>(size);
			final List<IBlockState> states = new ArrayList<>(size);
			final List<NBTTagCompound> nbts = new ArrayList<>();
			
			for (int z = zStart; zReverse != z < zEnd; z += zIncrement)
				for (int y = yStart; yReverse != y < yEnd; y += yIncrement)
					for (int x = xStart; xReverse != x < xEnd; x += xIncrement)
					{
						final BlockPos sourcePos = new BlockPos(x, y, z);
						
						final IBlockState sourceState = world.getBlockState(sourcePos);
						
						if (this.filter(sourceState))
						{
							final BlockPos targetPos = sourcePos.add(diff);
							
							final IBlockState targetState = world.getBlockState(targetPos);
							
							final boolean hasTe = sourceState.getBlock().hasTileEntity();
							
							if (sourceState != targetState || hasTe)
							{
								++succ;
								
								targetPositions.add(targetPos);
								states.add(sourceState);
								
								if (hasTe)
								{
									final TileEntity te = world.getTileEntity(sourcePos);
									
									if (te == null)
										nbts.add(null);
									else
									{
										final NBTTagCompound nbt = new NBTTagCompound();
										
										te.writeToNBT(nbt);
										
										nbts.add(nbt);
									}
								}
								
								if (sourceState != targetState)
									world.setBlockState(targetPos, CommandUtilities.getTempState(sourceState, targetState), 18);
							}
							else
								world.func_175722_b(targetPos, targetState.getBlock());
						}
						
						if (this.mode == Mode.move && sourceState.getBlock() != Blocks.air && !boxTarget.func_175898_b(sourcePos))
						{
							sourcePositions.add(sourcePos);
							
							world.setBlockState(sourcePos, CommandUtilities.getTempState(sourceState), 18);
						}
					}
			
			final Iterator<IBlockState> itState = states.iterator();
			final Iterator<NBTTagCompound> itNBT = nbts.iterator();
			
			for (final BlockPos pos : targetPositions)
			{
				final IBlockState state = itState.next();
				world.setBlockState(pos, state, 2);
				
				if (state.getBlock().hasTileEntity())
				{
					final NBTTagCompound nbt = itNBT.next();
					
					if (nbt != null)
						CommandUtilities.setNBT(world, pos, nbt);
				}
				
				world.func_175722_b(pos, state.getBlock());
			}
			
			if (this.mode == Mode.move)
				for (final BlockPos pos : sourcePositions)
				{
					world.setBlockState(pos, Blocks.air.getDefaultState(), 2);
					world.func_175722_b(pos, Blocks.air);
				}
		}
		
		if (tileTicks != null)
			for (final Object item : tileTicks)
			{
				final NextTickListEntry tlEntry = (NextTickListEntry) item;
				
				if (boxOrig.func_175898_b(tlEntry.field_180282_a))
					world.func_180497_b(tlEntry.field_180282_a.add(diff), tlEntry.func_151351_a(), (int) (tlEntry.scheduledTime - world.getWorldInfo().getWorldTotalTime()), tlEntry.priority);
			}
		
		if (succ == 0)
			throw new CommandException("commands.clone.failed");
		
		sender.func_174794_a(CommandResultStats.Type.AFFECTED_BLOCKS, succ);
		CommandUtilities.notifyOperators(sender, "commands.clone.success", succ);
		
		return succ;
	}
	
	@SuppressWarnings("unused")
	protected void evalArgs(final ICommandSender sender) throws CommandException
	{
	}
	
	protected void checkArgs() throws CommandException
	{
	}
	
	@SuppressWarnings("unused")
	protected boolean filter(final IBlockState state)
	{
		return true;
	}
	
	private static class Masked extends CommandClone
	{
		public Masked(final CParserData data, final Mode mode, final boolean silent)
		{
			super(data, mode, silent);
		}
		
		@Override
		protected boolean filter(final IBlockState state)
		{
			return state.getBlock().getMaterial() != Material.air;
		}
	}
	
	private static class Filtered extends CommandClone
	{
		private final CommandArg<Block> filterBlock;
		private final CommandArg<Integer> filterMeta;
		
		private Block eFilterBlock;
		private int eFilterMeta;
		
		public Filtered(final CParserData data, final Mode mode, final boolean silent)
		{
			super(data, mode, silent);
			
			this.filterBlock = data.get(TypeIDs.BlockID);
			this.filterMeta = data.get(TypeIDs.Integer);
		}
		
		@Override
		protected void evalArgs(final ICommandSender sender) throws CommandException
		{
			this.eFilterBlock = this.filterBlock.eval(sender);
			
			if (this.filterMeta != null)
				this.eFilterMeta = this.filterMeta.eval(sender);
		}
		
		@Override
		protected void checkArgs() throws CommandException
		{
			if (this.filterMeta != null)
				CommandUtilities.checkInt(this.eFilterMeta, 0, 15);
		}
		
		@Override
		protected boolean filter(final IBlockState state)
		{
			return (state.getBlock() == this.eFilterBlock && (this.filterMeta == null || state.getBlock().getMetaFromState(state) == this.eFilterMeta));
		}
	}
	
	private static int intersectionSize(final StructureBoundingBox box1, final StructureBoundingBox box2)
	{
		final int minX = Math.max(box1.minX, box2.minX);
		final int maxX = Math.min(box1.maxX, box2.maxX);
		
		if (maxX < minX)
			return 0;
		
		final int minY = Math.max(box1.minY, box2.minY);
		final int maxY = Math.min(box1.maxY, box2.maxY);
		
		if (maxY < minY)
			return 0;
		
		final int minZ = Math.max(box1.minZ, box2.minZ);
		final int maxZ = Math.min(box1.maxZ, box2.maxZ);
		
		if (maxZ < minZ)
			return 0;
		
		return (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
	}
}
