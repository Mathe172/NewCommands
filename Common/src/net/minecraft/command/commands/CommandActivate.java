package net.minecraft.command.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.construction.CommandConstructable;
import net.minecraft.command.construction.CommandDescriptorDefault.CParserData;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;

public class CommandActivate extends CommandArg<Integer>
{
	protected static final CommandActivate command = new CommandActivate();
	
	public static final CommandConstructable constructable = CommandConstructable.primitiveConstructable(command);
	
	public static final CommandConstructable constructableDelayed = new CommandConstructable()
	{
		@Override
		public Delayed construct(final CParserData data) throws SyntaxErrorException
		{
			return new Delayed(data.get(TypeIDs.Integer));
		}
	};
	
	public static final CommandConstructable constructablePos = new CommandConstructable()
	{
		@Override
		public Pos construct(final CParserData data) throws SyntaxErrorException
		{
			return new Pos(
				data.get(TypeIDs.Integer),
				data.get(TypeIDs.BlockPos));
		}
	};
	
	public static final CommandConstructable constructableBox = new CommandConstructable()
	{
		@Override
		public Box construct(final CParserData data) throws SyntaxErrorException
		{
			return new Box(
				data.get(TypeIDs.Integer),
				data.get(TypeIDs.BlockPos),
				data.get(TypeIDs.BlockPos));
		}
	};
	
	private CommandActivate()
	{
	}
	
	@Override
	public Integer eval(final ICommandSender sender) throws CommandException
	{
		procSingleBlock(sender.getPosition(), sender, 1);
		
		return 1;
	}
	
	public static void procSingleBlock(final BlockPos pos, final ICommandSender sender, final int delay) throws CommandException
	{
		sender.func_174794_a(CommandResultStats.Type.AFFECTED_BLOCKS, 0);
		
		final World world = sender.getEntityWorld();
		
		if (!world.isBlockLoaded(pos))
			throw new CommandException("Out of World");
		
		trigger(pos, world, delay);
		
		sender.func_174794_a(CommandResultStats.Type.AFFECTED_BLOCKS, 1);
	}
	
	public static void trigger(final BlockPos pos, final World world, final int delay)
	{
		world.scheduleUpdate(pos, world.getBlockState(pos).getBlock(), delay);
	}
	
	public static class Delayed extends CommandArg<Integer>
	{
		protected final CommandArg<Integer> delay;
		
		public Delayed(final CommandArg<Integer> delay)
		{
			this.delay = delay;
		}
		
		@Override
		public Integer eval(final ICommandSender sender) throws CommandException
		{
			final int delay = this.delay.eval(sender);
			
			procSingleBlock(sender.getPosition(), sender, delay);
			
			return 1;
		}
	}
	
	public static class Pos extends Delayed
	{
		protected final CommandArg<BlockPos> pos;
		
		public Pos(final CommandArg<Integer> delay, final CommandArg<BlockPos> pos)
		{
			super(delay);
			this.pos = pos;
		}
		
		@Override
		public Integer eval(final ICommandSender sender) throws CommandException
		{
			final int delay = this.delay.eval(sender);
			final BlockPos pos = this.pos.eval(sender);
			
			procSingleBlock(pos, sender, delay);
			
			return 1;
		}
	}
	
	public static class Box extends Pos
	{
		private final CommandArg<BlockPos> pos2;
		
		public Box(final CommandArg<Integer> delay, final CommandArg<BlockPos> pos, final CommandArg<BlockPos> pos2)
		{
			super(delay, pos);
			this.pos2 = pos2;
		}
		
		@Override
		public Integer eval(final ICommandSender sender) throws CommandException
		{
			final int delay = this.delay.eval(sender);
			final BlockPos pos = this.pos.eval(sender);
			final BlockPos pos2 = this.pos2.eval(sender);
			
			sender.func_174794_a(CommandResultStats.Type.AFFECTED_BLOCKS, 0);
			
			final StructureBoundingBox box = new StructureBoundingBox(pos, pos2);
			
			final int size = box.getXSize() * box.getYSize() * box.getZSize();
			
			if (size > 1024)
				throw new CommandException("Too many blocks: " + size + " > 1024");
			
			final World world = sender.getEntityWorld();
			
			if (!world.isAreaLoaded(box))
				throw new CommandException("Out of World");
			
			for (int z = box.minZ; z <= box.maxZ; ++z)
				for (int y = box.minY; y <= box.maxY; ++y)
					for (int x = box.minX; x <= box.maxX; ++x)
						trigger(new BlockPos(x, y, z), world, delay);
			
			sender.func_174794_a(CommandResultStats.Type.AFFECTED_BLOCKS, size);
			
			return size;
		}
	}
}
