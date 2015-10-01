package net.minecraft.client.entity;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityOtherPlayerMP extends AbstractClientPlayer
{
	private boolean isItemInUse;
	private int otherPlayerMPPosRotationIncrements;
	private double otherPlayerMPX;
	private double otherPlayerMPY;
	private double otherPlayerMPZ;
	private double otherPlayerMPYaw;
	private double otherPlayerMPPitch;
	private static final String __OBFID = "CL_00000939";
	
	public EntityOtherPlayerMP(final World worldIn, final GameProfile p_i45075_2_)
	{
		super(worldIn, p_i45075_2_);
		this.stepHeight = 0.0F;
		this.noClip = true;
		this.field_71082_cx = 0.25F;
		this.renderDistanceWeight = 10.0D;
	}
	
	/**
	 * Called when the entity is attacked.
	 */
	@Override
	public boolean attackEntityFrom(final DamageSource source, final float amount)
	{
		return true;
	}
	
	@Override
	public void func_180426_a(final double p_180426_1_, final double p_180426_3_, final double p_180426_5_, final float p_180426_7_, final float p_180426_8_, final int p_180426_9_, final boolean p_180426_10_)
	{
		this.otherPlayerMPX = p_180426_1_;
		this.otherPlayerMPY = p_180426_3_;
		this.otherPlayerMPZ = p_180426_5_;
		this.otherPlayerMPYaw = p_180426_7_;
		this.otherPlayerMPPitch = p_180426_8_;
		this.otherPlayerMPPosRotationIncrements = p_180426_9_;
	}
	
	/**
	 * Called to update the entity's position/logic.
	 */
	@Override
	public void onUpdate()
	{
		this.field_71082_cx = 0.0F;
		super.onUpdate();
		this.prevLimbSwingAmount = this.limbSwingAmount;
		final double var1 = this.posX - this.prevPosX;
		final double var3 = this.posZ - this.prevPosZ;
		float var5 = MathHelper.sqrt_double(var1 * var1 + var3 * var3) * 4.0F;
		
		if (var5 > 1.0F)
			var5 = 1.0F;
		
		this.limbSwingAmount += (var5 - this.limbSwingAmount) * 0.4F;
		this.limbSwing += this.limbSwingAmount;
		
		if (!this.isItemInUse && this.isEating() && this.inventory.mainInventory[this.inventory.currentItem] != null)
		{
			final ItemStack var6 = this.inventory.mainInventory[this.inventory.currentItem];
			this.setItemInUse(this.inventory.mainInventory[this.inventory.currentItem], var6.getItem().getMaxItemUseDuration(var6));
			this.isItemInUse = true;
		}
		else if (this.isItemInUse && !this.isEating())
		{
			this.clearItemInUse();
			this.isItemInUse = false;
		}
	}
	
	/**
	 * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons use this to react to sunlight and start to burn.
	 */
	@Override
	public void onLivingUpdate()
	{
		if (this.otherPlayerMPPosRotationIncrements > 0)
		{
			final double var1 = this.posX + (this.otherPlayerMPX - this.posX) / this.otherPlayerMPPosRotationIncrements;
			final double var3 = this.posY + (this.otherPlayerMPY - this.posY) / this.otherPlayerMPPosRotationIncrements;
			final double var5 = this.posZ + (this.otherPlayerMPZ - this.posZ) / this.otherPlayerMPPosRotationIncrements;
			double var7;
			
			for (var7 = this.otherPlayerMPYaw - this.rotationYaw; var7 < -180.0D; var7 += 360.0D)
				;
			
			while (var7 >= 180.0D)
				var7 -= 360.0D;
			
			this.rotationYaw = (float) (this.rotationYaw + var7 / this.otherPlayerMPPosRotationIncrements);
			this.rotationPitch = (float) (this.rotationPitch + (this.otherPlayerMPPitch - this.rotationPitch) / this.otherPlayerMPPosRotationIncrements);
			--this.otherPlayerMPPosRotationIncrements;
			this.setPosition(var1, var3, var5);
			this.setRotation(this.rotationYaw, this.rotationPitch);
		}
		
		this.prevCameraYaw = this.cameraYaw;
		this.updateArmSwingProgress();
		float var9 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
		float var2 = (float) Math.atan(-this.motionY * 0.20000000298023224D) * 15.0F;
		
		if (var9 > 0.1F)
			var9 = 0.1F;
		
		if (!this.onGround || this.getHealth() <= 0.0F)
			var9 = 0.0F;
		
		if (this.onGround || this.getHealth() <= 0.0F)
			var2 = 0.0F;
		
		this.cameraYaw += (var9 - this.cameraYaw) * 0.4F;
		this.cameraPitch += (var2 - this.cameraPitch) * 0.8F;
	}
	
	/**
	 * Sets the held item, or an armor slot. Slot 0 is held item. Slot 1-4 is armor. Params: Item, slot
	 */
	@Override
	public void setCurrentItemOrArmor(final int slotIn, final ItemStack itemStackIn)
	{
		if (slotIn == 0)
			this.inventory.mainInventory[this.inventory.currentItem] = itemStackIn;
		else
			this.inventory.armorInventory[slotIn - 1] = itemStackIn;
	}
	
	/**
	 * Notifies this sender of some sort of information. This is for messages intended to display to the user. Used for typical output (like "you asked for whether or not this game rule is set, so here's your answer"), warnings (like "I fetched this block for you by ID, but I'd like you to know that every time you do this, I die a little inside"), and errors (like "it's not called iron_pixacke, silly").
	 */
	@Override
	public void addChatMessage(final IChatComponent message)
	{
		Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(message);
	}
	
	/**
	 * Returns true if the command sender is allowed to use the given command.
	 */
	@Override
	public boolean canCommandSenderUseCommand(final int permissionLevel, final String command)
	{
		return false;
	}
	
	@Override
	public BlockPos getPosition()
	{
		return new BlockPos(this.posX + 0.5D, this.posY + 0.5D, this.posZ + 0.5D);
	}
}
