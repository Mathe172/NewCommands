package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.command.completion.TabCompletionData;
import net.minecraft.command.completion.TabCompletionData.Weighted;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class S3APacketTabComplete implements Packet
{
	private List<TabCompletionData> tcDataList;
	private static final String __OBFID = "CL_00001288";
	
	public S3APacketTabComplete()
	{
	}
	
	public S3APacketTabComplete(final Set<Weighted> tcDataSet)
	{
		this.tcDataList = new ArrayList<TabCompletionData>(tcDataSet);
	}
	
	/**
	 * Reads the raw packet data from the data stream.
	 */
	@Override
	public void readPacketData(final PacketBuffer data) throws IOException
	{
		final int amount = data.readInt();
		this.tcDataList = new ArrayList<TabCompletionData>(amount);
		
		for (int i = 0; i < amount; ++i)
		{
			this.tcDataList.add(new TabCompletionData(data.readStringFromBuffer(32767), data.readInt(), data.readInt(), data.readStringFromBuffer(32767), data.readInt()));
		}
	}
	
	/**
	 * Writes the raw packet data to the data stream.
	 */
	@Override
	public void writePacketData(final PacketBuffer data) throws IOException
	{
		data.writeInt(this.tcDataList.size());
		
		for (final TabCompletionData tcData : this.tcDataList)
		{
			data.writeString(tcData.name);
			data.writeInt(tcData.startIndex);
			data.writeInt(tcData.endIndex);
			data.writeString(tcData.replacement);
			data.writeInt(tcData.newCursorIndex);
		}
	}
	
	/**
	 * Passes this Packet on to the NetHandler for processing.
	 */
	public void processPacket(final INetHandlerPlayClient handler)
	{
		handler.handleTabComplete(this);
	}
	
	public List<TabCompletionData> getCompleters()
	{
		return this.tcDataList;
	}
	
	/**
	 * Passes this Packet on to the NetHandler for processing.
	 */
	@Override
	public void processPacket(final INetHandler handler)
	{
		this.processPacket((INetHandlerPlayClient) handler);
	}
}
