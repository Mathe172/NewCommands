package net.minecraft.command.commands;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.CommandUtilities;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.construction.CommandConstructable;
import net.minecraft.command.construction.CommandDescriptorDefault.CParserData;
import net.minecraft.command.type.custom.coordinate.TypeCoordinates;
import net.minecraft.command.type.custom.coordinate.TypeCoordinates.Shift;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class CommandExecuteAt extends CommandArg<Integer>
{
	public static final CommandConstructable constructable = new CommandConstructable()
	{
		@Override
		public CommandExecuteAt construct(final CParserData data) throws SyntaxErrorException
		{
			return new CommandExecuteAt(
				data.get(TypeIDs.ICmdSenderList),
				data.get(TypeIDs.Shift),
				data.get(TypeIDs.Integer));
		}
	};
	
	public static final CommandConstructable constructableDetect = new CommandConstructable()
	{
		@Override
		public Detect construct(final CParserData data) throws SyntaxErrorException
		{
			return new CommandExecuteAt.Detect(
				data.get(TypeIDs.ICmdSenderList),
				data.get(TypeIDs.Shift),
				data.get(TypeIDs.Shift),
				data.get(TypeIDs.BlockID),
				data.get(TypeIDs.Integer),
				data.get(TypeIDs.NBTCompound),
				data.get(TypeIDs.Integer));
		}
	};
	
	private final CommandArg<List<ICommandSender>> targets;
	private final CommandArg<Shift> position;
	private final CommandArg<Integer> command;
	
	public CommandExecuteAt(final CommandArg<List<ICommandSender>> targets, final CommandArg<Shift> position, final CommandArg<Integer> command)
	{
		this.targets = targets;
		this.position = position == null ? TypeCoordinates.trivialShift : position;
		this.command = command;
	}
	
	@Override
	public Integer eval(final ICommandSender sender) throws CommandException
	{
		final List<ICommandSender> targets = this.targets.eval(sender);
		final Shift shift = this.position.eval(sender);
		
		this.evalArgs(sender);
		
		this.checkArgs();
		
		int successCount = 0;
		for (final ICommandSender target : targets)
		{
			final Vec3 targetPos = target.getPositionVector();
			final ICommandSender wrapped = this.wrapTarget(target, sender, shift.addBase(targetPos));
			
			if (this.check(sender, wrapped))
				successCount += CommandHandler.executeCommand(wrapped, this.command); // Can also be changed to '+1 if successfull'
		}
		
		if (successCount < 1)
			throw new CommandException("commands.execute.allInvocationsFailed", ""); // The command string is dead... (for the moment)
			
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
			public boolean canCommandSenderUseCommand(final int permissionLevel, final String command)
			{
				return sender.canCommandSenderUseCommand(permissionLevel, command);
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
	
	@SuppressWarnings("unused")
	protected void evalArgs(final ICommandSender sender) throws CommandException
	{
	}
	
	protected void checkArgs() throws CommandException
	{
	}
	
	private static class Detect extends CommandExecuteAt
	{
		private final CommandArg<Shift> blockPosition;
		private final CommandArg<Block> blockID;
		private final CommandArg<Integer> metadata;
		private final CommandArg<NBTTagCompound> nbt;
		
		private Shift eBlockPosition;
		private Block eBlockID;
		private int eMetadata;
		private NBTTagCompound eNBT;
		
		public Detect(final CommandArg<List<ICommandSender>> targets, final CommandArg<Shift> position, final CommandArg<Shift> blockPosition, final CommandArg<Block> blockID, final CommandArg<Integer> metadata, final CommandArg<NBTTagCompound> nbt, final CommandArg<Integer> command)
		{
			super(targets, position, command);
			this.blockPosition = blockPosition;
			this.blockID = blockID;
			this.metadata = metadata;
			this.nbt = nbt;
		}
		
		@Override
		protected boolean check(final ICommandSender sender, final ICommandSender target)
		{
			final World world = target.getEntityWorld();
			BlockPos blockPos = null;
			try
			{
				blockPos = new BlockPos(this.eBlockPosition.addBase(target.getPositionVector()));
			} catch (final CommandException e)
			{
				CommandUtilities.errorMessage(sender, "commands.execute.failed", "detect", target.getName());
			}
			
			final IBlockState blockState = world.getBlockState(blockPos);
			
			boolean valid = blockState.getBlock() == Detect.this.eBlockID;
			
			if (valid && Detect.this.metadata != null)
				valid = blockState.getBlock().getMetaFromState(blockState) == Detect.this.eMetadata;
			
			if (valid && Detect.this.eNBT != null)
			{
				final TileEntity te = world.getTileEntity(blockPos);
				
				if (te != null)
				{
					final NBTTagCompound tag = new NBTTagCompound();
					te.writeToNBT(tag);
					
					valid = NBTBase.compareTags(Detect.this.eNBT, tag, true);
				}
				else
					valid = false;
			}
			
			if (!valid)
				CommandUtilities.errorMessage(sender, "commands.execute.failed", "detect", target.getName());
			
			return valid;
		}
		
		@Override
		protected void evalArgs(final ICommandSender sender) throws CommandException
		{
			this.eBlockPosition = this.blockPosition.eval(sender);
			this.eBlockID = this.blockID.eval(sender);
			
			if (this.metadata != null)
				this.eMetadata = this.metadata.eval(sender);
			
			this.eNBT = CommandArg.eval(this.nbt, sender);
		}
		
		@Override
		protected void checkArgs() throws CommandException
		{
			if (this.metadata != null)
				CommandUtilities.checkInt(this.eMetadata, 0, 15);
		}
	}
}
