package net.minecraft.command.commands;

import java.util.List;

import net.minecraft.command.CommandUtilities;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.EntityNotFoundException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.construction.CommandConstructable;
import net.minecraft.command.descriptors.CommandDescriptor.CParserData;
import net.minecraft.entity.Entity;

public class CommandKill extends CommandArg<Integer>
{
	private final CommandArg<List<Entity>> entities;
	
	public static final CommandConstructable constructable = new CommandConstructable()
	{
		@Override
		public CommandArg<Integer> construct(final CParserData data)
		{
			final CommandArg<List<Entity>> entities = data.get(TypeIDs.EntityList);
			
			if (entities == null)
				return NoParam.command;
			
			return new CommandKill(entities);
		}
	};
	
	public CommandKill(final CommandArg<List<Entity>> entities)
	{
		this.entities = entities;
	}
	
	@Override
	public Integer eval(final ICommandSender sender) throws CommandException
	{
		final List<Entity> entities = this.entities.eval(sender);
		
		for (final Entity entity : entities)
		{
			entity.func_174812_G();
			CommandUtilities.notifyOperators(sender, "commands.kill.successful", entity.getDisplayName());
		}
		
		sender.func_174794_a(CommandResultStats.Type.AFFECTED_ENTITIES, entities.size());
		return entities.size();
	}
	
	public static class NoParam extends CommandArg<Integer>
	{
		private NoParam()
		{
		}
		
		public static final NoParam command = new NoParam();
		
		@Override
		public Integer eval(final ICommandSender sender) throws CommandException
		{
			final Entity e = sender.getCommandSenderEntity();
			if (e == null)
				throw new EntityNotFoundException();
			
			e.func_174812_G();
			
			CommandUtilities.notifyOperators(sender, "commands.kill.successful", e.getDisplayName());
			
			sender.func_174794_a(CommandResultStats.Type.AFFECTED_ENTITIES, 1);
			return 1;
		}
	}
}
