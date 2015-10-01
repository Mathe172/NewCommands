package net.minecraft.command.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandUtilities;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.construction.CommandConstructable;
import net.minecraft.command.construction.CommandDescriptorDefault.CParserData;
import net.minecraft.command.type.custom.coordinate.TypeCoordinates.Shift;
import net.minecraft.item.Item;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public abstract class CommandParticle extends CommandArg<Integer>
{
	public static final CommandConstructable constructable = new CommandConstructable()
	{
		@Override
		public CommandArg<Integer> construct(final CParserData data) throws SyntaxErrorException
		{
			if (data.path.isEmpty())
				return new Primitive(data);
			
			return "iconcrack".equals(data.path.get(0)) ? new ItemParticle(data) : new BlockParticle(data);
		}
	};
	
	private final CommandArg<Shift> origin;
	
	private final CommandArg<Double> dx_r, dy_g, dz_b, vel_bright;
	private final CommandArg<Integer> count;
	private final CommandArg<String> mode;
	private final CommandArg<List<ICommandSender>> targets;
	
	private CommandParticle(final CParserData data, final int startIndex)
	{
		this.origin = data.get(TypeIDs.Shift, startIndex);
		this.dx_r = data.get(TypeIDs.Double);
		this.dy_g = data.get(TypeIDs.Double);
		this.dz_b = data.get(TypeIDs.Double);
		this.vel_bright = data.get(TypeIDs.Double);
		this.count = data.get(TypeIDs.Integer);
		this.mode = data.get(TypeIDs.String);
		this.targets = data.get(TypeIDs.ICmdSenderList);
	}
	
	protected Integer procCommand(final ICommandSender sender, final String particleID, final EnumParticleTypes particle, final int[] paramArray) throws CommandException, NumberInvalidException
	{
		final Shift shift = this.origin.eval(sender);
		
		final double dx_r = this.dx_r.eval(sender);
		final double dy_g = this.dy_g.eval(sender);
		final double dz_b = this.dz_b.eval(sender);
		
		final double vel_bright = this.vel_bright.eval(sender);
		
		final int count = this.count == null ? 0 : this.count.eval(sender);
		
		final boolean force = this.mode != null && "force".equalsIgnoreCase(this.mode.eval(sender));
		
		final List<ICommandSender> targets = this.targets == null ? Collections.singletonList(sender) : this.targets.eval(sender);
		
		CommandUtilities.checkInt(count, 0);
		
		final World world = sender.getEntityWorld();
		
		if (world instanceof WorldServer)
		{
			final WorldServer worldServer = (WorldServer) world;
			
			for (final ICommandSender target : targets)
			{
				final Vec3 targetPos = shift.addBase(target.getPositionVector());
				worldServer.func_180505_a(particle, force, targetPos.xCoord, targetPos.yCoord, targetPos.zCoord, count, dx_r, dy_g, dz_b, vel_bright, paramArray);
			}
			
			CommandUtilities.notifyOperators(sender, "commands.particle.success", particleID, Math.max(count, 1) * targets.size());
			
			return targets.size();
		}
		
		return 0;
	}
	
	private static class Primitive extends CommandParticle
	{
		private final CommandArg<String> particleType;
		
		private Primitive(final CParserData data)
		{
			super(data, 1);
			this.particleType = data.get(TypeIDs.String, 0);
		}
		
		@Override
		public Integer eval(final ICommandSender sender) throws CommandException
		{
			final String particleID = this.particleType.eval(sender);
			
			EnumParticleTypes particle;
			
			List<Integer> params = null;
			
			String particleID_ = particleID;
			
			while ((particle = EnumParticleTypes.getByName(particleID_)) == null)
			{
				final int index = particleID_.lastIndexOf('_', particleID_.length() - 2) + 1;
				
				if (index == 0)
					throw new CommandException("commands.particle.notFound", particleID);
				
				if (params == null)
					params = new ArrayList<>();
				
				try
				{
					params.add(Integer.parseInt(particleID_.substring(index, particleID_.length() - (params.isEmpty() ? 0 : 1))));
				} catch (final NumberFormatException ex)
				{
					throw new CommandException("commands.particle.notFound", particleID);
				}
				
				particleID_ = particleID_.substring(0, index);
			}
			
			final int paramCount = params == null ? 0 : params.size();
			
			if (particle.func_179345_d() != paramCount)
				throw new CommandException("commands.particle.notFound", particleID);
			
			final int[] paramArray = new int[paramCount];
			
			for (int i = 0; i < paramCount; ++i)
				paramArray[i] = params.get(paramCount - i - 1);
			
			return this.procCommand(sender, particleID, particle, paramArray);
		}
	}
	
	private static abstract class Parametrized extends CommandParticle
	{
		protected String particleID;
		
		private Parametrized(final CParserData data)
		{
			super(data, 2);
			this.particleID = data.path.get(0);
		}
		
		@Override
		public Integer eval(final ICommandSender sender) throws CommandException
		{
			final EnumParticleTypes particle = EnumParticleTypes.getByName(this.particleID + "_");
			
			if (particle == null)
				throw new CommandException("commands.particle.notFound", this.particleID);
			
			final int[] params = this.getParams(sender);
			return this.procCommand(sender, this.getParticleID(params), particle, params);
		}
		
		protected abstract int[] getParams(ICommandSender sender) throws CommandException;
		
		protected abstract String getParticleID(int[] params) throws CommandException;
		
	}
	
	private static class BlockParticle extends Parametrized
	{
		private final CommandArg<Block> blockID;
		private final CommandArg<Integer> meta;
		
		private BlockParticle(final CParserData data)
		{
			super(data);
			this.blockID = data.get(TypeIDs.BlockID, 0);
			this.meta = data.get(TypeIDs.Integer);
		}
		
		@Override
		protected int[] getParams(final ICommandSender sender) throws CommandException
		{
			return new int[] { Block.blockRegistry.getIDForObject(this.blockID.eval(sender)) + 4096 * this.meta.eval(sender) };
		}
		
		@Override
		protected String getParticleID(final int[] params) throws CommandException
		{
			return this.particleID + "_" + params[0];
		}
	}
	
	private static class ItemParticle extends Parametrized
	{
		private final CommandArg<Item> itemID;
		private final CommandArg<Integer> meta;
		
		private ItemParticle(final CParserData data)
		{
			super(data);
			this.itemID = data.get(TypeIDs.ItemID, 0);
			this.meta = data.get(TypeIDs.Integer);
		}
		
		@Override
		protected int[] getParams(final ICommandSender sender) throws CommandException
		{
			return new int[] { Item.itemRegistry.getIDForObject(this.itemID.eval(sender)), this.meta.eval(sender) };
		}
		
		@Override
		protected String getParticleID(final int[] params) throws CommandException
		{
			return this.particleID + "_" + params[0] + "_" + params[1];
		}
	}
}
