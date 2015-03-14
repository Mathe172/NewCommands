package net.minecraft.command.completion;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import net.minecraft.command.parser.ParsingManager;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.server.S3APacketTabComplete;
import net.minecraft.server.MinecraftServer;

public class TCDSet
{
	public final Set<TabCompletionData> primitiveData = new TreeSet<>();
	public final Set<DataRequest> requests = new HashSet<>();
	
	public final void add(TabCompletionData tcData)
	{
		this.primitiveData.add(tcData);
	}
	
	public final void add(DataRequest request)
	{
		this.requests.add(request);
	}
	
	public final void sendPacket(final NetHandlerPlayServer handler)
	{
		if (this.requests.isEmpty())
			handler.sendPacket(new S3APacketTabComplete(this.primitiveData));
		else
		{
			MinecraftServer.getServer().addScheduledTask(new Runnable()
			{
				@Override
				public void run()
				{
					for (final DataRequest request : TCDSet.this.requests)
						request.process();
					
					ParsingManager.submit(new Runnable()
					{
						@Override
						public void run()
						{
							for (final DataRequest request : TCDSet.this.requests)
								request.createCompletions(TCDSet.this.primitiveData);
							
							handler.sendPacket(new S3APacketTabComplete(TCDSet.this.primitiveData));
						}
					});
				}
			});
		}
	}
}
