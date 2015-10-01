package net.minecraft.command.construction;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.collections.Completers;
import net.minecraft.command.completion.ProviderCompleter;
import net.minecraft.command.type.IComplete;
import net.minecraft.command.type.custom.nbt.NBTDescriptor;
import net.minecraft.command.type.custom.nbt.NBTDescriptor.DefaultTag;
import net.minecraft.command.type.custom.nbt.NBTDescriptor.Tag;
import net.minecraft.command.type.custom.nbt.ParserNBTCompound;
import net.minecraft.command.type.custom.nbt.ParserNBTList;
import net.minecraft.command.type.custom.nbt.ParserNBTTag;

public final class NBTConstructorList extends NBTDescriptor.List implements Tag
{
	private final ParserNBTTag tagParser = new ParserNBTTag(this, Completers.bracketCompleter);
	private final List<Tag> itemDescriptors = new ArrayList<>();
	private final ParserNBTList listParser = new ParserNBTList(this);
	
	public NBTConstructorList()
	{
	}
	
	public NBTConstructorList(final Tag itemDescriptor)
	{
		this.then(itemDescriptor);
	}
	
	public NBTConstructorList(final IComplete completer)
	{
		this.then(completer);
	}
	
	public NBTConstructorList(final String... completions)
	{
		this.then(completions);
	}
	
	public final NBTConstructorList then(final Tag itemDescriptor)
	{
		this.itemDescriptors.add(itemDescriptor);
		return this;
	}
	
	public final NBTConstructorList then(final IComplete completer)
	{
		return this.then(new DefaultTag(completer));
	}
	
	public final NBTConstructorList then(final String... completions)
	{
		return this.then(new ProviderCompleter(completions));
	}
	
	@Override
	public ParserNBTTag getTagParser()
	{
		return this.tagParser;
	}
	
	@Override
	public ParserNBTList getListParser()
	{
		return this.listParser;
	}
	
	@Override
	public ParserNBTCompound getCompoundParser()
	{
		return NBTDescriptor.defaultTag.getCompoundParser();
	}
	
	@Override
	public Tag getTagDescriptor(final int index)
	{
		if (this.itemDescriptors.isEmpty())
			return NBTDescriptor.defaultTag;
		
		return this.itemDescriptors.get(Math.min(index, this.itemDescriptors.size() - 1));
	}
	
}
