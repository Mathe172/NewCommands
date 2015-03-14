package net.minecraft.tileentity;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.FutureCommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S33PacketUpdateSign;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentProcessor;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import com.google.gson.JsonParseException;

public class TileEntitySign extends TileEntity
{
	public final IChatComponent[] signText = new IChatComponent[] { new ChatComponentText(""), new ChatComponentText(""), new ChatComponentText(""), new ChatComponentText("") };
	private final FutureCommand[] commands = new FutureCommand[this.signText.length];
	
	/**
	 * The index of the line currently being edited. Only used on client side, but defined on both. Note this is only really used when the > < are going to be visible.
	 */
	public int lineBeingEdited = -1;
	private boolean isEditable = true;
	private EntityPlayer field_145917_k;
	private final CommandResultStats field_174883_i = new CommandResultStats();
	private static final String __OBFID = "CL_00000363";
	
	@Override
	public void writeToNBT(final NBTTagCompound compound)
	{
		super.writeToNBT(compound);
		
		for (int var2 = 0; var2 < 4; ++var2)
		{
			final String var3 = IChatComponent.Serializer.componentToJson(this.signText[var2]);
			compound.setString("Text" + (var2 + 1), var3);
		}
		
		this.field_174883_i.func_179670_b(compound);
	}
	
	@Override
	public void readFromNBT(final NBTTagCompound compound)
	{
		this.isEditable = false;
		super.readFromNBT(compound);
		final ICommandSender var2 = new ICommandSender()
		{
			private static final String __OBFID = "CL_00002039";
			
			@Override
			public String getName()
			{
				return "Sign";
			}
			
			@Override
			public IChatComponent getDisplayName()
			{
				return new ChatComponentText(this.getName());
			}
			
			@Override
			public void addChatMessage(final IChatComponent message)
			{
			}
			
			@Override
			public boolean canCommandSenderUseCommand(final int permissionLevel)
			{
				return true;
			}
			
			@Override
			public BlockPos getPosition()
			{
				return TileEntitySign.this.pos;
			}
			
			@Override
			public Vec3 getPositionVector()
			{
				return new Vec3(TileEntitySign.this.pos.getX() + 0.5D, TileEntitySign.this.pos.getY() + 0.5D, TileEntitySign.this.pos.getZ() + 0.5D);
			}
			
			@Override
			public World getEntityWorld()
			{
				return TileEntitySign.this.worldObj;
			}
			
			@Override
			public Entity getCommandSenderEntity()
			{
				return null;
			}
			
			@Override
			public boolean sendCommandFeedback()
			{
				return false;
			}
			
			@Override
			public void func_174794_a(final CommandResultStats.Type p_174794_1_, final int p_174794_2_)
			{
			}
		};
		
		for (int var3 = 0; var3 < 4; ++var3)
		{
			final String var4 = compound.getString("Text" + (var3 + 1));
			
			try
			{
				final IChatComponent var5 = IChatComponent.Serializer.jsonToComponent(var4);
				
				try
				{
					this.signText[var3] = ChatComponentProcessor.func_179985_a(var2, var5, (Entity) null);
				} catch (final CommandException var7)
				{
					this.signText[var3] = var5;
				}
			} catch (final JsonParseException var8)
			{
				this.signText[var3] = new ChatComponentText(var4);
			}
		}
		
		this.field_174883_i.func_179668_a(compound);
		
		for (int i = 0; i < this.signText.length; ++i)
		{
			final IChatComponent component = this.signText[i];
			
			if (component != null)
			{
				final ChatStyle style = component.getChatStyle();
				
				if (style != null)
				{
					final ClickEvent event = style.getChatClickEvent();
					
					if (event != null)
					{
						if (event.getAction() == ClickEvent.Action.RUN_COMMAND)
						{
							this.commands[i] = new FutureCommand(event.getValue());
							continue;
						}
					}
				}
			}
			
			this.commands[i] = null;
		}
	}
	
	/**
	 * Overriden in a sign to provide the text.
	 */
	@Override
	public Packet getDescriptionPacket()
	{
		final IChatComponent[] var1 = new IChatComponent[4];
		System.arraycopy(this.signText, 0, var1, 0, 4);
		return new S33PacketUpdateSign(this.worldObj, this.pos, var1);
	}
	
	public boolean getIsEditable()
	{
		return this.isEditable;
	}
	
	/**
	 * Sets the sign's isEditable flag to the specified parameter.
	 */
	public void setEditable(final boolean p_145913_1_)
	{
		this.isEditable = p_145913_1_;
		
		if (!p_145913_1_)
		{
			this.field_145917_k = null;
		}
	}
	
	public void func_145912_a(final EntityPlayer p_145912_1_)
	{
		this.field_145917_k = p_145912_1_;
	}
	
	public EntityPlayer func_145911_b()
	{
		return this.field_145917_k;
	}
	
	public boolean func_174882_b(final EntityPlayer p_174882_1_)
	{
		final ICommandSender var2 = new ICommandSender()
		{
			private static final String __OBFID = "CL_00002038";
			
			@Override
			public String getName()
			{
				return p_174882_1_.getName();
			}
			
			@Override
			public IChatComponent getDisplayName()
			{
				return p_174882_1_.getDisplayName();
			}
			
			@Override
			public void addChatMessage(final IChatComponent message)
			{
			}
			
			@Override
			public boolean canCommandSenderUseCommand(final int permissionLevel)
			{
				return true;
			}
			
			@Override
			public BlockPos getPosition()
			{
				return TileEntitySign.this.pos;
			}
			
			@Override
			public Vec3 getPositionVector()
			{
				return new Vec3(TileEntitySign.this.pos.getX() + 0.5D, TileEntitySign.this.pos.getY() + 0.5D, TileEntitySign.this.pos.getZ() + 0.5D);
			}
			
			@Override
			public World getEntityWorld()
			{
				return p_174882_1_.getEntityWorld();
			}
			
			@Override
			public Entity getCommandSenderEntity()
			{
				return p_174882_1_;
			}
			
			@Override
			public boolean sendCommandFeedback()
			{
				return false;
			}
			
			@Override
			public void func_174794_a(final CommandResultStats.Type p_174794_1_, final int p_174794_2_)
			{
				TileEntitySign.this.field_174883_i.func_179672_a(this, p_174794_1_, p_174794_2_);
			}
		};
		
		for (final FutureCommand command : this.commands)
		{
			if (command != null)
				MinecraftServer.getServer().getCommandManager().executeCommand(var2, command.getCommand());
		}
		
		return true;
	}
	
	public CommandResultStats func_174880_d()
	{
		return this.field_174883_i;
	}
}
