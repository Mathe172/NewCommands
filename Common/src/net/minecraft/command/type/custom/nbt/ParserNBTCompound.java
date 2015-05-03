package net.minecraft.command.type.custom.nbt;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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
		public final Map<String, CommandArg<NBTBase>> data = new HashMap<>();
		
		@Override
		public void put(final NBTBase data)
		{
			this.primitiveData.put(this.name, data);
		}
		
		@Override
		public void put(final CommandArg<NBTBase> data)
		{
			this.data.put(this.name, data);
		}
		
		public boolean containsKey(final String key)
		{
			return this.primitiveData.containsKey(key) || this.data.containsKey(key);
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
			parserData.put(new CommandArg<NBTBase>()
			{
				final NBTTagCompound compound = new NBTTagCompound(data.primitiveData);
				
				@Override
				public NBTTagCompound eval(final ICommandSender sender) throws CommandException
				{
					for (final Entry<String, CommandArg<NBTBase>> tag : data.data.entrySet())
						data.primitiveData.put(tag.getKey(), tag.getValue().eval(sender));
					
					return this.compound;
				}
			});
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
				
				throw parser.SEE("Unexpected '" + m.group(1) + "' around index ");
			}
			
			pair.parse(parser, data);
			
			if (!parser.findInc(m))
				throw parser.SEE("No delimiter found while parsing tag compound around index ");
			
			if ("}".equals(m.group(1)))
				return;
			
			if ("]".equals(m.group(1)))
				throw parser.SEE("Unexpected ']' around index ");
		}
	}
}
