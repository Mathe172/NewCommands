package net.minecraft.command.type.custom.json;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IExParse;
import net.minecraft.command.type.custom.json.JsonUtilities.DeserializationManager;
import net.minecraft.command.type.custom.json.JsonUtilities.JsonData;

public class ParserJsonObject
{
	private final JsonDescriptor.Object descriptor;
	
	public ParserJsonObject(final JsonDescriptor.Object descriptor)
	{
		this.descriptor = descriptor;
	}
	
	public static class JsonObjectData extends JsonData
	{
		public String name = null;
		
		public final JsonObject node = new JsonObject();
		public final ArrayList<Pair<String, CommandArg<JsonElement>>> data = new ArrayList<>();
		
		public JsonObjectData(final DeserializationManager manager)
		{
			super(manager);
		}
		
		public JsonObjectData(final JsonData data)
		{
			super(data);
		}
		
		@Override
		public void put(final JsonElement json)
		{
			this.node.add(this.name, json);
		}
		
		@Override
		public void add(final CommandArg<JsonElement> json)
		{
			this.data.add(new ImmutablePair<>(this.name, json));
		}
		
		public Set<String> keySet()
		{
			final Set<String> keySet = new HashSet<>();
			
			for (final Entry<String, ?> item : this.node.entrySet())
				keySet.add(item.getKey());
			
			for (final Pair<String, ?> item : this.data)
				keySet.add(item.getLeft());
			
			return keySet;
		}
	}
	
	public void parse(final Parser parser, final JsonData parserData) throws SyntaxErrorException
	{
		ParsingUtilities.terminateCompletion(parser);
		
		final JsonObjectData data = new JsonObjectData(parserData);
		
		this.parseItems(parser, data);
		
		if (data.data.isEmpty())
			parserData.put(data.node, this.descriptor.type());
		else
			parserData.add(createJsonObject(data));
	}
	
	public static final CommandArg<JsonElement> createJsonObject(final JsonObjectData data)
	{
		final ArrayList<Pair<String, CommandArg<JsonElement>>> dynamicData = data.data;
		dynamicData.trimToSize();
		
		data.procCache();
		
		return new CommandArg<JsonElement>()
		{
			final JsonObject node = data.node;
			
			@Override
			public JsonObject eval(final ICommandSender sender) throws CommandException
			{
				for (final Pair<String, CommandArg<JsonElement>> item : dynamicData)
					this.node.add(item.getKey(), item.getValue().eval(sender));
				
				return this.node;
			}
		};
	}
	
	public void parseItems(final Parser parser, final JsonObjectData data) throws SyntaxErrorException
	{
		final Matcher m = parser.getMatcher(ParsingUtilities.listEndMatcher);
		
		final IExParse<Void, JsonObjectData> pair = this.descriptor.getPair();
		
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
