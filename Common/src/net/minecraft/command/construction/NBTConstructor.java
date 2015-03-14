package net.minecraft.command.construction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.command.completion.TabCompletion;
import net.minecraft.command.type.IComplete;
import net.minecraft.command.type.IExParse;
import net.minecraft.command.type.custom.nbt.NBTDescriptor;
import net.minecraft.command.type.custom.nbt.NBTDescriptor.DefaultTag;
import net.minecraft.command.type.custom.nbt.NBTDescriptor.Tag;
import net.minecraft.command.type.custom.nbt.NBTPair;
import net.minecraft.command.type.custom.nbt.NBTUtilities;
import net.minecraft.command.type.custom.nbt.ParserNBTCompound;
import net.minecraft.command.type.custom.nbt.ParserNBTCompound.CompoundData;
import net.minecraft.command.type.custom.nbt.ParserNBTList;
import net.minecraft.command.type.custom.nbt.ParserNBTTag;
import net.minecraft.command.type.custom.nbt.ParserNBTTagCustom;

public final class NBTConstructor extends NBTDescriptor.Compound implements NBTDescriptor.Tag
{
	private final Set<TabCompletion> keyCompletions = new HashSet<>();
	private final Map<String, NBTDescriptor.Tag> subDescriptors = new HashMap<>();
	
	private final ParserNBTTag tagParser = new ParserNBTTagCustom(this, NBTUtilities.braceCompleter);
	private final ParserNBTCompound compoundParser = new ParserNBTCompound(this);
	private final IExParse<Void, CompoundData> pair = new NBTPair(this);
	
	@Override
	public Set<TabCompletion> getKeyCompletions()
	{
		return this.keyCompletions;
	}
	
	@Override
	public IExParse<Void, CompoundData> getPair()
	{
		return this.pair;
	}
	
	@Override
	public Tag getSubDescriptor(final String key)
	{
		final Tag subDescriptor = this.subDescriptors.get(key);
		
		if (subDescriptor == null)
			return NBTDescriptor.defaultTag;
		
		return subDescriptor;
	}
	
	public NBTConstructor key(final String key, final Tag subDescriptor)
	{
		this.subDescriptors.put(key, subDescriptor);
		final String s = key + ":";
		this.keyCompletions.add(new TabCompletion(s, s, key));
		
		return this;
	}
	
	/*
	 * Adds a key that is only suggested if the first letter is already entered (prevents cluttering of the suggestion list)
	 */
	public NBTConstructor sKey(final String key, final Tag subDescriptor)
	{
		this.subDescriptors.put(key, subDescriptor);
		final String s = key + ":";
		this.keyCompletions.add(new TabCompletion(s, s, key, false));
		
		return this;
	}
	
	public NBTConstructor key(final String key, final IComplete completer)
	{
		this.subDescriptors.put(key, new DefaultTag(completer));
		final String s = key + ":";
		this.keyCompletions.add(new TabCompletion(s, s, key));
		
		return this;
	}
	
	/*
	 * Adds a key that is only suggested if the first letter is already entered (prevents cluttering of the suggestion list)
	 */
	public NBTConstructor sKey(final String key, final IComplete completer)
	{
		this.subDescriptors.put(key, new DefaultTag(completer));
		final String s = key + ":";
		this.keyCompletions.add(new TabCompletion(s, s, key, false));
		
		return this;
	}
	
	public NBTConstructor key(final String... keys)
	{
		for (final String key : keys)
		{
			this.subDescriptors.put(key, NBTDescriptor.defaultTag);
			final String s = key + ":";
			this.keyCompletions.add(new TabCompletion(s, s, key));
		}
		return this;
	}
	
	/*
	 * Adds a key that is only suggested if the first letter is already entered (prevents cluttering of the suggestion list)
	 */
	public NBTConstructor sKey(final String... keys)
	{
		for (final String key : keys)
		{
			this.subDescriptors.put(key, NBTDescriptor.defaultTag);
			final String s = key + ":";
			this.keyCompletions.add(new TabCompletion(s, s, key, false));
		}
		return this;
	}
	
	@Override
	public ParserNBTTag getTagParser()
	{
		return this.tagParser;
	}
	
	@Override
	public ParserNBTList getListParser()
	{
		return NBTDescriptor.defaultTag.getListParser();
	}
	
	@Override
	public ParserNBTCompound getCompoundParser()
	{
		return this.compoundParser;
	}
}
