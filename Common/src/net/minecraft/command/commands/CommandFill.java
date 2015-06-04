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
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;

public class CommandFill extends CommandArg<Integer>
{
	public static final CommandConstructable constructable = new CommandConstructable()
	{
		@Override
		public CommandArg<Integer> construct(final CParserData data) throws SyntaxErrorException
		{
			return CommandFill.construct(data.getPath(), data, false);
		}
	};
	
	public static final CommandConstructable fastConstructable = new CommandConstructable()
	{
		@Override
		public CommandArg<Integer> construct(final CParserData data) throws SyntaxErrorException
		{
			return CommandFill.construct(data.getPath(1), data, true);
		}
	};
	
	private static final CommandArg<Integer> construct(final String mode, final CParserData data, final boolean fast)
	{
		if (mode == null)
			return new CommandFill(data, fast);
		
		switch (mode)
		{
		case "replace":
			if (data.params.get(4) == null)
				return new CommandFill(
					data.get(TypeIDs.BlockPos),
					data.get(TypeIDs.BlockPos),
					data.get(TypeIDs.BlockID),
					data.get(TypeIDs.Integer),
					data.get(TypeIDs.NBTCompound, 6), fast);
			
			return new Replace(
				data.get(TypeIDs.BlockPos),
				data.get(TypeIDs.BlockPos),
				data.get(TypeIDs.BlockID),
				data.get(TypeIDs.Integer),
				data.get(TypeIDs.BlockID),
				data.get(TypeIDs.Integer),
				data.get(TypeIDs.NBTCompound), fast);
		case "destroy":
			return new Destroy(data, fast);
		case "keep":
			return new Keep(data, fast);
		case "outline":
			return new Outline(data, fast);
		case "hollow":
			return new Hollow(data, fast);
		}
		
		return null;
	}
	
	private final CommandArg<BlockPos> pos1;
	private final CommandArg<BlockPos> pos2;
	
	private final CommandArg<Block> blockID;
	private final CommandArg<Integer> meta;
	
	private final CommandArg<NBTTagCompound> nbt;
	
	private final boolean fast;
	
	public CommandFill(final CommandArg<BlockPos> pos1, final CommandArg<BlockPos> pos2,
		final CommandArg<Block> blockID, final CommandArg<Integer> meta,
		final CommandArg<NBTTagCompound> nbt, final boolean fast)
	{
		this.pos1 = pos1;
		this.pos2 = pos2;
		this.blockID = blockID;
		this.meta = meta;
		this.nbt = nbt;
		this.fast = fast;
	}
	
	private CommandFill(final CParserData data, final boolean fast)
	{
		this.pos1 = data.get(TypeIDs.BlockPos);
		this.pos2 = data.get(TypeIDs.BlockPos);
		this.blockID = data.get(TypeIDs.BlockID);
		this.meta = data.get(TypeIDs.Integer);
		this.nbt = data.get(TypeIDs.NBTCompound);
		this.fast = fast;
	}
	
	@Override
	public Integer eval(final ICommandSender sender) throws CommandException
	{
		final BlockPos pos1 = this.pos1.eval(sender);
		final BlockPos pos2 = this.pos2.eval(sender);
		
		final Block blockID = this.blockID.eval(sender);
		final int meta = this.meta == null ? 0 : this.meta.eval(sender);
		
		this.evalArgs(sender);
		
		final NBTTagCompound nbt = this.nbt == null ? null : new NBTTagCompound.CopyOnWrite(this.nbt.eval(sender));
		
		sender.func_174794_a(CommandResultStats.Type.AFFECTED_BLOCKS, 0);
		
		CommandUtilities.checkInt(meta, 0, 15);
		final IBlockState state = blockID.getStateFromMeta(meta);
		
		final StructureBoundingBox box = new StructureBoundingBox(pos1, pos2);
		final int size = box.getXSize() * box.getYSize() * box.getZSize();
		
		if (size > 32768)
			throw new CommandException("commands.fill.tooManyBlocks", size, 32768);
		
		if (box.minY < 0 || box.maxY >= 256)
			throw new CommandException("commands.fill.outOfWorld");
		
		final World world = sender.getEntityWorld();
		
		if (!world.isAreaLoaded(box))
			throw new CommandException("commands.fill.outOfWorld");
		
		this.checkArgs();
		
		final int flags = nbt == null ? 26 : 18;
		
		int succ = 0;
		
		if (this.fast)
			for (int z = box.minZ; z <= box.maxZ; ++z)
				for (int y = box.minY; y <= box.maxY; ++y)
					for (int x = box.minX; x <= box.maxX; ++x)
					{
						final BlockPos pos = new BlockPos(x, y, z);
						
						final IBlockState newState = this.clear(world, box, pos, state);
						
						if (newState == null)
							continue;
						
						final boolean hasTe = newState.getBlock().hasTileEntity();
						
						if (world.setBlockState(pos, newState, flags) || hasTe)
							++succ;
						
						if (hasTe && nbt != null)
							CommandUtilities.setNBT(world, pos, nbt);
					}
		else
		{
			final List<BlockPos> changedPositions = new ArrayList<>(size);
			final List<IBlockState> targetStates = new ArrayList<>(size);
			
			for (int z = box.minZ; z <= box.maxZ; ++z)
				for (int y = box.minY; y <= box.maxY; ++y)
					for (int x = box.minX; x <= box.maxX; ++x)
					{
						final BlockPos pos = new BlockPos(x, y, z);
						
						final IBlockState newState = this.clear(world, box, pos, state);
						
						if (newState != null)
						{
							changedPositions.add(pos);
							targetStates.add(newState);
							++succ;
						}
					}
			
			final Iterator<IBlockState> it = targetStates.iterator();
			
			for (final BlockPos pos : changedPositions)
			{
				final IBlockState targetState = it.next();
				
				world.setBlockState(pos, targetState, flags);
				
				if (targetState.getBlock().hasTileEntity() && nbt != null)
					CommandUtilities.setNBT(world, pos, nbt);
				
				world.func_175722_b(pos, blockID);
			}
		}
		
		if (succ == 0)
			throw new CommandException("commands.fill.failed");
		
		sender.func_174794_a(CommandResultStats.Type.AFFECTED_BLOCKS, succ);
		CommandUtilities.notifyOperators(sender, "commands.fill.success", succ);
		
		return succ;
	}
	
	protected void evalArgs(@SuppressWarnings("unused") final ICommandSender sender) throws CommandException
	{
	}
	
	protected void checkArgs() throws CommandException
	{
	}
	
	protected IBlockState clear(final World world, @SuppressWarnings("unused") final StructureBoundingBox box, final BlockPos pos, final IBlockState targetState)
	{
		if (this.fast)
			return targetState;
		
		return iClear(world, pos, targetState, world.getBlockState(pos));
	}
	
	protected final IBlockState clear(final World world, final BlockPos pos, final IBlockState targetState, final IBlockState oldState)
	{
		if (this.fast)
			return targetState;
		
		return iClear(world, pos, targetState, oldState);
	}
	
	private static IBlockState iClear(final World world, final BlockPos pos, final IBlockState targetState, final IBlockState oldState)
	{
		if (oldState == targetState)
			return targetState.getBlock().hasTileEntity() ? targetState : null;
		
		world.setBlockState(pos, CommandUtilities.getTempState(oldState, targetState), 18);
		
		return targetState;
	}
	
	private static final class Destroy extends CommandFill
	{
		public Destroy(final CParserData data, final boolean fast)
		{
			super(data, fast);
		}
		
		@Override
		protected IBlockState clear(final World world, final StructureBoundingBox box, final BlockPos pos, final IBlockState targetState)
		{
			world.destroyBlock(pos, true);
			
			return targetState;
		}
	}
	
	private static final class Keep extends CommandFill
	{
		public Keep(final CParserData data, final boolean fast)
		{
			super(data, fast);
		}
		
		@Override
		protected IBlockState clear(final World world, final StructureBoundingBox box, final BlockPos pos, final IBlockState targetState)
		{
			final IBlockState oldState = world.getBlockState(pos);
			
			if (oldState.getBlock().getMaterial() == Material.air)
				return super.clear(world, pos, targetState, oldState);
			
			return null;
		}
	}
	
	private static final class Replace extends CommandFill
	{
		private final CommandArg<Block> filterBlock;
		private final CommandArg<Integer> filterMeta;
		
		public Replace(final CommandArg<BlockPos> pos1, final CommandArg<BlockPos> pos2,
			final CommandArg<Block> blockID, final CommandArg<Integer> meta,
			final CommandArg<Block> blockFilter, final CommandArg<Integer> metaFilter,
			final CommandArg<NBTTagCompound> nbt, final boolean fast)
		{
			super(pos1, pos2, blockID, meta, nbt, fast);
			this.filterBlock = blockFilter;
			this.filterMeta = metaFilter;
		}
		
		private Block eFilterBlock;
		private int eFilterMeta;
		
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
		protected IBlockState clear(final World world, final StructureBoundingBox box, final BlockPos pos, final IBlockState targetState)
		{
			final IBlockState oldState = world.getBlockState(pos);
			
			if (oldState.getBlock() == this.eFilterBlock && (this.filterMeta == null || this.eFilterMeta == oldState.getBlock().getMetaFromState(oldState)))
				return super.clear(world, pos, targetState, oldState);
			
			return null;
		}
	}
	
	private static final class Outline extends CommandFill
	{
		public Outline(final CParserData data, final boolean fast)
		{
			super(data, fast);
		}
		
		@Override
		protected IBlockState clear(final World world, final StructureBoundingBox box, final BlockPos pos, final IBlockState targetState)
		{
			if (pos.getX() != box.minX && pos.getX() != box.maxX
				&& pos.getY() != box.minY && pos.getY() != box.maxY
				&& pos.getZ() != box.minZ && pos.getZ() != box.maxZ)
				return null;
			
			return super.clear(world, box, pos, targetState);
		}
	}
	
	private static final class Hollow extends CommandFill
	{
		public Hollow(final CParserData data, final boolean fast)
		{
			super(data, fast);
		}
		
		@Override
		protected IBlockState clear(final World world, final StructureBoundingBox box, final BlockPos pos, final IBlockState targetState)
		{
			return super.clear(world, box, pos,
				pos.getX() != box.minX && pos.getX() != box.maxX
					&& pos.getY() != box.minY && pos.getY() != box.maxY
					&& pos.getZ() != box.minZ && pos.getZ() != box.maxZ
					? Blocks.air.getDefaultState()
					: targetState);
		}
	}
}
