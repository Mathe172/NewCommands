package net.minecraft.command.type.custom.nbt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IExParse;
import net.minecraft.command.type.custom.nbt.NBTDescriptor.Compound;
import net.minecraft.command.type.custom.nbt.NBTUtilities.NBTData;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class ParserNBTCompound
{
	private final Compound descriptor;
	
	public ParserNBTCompound(final Compound descriptor)
	{
		this.descriptor = descriptor;
	}
	
	public static class CompoundData extends NBTData
	{
		public String name = null;
		
		public final Map<String, NBTBase> primitiveData = new HashMap<>();
		public final Set<String> keySet = new HashSet<>();
		public final ArrayList<Pair<String, CommandArg<NBTBase>>> data = new ArrayList<>();
		
		@Override
		public void put(final NBTBase data)
		{
			this.primitiveData.put(this.name, data);
			this.keySet.add(this.name);
		}
		
		@Override
		public void add(final CommandArg<NBTBase> data)
		{
			this.data.add(new ImmutablePair<>(this.name, data));
			this.keySet.add(this.name);
		}
		
		public boolean containsKey(final String key)
		{
			return this.keySet.contains(key);
		}
	}
	
	public void parse(final Parser parser, final NBTData parserData) throws SyntaxErrorException, CompletionException
	{
		parser.terminateCompletion();
		
		final CompoundData data = new CompoundData();
		
		this.parseItems(parser, data);
		
		if (data.data.isEmpty())
			parserData.put(new NBTTagCompound(data.primitiveData));
		else
			parserData.add(createNBTCompound(data));
	}
	
	public static final CommandArg<NBTBase> createNBTCompound(final CompoundData data)
	{
		final ArrayList<Pair<String, CommandArg<NBTBase>>> dynamicData = data.data;
		dynamicData.trimToSize();
		
		return new CommandArg<NBTBase>()
		{
			final NBTTagCompound compound = new NBTTagCompound(data.primitiveData);
			
			@Override
			public NBTTagCompound eval(final ICommandSender sender) throws CommandException
			{
				for (final Pair<String, CommandArg<NBTBase>> tag : dynamicData)
					this.compound.setTag(tag.getKey(), tag.getValue().eval(sender));
				
				return this.compound;
			}
		};
	}
	
	public void parseItems(final Parser parser, final CompoundData data) throws SyntaxErrorException, CompletionException
	{
		final Matcher m = parser.getMatcher(ParsingUtilities.listEndMatcher);
		
		final IExParse<Void, CompoundData> pair = this.descriptor.getPair();
		
		while (true)
		{
			if (parser.findInc(m)) // Because {Name:Value,} is valid... (or at least the output of NBTTagCompound.toString)
			{
				if ("}".equals(m.group(1)))
					return;
				
				throw parser.SEE("Unexpected '" + m.group(1) + "' ");
			}
			
			pair.parse(parser, data);
			
			if (!parser.findInc(m))
				throw parser.SEE("No delimiter found while parsing tag compound ");
			
			if ("}".equals(m.group(1)))
				return;
			
			if ("]".equals(m.group(1)))
				throw parser.SEE("Unexpected ']' ");
		}
	}
}
