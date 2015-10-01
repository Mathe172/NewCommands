package net.minecraft.command.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandUtilities;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.arg.Setter;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.construction.CommandConstructable;
import net.minecraft.command.construction.CommandDescriptorDefault.CParserData;
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

public class CommandSummon extends CommandArg<Integer>
{
	private final CommandArg<Vec3> coords;
	private final CommandArg<String> name;
	private final CommandArg<NBTTagCompound> tag;
	
	public static final CommandConstructable constructable = new CommandConstructable()
	{
		@Override
		public CommandSummon construct(final CParserData data)
		{
			return new CommandSummon(
				data.get(TypeIDs.String),
				data.get(TypeIDs.Coordinates),
				data.get(TypeIDs.NBTCompound));
		}
	};
	
	public static final CommandConstructable labelConstructable = new CommandConstructable()
	{
		@Override
		public CommandArg<Integer> construct(final CParserData data) throws SyntaxErrorException
		{
			data.registerLabel(data.parser);
			
			return new CommandSummon.Label(
				data.getLabel(TypeIDs.Entity),
				data.get(TypeIDs.String),
				data.get(TypeIDs.Coordinates),
				data.get(TypeIDs.NBTCompound));
		}
	};
	
	public CommandSummon(final CommandArg<String> name, final CommandArg<Vec3> coords, final CommandArg<NBTTagCompound> nbt)
	{
		this.coords = coords;
		this.name = name;
		this.tag = nbt;
	}
	
	@Override
	public Integer eval(final ICommandSender sender) throws CommandException
	{
		this.proc(sender);
		
		return 1;
	}
	
	protected Entity proc(final ICommandSender sender) throws CommandException
	{
		final String name = this.name.eval(sender);
		final Vec3 targetVec = this.coords != null ? this.coords.eval(sender) : sender.getPositionVector();
		
		final NBTTagCompound tag = this.tag == null ? new NBTTagCompound() : new CopyOnWrite(this.tag.eval(sender));
		
		final double x = targetVec.xCoord;
		final double y = targetVec.yCoord;
		final double z = targetVec.zCoord;
		
		final World world = sender.getEntityWorld();
		
		if (!world.isBlockLoaded(new BlockPos(targetVec)))
			throw new CommandException("commands.summon.outOfWorld");
		
		if ("LightningBolt".equals(name))
		{
			world.addWeatherEffect(new EntityLightningBolt(world, x, y, z));
			CommandUtilities.notifyOperators(sender, "commands.summon.success");
			return null;
		}
		
		Entity entity;
		
		try
		{
			entity = EntityList.createEntityFromNBT(tag, world, name);
		} catch (final RuntimeException e)
		{
			throw new CommandException("commands.summon.failed");
		}
		
		if (entity == null)
			throw new CommandException("commands.summon.failed");
		
		entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);
		
		if (this.tag == null && entity instanceof EntityLiving)
			((EntityLiving) entity).func_180482_a(world.getDifficultyForLocation(new BlockPos(entity)), (IEntityLivingData) null);
		
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
		
		CommandUtilities.notifyOperators(sender, "commands.summon.success");
		
		return entity;
	}
	
	private static class Label extends CommandSummon
	{
		private final Setter<Entity> label;
		
		public Label(final Setter<Entity> label, final CommandArg<String> name, final CommandArg<Vec3> coords, final CommandArg<NBTTagCompound> nbt)
		{
			super(name, coords, nbt);
			this.label = label;
		}
		
		@Override
		public Integer eval(final ICommandSender sender) throws CommandException
		{
			this.label.set(this.proc(sender));
			
			return 1;
		}
		
	}
}
