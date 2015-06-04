package net.minecraft.command.descriptors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.command.IPermission;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CompoundArg;
import net.minecraft.command.arg.Processable;
import net.minecraft.command.arg.TypedWrapper;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletion;
import net.minecraft.command.completion.TabCompletionData;
import net.minecraft.command.descriptors.SelectorDescriptorDefault.DefaultParserData;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IDataType;
import net.minecraft.command.type.management.TypeID;

public abstract class SelectorDescriptorDefault extends SelectorDescriptor<DefaultParserData>
{
	public static class DefaultParserData extends SParserData
	{
		public final List<Processable> toProcess = new ArrayList<>();
		public final Map<String, TypedWrapper<?>> namedParams = new HashMap<>();
		public final List<TypedWrapper<?>> unnamedParams = new ArrayList<>();
		
		public DefaultParserData(final Parser parser)
		{
			super(parser);
		}
		
		@Override
		public ArgWrapper<?> finalize(final ArgWrapper<?> selector)
		{
			return CompoundArg.create(this.toProcess, selector);
		}
		
		@Override
		public boolean requiresKey()
		{
			return !this.namedParams.isEmpty();
		}
	}
	
	private final List<IDataType<?>> unnamedTypes;
	private final Map<String, IDataType<?>> namedTypes;
	private final Set<ITabCompletion> keyCompletions;
	
	public SelectorDescriptorDefault(final List<IDataType<?>> unnamedTypes, final Map<String, IDataType<?>> namedTypes, final Set<TypeID<?>> resultTypes, final IPermission permission)
	{
		super(resultTypes, permission);
		this.unnamedTypes = unnamedTypes;
		this.namedTypes = namedTypes;
		this.keyCompletions = new HashSet<>(namedTypes.size());
		
		for (final String key : namedTypes.keySet())
		{
			final String s = key + "=";
			this.keyCompletions.add(new TabCompletion(s, s, key));
		}
	}
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData, final DefaultParserData data)
	{
		for (final ITabCompletion tc : this.keyCompletions)
			if (!data.namedParams.containsKey(tc.name.toLowerCase()))
				TabCompletionData.addToSet(tcDataSet, startIndex, cData, tc);
	}
	
	@Override
	public void parse(final Parser parser, final String key, final DefaultParserData data) throws SyntaxErrorException, CompletionException
	{
		final IDataType<?> valueType = this.namedTypes.get(key);
		
		if (valueType == null)
			throw parser.SEE("Unknown parameter key '" + key + "' encountered ");
		
		data.namedParams.put(key, valueType.parse(parser).addToProcess(data.toProcess));
	}
	
	@Override
	public void parse(final Parser parser, final DefaultParserData data) throws SyntaxErrorException, CompletionException
	{
		if (this.unnamedTypes.size() <= data.unnamedParams.size())
			throw parser.SEE("Too many unnamed parameters encountered while parsing selector (", ")");
		
		final IDataType<?> valueType = this.unnamedTypes.get(data.unnamedParams.size());
		
		final ArgWrapper<?> value = valueType.parse(parser);
		
		data.unnamedParams.add(value.addToProcess(data.toProcess));
	}
	
	@Override
	public DefaultParserData newParserData(final Parser parser)
	{
		return new DefaultParserData(parser);
	}
}
