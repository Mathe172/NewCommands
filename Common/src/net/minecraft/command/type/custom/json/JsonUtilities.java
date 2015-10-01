package net.minecraft.command.type.custom.json;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapterFactory;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ParsingUtilities.PrimitiveCallback;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.parser.Parser;

public class JsonUtilities
{
	private JsonUtilities()
	{
	}
	
	public static class DeserializationManager
	{
		private final ThreadLocal<Map<JsonElement, Map<Type, CacheResult>>> cached;
		
		private final Gson gson;
		private final Gson uncachedGson;
		
		public DeserializationManager(final ThreadLocal<Map<JsonElement, Map<Type, CacheResult>>> cached, final Gson gson, final Gson uncachedGson)
		{
			this.cached = cached;
			this.gson = gson;
			this.uncachedGson = uncachedGson;
		}
		
		public DeserializationManager(final ThreadLocal<Map<JsonElement, Map<Type, CacheResult>>> cached, final Gson gson)
		{
			this.cached = cached;
			this.gson = gson;
			this.uncachedGson = gson;
		}
		
		public Map<Type, CacheResult> get(final JsonElement json)
		{
			return this.cached.get().get(json);
		}
		
		private Map<JsonElement, Map<Type, CacheResult>> get()
		{
			return this.cached.get();
		}
		
		private void set(final Map<JsonElement, Map<Type, CacheResult>> cached)
		{
			this.cached.set(cached);
		}
		
		public void resetCache()
		{
			this.cached.set(null);
		}
		
		public void initCache()
		{
			this.cached.set(new IdentityHashMap<JsonElement, Map<Type, CacheResult>>());
		}
		
		public void procCache(final JsonElement json, final Set<Type> types)
		{
			final Map<Type, CacheResult> results = new HashMap<>();
			
			if (types != null)
				for (final Type type : types)
					try
					{
						results.put(type, new CacheResult.Res(this.uncachedGson.fromJson(json, type)));
					} catch (final JsonSyntaxException ex)
					{
						results.put(type, new CacheResult.Ex(ex));
					}
			
			this.cached.get().put(json, results);
		}
		
		public <T> T fromJson(final JsonElement json, final Type typeOfT) throws JsonParseException
		{
			return this.gson.fromJson(json, typeOfT);
		}
		
		public <T> T fromJsonUncached(final JsonElement json, final Type typeOfT) throws JsonParseException
		{
			return this.uncachedGson.fromJson(json, typeOfT);
		}
		
		public abstract static class CacheResult
		{
			public abstract <T> T get() throws JsonParseException;
			
			public static class Res extends CacheResult
			{
				private final Object res;
				
				public Res(final Object res)
				{
					this.res = res;
				}
				
				@SuppressWarnings("unchecked")
				@Override
				public <T> T get() throws JsonParseException
				{
					return (T) this.res;
				}
			}
			
			public static class Ex extends CacheResult
			{
				private final JsonParseException ex;
				
				public Ex(final JsonParseException ex)
				{
					this.ex = ex;
				}
				
				@Override
				public <T> T get() throws JsonParseException
				{
					throw this.ex;
				}
			}
		}
		
		public <T> CommandArg<T> createCmdArg(final CommandArg<JsonElement> element, final Type typeOfT)
		{
			final Map<JsonElement, Map<Type, CacheResult>> cached = this.get();
			this.resetCache();
			
			return new CommandArg<T>()
			{
				@Override
				public T eval(final ICommandSender sender) throws CommandException
				{
					try
					{
						DeserializationManager.this.set(cached);
						return DeserializationManager.this.fromJson(element.eval(sender), typeOfT);
					} catch (final JsonParseException e)
					{
						throw new CommandException("Unable to parse JSON" + e.getMessage() == null ? "" : (" : " + e.getMessage()));
					} finally
					{
						DeserializationManager.this.resetCache();
					}
				}
			};
		}
		
		public static class Builder
		{
			private final ThreadLocal<Map<JsonElement, Map<Type, CacheResult>>> cached = new ThreadLocal<>();
			private final GsonBuilder gson;
			
			public Builder(final GsonBuilder gson)
			{
				this.gson = gson;
			}
			
			public Builder()
			{
				this.gson = new GsonBuilder();
			}
			
			public <T> JsonDeserializer<T> cachedDeserializer(final JsonDeserializer<T> deserializer)
			{
				return new JsonDeserializer<T>()
				{
					final ThreadLocal<Map<JsonElement, Map<Type, CacheResult>>> cached = Builder.this.cached;
					
					@Override
					public T deserialize(final JsonElement json, final Type type, final JsonDeserializationContext context) throws JsonParseException
					{
						final Map<Type, CacheResult> cached = this.cached.get().get(json);
						
						if (cached == null)
							return deserializer.deserialize(json, type, context);
						
						final CacheResult res = cached.get(type);
						
						if (res != null)
							return res.get();
						
						try
						{
							final T tmp = deserializer.deserialize(json, type, context);
							cached.put(type, new CacheResult.Res(tmp));
							return tmp;
						} catch (final JsonParseException ex)
						{
							cached.put(type, new CacheResult.Ex(ex));
							throw ex;
						}
					}
				};
			}
			
			public Builder registerTypeHierarchyAdapter(final Class<?> baseType, final JsonDeserializer<?> typeAdapter)
			{
				this.gson.registerTypeHierarchyAdapter(baseType, this.cachedDeserializer(typeAdapter));
				return this;
			}
			
			public Builder registerTypeAdapterFactory(final TypeAdapterFactory factory)
			{
				this.gson.registerTypeAdapterFactory(factory);
				return this;
			}
			
			public DeserializationManager create(final Gson uncachedGson)
			{
				return new DeserializationManager(this.cached, this.gson.create(), uncachedGson);
			}
			
			public DeserializationManager create()
			{
				return new DeserializationManager(this.cached, this.gson.create());
			}
		}
	}
	
	public static abstract class JsonData implements PrimitiveCallback<String>
	{
		private final Map<JsonElement, Set<Type>> toProcess = new IdentityHashMap<>();
		
		private final DeserializationManager manager;
		
		public JsonData(final DeserializationManager manager)
		{
			this.manager = manager;
		}
		
		public JsonData(final JsonData data)
		{
			this.manager = data.manager;
		}
		
		public void put(final JsonElement json, final Set<Type> type)
		{
			this.put(json);
			this.toProcess.put(json, type);
		}
		
		public abstract void put(final JsonElement json);
		
		public abstract void add(CommandArg<JsonElement> data);
		
		public void procCache()
		{
			for (final Entry<JsonElement, Set<Type>> data : this.toProcess.entrySet())
				this.manager.procCache(data.getKey(), data.getValue());
		}
		
		@Override
		public CommandArg<String> call(final Parser parser, final String s) throws SyntaxErrorException
		{
			this.put(new JsonPrimitive(s));
			return null;
		}
	}
	
	public static CommandArg<JsonElement> tranfsorm(final CommandArg<String> toTransform)
	{
		return new CommandArg<JsonElement>()
		{
			@Override
			public JsonElement eval(final ICommandSender sender) throws CommandException
			{
				return new JsonPrimitive(toTransform.eval(sender));
			}
		};
	}
	
	public static CommandArg<JsonElement> parseSelector(final Parser parser) throws SyntaxErrorException
	{
		return JsonUtilities.tranfsorm(TypeIDs.String.selectorParser.parse(parser).arg());
	}
	
	public static CommandArg<JsonElement> parseLabel(final Parser parser) throws SyntaxErrorException
	{
		return JsonUtilities.tranfsorm(TypeIDs.String.labelParser.parse(parser).arg());
	}
}
