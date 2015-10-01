package net.minecraft.command.type.custom.nbt;

import java.util.Collections;
import java.util.Set;

import net.minecraft.command.collections.Completers;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.type.IComplete;
import net.minecraft.command.type.IExParse;
import net.minecraft.command.type.custom.nbt.ParserNBTCompound.CompoundData;

public class NBTDescriptor
{
	private NBTDescriptor()
	{
	}
	
	public static interface Tag
	{
		public ParserNBTTag getTagParser();
		
		public ParserNBTList getListParser();
		
		public ParserNBTCompound getCompoundParser();
	}
	
	public static abstract class Compound
	{
		public abstract IExParse<Void, CompoundData> getPair();
		
		public abstract Set<ITabCompletion> getKeyCompletions();
		
		public abstract Tag getSubDescriptor(String key);
		
	}
	
	public static abstract class List
	{
		public abstract Tag getTagDescriptor(int index);
	}
	
	private static final DefaultCompound primitiveCompound = new DefaultCompound();
	private static final DefaultList primitiveList = new DefaultList();
	
	public static final Tag defaultTag = new DefaultTag();
	public static final Tag defaultList = new DefaultTag(Completers.bracketCompleter);
	public static final Tag defaultCompound = new DefaultTag(Completers.braceCompleter);
	
	public static final Tag baseDescriptor = new DefaultTag(Completers.braceBracketCompleter);
	
	public static final class DefaultTag implements Tag
	{
		private final ParserNBTTag tagParser;
		private final ParserNBTList listParser = new ParserNBTList(primitiveList);
		private final ParserNBTCompound compoundParser = new ParserNBTCompound(primitiveCompound);
		
		private DefaultTag()
		{
			this.tagParser = new ParserNBTTag(this);
		}
		
		public DefaultTag(final IComplete completer)
		{
			this.tagParser = new ParserNBTTag(this, completer);
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
			return this.compoundParser;
		}
	};
	
	private static class DefaultCompound extends Compound
	{
		private final TypeNBTPair pair = new TypeNBTPair(this);
		
		@Override
		public Tag getSubDescriptor(final String key)
		{
			return defaultTag;
		}
		
		@Override
		public IExParse<Void, CompoundData> getPair()
		{
			return this.pair;
		}
		
		@Override
		public Set<ITabCompletion> getKeyCompletions()
		{
			return Collections.emptySet();
		}
	};
	
	private static class DefaultList extends List
	{
		@Override
		public Tag getTagDescriptor(final int index)
		{
			return defaultTag;
		}
	};
}
