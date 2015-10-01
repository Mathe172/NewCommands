package net.minecraft.command.construction;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections4.trie.PatriciaTrie;

import net.minecraft.command.collections.Completers;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.completion.TabCompletion;
import net.minecraft.command.type.IComplete;
import net.minecraft.command.type.IExParse;
import net.minecraft.command.type.custom.json.JsonDescriptor;
import net.minecraft.command.type.custom.json.JsonDescriptor.AllDescriptor;
import net.minecraft.command.type.custom.json.JsonDescriptor.DefaultElement;
import net.minecraft.command.type.custom.json.JsonDescriptor.Element;
import net.minecraft.command.type.custom.json.JsonDescriptor.TypedImplementation;
import net.minecraft.command.type.custom.json.ParserJsonArray;
import net.minecraft.command.type.custom.json.ParserJsonElement;
import net.minecraft.command.type.custom.json.ParserJsonObject;
import net.minecraft.command.type.custom.json.ParserJsonObject.JsonObjectData;
import net.minecraft.command.type.custom.json.TypeJsonPair;

public final class JsonConstructor extends TypedImplementation implements JsonDescriptor.Object, Element
{
	private final Set<ITabCompletion> keyCompletions = new HashSet<>();
	private final PatriciaTrie<JsonDescriptor.Element> subDescriptors = new PatriciaTrie<>();
	
	private final ParserJsonElement elementParser = new ParserJsonElement(this, Completers.braceCompleter);
	private final ParserJsonObject objectParser = new ParserJsonObject(this);
	private final IExParse<Void, JsonObjectData> pair = new TypeJsonPair(this);
	
	public JsonConstructor(final Type... type)
	{
		super(type);
	}
	
	@Override
	public Set<ITabCompletion> getKeyCompletions()
	{
		return this.keyCompletions;
	}
	
	@Override
	public IExParse<Void, JsonObjectData> getPair()
	{
		return this.pair;
	}
	
	@Override
	public Element getSubDescriptor(final String key)
	{
		final Element subDescriptor = this.subDescriptors.get(key);
		
		if (subDescriptor == null)
			return JsonDescriptor.defaultElement;
		
		return subDescriptor;
	}
	
	public JsonConstructor key(final String key, final Element subDescriptor)
	{
		this.subDescriptors.put(key, subDescriptor);
		final String s = key + ":";
		this.keyCompletions.add(new TabCompletion(s, s, key));
		
		return this;
	}
	
	public JsonConstructor key(final String key, final IComplete completer)
	{
		this.subDescriptors.put(key, new DefaultElement(completer));
		final String s = key + ":";
		this.keyCompletions.add(new TabCompletion(s, s, key));
		
		return this;
	}
	
	public JsonConstructor key(final String... keys)
	{
		for (final String key : keys)
		{
			this.subDescriptors.put(key, JsonDescriptor.defaultElement);
			final String s = key + ":";
			this.keyCompletions.add(new TabCompletion(s, s, key));
		}
		return this;
	}
	
	@Override
	public ParserJsonElement getElementParser()
	{
		return this.elementParser;
	}
	
	@Override
	public ParserJsonArray getArrayParser()
	{
		return JsonDescriptor.defaultArray.getArrayParser();
	}
	
	@Override
	public ParserJsonObject getObjectParser()
	{
		return this.objectParser;
	}
	
	public static class ConstructionHelper
	{
		protected static final Element defElement = JsonDescriptor.defaultElement;
		protected static final Element defObject = JsonDescriptor.defaultObject;
		protected static final Element defArray = JsonDescriptor.defaultArray;
		
		protected ConstructionHelper()
		{
		}
		
		protected static final JsonConstructor object(final Type... type)
		{
			return new JsonConstructor(type);
		}
		
		protected static final JsonConstructorArray array()
		{
			return new JsonConstructorArray();
		}
		
		protected static final JsonConstructorArray array(final Type... type)
		{
			return new JsonConstructorArray(type);
		}
		
		protected static final JsonConstructorArray array(final String... completions)
		{
			return new JsonConstructorArray(completions);
		}
		
		protected static final JsonConstructorArray array(final IComplete completer, final Type... type)
		{
			return new JsonConstructorArray(completer, type);
		}
		
		protected static final JsonConstructorArray array(final Element itemDescriptor, final Type... type)
		{
			return new JsonConstructorArray(itemDescriptor, type);
		}
		
		protected static final JsonConstructorArray array(final String[] completions, final Type... type)
		{
			return new JsonConstructorArray(completions, type);
		}
		
		protected static final AllDescriptor merge(final JsonConstructor object, final JsonConstructorArray array)
		{
			final Set<Type> mergedTypes = new HashSet<>();
			
			if (object.type() != null)
				mergedTypes.addAll(object.type());
			if (array.type() != null)
				mergedTypes.addAll(array.type());
			
			return new AllDescriptor()
			{
				ParserJsonElement elementParser = new ParserJsonElement(this, Completers.braceBracketCompleter);
				
				@Override
				public Set<Type> type()
				{
					return mergedTypes;
				}
				
				@Override
				public ParserJsonElement getElementParser()
				{
					return this.elementParser;
				}
				
				@Override
				public ParserJsonArray getArrayParser()
				{
					return array.getArrayParser();
				}
				
				@Override
				public ParserJsonObject getObjectParser()
				{
					return object.getObjectParser();
				}
				
				@Override
				public IExParse<Void, JsonObjectData> getPair()
				{
					return object.getPair();
				}
				
				@Override
				public Set<ITabCompletion> getKeyCompletions()
				{
					return object.getKeyCompletions();
				}
				
				@Override
				public Element getSubDescriptor(final String key)
				{
					return object.getSubDescriptor(key);
				}
				
				@Override
				public Element getElementDescriptor(final int index)
				{
					return array.getElementDescriptor(index);
				}
			};
		}
		
		@SafeVarargs
		protected static final <T> T[] c(final T... elements)
		{
			return elements;
		}
	}
}
