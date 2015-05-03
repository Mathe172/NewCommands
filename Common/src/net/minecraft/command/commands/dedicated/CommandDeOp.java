package net.minecraft.command.commands.dedicated;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.construction.CommandConstructable;
import net.minecraft.command.descriptors.CommandDescriptor.ParserData;
import net.minecraft.server.MinecraftServer;

import com.mojang.authlib.GameProfile;

public class CommandDeOp extends CommandArg<Integer>
{
	public static final CommandConstructable constructable = new CommandConstructable()
	{
		@Override
		public CommandDeOp construct(final ParserData data)
		{
			return new CommandDeOp(data.get(0).get(TypeIDs.String));
		}
	};
	
	public CommandDeOp(final CommandArg<String> name)
	{
		this.name = name;
	}
	
	private final CommandArg<String> name;
	
	@Override
	public Integer eval(final ICommandSender sender) throws CommandException
	{
		final MinecraftServer server = MinecraftServer.getServer();
		final String name = this.name.eval(sender);
		final GameProfile profile = server.getConfigurationManager().getOppedPlayers().getGameProfileFromName(name);
		
		if (profile == null)
			throw new CommandException("commands.deop.failed",  name );
		
		server.getConfigurationManager().removeOp(profile);
		CommandBase.notifyOperators(sender, "commands.deop.success", name );
		
		sender.func_174794_a(CommandResultStats.Type.AFFECTED_ENTITIES, 1);
		
		return 1;
	}
}
