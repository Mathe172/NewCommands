package net.minecraft.command.commands;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.EntityNotFoundException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.IPermission;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.construction.CommandConstructable;
import net.minecraft.command.descriptors.CommandDescriptor;
import net.minecraft.command.type.custom.TypeIDs;
import net.minecraft.entity.Entity;

public class CommandKill extends CommandBase
{
	private final CommandArg<List<Entity>> entities;
	
	public static final CommandConstructable constructable = new CommandConstructable()
	{
		@Override
		public CommandBase construct(final List<ArgWrapper<?>> params, final IPermission permission)
		{
			final CommandArg<List<Entity>> entities = CommandDescriptor.getParam(TypeIDs.EntityList, 0, params);
			
			if (entities == null)
				return new NoParam(permission);
			
			return new CommandKill(entities, permission);
		}
	};
	
	public CommandKill(final CommandArg<List<Entity>> entities, final IPermission permission)
	{
		super(permission);
		this.entities = entities;
	}
	
	@Override
	public int procCommand(final ICommandSender sender) throws CommandException
	{
		final List<Entity> entities = this.entities.eval(sender);
		
		for (final Entity entity : entities)
		{
			entity.func_174812_G();
			this.notifyOperators(sender, "commands.kill.successful", new Object[] { entity.getDisplayName() });
		}
		
		sender.func_174794_a(CommandResultStats.Type.AFFECTED_ENTITIES, entities.size());
		return entities.size();
	}
	
	public static class NoParam extends CommandBase
	{
		public NoParam(final IPermission permission)
		{
			super(permission);
		}
		
		@Override
		public int procCommand(final ICommandSender sender) throws CommandException
		{
			final Entity e = sender.getCommandSenderEntity();
			if (e == null)
				throw new EntityNotFoundException();
			
			e.func_174812_G();
			
			this.notifyOperators(sender, "commands.kill.successful", new Object[] { e.getDisplayName() });
			
			sender.func_174794_a(CommandResultStats.Type.AFFECTED_ENTITIES, 1);
			return 1;
		}
	}
}
