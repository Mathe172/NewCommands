package net.minecraft.command.construction;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.commons.collections4.trie.PatriciaTrie;

import net.minecraft.command.collections.Completers;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.completion.TabCompletion;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.type.IComplete;
import net.minecraft.command.type.IExParse;
import net.minecraft.command.type.custom.nbt.NBTDescriptor;
import net.minecraft.command.type.custom.nbt.NBTDescriptor.DefaultTag;
import net.minecraft.command.type.custom.nbt.NBTDescriptor.Tag;
import net.minecraft.command.type.custom.nbt.ParserNBTCompound;
import net.minecraft.command.type.custom.nbt.ParserNBTCompound.CompoundData;
import net.minecraft.command.type.custom.nbt.ParserNBTList;
import net.minecraft.command.type.custom.nbt.ParserNBTTag;
import net.minecraft.command.type.custom.nbt.TypeNBTPair;

public final class NBTConstructor extends NBTDescriptor.Compound implements NBTDescriptor.Tag
{
	private final Set<ITabCompletion> keyCompletions = new HashSet<>();
	private final PatriciaTrie<NBTDescriptor.Tag> subDescriptors = new PatriciaTrie<>();
	
	private final ParserNBTTag tagParser = new ParserNBTTag(this, Completers.braceCompleter);
	private final ParserNBTCompound compoundParser = new ParserNBTCompound(this);
	private final IExParse<Void, CompoundData> pair = new TypeNBTPair(this);
	
	@Override
	public Set<ITabCompletion> getKeyCompletions()
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
		this.keyCompletions.add(new TabCompletion(s, s, key)
		{
			@Override
			public double weightOffset(final Matcher m, final CompletionData cData)
			{
				return -1.0;
			}
		});
		
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
		this.keyCompletions.add(new TabCompletion(s, s, key)
		{
			@Override
			public double weightOffset(final Matcher m, final CompletionData cData)
			{
				return -1.0;
			}
		});
		
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
			this.keyCompletions.add(new TabCompletion(s, s, key)
			{
				@Override
				public double weightOffset(final Matcher m, final CompletionData cData)
				{
					return -1.0;
				}
			});
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
	
	public static class ConstructionHelper
	{
		protected static final Tag defTag = NBTDescriptor.defaultTag;
		protected static final Tag defList = NBTDescriptor.defaultList;
		protected static final Tag defCompound = NBTDescriptor.defaultCompound;
		
		protected ConstructionHelper()
		{
		}
		
		protected static final NBTConstructor compound()
		{
			return new NBTConstructor();
		}
		
		protected static final NBTConstructorList list()
		{
			return new NBTConstructorList();
		}
		
		protected static final NBTConstructorList list(final IComplete completer)
		{
			return new NBTConstructorList(completer);
		}
		
		protected static final NBTConstructorList list(final Tag itemDescriptor)
		{
			return new NBTConstructorList(itemDescriptor);
		}
		
		protected static final NBTConstructorList list(final String... completions)
		{
			return new NBTConstructorList(completions);
		}
	}
}
