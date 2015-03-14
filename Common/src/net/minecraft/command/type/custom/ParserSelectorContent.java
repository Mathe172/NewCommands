package net.minecraft.command.type.custom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.Processable;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.descriptors.SelectorDescriptor;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IExParse;
import net.minecraft.command.type.TypeCompletable;
import net.minecraft.command.type.custom.nbt.NBTUtilities;

public class ParserSelectorContent extends TypeCompletable<ArgWrapper<?>>
{
	public static class ParserData
	{
		public final Map<String, ArgWrapper<?>> namedParams = new HashMap<>();
		public final List<ArgWrapper<?>> unnamedParams = new ArrayList<>();
		public String label = null;
	}
	
	private final SelectorDescriptor descriptor;
	
	public ParserSelectorContent(final SelectorDescriptor descriptor)
	{
		this.descriptor = descriptor;
	}
	
	@Override
	public ArgWrapper<?> iParse(final Parser parser) throws SyntaxErrorException, CompletionException
	{
		if (parser.endReached() || parser.toParse.charAt(parser.getIndex()) != '[')
			return this.descriptor.construct(Collections.<ArgWrapper<?>> emptyList(), Collections.<String, ArgWrapper<?>> emptyMap());
		
		parser.incIndex(1);
		
		parser.terminateCompletion();
		
		final ParserSelectorContent.ParserData parserData = new ParserSelectorContent.ParserData();
		
		final IExParse<Void, ParserSelectorContent.ParserData> kvPair = this.descriptor.getKVPair();
		
		final Matcher m = parser.listEndMatcher;
		
		while (true)
		{
			kvPair.parse(parser, parserData);
			
			if (!parser.findInc(m))
				throw parser.SEE("No delimiter found while parsing selector around index ");
			
			if ("]".equals(m.group(1)))
			{
				final ArgWrapper<?> ret = this.descriptor.construct(parserData.unnamedParams, parserData.namedParams);
				
				if (parserData.label == null)
					return ret;
				
				final ArgWrapper<?> cached = ret.cachedWrapper();
				
				parser.addToProcess((Processable) cached.arg);
				parser.addIgnoreErrors(false);
				
				parser.addLabel(parserData.label, cached);
				
				return cached;
			}
			
			if ("}".equals(m.group(1)))
				throw parser.SEE("Unexpected '}' around index ");
		}
	}
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
	{
		NBTUtilities.bracketCompleter.complete(tcDataSet, parser, startIndex, cData);
	}
}
