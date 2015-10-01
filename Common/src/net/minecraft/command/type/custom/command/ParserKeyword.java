package net.minecraft.command.type.custom.command;

import java.util.regex.Matcher;

import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletionData;
import net.minecraft.command.construction.CommandDescriptorDefault;
import net.minecraft.command.construction.CommandDescriptorDefault.CParserData;
import net.minecraft.command.descriptors.ICommandDescriptor;
import net.minecraft.command.parser.CompletionParser;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.base.ExCustomCompletable;
import net.minecraft.command.type.metadata.MetaEntry;
import net.minecraft.command.type.metadata.MetaEntry.PrimitiveHint;

public class ParserKeyword extends ExCustomCompletable<ICommandDescriptor<? super CParserData>, CParserData>
{
	private final CommandDescriptorDefault descriptor;
	
	public ParserKeyword(final CommandDescriptorDefault descriptor)
	{
		this.descriptor = descriptor;
	}
	
	private static final MetaEntry<PrimitiveHint, Void> hint = new MetaEntry<PrimitiveHint, Void>(CompletionParser.hintID)
	{
		@Override
		public PrimitiveHint get(final Parser parser, final Void parserData)
		{
			final Matcher m = parser.getMatcher(ParsingUtilities.keyMatcher);
			final Matcher wm = parser.getMatcher(ParsingUtilities.whitespaceMatcher);
			
			final int index = parser.getIndex() + (parser.find(m) ? m.group().length() : 0);
			wm.find(index);
			
			return wm.group().length() + index == parser.len ? CompletionParser.propose : null;
		}
	};
	
	@Override
	public ICommandDescriptor<? super CParserData> iParse(final Parser parser, final CParserData data) throws SyntaxErrorException
	{
		// No parser.checkEnd() needed
		final Matcher m = parser.getMatcher(ParsingUtilities.keyMatcher);
		
		if (parser.find(m))
		{
			final String keyword = m.group(1).toLowerCase();
			
			final ICommandDescriptor<? super CParserData> ret = this.descriptor.getSubDescriptor(keyword);
			
			if (ret != null)
			{
				parser.incIndex(m);
				
				data.add(keyword);
				
				return ret;
			}
		}
		
		parser.supplyHint(hint);
		
		return this.descriptor.getSubDescriptor("");
	}
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData, final CParserData data)
	{
		TabCompletionData.addToSet(tcDataSet, startIndex, cData, this.descriptor.getKeywordCompletions());
	}
}
