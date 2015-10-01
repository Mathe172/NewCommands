package net.minecraft.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.BlockPos;

public class EntityAIForceMove extends EntityAIBase
{
	private final EntityLiving entity;
	
	private final double speed;
	private final IAttributeInstance range;
	
	private PathEntity path;
	
	private BlockPos pos;
	
	public EntityAIForceMove(final EntityLiving entity, final double speed)
	{
		this.entity = entity;
		this.speed = speed;
		this.range = entity.getEntityAttribute(SharedMonsterAttributes.followRange);
		this.setMutexBits(1);
	}
	
	@Override
	public boolean shouldExecute()
	{
		return this.path != null;
	}
	
	@Override
	public boolean continueExecuting()
	{
		return !this.entity.getNavigator().noPath() || this.entity.getNavigator().setPath(this.findPath(), this.speed);
	}
	
	@Override
	public void startExecuting()
	{
		this.entity.getNavigator().setPath(this.path, this.speed);
	}
	
	@Override
	public void resetTask()
	{
		this.entity.getNavigator().clearPathEntity();
	}
	
	public void cancel()
	{
		this.entity.getNavigator().clearPathEntity();
	}
	
	public boolean start(final BlockPos pos)
	{
		this.pos = pos;
		
		return (this.path = this.findPath()) != null;
	}
	
	public PathEntity findPath()
	{
		final double lastRange = this.range.getBaseValue();
		this.range.setBaseValue(64);
		
		final PathEntity path = this.entity.getNavigator().func_179680_a(this.pos);
		
		this.range.setBaseValue(lastRange);
		
		return path;
	}
	
	public boolean update(final BlockPos pos)
	{
		this.pos = pos;
		
		return this.entity.getNavigator().setPath(this.findPath(), this.speed);
	}
}
