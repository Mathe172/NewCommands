package net.minecraft.command.commands;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.IPermission;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.construction.CommandConstructable;
import net.minecraft.command.type.custom.TypeIDs;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;

public class CommandSay extends CommandBase
{
	public static final CommandConstructable constructable = new CommandConstructable()
	{
		@Override
		public CommandSay construct(final List<ArgWrapper<?>> params, final IPermission permission)
		{
			return new CommandSay(params.get(0).get(TypeIDs.String), permission);
		}
	};
	
	private final CommandArg<String> str;
	
	public CommandSay(final CommandArg<String> str, final IPermission permission)
	{
		super(permission);
		this.str = str;
	}
	
	@Override
	public int procCommand(final ICommandSender sender) throws CommandException
	{
		MinecraftServer.getServer().getConfigurationManager().sendChatMsg(new ChatComponentTranslation("chat.type.announcement", new Object[] { sender.getDisplayName(), this.str.eval(sender) }));
		return 1;
	}
}
