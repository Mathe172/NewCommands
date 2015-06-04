package net.minecraft.command.commands;

import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats.Type;
import net.minecraft.command.CommandUtilities;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.construction.CommandConstructable;
import net.minecraft.command.descriptors.CommandDescriptor.CParserData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class CommandEntityData extends CommandArg<Integer>
{
	public static final CommandConstructable constructable = new CommandConstructable()
	{
		@Override
		public CommandArg<Integer> construct(final CParserData data) throws SyntaxErrorException
		{
			return new CommandEntityData(
				data.get(TypeIDs.EntityList),
				data.get(TypeIDs.NBTCompound));
		}
	};
	
	private final CommandArg<List<Entity>> entities;
	private final CommandArg<NBTTagCompound> nbt;
	
	public CommandEntityData(final CommandArg<List<Entity>> entities, final CommandArg<NBTTagCompound> nbt)
	{
		this.entities = entities;
		this.nbt = nbt;
	}
	
	@Override
	public Integer eval(final ICommandSender sender) throws CommandException
	{
		final List<Entity> entities = this.entities.eval(sender);
		final NBTTagCompound nbt = new NBTTagCompound.CopyOnWrite(this.nbt.eval(sender));
		
		sender.func_174794_a(Type.AFFECTED_ENTITIES, 0);
		
		int successCount = 0;
		
		for (final Entity e : entities)
			if (e instanceof EntityPlayer)
				CommandUtilities.errorMessage(sender, "commands.entitydata.noPlayers", e.getDisplayName());
			else
			{
				final NBTTagCompound nbtEntity = new NBTTagCompound();
				e.writeToNBT(nbtEntity);
				
				nbt.removeTag("UUIDMost");
				nbt.removeTag("UUIDLeast");
				
				if (nbtEntity.mergeChecked(nbt))
				{
					e.readFromNBT(nbtEntity);
					CommandUtilities.notifyOperators(sender, "commands.entitydata.success", nbtEntity.toString());
					++successCount;
				}
				else
					CommandUtilities.errorMessage(sender, "commands.entitydata.failed", nbtEntity.toString());
			}
		
		sender.func_174794_a(Type.AFFECTED_ENTITIES, entities.size());
		
		return successCount;
	}
}
