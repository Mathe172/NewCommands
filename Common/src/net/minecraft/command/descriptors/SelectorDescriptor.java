package net.minecraft.command.descriptors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.completion.TabCompletion;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IDataType;
import net.minecraft.command.type.IExParse;
import net.minecraft.command.type.IParse;
import net.minecraft.command.type.TypeID;
import net.minecraft.command.type.custom.KVPair;
import net.minecraft.command.type.custom.ParserSelectorContent;

public abstract class SelectorDescriptor
{
	private final Set<TypeID<?>> resultTypes;
	
	private final KVPair kvPair;
	
	private final IParse<ArgWrapper<?>> contentParser;
	
	public abstract IDataType<?> getRequiredType(int index);
	
	public abstract IDataType<?> getRequiredType(String key);
	
	public abstract Set<TabCompletion> getKeyCompletions();
	
	private final static HashMap<String, SelectorDescriptor> selectors = new HashMap<>();
	private static final Set<TabCompletion> selectorCompletions = new HashSet<>();
	
	public SelectorDescriptor(final Set<TypeID<?>> resultTypes)
	{
		this.resultTypes = resultTypes;
		this.kvPair = new KVPair(this);
		this.contentParser = new ParserSelectorContent(this);
	}
	
	public static final void clear()
	{
		selectors.clear();
		selectorCompletions.clear();
	}
	
	public static void registerSelector(final String name, final SelectorDescriptor descriptor)
	{
		if (selectors.put(name, descriptor) != null)
			throw new IllegalArgumentException("Selector already registerd: " + name);
		
		final TabCompletion completion = new TabCompletion(name);
		selectorCompletions.add(completion);
		
		for (final TypeID<?> resultType : descriptor.resultTypes)
			resultType.addSelector(completion);
	}
	
	public static void registerSelector(final SelectorDescriptor descriptor, final String... names)
	{
		for (final String name : names)
			registerSelector(name, descriptor);
	}
	
	public static final SelectorDescriptor getDescriptor(final String name)
	{
		return selectors.get(name);
	}
	
	public final <T> ArgWrapper<T> construct(final TypeID<T> target, final List<ArgWrapper<?>> unnamedParams, final Map<String, ArgWrapper<?>> namedParams) throws SyntaxErrorException
	{
		// return this.resultType.convertTo(this.construct(unnamedParams, namedParams), target);
		return this.construct(unnamedParams, namedParams).convertTo(target);
	}
	
	public abstract ArgWrapper<?> construct(List<ArgWrapper<?>> unnamedParams, Map<String, ArgWrapper<?>> namedParams) throws SyntaxErrorException;
	
	public static final Set<TabCompletion> getCompletions()
	{
		return selectorCompletions;
	}
	
	public void parse(final Parser parser, final String key, final ParserSelectorContent.ParserData data) throws SyntaxErrorException, CompletionException
	{
		final IDataType<?> valueType = this.getRequiredType(key);
		
		if (valueType == null)
			throw parser.SEE("Unknown parameter key '" + key + "' encountered around index ");
		
		final ArgWrapper<?> value = valueType.parse(parser);
		
		data.namedParams.put(key, value);
		
	}
	
	public void parse(final Parser parser, final ParserSelectorContent.ParserData data) throws SyntaxErrorException, CompletionException
	{
		final IDataType<?> valueType = this.getRequiredType(data.unnamedParams.size());
		
		if (valueType == null)
			throw ParsingUtilities.SEE("Too many unnamed paraWmeters encountered while parsing selector (around index " + parser.getIndex() + ")");
		
		final ArgWrapper<?> value = valueType.parse(parser);
		
		data.unnamedParams.add(value);
	}
	
	public IExParse<Void, ParserSelectorContent.ParserData> getKVPair()
	{
		return this.kvPair;
	}
	
	public IParse<ArgWrapper<?>> getContentParser()
	{
		return this.contentParser;
	}
	
	public static final <T> CommandArg<T> getParam(final TypeID<T> type, final int index, final String name, final List<ArgWrapper<?>> unnamedParams, final Map<String, ArgWrapper<?>> namedParams)
	{
		if (name != null)
		{
			final ArgWrapper<?> ret = namedParams.get(name);
			if (ret != null)
				return ret.get(type);
		}
		
		return index < unnamedParams.size() ? unnamedParams.get(index).get(type) : null;
	}
	
	public static final <T> CommandArg<T> getParam(final TypeID<T> type, final int index, final List<ArgWrapper<?>> unnamedParams)
	{
		return index < unnamedParams.size() ? unnamedParams.get(index).get(type) : null;
	}
	
	public static final <T> CommandArg<T> getRequiredParam(final TypeID<T> type, final int index, final String name, final List<ArgWrapper<?>> unnamedParams, final Map<String, ArgWrapper<?>> namedParams) throws SyntaxErrorException
	{
		final CommandArg<T> ret = getParam(type, index, name, unnamedParams, namedParams);
		
		if (ret != null)
			return ret;
		
		throw ParsingUtilities.SEE("Missing parameter for selector: " + (name != null ? name : index));
	}
	
	public static final <T> CommandArg<T> getRequiredParam(final TypeID<T> type, final int index, final List<ArgWrapper<?>> unnamedParams) throws SyntaxErrorException
	{
		final CommandArg<T> ret = getParam(type, index, unnamedParams);
		
		if (ret != null)
			return ret;
		
		throw ParsingUtilities.SEE("Missing parameter for selector: " + index);
	}
	
	public static final <T> CommandArg<T> getRequiredParam(final TypeID<T> type, final String name, final Map<String, ArgWrapper<?>> namedParams) throws SyntaxErrorException
	{
		final ArgWrapper<?> ret = namedParams.get(name);
		
		if (ret != null)
			return ret.get(type);
		
		throw ParsingUtilities.SEE("Missing parameter for selector: " + name);
	}
	
	public static void addAlias(final String name, final String... aliases)
	{
		final SelectorDescriptor descriptor = getDescriptor(name);
		
		registerSelector(descriptor, aliases);
	}
}
