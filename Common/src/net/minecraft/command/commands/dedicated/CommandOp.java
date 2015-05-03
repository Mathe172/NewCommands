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

public class CommandOp extends CommandArg<Integer>
{
	public final static CommandConstructable constructable = new CommandConstructable()
	{
		@Override
		public CommandOp construct(final ParserData data)
		{
			return new CommandOp(data.get(0).get(TypeIDs.String));
		}
	};
	
	private final CommandArg<String> name;
	
	public CommandOp(final CommandArg<String> name)
	{
		this.name = name;
	}
	
	@Override
	public Integer eval(final ICommandSender sender) throws CommandException
	{
		final String name = this.name.eval(sender).trim();
		
		final MinecraftServer server = MinecraftServer.getServer();
		final GameProfile profile = server.getPlayerProfileCache().getGameProfileForUsername(name);
		
		if (profile == null)
			throw new CommandException("commands.op.failed",  name );
		
		server.getConfigurationManager().addOp(profile);
		CommandBase.notifyOperators(sender, "commands.op.success",  name );
		
		sender.func_174794_a(CommandResultStats.Type.AFFECTED_ENTITIES, 1);
		
		return 1;
	}
	
}
