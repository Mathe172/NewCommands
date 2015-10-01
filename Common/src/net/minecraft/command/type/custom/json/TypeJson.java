package net.minecraft.command.type.custom.json;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CTypeParse;
import net.minecraft.command.type.custom.json.JsonDescriptor.Element;
import net.minecraft.command.type.custom.json.JsonUtilities.DeserializationManager;
import net.minecraft.command.type.custom.json.JsonUtilities.JsonData;
import net.minecraft.command.type.management.TypeID;

public class TypeJson<T> extends CTypeParse<T>
{
	private final Element baseDescriptor;
	private final DeserializationManager manager;
	private final Type type;
	private final TypeID<T> typeID;
	
	public TypeJson(final Element baseDescriptor, final DeserializationManager manager, final Type type, final TypeID<T> typeID)
	{
		this.baseDescriptor = baseDescriptor;
		this.manager = manager;
		this.type = type;
		this.typeID = typeID;
	}
	
	@Override
	public ArgWrapper<T> iParse(final Parser parser, final Context context) throws SyntaxErrorException
	{
		final ArgWrapper<T> ret = context.generalParse(parser, this.typeID);
		
		if (ret != null)
			return ret;
		
		this.manager.initCache();
		
		final JsonElementData data = new JsonElementData(this.manager);
		
		this.baseDescriptor.getElementParser().parse(parser, data);
		
		if (data.constElement == null)
			return this.typeID.wrap(this.manager.<T> createCmdArg(data.dynamicElement, this.type));
		
		try
		{
			return this.typeID.wrap(this.manager.<T> fromJsonUncached(data.constElement, this.type));
		} catch (final JsonParseException ex)
		{
			throw parser.SEE("Could not parse JSON: " + ex.getMessage() + " ");
		}
	}
	
	private static class JsonElementData extends JsonData
	{
		public JsonElement constElement = null;
		public CommandArg<JsonElement> dynamicElement = null;
		
		public JsonElementData(final DeserializationManager manager)
		{
			super(manager);
		}
		
		@Override
		public void put(final JsonElement json)
		{
			this.constElement = json;
		}
		
		@Override
		public void add(final CommandArg<JsonElement> data)
		{
			this.dynamicElement = data;
		}
		
	}
}
