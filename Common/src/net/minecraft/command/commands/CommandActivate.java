package net.minecraft.command.commands;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.IPermission;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.construction.CommandConstructable;
import net.minecraft.command.descriptors.CommandDescriptor;
import net.minecraft.command.type.custom.TypeIDs;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;

public class CommandActivate extends CommandBase
{
	public static final CommandConstructable constructable = new CommandConstructable()
	{
		@Override
		public CommandBase construct(final List<ArgWrapper<?>> params, final IPermission permission) throws SyntaxErrorException
		{
			return new CommandActivate(permission);
		}
	};
	
	public static final CommandConstructable constructableDelayed = new CommandConstructable()
	{
		@Override
		public CommandBase construct(final List<ArgWrapper<?>> params, final IPermission permission) throws SyntaxErrorException
		{
			return new Delayed(CommandDescriptor.getParam(TypeIDs.Integer, 0, params), permission);
		}
	};
	
	public static final CommandConstructable constructablePos = new CommandConstructable()
	{
		@Override
		public CommandBase construct(final List<ArgWrapper<?>> params, final IPermission permission) throws SyntaxErrorException
		{
			return new Pos(CommandDescriptor.getParam(TypeIDs.Integer, 0, params)
				, CommandDescriptor.getParam(TypeIDs.Coordinates, 1, params)
				, permission);
		}
	};
	
	public static final CommandConstructable constructableBox = new CommandConstructable()
	{
		@Override
		public CommandBase construct(final List<ArgWrapper<?>> params, final IPermission permission) throws SyntaxErrorException
		{
			return new Box(CommandDescriptor.getParam(TypeIDs.Integer, 0, params)
				, CommandDescriptor.getParam(TypeIDs.Coordinates, 1, params)
				, CommandDescriptor.getParam(TypeIDs.Coordinates, 2, params)
				, permission);
		}
	};
	
	public CommandActivate(final IPermission permission)
	{
		super(permission);
	}
	
	@Override
	public int procCommand(final ICommandSender sender) throws CommandException
	{
		procSingleBlock(sender.getPosition(), sender, 1);
		
		return 1;
	}
	
	public static void procSingleBlock(final BlockPos pos, final ICommandSender sender, final int delay) throws CommandException
	{
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
	
	public static class Delayed extends CommandBase
	{
		protected final CommandArg<Integer> delay;
		
		public Delayed(final CommandArg<Integer> delay, final IPermission permission)
		{
			super(permission);
			this.delay = delay;
		}
		
		@Override
		public int procCommand(final ICommandSender sender) throws CommandException
		{
			procSingleBlock(sender.getPosition(), sender, this.delay.eval(sender));
			
			return 1;
		}
	}
	
	public static class Pos extends Delayed
	{
		protected final CommandArg<Vec3> pos;
		
		public Pos(final CommandArg<Integer> delay, final CommandArg<Vec3> pos, final IPermission permission)
		{
			super(delay, permission);
			this.pos = pos;
		}
		
		@Override
		public int procCommand(final ICommandSender sender) throws CommandException
		{
			procSingleBlock(new BlockPos(this.pos.eval(sender)), sender, this.delay.eval(sender));
			
			return 1;
		}
	}
	
	public static class Box extends Pos
	{
		private final CommandArg<Vec3> pos2;
		
		public Box(final CommandArg<Integer> delay, final CommandArg<Vec3> pos, final CommandArg<Vec3> pos2, final IPermission permission)
		{
			super(delay, pos, permission);
			this.pos2 = pos2;
		}
		
		@Override
		public int procCommand(final ICommandSender sender) throws CommandException
		{
			final StructureBoundingBox box = new StructureBoundingBox(new BlockPos(this.pos.eval(sender)), new BlockPos(this.pos2.eval(sender)));
			
			final int size = box.getXSize() * box.getYSize() * box.getZSize();
			
			if (size > 1024)
				throw new CommandException("Too many blocks: " + size + " > 1024");
			
			final World world = sender.getEntityWorld();
			
			if (!world.isAreaLoaded(box))
				throw new CommandException("Out of World");
			
			for (int z = box.minZ; z <= box.maxZ; ++z)
				for (int y = box.minY; y <= box.maxY; ++y)
					for (int x = box.minX; x <= box.maxX; ++x)
						trigger(new BlockPos(x, y, z), world, this.delay.eval(sender));
			
			sender.func_174794_a(CommandResultStats.Type.AFFECTED_BLOCKS, size);
			
			return size;
		}
	}
}
