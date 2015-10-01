package net.minecraft.util;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public interface IChatComponent extends Iterable
{
	IChatComponent setChatStyle(ChatStyle style);
	
	ChatStyle getChatStyle();
	
	/**
	 * Appends the given text to the end of this component.
	 */
	IChatComponent appendText(String text);
	
	/**
	 * Appends the given component to the end of this one.
	 */
	IChatComponent appendSibling(IChatComponent component);
	
	/**
	 * Gets the text of this component, without any special formatting codes added, for chat. TODO: why is this two different methods?
	 */
	String getUnformattedTextForChat();
	
	/**
	 * Gets the text of this component, without any special formatting codes added. TODO: why is this two different methods?
	 */
	String getUnformattedText();
	
	/**
	 * Gets the sibling components of this one.
	 */
	List getSiblings();
	
	/**
	 * Creates a copy of this component. Almost a deep copy, except the style is shallow-copied.
	 */
	IChatComponent createCopy();
	
	public static class Serializer implements JsonDeserializer, JsonSerializer
	{
		public static final Gson GSON;
		@SuppressWarnings("unused")
		private static final String __OBFID = "CL_00001263";
		
		@Override
		public IChatComponent deserialize(final JsonElement json, final Type type, final JsonDeserializationContext context)
		{
			if (json.isJsonPrimitive())
				return new ChatComponentText(json.getAsString());
			else if (!json.isJsonObject())
			{
				if (json.isJsonArray())
				{
					final JsonArray array = json.getAsJsonArray();
					IChatComponent ret = null;
					
					for (final JsonElement item : array)
					{
						final IChatComponent itemICC = context.deserialize(item, type);
						
						if (ret == null)
							ret = itemICC;
						else
							ret.appendSibling(itemICC);
					}
					
					return ret;
				}
				
				throw new JsonParseException("Don\'t know how to turn " + json.toString() + " into a Component");
			}
			else
			{
				final JsonObject object = json.getAsJsonObject();
				IChatComponent ret;
				
				if (object.has("text"))
					ret = new ChatComponentText(object.get("text").getAsString());
				else if (object.has("translate"))
				{
					final String translate = object.get("translate").getAsString();
					
					if (object.has("with"))
					{
						final JsonElement with = object.get("with");
						
						final Object[] params;
						
						if (with.isJsonArray())
						{
							final JsonArray array = (JsonArray) with;
							params = new Object[array.size()];
							for (int i = 0; i < params.length; ++i)
								params[i] = optimizeTranslationParam(array.get(i), type, context);
						}
						else
							params = new Object[] { optimizeTranslationParam(with, type, context) };
						
						ret = new ChatComponentTranslation(translate, params);
					}
					else
						ret = new ChatComponentTranslation(translate);
				}
				else if (object.has("score"))
				{
					final JsonObject score = object.getAsJsonObject("score");
					
					if (!score.has("name") || !score.has("objective"))
						throw new JsonParseException("A score component needs a least a name and an objective");
					
					ret = new ChatComponentScore(JsonUtils.getJsonObjectStringFieldValue(score, "name"), JsonUtils.getJsonObjectStringFieldValue(score, "objective"));
					
					if (score.has("value"))
						((ChatComponentScore) ret).func_179997_b(JsonUtils.getJsonObjectStringFieldValue(score, "value"));
				}
				else
				{
					if (!object.has("selector"))
						throw new JsonParseException("Don\'t know how to turn " + json.toString() + " into a Component");
					
					ret = new ChatComponentSelector(JsonUtils.getJsonObjectStringFieldValue(object, "selector"));
				}
				
				if (object.has("extra"))
				{
					final JsonElement extra = object.get("extra");
					if (extra.isJsonArray())
					{
						final JsonArray array = (JsonArray) extra;
						
						if (array.size() <= 0)
							throw new JsonParseException("Unexpected empty array of components");
						
						for (final JsonElement element : array)
							ret.appendSibling(context.<IChatComponent> deserialize(element, type));
					}
					else
						ret.appendSibling(context.<IChatComponent> deserialize(extra, type));
				}
				
				ret.setChatStyle((ChatStyle) context.deserialize(json, ChatStyle.class));
				return ret;
			}
		}
		
		private static Object optimizeTranslationParam(final JsonElement param, final Type type, final JsonDeserializationContext context) throws JsonParseException
		{
			final Object ret = context.deserialize(param, type);
			
			if (ret instanceof ChatComponentText)
			{
				final ChatComponentText item = (ChatComponentText) ret;
				
				if (item.getChatStyle().isEmpty() && item.getSiblings().isEmpty())
					return item.getChatComponentText_TextValue();
			}
			
			return ret;
		}
		
		private void serializeChatStyle(final ChatStyle style, final JsonObject object, final JsonSerializationContext ctx)
		{
			final JsonElement var4 = ctx.serialize(style);
			
			if (var4.isJsonObject())
			{
				final JsonObject var5 = (JsonObject) var4;
				final Iterator var6 = var5.entrySet().iterator();
				
				while (var6.hasNext())
				{
					final Entry var7 = (Entry) var6.next();
					object.add((String) var7.getKey(), (JsonElement) var7.getValue());
				}
			}
		}
		
		public JsonElement serialize(final IChatComponent p_serialize_1_, final Type p_serialize_2_, final JsonSerializationContext p_serialize_3_)
		{
			if (p_serialize_1_ instanceof ChatComponentText && p_serialize_1_.getChatStyle().isEmpty() && p_serialize_1_.getSiblings().isEmpty())
				return new JsonPrimitive(((ChatComponentText) p_serialize_1_).getChatComponentText_TextValue());
			final JsonObject var4 = new JsonObject();
			
			if (!p_serialize_1_.getChatStyle().isEmpty())
				this.serializeChatStyle(p_serialize_1_.getChatStyle(), var4, p_serialize_3_);
			
			if (!p_serialize_1_.getSiblings().isEmpty())
			{
				final JsonArray var5 = new JsonArray();
				final Iterator var6 = p_serialize_1_.getSiblings().iterator();
				
				while (var6.hasNext())
				{
					final IChatComponent var7 = (IChatComponent) var6.next();
					var5.add(this.serialize(var7, var7.getClass(), p_serialize_3_));
				}
				
				var4.add("extra", var5);
			}
			
			if (p_serialize_1_ instanceof ChatComponentText)
				var4.addProperty("text", ((ChatComponentText) p_serialize_1_).getChatComponentText_TextValue());
			else if (p_serialize_1_ instanceof ChatComponentTranslation)
			{
				final ChatComponentTranslation var11 = (ChatComponentTranslation) p_serialize_1_;
				var4.addProperty("translate", var11.getKey());
				
				if (var11.getFormatArgs() != null && var11.getFormatArgs().length > 0)
				{
					final JsonArray var14 = new JsonArray();
					final Object[] var16 = var11.getFormatArgs();
					final int var8 = var16.length;
					
					for (int var9 = 0; var9 < var8; ++var9)
					{
						final Object var10 = var16[var9];
						
						if (var10 instanceof IChatComponent)
							var14.add(this.serialize((IChatComponent) var10, var10.getClass(), p_serialize_3_));
						else
							var14.add(new JsonPrimitive(String.valueOf(var10)));
					}
					
					var4.add("with", var14);
				}
			}
			else if (p_serialize_1_ instanceof ChatComponentScore)
			{
				final ChatComponentScore var12 = (ChatComponentScore) p_serialize_1_;
				final JsonObject var15 = new JsonObject();
				var15.addProperty("name", var12.func_179995_g());
				var15.addProperty("objective", var12.func_179994_h());
				var15.addProperty("value", var12.getUnformattedTextForChat());
				var4.add("score", var15);
			}
			else
			{
				if (!(p_serialize_1_ instanceof ChatComponentSelector))
					throw new IllegalArgumentException("Don\'t know how to serialize " + p_serialize_1_ + " as a Component");
				
				final ChatComponentSelector var13 = (ChatComponentSelector) p_serialize_1_;
				var4.addProperty("selector", var13.func_179992_g());
			}
			
			return var4;
		}
		
		public static String componentToJson(final IChatComponent component)
		{
			return GSON.toJson(component);
		}
		
		public static IChatComponent jsonToComponent(final String json)
		{
			return GSON.fromJson(json, IChatComponent.class);
		}
		
		public static IChatComponent jsonToComponent(final JsonElement json)
		{
			return GSON.fromJson(json, IChatComponent.class);
		}
		
		@Override
		public JsonElement serialize(final Object p_serialize_1_, final Type p_serialize_2_, final JsonSerializationContext p_serialize_3_)
		{
			return this.serialize((IChatComponent) p_serialize_1_, p_serialize_2_, p_serialize_3_);
		}
		
		static
		{
			final GsonBuilder var0 = new GsonBuilder();
			var0.registerTypeHierarchyAdapter(IChatComponent.class, new IChatComponent.Serializer());
			var0.registerTypeHierarchyAdapter(ChatStyle.class, new ChatStyle.Serializer());
			var0.registerTypeAdapterFactory(new EnumTypeAdapterFactory());
			GSON = var0.create();
		}
	}
}
