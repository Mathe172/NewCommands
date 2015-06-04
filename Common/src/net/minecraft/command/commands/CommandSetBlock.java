package net.minecraft.command.commands;

import net.minecraft.block.Block;
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

public class CommandSetBlock extends CommandArg<Integer>
{
	public static final CommandConstructable constructable = new CommandConstructable()
	{
		@Override
		public CommandArg<Integer> construct(final CParserData data) throws SyntaxErrorException
		{
			final String mode = data.getPath();
			
			return new CommandSetBlock(
				data.get(TypeIDs.BlockPos),
				data.get(TypeIDs.BlockID),
				data.get(TypeIDs.Integer),
				data.get(TypeIDs.NBTCompound),
				mode == null ? Mode.replace : "destroy".equals(mode) ? Mode.destroy : "keep".equals(mode) ? Mode.keep : null);
		}
	};
	
	private final CommandArg<BlockPos> pos;
	private final CommandArg<Block> blockID;
	private final CommandArg<Integer> meta;
	private final CommandArg<NBTTagCompound> nbt;
	
	private final Mode mode;
	
	public CommandSetBlock(final CommandArg<BlockPos> pos, final CommandArg<Block> blockID, final CommandArg<Integer> meta, final CommandArg<NBTTagCompound> nbt, final Mode mode)
	{
		this.pos = pos;
		this.blockID = blockID;
		this.meta = meta;
		this.nbt = nbt;
		this.mode = mode;
	}
	
	private enum Mode
	{
		destroy, keep, replace;
	}
	
	@Override
	public Integer eval(final ICommandSender sender) throws CommandException
	{
		final BlockPos pos = this.pos.eval(sender);
		final Block blockID = this.blockID.eval(sender);
		final int meta = this.meta == null ? 0 : this.meta.eval(sender);
		final NBTTagCompound nbt = this.nbt == null ? null : new NBTTagCompound.CopyOnWrite(this.nbt.eval(sender));
		
		sender.func_174794_a(CommandResultStats.Type.AFFECTED_BLOCKS, 0);
		
		CommandUtilities.checkInt(meta, 0, 15);
		
		final World world = sender.getEntityWorld();
		
		if (!world.isBlockLoaded(pos))
			throw new CommandException("commands.setblock.outOfWorld");
		
		if (this.mode == Mode.destroy)
		{
			world.destroyBlock(pos, true);
			
			if (blockID == Blocks.air)
			{
				CommandUtilities.notifyOperators(sender, "commands.setblock.success");
				sender.func_174794_a(CommandResultStats.Type.AFFECTED_BLOCKS, 1);
				
				return 1;
			}
		}
		
		if (this.mode == Mode.keep && !world.isAirBlock(pos))
			throw new CommandException("commands.setblock.noChange");
		
		final IBlockState state = blockID.getStateFromMeta(meta);
		
		if (!world.setBlockState(pos, state, nbt == null ? 26 : 18) && !blockID.hasTileEntity())
			throw new CommandException("commands.setblock.noChange");
		
		if (nbt != null && blockID.hasTileEntity())
			CommandUtilities.setNBT(world, pos, nbt);
		
		world.func_175722_b(pos, blockID);
		
		sender.func_174794_a(CommandResultStats.Type.AFFECTED_BLOCKS, 1);
		CommandUtilities.notifyOperators(sender, "commands.setblock.success");
		
		return 1;
	}
}
