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
import net.minecraft.util.Vec3;

public class CommandTemp extends CommandBase
{
	public static final CommandConstructable constructable = new CommandConstructable()
	{
		@Override
		public CommandTemp construct(final List<ArgWrapper<?>> params, final IPermission permission)
		{
			return new CommandTemp(params.get(0).get(TypeIDs.Coordinates), permission);
		}
	};
	
	public CommandTemp(final CommandArg<Vec3> arg, final IPermission permission)
	{
		super(permission);
		this.arg = arg;
	}
	
	private final CommandArg<Vec3> arg;
	
	@Override
	public int procCommand(final ICommandSender sender) throws CommandException
	{
		final Vec3 vec = this.arg.eval(sender);
		MinecraftServer.getServer().getConfigurationManager().sendChatMsg(new ChatComponentTranslation("chat.type.announcement", new Object[] { sender.getDisplayName(), vec.xCoord + " " + vec.yCoord + " " + vec.zCoord }));
		return 1;
	}
}
