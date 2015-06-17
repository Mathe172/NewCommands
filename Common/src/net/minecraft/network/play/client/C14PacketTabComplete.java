package net.minecraft.network.play.client;

import java.io.IOException;

import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.BlockPos;

import org.apache.commons.lang3.StringUtils;

public class C14PacketTabComplete implements Packet
{
	private String message;
	private int cursorIndex;
	private BlockPos pos;
	private boolean moddedClient; // TODO: Backwards-compatibility
	
	private static final String __OBFID = "CL_00001346";
	
	public C14PacketTabComplete()
	{
	}
	
	public C14PacketTabComplete(final String msg)
	{
		this(msg, 0, (BlockPos) null);
	}
	
	public C14PacketTabComplete(final String message, final int cursorIndex, final BlockPos pos)
	{
		this.message = message;
		this.cursorIndex = cursorIndex;
		this.pos = pos;
		this.moddedClient = true;
	}
	
	/**
	 * Reads the raw packet data from the data stream.
	 */
	@Override
	public void readPacketData(final PacketBuffer data) throws IOException
	{
		this.message = data.readStringFromBuffer(32767);
		
		final byte var2 = data.readByte();
		
		this.moddedClient = (var2 & 2) != 0;
		
		this.cursorIndex = this.moddedClient ? data.readInt() : this.message.length();
		
		if ((var2 & 1) != 0)
			this.pos = data.readBlockPos();
	}
	
	/**
	 * Writes the raw packet data to the data stream.
	 */
	@Override
	public void writePacketData(final PacketBuffer data) throws IOException
	{
		data.writeString(StringUtils.substring(this.message, 0, 32767));
		
		final boolean var2 = this.pos != null;
		data.writeByte(var2 ? 3 : 2);
		
		data.writeInt(this.cursorIndex);
		
		if (var2)
			data.writeBlockPos(this.pos);
	}
	
	public void func_180756_a(final INetHandlerPlayServer p_180756_1_)
	{
		p_180756_1_.processTabComplete(this);
	}
	
	public String getMessage()
	{
		return this.message;
	}
	
	public int getCursorIndex()
	{
		return this.cursorIndex;
	}
	
	public BlockPos func_179709_b()
	{
		return this.pos;
	}
	
	public boolean isModdedClient()
	{
		return this.moddedClient;
	}
	
	/**
	 * Passes this Packet on to the NetHandler for processing.
	 */
	@Override
	public void processPacket(final INetHandler handler)
	{
		this.func_180756_a((INetHandlerPlayServer) handler);
	}
}
