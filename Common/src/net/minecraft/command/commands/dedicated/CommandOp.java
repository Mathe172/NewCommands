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

public class CommandOp extends CommandBase
{
	public final static CommandConstructable constructable = new CommandConstructable()
	{
		@Override
		public CommandBase construct(final List<ArgWrapper<?>> params, final IPermission permission)
		{
			return new CommandOp(params.get(0).get(TypeIDs.String), permission);
		}
	};
	
	private final CommandArg<String> name;
	
	public CommandOp(final CommandArg<String> name, final IPermission permission)
	{
		super(permission);
		this.name = name;
	}
	
	@Override
	public int procCommand(final ICommandSender sender) throws CommandException
	{
		final String name = this.name.eval(sender).trim();
		
		final MinecraftServer server = MinecraftServer.getServer();
		final GameProfile profile = server.getPlayerProfileCache().getGameProfileForUsername(name);
		
		if (profile == null)
			throw new CommandException("commands.op.failed", new Object[] { name });
		
		server.getConfigurationManager().addOp(profile);
		this.notifyOperators(sender, "commands.op.success", new Object[] { name });
		
		return 1;
	}
	
}
