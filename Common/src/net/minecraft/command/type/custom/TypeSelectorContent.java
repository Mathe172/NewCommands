package net.minecraft.command.type.custom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.Processable;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletion;
import net.minecraft.command.completion.TabCompletionData;
import net.minecraft.command.descriptors.SelectorDescriptor;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IExParse;
import net.minecraft.command.type.TypeCompletable;
import net.minecraft.command.type.custom.TypeSelectorContent.ParserData;

public class TypeSelectorContent<D extends ParserData> extends TypeCompletable<ArgWrapper<?>>
{
	public static class ParserData
	{
		public final Map<String, ArgWrapper<?>> namedParams = new HashMap<>();
		public final List<ArgWrapper<?>> unnamedParams = new ArrayList<>();
		public String label = null;
	}
	
	private final SelectorDescriptor<D> descriptor;
	private final D emptyData;
	
	public TypeSelectorContent(final SelectorDescriptor<D> descriptor)
	{
		this.descriptor = descriptor;
		this.emptyData = descriptor.newParserData();
	}
	
	@Override
	public ArgWrapper<?> iParse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
	{
		if (parser.endReached() || parser.toParse.charAt(parser.getIndex()) != '[')
			return this.descriptor.construct(this.emptyData);
		
		parser.incIndex(1);
		
		parser.terminateCompletion();
		
		final D parserData = this.descriptor.newParserData();
		
		final IExParse<Void, D> kvPair = this.descriptor.getKVPair();
		
		final Matcher m = parser.getMatcher(ParsingUtilities.listEndMatcher);
		
		while (true)
		{
			kvPair.parse(parser, parserData);
			
			if (!parser.findInc(m))
				throw parser.SEE("No delimiter found while parsing selector around index ");
			
			if ("]".equals(m.group(1)))
			{
				final ArgWrapper<?> ret = this.descriptor.construct(parserData);
				
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
	
	public static final ITabCompletion bracketCompletion = new TabCompletion(Pattern.compile("\\A\\[?+\\z"), "[]", "[]")
	{
		@Override
		public boolean complexFit()
		{
			return false;
		}
		
		@Override
		public int getSkipOffset(final Matcher m, final CompletionData cData)
		{
			return 0;
		}
		
		@Override
		public int getCursorOffset(final Matcher m, final CompletionData cData)
		{
			return -1;
		};
		
		@Override
		public double weightOffset(final Matcher m, final CompletionData cData)
		{
			return 1.0;
		}
		
		@Override
		public boolean fullMatch(final Matcher m, final CompletionData cData, final String replacement)
		{
			return false;
		}
	};
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
	{
		TabCompletionData.addToSet(tcDataSet, startIndex, cData, bracketCompletion);
	}
}
