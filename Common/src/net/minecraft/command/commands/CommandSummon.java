package net.minecraft.command.commands;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.IPermission;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.construction.CommandConstructable;
import net.minecraft.command.descriptors.CommandDescriptor;
import net.minecraft.command.type.custom.TypeIDs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagCompound.CopyOnWrite;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class CommandSummon extends CommandBase
{
	
	private final CommandArg<Vec3> coords;
	private final CommandArg<String> name;
	private final CommandArg<NBTTagCompound> tag;
	
	public static final CommandConstructable constructable = new CommandConstructable()
	{
		@Override
		public CommandBase construct(final List<ArgWrapper<?>> params, final IPermission permission)
		{
			return new CommandSummon(params.get(0).get(TypeIDs.String), CommandDescriptor.getParam(TypeIDs.Coordinates, 1, params), CommandDescriptor.getParam(TypeIDs.NBTCompound, 2, params), permission);
		}
	};
	
	public CommandSummon(final CommandArg<String> name, final CommandArg<Vec3> coords, final CommandArg<NBTTagCompound> nbt, final IPermission permission)
	{
		super(permission);
		this.coords = coords;
		this.name = name;
		this.tag = nbt;
	}
	
	@Override
	public int procCommand(final ICommandSender sender) throws CommandException
	{
		final String name = this.name.eval(sender);
		final Vec3 targetVec = this.coords != null ? this.coords.eval(sender) : sender.getPositionVector();
		
		final double x = targetVec.xCoord;
		final double y = targetVec.yCoord;
		final double z = targetVec.zCoord;
		
		final World world = sender.getEntityWorld();
		
		if (!world.isBlockLoaded(new BlockPos(targetVec)))
		{
			throw new CommandException("commands.summon.outOfWorld", new Object[0]);
		}
		else if ("LightningBolt".equals(name))
		{
			world.addWeatherEffect(new EntityLightningBolt(world, x, y, z));
			this.notifyOperators(sender, "commands.summon.success", new Object[0]);
		}
		else
		{
			Entity entity;
			final NBTTagCompound tag = this.tag == null ? new NBTTagCompound() : new CopyOnWrite(this.tag.eval(sender));
			
			try
			{
				entity = EntityList.createEntityFromNBT(tag, world, name);
			} catch (final RuntimeException e)
			{
				throw new CommandException("commands.summon.failed", new Object[0]);
			}
			
			if (entity == null)
			{
				throw new CommandException("commands.summon.failed", new Object[0]);
			}
			else
			{
				entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);
				
				if (this.tag == null && entity instanceof EntityLiving)
				{
					((EntityLiving) entity).func_180482_a(world.getDifficultyForLocation(new BlockPos(entity)), (IEntityLivingData) null);
				}
				
				world.spawnEntityInWorld(entity);
				Entity curEntity = entity;
				
				for (NBTTagCompound curTag = tag; curEntity != null && curTag.hasKey("Riding", 10);)
				{
					final Entity newEnity = EntityList.createEntityFromNBT(curTag = curTag.getCompoundTag("Riding"), world);
					
					if (newEnity != null)
					{
						newEnity.setLocationAndAngles(x, y, z, newEnity.rotationYaw, newEnity.rotationPitch);
						world.spawnEntityInWorld(newEnity);
						curEntity.mountEntity(newEnity);
					}
					
					curEntity = newEnity;
				}
				
				this.notifyOperators(sender, "commands.summon.success", new Object[0]);
			}
		}
		
		return 1;
	}
}
