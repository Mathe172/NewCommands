package net.minecraft.command.commands;

import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.CommandUtilities;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.construction.CommandConstructable;
import net.minecraft.command.construction.CommandDescriptorDefault.CParserData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

public final class CommandUseItem extends CommandArg<Integer>
{
	public static final CommandConstructable constructable = new CommandConstructable()
	{
		@Override
		public CommandArg<Integer> construct(final CParserData data) throws SyntaxErrorException
		{
			return new CommandUseItem(data.get(TypeIDs.EntityList));
		}
	};
	
	private final CommandArg<List<Entity>> entities;
	
	public CommandUseItem(final CommandArg<List<Entity>> entities)
	{
		this.entities = entities;
	}
	
	@Override
	public Integer eval(final ICommandSender sender) throws CommandException
	{
		final List<Entity> entities = this.entities.eval(sender);
		
		sender.func_174794_a(CommandResultStats.Type.AFFECTED_ENTITIES, 0);
		
		int succ = 0;
		
		for (final Entity e : entities)
			if (e instanceof EntityLivingBase)
			{
				((EntityLivingBase) e).swingItem();
				++succ;
			}
			else
				CommandUtilities.errorMessage(sender, e.getName() + " is not a living entity");
		
		if (succ == 0)
			throw new CommandException("commands.useItem.noSuccess");
		
		sender.func_174794_a(CommandResultStats.Type.AFFECTED_ENTITIES, succ);
		
		return succ;
	}
}
