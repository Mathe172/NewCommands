package net.minecraft.command.descriptors;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.trie.PatriciaTrie;

import net.minecraft.command.IPermission;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.PermissionWrapper;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletion;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IExParse;
import net.minecraft.command.type.IParse;
import net.minecraft.command.type.custom.KVPair;
import net.minecraft.command.type.custom.TypeSelectorContent;
import net.minecraft.command.type.management.TypeID;

public abstract class SelectorDescriptor<D extends SParserData>
{
	private final Set<TypeID<?>> resultTypes;
	private final IPermission permission;
	
	private final KVPair<D> kvPair;
	
	private final IParse<ArgWrapper<?>> contentParser;
	
	public abstract void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData, final D data);
	
	private final static PatriciaTrie<SelectorDescriptor<?>> selectors = new PatriciaTrie<>();
	public static final Map<ITabCompletion, IPermission> selectorCompletions = new HashMap<>();
	
	public SelectorDescriptor(final Set<TypeID<?>> resultTypes, final IPermission permission)
	{
		this.resultTypes = resultTypes;
		this.permission = permission;
		this.kvPair = new KVPair<D>(this);
		this.contentParser = new TypeSelectorContent<D>(this);
	}
	
	public static final void clear()
	{
		selectors.clear();
		selectorCompletions.clear();
	}
	
	public static void registerSelector(final String name, final SelectorDescriptor<?> descriptor)
	{
		if (selectors.put(name.toLowerCase(), descriptor) != null)
			throw new IllegalArgumentException("Selector already registerd: " + name);
		
		final ITabCompletion completion = name.length() == 1 ? new TabCompletion.SingleChar(name.charAt(0)) : new TabCompletion(name);
		selectorCompletions.put(completion, descriptor.permission);
		
		for (final TypeID<?> resultType : descriptor.resultTypes)
			resultType.addSelector(completion, descriptor.permission);
	}
	
	public static void registerSelector(final String name, final List<String> aliases, final SelectorDescriptor<?> descriptor)
	{
		registerSelector(name, descriptor);
		
		for (final String alias : aliases)
			registerSelector(alias, descriptor);
	}
	
	public static final SelectorDescriptor<?> getDescriptor(final String name)
	{
		return selectors.get(name.toLowerCase());
	}
	
	public final Set<TypeID<?>> getResultTypes()
	{
		return Collections.unmodifiableSet(this.resultTypes);
	}
	
	public final <T> ArgWrapper<T> construct(final TypeID<T> target, final D data) throws SyntaxErrorException
	{
		// return this.resultType.convertTo(this.construct(unnamedParams, namedParams), target);
		return this.construct(data).convertTo(data.parser, target);
	}
	
	public abstract ArgWrapper<?> construct(D data) throws SyntaxErrorException;
	
	public abstract void parse(final Parser parser, final String key, final D data) throws SyntaxErrorException;
	
	public abstract void parse(final Parser parser, final D data) throws SyntaxErrorException;
	
	public IExParse<Void, D> getKVPair()
	{
		return this.kvPair;
	}
	
	public final ArgWrapper<?> parse(final Parser parser) throws SyntaxErrorException
	{
		return PermissionWrapper.wrap(this.contentParser.parse(parser), this.permission);
	}
	
	public abstract D newParserData(Parser parser);
	
	public static void addAlias(final String name, final String... aliases)
	{
		final SelectorDescriptor<?> descriptor = getDescriptor(name);
		
		for (final String alias : aliases)
			registerSelector(alias, descriptor);
	}
}
