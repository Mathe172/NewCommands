package net.minecraft.command.operators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.construction.OperatorConstructable;
import net.minecraft.command.descriptors.OperatorDescriptor.ListOperands;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class OperatorItems extends CommandArg<NBTBase>
{
	public static final OperatorConstructable constructable = new OperatorConstructable()
	{
		@Override
		public ArgWrapper<?> construct(final ListOperands operands) throws SyntaxErrorException
		{
			return TypeIDs.NBTBase.wrap(
				new OperatorItems(
					operands.get(TypeIDs.NBTBase),
					operands.get(TypeIDs.IntList)));
		}
	};
	
	private final CommandArg<NBTBase> nbt;
	private final CommandArg<List<Integer>> slots;
	
	public OperatorItems(final CommandArg<NBTBase> nbt, final CommandArg<List<Integer>> slots)
	{
		this.nbt = nbt;
		this.slots = slots;
	}
	
	@Override
	public NBTBase eval(final ICommandSender sender) throws CommandException
	{
		final NBTTagList ret = new NBTTagList();
		
		for (final NBTBase item : this.getList(sender))
			ret.appendTag(item);
		
		return ret;
	}
	
	private final List<NBTBase> getList(final ICommandSender sender) throws CommandException
	{
		final NBTBase nbt = this.nbt.eval(sender);
		final List<Integer> items = this.slots.eval(sender);
		
		if (nbt.getId() == 10 && items.size() == 1)
			if (items.size() == 1)
			{
				final NBTTagCompound compound = (NBTTagCompound) nbt;
				
				return Collections.<NBTBase> singletonList(hasSlot(compound, items.get(0)) ? compound : new NBTTagCompound());
			}
		
		final List<NBTBase> ret = new ArrayList<>(items.size());
		
		if (nbt.getId() == 9)
		{
			final NBTTagList list = (NBTTagList) nbt;
			
			if (list.getTagType() == 10)
			{
				for (final int i : items)
					ret.add(getItem(list, i));
				
				return ret;
			}
		}
		
		for (int i = 0; i < items.size(); ++i)
			ret.add(new NBTTagCompound());
		
		return ret;
	}
	
	private static NBTBase getItem(final NBTTagList list, final int index)
	{
		for (int i = 0; i < list.tagCount(); ++i)
		{
			final NBTTagCompound compound = (NBTTagCompound) list.get(i);
			
			if (hasSlot(compound, index))
				return compound;
		}
		
		return new NBTTagCompound();
	}
	
	private static boolean hasSlot(final NBTTagCompound compound, final int slot)
	{
		final NBTBase tag = compound.getTag("Slot");
		
		return tag != null && tag.getId() == 1 && ((NBTTagByte) tag).getInt() == slot;
	}
}
