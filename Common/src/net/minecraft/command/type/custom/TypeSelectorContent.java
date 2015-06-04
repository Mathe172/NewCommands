package net.minecraft.command.type.custom;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.LabelWrapper;
import net.minecraft.command.arg.Setter;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletion;
import net.minecraft.command.completion.TabCompletionData;
import net.minecraft.command.descriptors.SParserData;
import net.minecraft.command.descriptors.SelectorDescriptor;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IExParse;
import net.minecraft.command.type.TypeCompletable;

public class TypeSelectorContent<D extends SParserData> extends TypeCompletable<ArgWrapper<?>>
{
	private final SelectorDescriptor<D> descriptor;
	
	public TypeSelectorContent(final SelectorDescriptor<D> descriptor)
	{
		this.descriptor = descriptor;
	}
	
	@Override
	public ArgWrapper<?> iParse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
	{
		final D parserData = this.descriptor.newParserData(parser);
		
		if (parser.endReached() || parser.toParse.charAt(parser.getIndex()) != '[')
			return this.descriptor.construct(parserData);
		
		parser.incIndex(1);
		
		parser.terminateCompletion();
		
		final IExParse<Void, D> kvPair = this.descriptor.getKVPair();
		
		final Matcher m = parser.getMatcher(ParsingUtilities.listEndMatcher);
		
		while (true)
		{
			kvPair.parse(parser, parserData);
			
			if (!parser.findInc(m))
				throw parser.SEE("No delimiter found while parsing selector ");
			
			if ("]".equals(m.group(1)))
			{
				final ArgWrapper<?> ret = this.descriptor.construct(parserData);
				
				if (parserData.label == null)
					return parserData.finalize(ret);
				
				return parserData.finalize(procLabelModifier(parser, parserData, ret));
			}
			
			if ("}".equals(m.group(1)))
				throw parser.SEE("Unexpected '}' ");
		}
	}
	
	private <T> ArgWrapper<T> procLabelModifier(final Parser parser, final D parserData, final ArgWrapper<T> ret) throws SyntaxErrorException
	{
		if (parserData.labelModifier == 0)
		{
			final LabelWrapper<?> label = new LabelWrapper<>(
				parserData.labelType == null
					? ret.type
					: parserData.labelType, parserData.label);
			
			parser.addLabel(parserData.label, label);
			
			return ret.linkSetter(label.getLabelSetter(parser, ret.type, true));
		}
		
		final Setter<T> setter = parser.getLabelSetter(parserData.label, ret.type, parserData.labelModifier == '^');
		
		return ret.linkSetter(setter);
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
