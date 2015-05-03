package net.minecraft.command.type.custom;

import java.util.regex.Matcher;

import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletion;
import net.minecraft.command.completion.TabCompletionData;
import net.minecraft.command.descriptors.SelectorDescriptor;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.base.ExCustomCompletable;
import net.minecraft.command.type.custom.TypeSelectorContent.ParserData;

public class KVPair<D extends ParserData> extends ExCustomCompletable<Void, D>
{
	private final SelectorDescriptor<D> descriptor;
	
	public KVPair(final SelectorDescriptor<D> descriptor)
	{
		this.descriptor = descriptor;
	}
	
	@Override
	public Void iParse(final Parser parser, final D data) throws SyntaxErrorException, CompletionException
	{
		final Matcher m = parser.getMatcher(ParsingUtilities.aKeyMatcher);
		
		if (parser.findInc(m))
		{
			parser.terminateCompletion();
			final String key = m.group(1).toLowerCase();
			
			if ("label".equals(key))
			{
				if (data.label != null)
					throw parser.SEE("Multiple labels encountered while parsing selector around index ");
				
				final Matcher km = parser.getMatcher(ParsingUtilities.keyMatcher);
				
				if (!parser.findInc(km))
					throw parser.SEE("Expected label name around index ");
				
				data.label = km.group(1);
				return null;
			}
			
			this.descriptor.parse(parser, key, data);
			
			return null;
		}
		
		if (!data.namedParams.isEmpty())
			throw parser.SEE("Missing key for parameter around index ");
		
		this.descriptor.parse(parser, data);
		
		return null;
	}
	
	private static final ITabCompletion labelCompletion = new TabCompletion("label=", "label");
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData, final D data)
	{
		this.descriptor.complete(tcDataSet, parser, startIndex, cData, data);
		
		if (data.label == null)
			TabCompletionData.addToSet(tcDataSet, startIndex, cData, labelCompletion);
	}
}
