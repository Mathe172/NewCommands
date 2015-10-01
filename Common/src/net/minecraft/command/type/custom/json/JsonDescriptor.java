package net.minecraft.command.type.custom.json;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.collections.Completers;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.type.IComplete;
import net.minecraft.command.type.IExParse;
import net.minecraft.command.type.custom.json.ParserJsonObject.JsonObjectData;

public class JsonDescriptor
{
	private JsonDescriptor()
	{
	}
	
	public static interface Typed
	{
		public Set<Type> type();
	}
	
	public static class TypedImplementation implements Typed
	{
		private final Set<Type> type;
		
		public TypedImplementation(final Type... type)
		{
			this.type = ParsingUtilities.setOrNull(type);
		}
		
		@Override
		public Set<Type> type()
		{
			return this.type;
		}
	}
	
	public static interface Element extends Typed
	{
		public ParserJsonElement getElementParser();
		
		public ParserJsonArray getArrayParser();
		
		public ParserJsonObject getObjectParser();
	}
	
	public static interface Object extends Typed
	{
		public IExParse<Void, JsonObjectData> getPair();
		
		public Set<ITabCompletion> getKeyCompletions();
		
		public Element getSubDescriptor(String key);
	}
	
	public static interface Array extends Typed
	{
		public Element getElementDescriptor(int index);
	}
	
	public static abstract class AllDescriptor implements Element, Object, Array
	{
	}
	
	private static final DefaultObject primitiveObject = new DefaultObject();
	private static final DefaultArray primitiveArray = new DefaultArray();
	
	public static final Element defaultElement = new DefaultElement();
	public static final Element defaultArray = new DefaultElement(Completers.bracketCompleter);
	public static final Element defaultObject = new DefaultElement(Completers.braceCompleter);
	
	public static final class DefaultElement implements Element
	{
		private final ParserJsonElement elementParser;
		private final ParserJsonArray arrayParser = new ParserJsonArray(primitiveArray);
		private final ParserJsonObject objectParser = new ParserJsonObject(primitiveObject);
		
		private final Set<Type> type;
		
		private DefaultElement()
		{
			this.type = null;
			this.elementParser = new ParserJsonElement(this);
		}
		
		public DefaultElement(final IComplete completer)
		{
			this.type = null;
			this.elementParser = new ParserJsonElement(this, completer);
		}
		
		public DefaultElement(final IComplete completer, final Type... type)
		{
			this.type = ParsingUtilities.setOrNull(type);
			this.elementParser = new ParserJsonElement(this, completer);
		}
		
		@Override
		public ParserJsonElement getElementParser()
		{
			return this.elementParser;
		}
		
		@Override
		public ParserJsonArray getArrayParser()
		{
			return this.arrayParser;
		}
		
		@Override
		public ParserJsonObject getObjectParser()
		{
			return this.objectParser;
		}
		
		@Override
		public Set<Type> type()
		{
			return this.type;
		}
	};
	
	private static class DefaultObject extends TypedImplementation implements JsonDescriptor.Object
	{
		private final TypeJsonPair pair = new TypeJsonPair(this);
		
		@Override
		public Element getSubDescriptor(final String key)
		{
			return defaultElement;
		}
		
		@Override
		public IExParse<Void, JsonObjectData> getPair()
		{
			return this.pair;
		}
		
		@Override
		public Set<ITabCompletion> getKeyCompletions()
		{
			return Collections.emptySet();
		}
	};
	
	private static class DefaultArray extends TypedImplementation implements Array
	{
		@Override
		public Element getElementDescriptor(final int index)
		{
			return defaultElement;
		}
	};
}
