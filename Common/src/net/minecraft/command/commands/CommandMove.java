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
import net.minecraft.command.descriptors.CommandDescriptor.CParserData;
import net.minecraft.command.type.custom.coordinate.TypeCoordinates.Shift;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.BlockPos;

public abstract class CommandMove extends CommandArg<Integer>
{
	public static final CommandConstructable constructable = new CommandConstructable()
	{
		@Override
		public CommandArg<Integer> construct(final CParserData data) throws SyntaxErrorException
		{
			if (data.size() == 1)
				return new Cancel(
					data.get(TypeIDs.EntityList));
			
			return new Start(
				data.get(TypeIDs.EntityList),
				data.get(TypeIDs.Shift));
		}
	};
	
	protected final CommandArg<List<Entity>> entities;
	
	public CommandMove(final CommandArg<List<Entity>> entities)
	{
		this.entities = entities;
	}
	
	private static class Cancel extends CommandMove
	{
		public Cancel(final CommandArg<List<Entity>> entities)
		{
			super(entities);
		}
		
		@Override
		public Integer eval(final ICommandSender sender) throws CommandException
		{
			final List<Entity> entities = this.entities.eval(sender);
			sender.func_174794_a(CommandResultStats.Type.AFFECTED_ENTITIES, 0);
			
			int succ = 0;
			
			for (final Entity e : entities)
				if (e instanceof EntityLiving)
				{
					if (((EntityLiving) e).cancelMove())
						++succ;
				}
				else
					CommandUtilities.errorMessage(sender, e.getCustomNameTag() + " is not a living entity");
			
			if (succ == 0)
				throw new CommandException("commands.move.noSuccess");
			
			sender.func_174794_a(CommandResultStats.Type.AFFECTED_ENTITIES, succ);
			
			return succ;
		}
	}
	
	private static class Start extends CommandMove
	{
		private final CommandArg<Shift> shift;
		
		public Start(final CommandArg<List<Entity>> entities, final CommandArg<Shift> shift)
		{
			super(entities);
			this.shift = shift;
		}
		
		@Override
		public Integer eval(final ICommandSender sender) throws CommandException
		{
			final List<Entity> entities = this.entities.eval(sender);
			final Shift shift = this.shift.eval(sender);
			
			sender.func_174794_a(CommandResultStats.Type.AFFECTED_ENTITIES, 0);
			
			int succ = 0;
			
			for (final Entity e : entities)
				if (e instanceof EntityLiving)
				{
					if (((EntityLiving) e).forceMove(new BlockPos(shift.addBase(e.getPositionVector()))))
						++succ;
				}
				else
					CommandUtilities.errorMessage(sender, e.getCustomNameTag() + " is not a living entity");
			
			if (succ == 0)
				throw new CommandException("commands.move.noSuccess");
			
			sender.func_174794_a(CommandResultStats.Type.AFFECTED_ENTITIES, succ);
			
			return succ;
		}
	}
}
