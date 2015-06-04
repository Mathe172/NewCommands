package net.minecraft.command.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.construction.CommandConstructable;
import net.minecraft.command.descriptors.CommandDescriptor.CParserData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;

public class CommandSay extends CommandArg<Integer>
{
	public static final CommandConstructable constructable = new CommandConstructable()
	{
		@Override
		public CommandSay construct(final CParserData data)
		{
			return new CommandSay(data.get(0).get(TypeIDs.IChatComponent));
		}
	};
	
	private final CommandArg<IChatComponent> icc;
	
	public CommandSay(final CommandArg<IChatComponent> icc)
	{
		this.icc = icc;
	}
	
	@Override
	public Integer eval(final ICommandSender sender) throws CommandException
	{
		MinecraftServer.getServer().getConfigurationManager().sendChatMsg(new ChatComponentTranslation("chat.type.announcement", sender.getDisplayName(), this.icc.eval(sender)));
		return 1;
	}
}
