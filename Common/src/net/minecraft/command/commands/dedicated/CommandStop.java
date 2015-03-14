package net.minecraft.command.commands.dedicated;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.IPermission;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.construction.CommandConstructable;
import net.minecraft.server.MinecraftServer;

public class CommandStop extends CommandBase
{
	public static final CommandConstructable constructable = new CommandConstructable()
	{
		@Override
		public CommandStop construct(final List<ArgWrapper<?>> params, final IPermission permission)
		{
			return new CommandStop(permission);
		}
	};
	
	public CommandStop(final IPermission permission)
	{
		super(permission);
	}
	
	@Override
	public int procCommand(final ICommandSender sender) throws CommandException
	{
		if (MinecraftServer.getServer().worldServers != null)
			this.notifyOperators(sender, "commands.stop.start", new Object[0]);
		
		MinecraftServer.getServer().initiateShutdown();
		return 0;
	}
}
