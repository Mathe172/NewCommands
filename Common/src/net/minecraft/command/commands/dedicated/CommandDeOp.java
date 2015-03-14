package net.minecraft.command.commands.dedicated;

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

import com.mojang.authlib.GameProfile;

public class CommandDeOp extends CommandBase
{
	public static final CommandConstructable constructable = new CommandConstructable()
	{
		@Override
		public CommandBase construct(final List<ArgWrapper<?>> params, final IPermission permission)
		{
			return new CommandDeOp(params.get(0).get(TypeIDs.String), permission);
		}
	};
	
	public CommandDeOp(final CommandArg<String> name, final IPermission permission)
	{
		super(permission);
		this.name = name;
	}
	
	private final CommandArg<String> name;
	
	@Override
	public int procCommand(final ICommandSender sender) throws CommandException
	{
		final MinecraftServer server = MinecraftServer.getServer();
		final String name = this.name.eval(sender);
		final GameProfile profile = server.getConfigurationManager().getOppedPlayers().getGameProfileFromName(name);
		
		if (profile == null)
		{
			throw new CommandException("commands.deop.failed", new Object[] { name });
		}
		else
		{
			server.getConfigurationManager().removeOp(profile);
			this.notifyOperators(sender, "commands.deop.success", new Object[] { name });
		}
		
		return 1;
	}
}
