package net.minecraft.command.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.CommandUtilities;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.construction.CommandConstructable;
import net.minecraft.command.construction.CommandDescriptorDefault.CParserData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public final class CommandBlockData extends CommandArg<Integer>
{
	private final CommandArg<BlockPos> pos;
	private final CommandArg<NBTTagCompound> nbt;
	
	public static final CommandConstructable constructable = new CommandConstructable()
	{
		@Override
		public CommandArg<Integer> construct(final CParserData data) throws SyntaxErrorException
		{
			return new CommandBlockData(
				data.get(TypeIDs.BlockPos),
				data.get(TypeIDs.NBTCompound));
		}
	};
	
	public CommandBlockData(final CommandArg<BlockPos> pos, final CommandArg<NBTTagCompound> nbt)
	{
		this.pos = pos;
		this.nbt = nbt;
	}
	
	@Override
	public Integer eval(final ICommandSender sender) throws CommandException
	{
		final BlockPos pos = this.pos.eval(sender);
		final NBTTagCompound nbt = new NBTTagCompound.CopyOnWrite(this.nbt.eval(sender));
		
		sender.func_174794_a(CommandResultStats.Type.AFFECTED_BLOCKS, 0);
		
		final World world = sender.getEntityWorld();
		
		if (!world.isBlockLoaded(pos))
			throw new CommandException("commands.blockdata.outOfWorld");
		
		final TileEntity tileEntity = world.getTileEntity(pos);
		
		if (tileEntity == null)
			throw new CommandException("commands.blockdata.notValid");
		
		final NBTTagCompound nbtOld = new NBTTagCompound();
		tileEntity.writeToNBT(nbtOld);
		
		final NBTTagCompound nbtCopy = (NBTTagCompound) nbtOld.copy();
		
		nbtOld.merge(nbt);
		nbtOld.setInteger("x", pos.getX());
		nbtOld.setInteger("y", pos.getY());
		nbtOld.setInteger("z", pos.getZ());
		
		if (nbtOld.equals(nbtCopy))
			throw new CommandException("commands.blockdata.failed", nbtOld.toString());
		
		tileEntity.readFromNBT(nbtOld);
		tileEntity.markDirty();
		world.markBlockForUpdate(pos);
		
		sender.func_174794_a(CommandResultStats.Type.AFFECTED_BLOCKS, 1);
		
		CommandUtilities.notifyOperators(sender, "commands.blockdata.success", nbtOld.toString());
		
		return 1;
	}
}
