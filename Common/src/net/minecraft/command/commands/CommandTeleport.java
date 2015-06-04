package net.minecraft.command.commands;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats.Type;
import net.minecraft.command.CommandUtilities;
import net.minecraft.command.EntityNotFoundException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.construction.CommandConstructable;
import net.minecraft.command.descriptors.CommandDescriptor.CParserData;
import net.minecraft.command.type.custom.coordinate.TypeCoordinate;
import net.minecraft.command.type.custom.coordinate.TypeCoordinate.SingleShift;
import net.minecraft.command.type.custom.coordinate.TypeCoordinates.Shift;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S08PacketPlayerPosLook.EnumFlags;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public abstract class CommandTeleport extends CommandArg<Integer>
{
	public static final CommandConstructable constructable = new CommandConstructable()
	{
		@Override
		public CommandArg<Integer> construct(final CParserData data) throws SyntaxErrorException
		{
			final CommandArg<List<Entity>> sourceEntities = data.get(TypeIDs.EntityList);
			
			final ArgWrapper<?> arg2 = data.get();
			
			final CommandArg<SingleShift> yaw = data.get(TypeIDs.SingleShift);
			final CommandArg<SingleShift> pitch = data.get(TypeIDs.SingleShift);
			
			if (arg2 == null)
			{
				if (sourceEntities == null)
					throw data.parser.WUE("commands.tp.usage");
				
				return new EntityTarget(null, yaw, pitch, new CommandArg<ICommandSender>()
				{
					@Override
					public Entity eval(final ICommandSender sender) throws CommandException
					{
						final List<Entity> ret = sourceEntities.eval(sender);
						
						if (ret.size() != 1)
							throw new EntityNotFoundException();
						
						return ret.get(0);
					}
				});
			}
			
			if (arg2.type == TypeIDs.ICmdSender)
				return new EntityTarget(sourceEntities, yaw, pitch, arg2.get(TypeIDs.ICmdSender));
			
			return new CoordinatesTarget(sourceEntities, yaw, pitch, arg2.get(TypeIDs.Shift));
		}
	};
	
	private final CommandArg<List<Entity>> sources;
	protected final CommandArg<SingleShift> yaw;
	protected final CommandArg<SingleShift> pitch;
	
	public CommandTeleport(final CommandArg<List<Entity>> sources, final CommandArg<SingleShift> yaw, final CommandArg<SingleShift> pitch)
	{
		this.sources = sources;
		this.yaw = yaw == null ? TypeCoordinate.trivialShift : yaw;
		this.pitch = pitch == null ? TypeCoordinate.trivialShift : pitch;
	}
	
	protected List<Entity> getSources(final ICommandSender sender) throws CommandException
	{
		if (this.sources == null)
		{
			final Entity senderEntity = sender.getCommandSenderEntity();
			if (senderEntity == null)
				throw new EntityNotFoundException();
			
			sender.func_174794_a(Type.AFFECTED_ENTITIES, 1);
			
			return Collections.singletonList(senderEntity);
		}
		
		final List<Entity> ret = this.sources.eval(sender);
		
		sender.func_174794_a(Type.AFFECTED_ENTITIES, ret.size());
		return ret;
	}
	
	private static class CoordinatesTarget extends CommandTeleport
	{
		private final CommandArg<Shift> targetPos;
		
		public CoordinatesTarget(final CommandArg<List<Entity>> sources, final CommandArg<SingleShift> yaw, final CommandArg<SingleShift> pitch, final CommandArg<Shift> targetPos)
		{
			super(sources, yaw, pitch);
			this.targetPos = targetPos;
		}
		
		@Override
		public Integer eval(final ICommandSender sender) throws CommandException
		{
			final Shift targetShift = this.targetPos.eval(sender);
			
			final Vec3 targetShiftVal = targetShift.getShiftValues();
			
			final SingleShift yawShift = this.yaw.eval(sender);
			final SingleShift pitchShift = this.pitch.eval(sender);
			
			final float yawShiftVal = (float) yawShift.getShiftValue();
			final float pitchShiftVal = (float) pitchShift.getShiftValue();
			
			final EnumSet<EnumFlags> relativeData = EnumSet.noneOf(S08PacketPlayerPosLook.EnumFlags.class);
			
			if (targetShift.xRelative())
				relativeData.add(S08PacketPlayerPosLook.EnumFlags.X);
			
			if (targetShift.yRelative())
				relativeData.add(S08PacketPlayerPosLook.EnumFlags.Y);
			
			if (targetShift.zRelative())
				relativeData.add(S08PacketPlayerPosLook.EnumFlags.Z);
			
			if (pitchShift.relative())
				relativeData.add(S08PacketPlayerPosLook.EnumFlags.X_ROT);
			
			if (yawShift.relative())
				relativeData.add(S08PacketPlayerPosLook.EnumFlags.Y_ROT);
			
			int successCount = 0;
			
			for (final Entity entity : this.getSources(sender))
			{
				if (entity.worldObj == null)
					continue;
				
				final Vec3 targetPos = targetShift.addBase(entity.getPositionVector());
				
				float yaw = (float) MathHelper.wrapAngleTo180_double(yawShift.addBase(entity.rotationYaw));
				float pitch = (float) MathHelper.wrapAngleTo180_double(pitchShift.addBase(entity.rotationPitch));
				
				if (pitch > 90.0F || pitch < -90.0F)
				{
					pitch = MathHelper.wrapAngleTo180_float(180.0F - pitch);
					yaw = MathHelper.wrapAngleTo180_float(yaw + 180.0F);
				}
				
				entity.mountEntity((Entity) null);
				
				if (entity instanceof EntityPlayerMP)
					((EntityPlayerMP) entity).playerNetServerHandler.func_175089_a(targetShiftVal.xCoord, targetShiftVal.yCoord, targetShiftVal.zCoord, yawShiftVal, pitchShiftVal, relativeData);
				else
					entity.setLocationAndAngles(targetPos.xCoord, targetPos.yCoord, targetPos.zCoord, yaw, pitch);
				
				entity.setRotationYawHead(yaw);
				
				++successCount;
				CommandUtilities.notifyOperators(sender, "commands.tp.success.coordinates", entity.getName(), targetPos.xCoord, targetPos.yCoord, targetPos.zCoord);
			}
			return successCount;
		}
	}
	
	private static class EntityTarget extends CommandTeleport
	{
		private final CommandArg<ICommandSender> target;
		
		public EntityTarget(final CommandArg<List<Entity>> sources, final CommandArg<SingleShift> yaw, final CommandArg<SingleShift> pitch, final CommandArg<ICommandSender> commandArg)
		{
			super(sources, yaw, pitch);
			this.target = commandArg;
		}
		
		@Override
		public Integer eval(final ICommandSender sender) throws CommandException
		{
			final ICommandSender target = this.target.eval(sender);
			final SingleShift yawShift = this.yaw.eval(sender);
			final SingleShift pitchShift = this.pitch.eval(sender);
			
			int successCount = 0;
			
			for (final Entity entity : this.getSources(sender))
			{
				if (target.getEntityWorld() != entity.worldObj)
					CommandUtilities.errorMessage(sender, "commands.tp.notSameDimension");
				else
				{
					entity.mountEntity(null);
					
					float yaw = (float) MathHelper.wrapAngleTo180_double(yawShift.addBase(entity.rotationYaw));
					float pitch = (float) MathHelper.wrapAngleTo180_double(pitchShift.addBase(entity.rotationPitch));
					
					if (pitch > 90.0F || pitch < -90.0F)
					{
						pitch = MathHelper.wrapAngleTo180_float(180.0F - pitch);
						yaw = MathHelper.wrapAngleTo180_float(yaw + 180.0F);
					}
					
					final Vec3 targetPos = target.getPositionVector();
					
					if (entity instanceof EntityPlayerMP)
						((EntityPlayerMP) entity).playerNetServerHandler.setPlayerLocation(targetPos.xCoord, targetPos.yCoord, targetPos.zCoord, yaw, pitch);
					else
						entity.setLocationAndAngles(targetPos.xCoord, targetPos.yCoord, targetPos.zCoord, yaw, pitch);
					
					entity.setRotationYawHead(yaw);
					
					++successCount;
					CommandUtilities.notifyOperators(sender, "commands.tp.success", entity.getName(), target.getName());
				}
			}
			
			return successCount;
		}
	}
}
