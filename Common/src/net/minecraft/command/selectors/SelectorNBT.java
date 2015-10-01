package net.minecraft.command.selectors;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.arg.TypedWrapper;
import net.minecraft.command.arg.TypedWrapper.Getter;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.construction.SelectorConstructable;
import net.minecraft.command.descriptors.SelectorDescriptorDefault.DefaultParserData;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.custom.nbt.TypeNBTArg;
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
		public ArgWrapper<NBTBase> construct(final DefaultParserData parserData) throws SyntaxErrorException
		{
			final TypedWrapper<?> nbt = getRequiredParam(0, parserData);
			final Getter<String> path = getParam(TypeIDs.String, 1, parserData);
			
			final TypeID<?> type = nbt.type();
			
			if (type == TypeIDs.Entity)
				return TypeIDs.NBTBase.wrap(new EntityNBT(path, nbt.get(TypeIDs.Entity)));
			
			if (type == TypeIDs.NBTBase)
				return TypeIDs.NBTBase.wrap(new CompoundNBT(path, nbt.get(TypeIDs.NBTBase)));
			
			if (type == TypeIDs.Coordinates)
				return TypeIDs.NBTBase.wrap(new BlockNBT(path, nbt.get(TypeIDs.Coordinates)));
			
			return TypeIDs.NBTBase.wrap(new StringNBT(path, nbt.get(TypeIDs.String)));
		}
	};
	
	private final Getter<String> path;
	
	public SelectorNBT(final Getter<String> path)
	{
		this.path = path;
	}
	
	public NBTBase lookup(NBTBase tag) throws CommandException
	{
		if (this.path == null)
			return tag;
		
		final String path = this.path.get();
		
		final String[] parts = path.split("\\.");
		
		for (final String part : parts)
		{
			switch (tag.getId())
			{
			case 10:
				tag = ((NBTTagCompound) tag).getTag(part);
				break;
			case 9:
				try
				{
					tag = ((NBTTagList) tag).get(Integer.parseInt(part));
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
		private final Getter<Entity> entity;
		
		public EntityNBT(final Getter<String> path, final Getter<Entity> getter)
		{
			super(path);
			this.entity = getter;
		}
		
		@Override
		public NBTBase eval(final ICommandSender sender) throws CommandException
		{
			final NBTTagCompound tag = new NBTTagCompound();
			
			final Entity e = this.entity.get();
			if (!e.writeMountToNBT(tag))
				e.writeToNBT(tag);
			
			return this.lookup(tag);
		}
	}
	
	public static class CompoundNBT extends SelectorNBT
	{
		private final Getter<NBTBase> tag;
		
		public CompoundNBT(final Getter<String> path, final Getter<NBTBase> tag)
		{
			super(path);
			this.tag = tag;
		}
		
		@Override
		public NBTBase eval(final ICommandSender sender) throws CommandException
		{
			return this.lookup(this.tag.get());
		}
	}
	
	public static class BlockNBT extends SelectorNBT
	{
		private final Getter<Vec3> coord;
		
		public BlockNBT(final Getter<String> path, final Getter<Vec3> coord)
		{
			super(path);
			this.coord = coord;
		}
		
		@Override
		public NBTBase eval(final ICommandSender sender) throws CommandException
		{
			final BlockPos pos = new BlockPos(this.coord.get());
			
			final TileEntity te = sender.getEntityWorld().getTileEntity(pos);
			
			if (te == null)
				throw new CommandException("Block at " + pos.toString() + " has no associated NBT-data");
			
			final NBTTagCompound tag = new NBTTagCompound();
			
			te.writeToNBT(tag);
			
			return this.lookup(tag);
		}
	}
	
	public static class StringNBT extends SelectorNBT
	{
		private final Getter<String> nbtString;
		
		public StringNBT(final Getter<String> path, final Getter<String> nbtString)
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
				final Parser parser = new Parser(this.nbtString.get());
				nbt = TypeNBTArg.parserDefault.parse(parser).arg();
			} catch (final Throwable t)
			{
				throw new CommandException("Unable to parse NBT-Tag", t);
			}
			
			return this.lookup(nbt.eval(sender));
		}
	}
}
