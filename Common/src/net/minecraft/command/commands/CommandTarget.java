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
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;

public final class CommandTarget extends CommandArg<Integer>
{
	public static final CommandConstructable constructable = new CommandConstructable()
	{
		@Override
		public CommandArg<Integer> construct(final CParserData data) throws SyntaxErrorException
		{
			return new CommandTarget(
				data.get(TypeIDs.EntityList),
				data.get(TypeIDs.Entity));
		}
	};
	
	private final CommandArg<List<Entity>> entities;
	private final CommandArg<Entity> target;
	
	public CommandTarget(final CommandArg<List<Entity>> entities, final CommandArg<Entity> target)
	{
		this.entities = entities;
		this.target = target;
	}
	
	@Override
	public Integer eval(final ICommandSender sender) throws CommandException
	{
		final List<Entity> entities = this.entities.eval(sender);
		final Entity targetTmp = this.target == null ? null : this.target.eval(sender);
		
		sender.func_174794_a(CommandResultStats.Type.AFFECTED_ENTITIES, 0);
		
		if (this.target != null && !(targetTmp instanceof EntityLivingBase))
			throw new CommandException(targetTmp.getName() + " is not a living entity");
		
		final EntityLivingBase target = this.target == null ? null : (EntityLivingBase) targetTmp;
		
		int succ = 0;
		
		for (final Entity e : entities)
			if (e instanceof EntityCreature)
			{
				if (((EntityLiving) e).forceTarget(target))
					++succ;
			}
			else
				CommandUtilities.errorMessage(sender, e.getName() + " is not a creature");
		
		if (succ == 0)
			throw new CommandException("commands.target.noSuccess");
		
		sender.func_174794_a(CommandResultStats.Type.AFFECTED_ENTITIES, succ);
		
		return succ;
	}
}
