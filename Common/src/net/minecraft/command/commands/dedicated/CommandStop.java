package net.minecraft.command.commands.dedicated;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.construction.CommandConstructable;
import net.minecraft.server.MinecraftServer;

public final class CommandStop extends CommandArg<Integer>
{
	private static final CommandStop command = new CommandStop();
	
	public static final CommandConstructable constructable = CommandConstructable.primitiveConstructable(command);
	
	private CommandStop()
	{
	}
	
	@Override
	public Integer eval(final ICommandSender sender) throws CommandException
	{
		if (MinecraftServer.getServer().worldServers != null)
			CommandBase.notifyOperators(sender, "commands.stop.start");
		
		MinecraftServer.getServer().initiateShutdown();
		return 0;
	}
}
