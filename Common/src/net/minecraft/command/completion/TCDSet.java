package net.minecraft.command.completion;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import net.minecraft.command.completion.TabCompletionData.Weighted;
import net.minecraft.command.parser.ParsingManager;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S3APacketTabComplete;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public class TCDSet
{
	public final Set<Weighted> primitiveData = new TreeSet<>();
	public final Set<DataRequest> requests = new HashSet<>();
	
	public final void add(final Weighted tcData)
	{
		this.primitiveData.add(tcData);
	}
	
	public final void add(final DataRequest request)
	{
		this.requests.add(request);
	}
	
	// TODO: Backwards compatibility
	public final void sendPacket(final NetHandlerPlayServer handler, final String vanillaData)
	{
		if (this.requests.isEmpty())
		{
			if (!this.primitiveData.isEmpty())
				sendModNote(handler, vanillaData == null);
			
			handler.sendPacket(new S3APacketTabComplete(this.primitiveData, vanillaData));
		}
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
							
							if (!TCDSet.this.primitiveData.isEmpty())
								sendModNote(handler, vanillaData == null);
							
							handler.sendPacket(new S3APacketTabComplete(TCDSet.this.primitiveData, vanillaData));
						}
					});
				}
			});
		}
	}
	
	private final void sendModNote(final NetHandlerPlayServer handler, final boolean moddedClient)
	{
		if (!moddedClient)
		{
			final IChatComponent msg1 = new ChatComponentText("For optimal use of tab-completion, please use the ");
			msg1.getChatStyle().setColor(EnumChatFormatting.RED);
			final IChatComponent msg2 = new ChatComponentText("NewCommands-Mod");
			msg2.getChatStyle()
				.setColor(EnumChatFormatting.AQUA)
				.setUnderlined(true)
				.setChatClickEvent(
					new ClickEvent(Action.OPEN_URL, "http://minecraft.curseforge.com/mc-mods/231539-newcommands"));
			msg1.appendSibling(msg2);
			handler.sendPacket(new S02PacketChat(msg1));
		}
	}
}
