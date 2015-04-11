package net.minecraft.command.commands;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.IPermission;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.construction.CommandConstructable;
import net.minecraft.command.descriptors.CommandDescriptor;
import net.minecraft.command.type.custom.TypeIDs;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class CommandExecuteAt extends CommandBase
{
	public static final CommandConstructable constructable = new CommandConstructable()
	{
		@Override
		public CommandBase construct(final List<ArgWrapper<?>> params, final IPermission permission) throws SyntaxErrorException
		{
			return new CommandExecuteAt(
				CommandDescriptor.getParam(TypeIDs.ICmdSenderList, 0, params),
				CommandDescriptor.getParam(TypeIDs.Coordinates, 1, params),
				CommandDescriptor.getParam(TypeIDs.Integer, 2, params),
				permission);
		}
	};
	
	public static final CommandConstructable constructableDetect = new CommandConstructable()
	{
		@Override
		public CommandBase construct(final List<ArgWrapper<?>> params, final IPermission permission) throws SyntaxErrorException
		{
			return new CommandExecuteAt.Detect(
				CommandDescriptor.getParam(TypeIDs.ICmdSenderList, 0, params),
				CommandDescriptor.getParam(TypeIDs.Coordinates, 1, params),
				CommandDescriptor.getParam(TypeIDs.Coordinates, 2, params),
				CommandDescriptor.getParam(TypeIDs.String, 3, params),
				CommandDescriptor.getParam(TypeIDs.Integer, 4, params),
				CommandDescriptor.getParam(TypeIDs.NBTCompound, 5, params),
				CommandDescriptor.getParam(TypeIDs.Integer, 6, params),
				permission);
		}
	};
	
	private final CommandArg<List<ICommandSender>> targets;
	private final CommandArg<Vec3> position;
	private final CommandArg<Integer> command;
	
	public CommandExecuteAt(final CommandArg<List<ICommandSender>> targets, final CommandArg<Vec3> position, final CommandArg<Integer> command, final IPermission permission)
	{
		super(permission);
		this.targets = targets;
		this.position = position;
		this.command = command;
	}
	
	@Override
	public int procCommand(final ICommandSender sender) throws CommandException
	{
		int successCount = 0;
		final List<ICommandSender> targets = this.targets.eval(sender);
		
		for (final ICommandSender target : targets)
		{
			final Vec3 targetPos = target.getPositionVector();
			ICommandSender wrapped = wrapTarget(target, sender, targetPos);
			
			if (this.position != null)
				wrapped = wrapTarget(target, sender, this.position.eval(wrapped));
			
			final ICommandManager server = MinecraftServer.getServer().getCommandManager();
			
			try
			{
				if (check(sender, wrapped))
					successCount += server.executeCommand(wrapped, this.command); // Can also be changed to '+1 if successfull'
					
			} catch (final Throwable t)
			{
				throw new CommandException("commands.execute.failed", new Object[] { "", target.getName() });
			}
		}
		
		if (successCount < 1)
		{
			throw new CommandException("commands.execute.allInvocationsFailed", new Object[] { "" }); // The command string is dead... (for the moment)
		}
		
		sender.func_174794_a(CommandResultStats.Type.AFFECTED_ENTITIES, targets.size());
		return successCount;
		
	}
	
	private ICommandSender wrapTarget(final ICommandSender target, final ICommandSender sender, final Vec3 pos)
	{
		return new ICommandSender()
		{
			@Override
			public String getName()
			{
				return target.getName();
			}
			
			@Override
			public IChatComponent getDisplayName()
			{
				return target.getDisplayName();
			}
			
			@Override
			public void addChatMessage(final IChatComponent message)
			{
				sender.addChatMessage(message);
			}
			
			@Override
			public boolean canCommandSenderUseCommand(final int permissionLevel)
			{
				return sender.canCommandSenderUseCommand(permissionLevel);
			}
			
			BlockPos blockPos = new BlockPos(pos);
			
			@Override
			public BlockPos getPosition()
			{
				return new BlockPos(this.blockPos);
			}
			
			@Override
			public Vec3 getPositionVector()
			{
				return pos;
			}
			
			@Override
			public World getEntityWorld()
			{
				return target.getEntityWorld();
			}
			
			@Override
			public Entity getCommandSenderEntity()
			{
				return target instanceof Entity ? (Entity) target : null;
			}
			
			@Override
			public boolean sendCommandFeedback()
			{
				final MinecraftServer server = MinecraftServer.getServer();
				return server == null || server.worldServers[0].getGameRules().getGameRuleBooleanValue("commandBlockOutput");
			}
			
			@Override
			public void func_174794_a(final CommandResultStats.Type stat, final int value)
			{
				target.func_174794_a(stat, value);
			}
		};
	}
	
	@SuppressWarnings("unused")
	protected boolean check(final ICommandSender sender, final ICommandSender target)
	{
		return true;
	}
	
	private static class Detect extends CommandExecuteAt
	{
		private final CommandArg<Vec3> blockPosition;
		private final CommandArg<String> blockID;
		private final CommandArg<Integer> metadata;
		private final CommandArg<NBTTagCompound> nbt;
		
		public Detect(final CommandArg<List<ICommandSender>> targets, final CommandArg<Vec3> position, final CommandArg<Vec3> blockPosition, final CommandArg<String> blockID, final CommandArg<Integer> metadata, final CommandArg<NBTTagCompound> nbt, final CommandArg<Integer> command, final IPermission permission)
		{
			super(targets, position, command, permission);
			this.blockPosition = blockPosition;
			this.blockID = blockID;
			this.metadata = metadata;
			this.nbt = nbt;
		}
		
		@Override
		protected boolean check(final ICommandSender sender, final ICommandSender target)
		{
			return MinecraftServer.getServer().getCommandManager().executeCommand(sender, new CommandArg<Integer>()
			{
				@Override
				public Integer eval(final ICommandSender sender) throws CommandException
				{
					final World world = target.getEntityWorld();
					final BlockPos blockPos = new BlockPos(Detect.this.blockPosition.eval(target));
					
					final Block block = getBlockByText(Detect.this.blockID.eval(target));
					
					final IBlockState blockState = world.getBlockState(blockPos);
					
					boolean valid = blockState.getBlock() == block;
					
					if (valid && Detect.this.metadata != null)
						valid = blockState.getBlock().getMetaFromState(blockState) == parseInt(Detect.this.metadata.eval(target), -1, 15);
					
					if (valid && Detect.this.nbt != null)
					{
						final TileEntity te = world.getTileEntity(blockPos);
						
						if (te != null)
						{
							final NBTTagCompound tag = new NBTTagCompound();
							te.writeToNBT(tag);
							
							valid = NBTBase.compareTags(Detect.this.nbt.eval(target), tag, true);
						}
						else
							valid = false;
					}
					
					if (valid)
						return 1;
					
					throw new CommandException("commands.execute.failed", new Object[] { "detect", target.getName() });
				}
			}) != 0;
		}
	}
	
}