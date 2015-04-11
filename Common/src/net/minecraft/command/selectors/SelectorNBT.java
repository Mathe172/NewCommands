package net.minecraft.command.selectors;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.construction.SelectorConstructable;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.custom.TypeIDs;
import net.minecraft.command.type.custom.TypeSelectorContent.ParserData;
import net.minecraft.command.type.custom.Types;
import net.minecraft.command.type.management.TypeID;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

public abstract class SelectorNBT extends CommandArg<NBTBase>
{
	public static final SelectorConstructable constructable = new SelectorConstructable()
	{
		@Override
		public ArgWrapper<NBTBase> construct(final ParserData parserData) throws SyntaxErrorException
		{
			final ArgWrapper<?> nbt = ParsingUtilities.getRequiredParam(0, parserData);
			final CommandArg<String> path = ParsingUtilities.getParam(TypeIDs.String, 1, parserData);
			
			final TypeID<?> type = nbt.type;
			
			if (type == TypeIDs.Entity)
				return TypeIDs.NBTBase.wrap(new EntityNBT(path, nbt.get(TypeIDs.Entity)));
			
			if (type == TypeIDs.NBTBase)
				return TypeIDs.NBTBase.wrap(new CompoundNBT(path, nbt.get(TypeIDs.NBTBase)));
			
			if (type == TypeIDs.Coordinates)
				return TypeIDs.NBTBase.wrap(new BlockNBT(path, nbt.get(TypeIDs.Coordinates)));
			
			return TypeIDs.NBTBase.wrap(new StringNBT(path, nbt.get(TypeIDs.String)));
		}
	};
	
	private final CommandArg<String> path;
	
	public SelectorNBT(final CommandArg<String> path)
	{
		this.path = path;
	}
	
	public NBTBase lookup(final ICommandSender sender, NBTBase tag) throws CommandException
	{
		if (this.path == null)
			return tag;
		
		final String path = this.path.eval(sender);
		
		final String[] parts = path.split("\\.");
		
		for (int i = 0; i < parts.length; ++i)
		{
			switch (tag.getId())
			{
			case 10:
				tag = ((NBTTagCompound) tag).getTag(parts[i]);
				break;
			case 9:
				try
				{
					tag = ((NBTTagList) tag).get(Integer.parseInt(parts[i]));
					break;
				} catch (final NumberFormatException e)
				{
				}
			default:
				tag = null;
			}
			
			if (tag == null)
				throw new CommandException("Unable to find '" + path + "' in tag");
		}
		
		return tag;
	}
	
	public static class EntityNBT extends SelectorNBT
	{
		private final CommandArg<Entity> entity;
		
		public EntityNBT(final CommandArg<String> path, final CommandArg<Entity> entity)
		{
			super(path);
			this.entity = entity;
		}
		
		@Override
		public NBTBase eval(final ICommandSender sender) throws CommandException
		{
			final NBTTagCompound tag = new NBTTagCompound();
			
			final Entity e = this.entity.eval(sender);
			if (!e.writeMountToNBT(tag))
				e.writeToNBT(tag);
			
			return this.lookup(sender, tag);
		}
	}
	
	public static class CompoundNBT extends SelectorNBT
	{
		private final CommandArg<NBTBase> tag;
		
		public CompoundNBT(final CommandArg<String> path, final CommandArg<NBTBase> tag)
		{
			super(path);
			this.tag = tag;
		}
		
		@Override
		public NBTBase eval(final ICommandSender sender) throws CommandException
		{
			return this.lookup(sender, this.tag.eval(sender));
		}
	}
	
	public static class BlockNBT extends SelectorNBT
	{
		private final CommandArg<Vec3> coord;
		
		public BlockNBT(final CommandArg<String> path, final CommandArg<Vec3> coord)
		{
			super(path);
			this.coord = coord;
		}
		
		@Override
		public NBTBase eval(final ICommandSender sender) throws CommandException
		{
			final BlockPos pos = new BlockPos(this.coord.eval(sender));
			
			final TileEntity te = sender.getEntityWorld().getTileEntity(pos);
			
			if (te == null)
				throw new CommandException("Block at " + pos.toString() + " has no associated NBT-data");
			
			final NBTTagCompound tag = new NBTTagCompound();
			
			te.writeToNBT(tag);
			
			return this.lookup(sender, tag);
		}
	}
	
	public static class StringNBT extends SelectorNBT
	{
		private final CommandArg<String> nbtString;
		
		public StringNBT(final CommandArg<String> path, final CommandArg<String> nbtString)
		{
			super(path);
			this.nbtString = nbtString;
		}
		
		@Override
		public NBTBase eval(final ICommandSender sender) throws CommandException
		{
			CommandArg<NBTTagCompound> nbt;
			
			try
			{
				final Parser parser = new Parser(this.nbtString.eval(sender));
				nbt = parser.parseInit(Types.IPNBT);
			} catch (final Throwable t)
			{
				throw (CommandException) (new CommandException("Unable to parse   NBT-Tag").initCause(t));
			}
			
			return this.lookup(sender, nbt.eval(sender));
		}
	}
}
