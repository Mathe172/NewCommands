package net.minecraft.command.type.custom.json;

import java.util.ArrayList;
import java.util.regex.Matcher;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.arg.PrimitiveParameter;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.custom.json.JsonUtilities.JsonData;

public class ParserJsonArray
{
	private final JsonDescriptor.Array descriptor;
	
	public ParserJsonArray(final JsonDescriptor.Array descriptor)
	{
		this.descriptor = descriptor;
	}
	
	public static class ArrayData extends JsonData
	{
		public final JsonArray node = new JsonArray();
		public final ArrayList<CommandArg<JsonElement>> data = new ArrayList<>();
		
		public ArrayData(final JsonData data)
		{
			super(data);
		}
		
		@Override
		public void put(final JsonElement json)
		{
			if (this.data.isEmpty())
				this.node.add(json);
			else
				this.data.add(new PrimitiveParameter<>(json));
		}
		
		@Override
		public void add(final CommandArg<JsonElement> json)
		{
			this.data.add(json);
		}
		
	}
	
	public void parse(final Parser parser, final JsonData parserData) throws SyntaxErrorException
	{
		ParsingUtilities.terminateCompletion(parser);
		
		final ArrayData data = new ArrayData(parserData);
		
		this.parseItems(parser, data);
		
		if (data.data.isEmpty())
			parserData.put(data.node, this.descriptor.type());
		else
		{
			final ArrayList<CommandArg<JsonElement>> dynamicData = data.data;
			dynamicData.trimToSize();
			
			data.procCache();
			
			final JsonArray json = data.node;
			
			parserData.add(new CommandArg<JsonElement>()
			{
				@Override
				public JsonElement eval(final ICommandSender sender) throws CommandException
				{
					final JsonArray ret = new JsonArray();
					ret.addAll(json);
					
					for (final CommandArg<JsonElement> item : dynamicData)
						ret.add(item.eval(sender));
					
					return ret;
				}
			});
		}
	}
	
	public void parseItems(final Parser parser, final ArrayData data) throws SyntaxErrorException
	{
		final Matcher m = parser.getMatcher(ParsingUtilities.listEndMatcher);
		
		for (int i = 0;; ++i)
		{
			if (parser.findInc(m)) // Because [Item1,Item2,] is valid... (or at least the output of NBTTagList.toString)
			{
				if (!"]".equals(m.group(1)))
					throw parser.SEE("Unexpected '" + m.group(1) + "' ");
				return;
			}
			
			this.descriptor.getElementDescriptor(i).getElementParser().parse(parser, data);
			
			if (!parser.findInc(m))
				throw parser.SEE("No delimiter found while parsing tag list ");
			
			if ("]".equals(m.group(1)))
				return;
			
			if ("}".equals(m.group(1)))
				throw parser.SEE("Unexpected '}' ");
		}
	}
}
