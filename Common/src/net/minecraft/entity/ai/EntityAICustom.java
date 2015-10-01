package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockPos;

public class EntityAICustom extends EntityAIBase
{
	private boolean running = false;
	private boolean requiresRestart = false;
	private EntityAIBase ai = null;
	
	private final EntityLiving entity;
	
	private final EntityAIForceMove forceMove;
	private final EntityAIAttackOnCollide forceTarget;
	
	public EntityAICustom(final EntityLiving entity, final double speed)
	{
		this.entity = entity;
		
		this.forceMove = new EntityAIForceMove(entity, speed);
		
		if (entity instanceof EntityCreature)
			this.forceTarget = new EntityAIAttackOnCollide((EntityCreature) entity, speed, true);
		else
			this.forceTarget = null;
	}
	
	@Override
	public boolean shouldExecute()
	{
		if (this.ai == null)
			return false;
		
		if (this.ai.shouldExecute())
			return true;
		
		this.ai = null;
		
		return false;
	}
	
	@Override
	public boolean continueExecuting()
	{
		if (this.requiresRestart)
			return this.requiresRestart = false;
		
		if (this.ai == null)
			return false;
		
		if (this.ai.continueExecuting())
			return true;
		
		this.ai.resetTask();
		this.ai = null;
		
		return false;
	}
	
	@Override
	public void startExecuting()
	{
		this.running = true;
		this.ai.startExecuting();
	}
	
	@Override
	public void resetTask()
	{
		this.running = false;
		
		if (this.ai != null)
			this.setMutexBits(this.ai.getMutexBits());
	}
	
	@Override
	public void updateTask()
	{
		this.ai.updateTask();
	}
	
	public void setAI(final EntityAIBase ai)
	{
		if (this.running)
		{
			this.ai.resetTask();
			this.ai = ai;
			
			if (ai != null)
				if (ai.getMutexBits() != this.getMutexBits())
					this.requiresRestart = true;
				else
					this.ai.startExecuting();
		}
		else
		{
			this.ai = ai;
			
			if (ai != null)
				this.setMutexBits(ai.getMutexBits());
		}
	}
	
	public boolean forceMove(final BlockPos pos)
	{
		if (this.running && this.ai == this.forceMove)
			return this.forceMove.update(pos);
		
		this.setAI(this.forceMove);
		return this.forceMove.start(pos);
	}
	
	public boolean cancelMove()
	{
		if (this.ai != this.forceMove)
			return false;
		
		this.setAI(null);
		return true;
	}
	
	public boolean forceTarget(final EntityLivingBase target)
	{
		if (this.forceTarget == null)
			return false;
		
		if (!this.running || this.ai != this.forceTarget)
			this.setAI(this.forceTarget);
		
		final EntityLivingBase targetOld = this.entity.getAttackTarget();
		this.entity.setAttackTarget(target);
		
		return target != targetOld;
	}
	
	public boolean cancelTarget()
	{
		this.entity.setAttackTarget(null);
		
		if (this.forceTarget == null)
			return false;
		
		if (this.ai != this.forceTarget)
			return false;
		
		this.setAI(null);
		
		return true;
	}
}
