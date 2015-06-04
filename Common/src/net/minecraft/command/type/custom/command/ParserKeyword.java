package net.minecraft.command.type.custom.command;

import java.util.regex.Matcher;

import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletionData;
import net.minecraft.command.descriptors.CommandDescriptor;
import net.minecraft.command.descriptors.CommandDescriptor.CParserData;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.base.ExCustomCompletable;

public class ParserKeyword<D extends CParserData> extends ExCustomCompletable<CommandDescriptor<? super D>, D>
{
	private final CommandDescriptor<D> descriptor;
	
	public ParserKeyword(final CommandDescriptor<D> descriptor)
	{
		this.descriptor = descriptor;
	}
	
	@Override
	public CommandDescriptor<? super D> iParse(final Parser parser, final D data) throws SyntaxErrorException
	{
		// No parser.checkEnd() needed
		final Matcher m = parser.getMatcher(ParsingUtilities.keyMatcher);
		
		if (parser.find(m))
		{
			final String keyword = m.group(1).toLowerCase(); // TODO:...
			
			final CommandDescriptor<? super D> ret = this.descriptor.getSubType(keyword);
			
			if (ret != null)
			{
				parser.incIndex(m);
				
				data.add(keyword);
				
				return ret;
			}
		}
		
		parser.proposeCompletion();
		
		return this.descriptor.getSubType("");
	}
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData, final CParserData data)
	{
		TabCompletionData.addToSet(tcDataSet, startIndex, cData, this.descriptor.getKeywordCompletions());
	}
}
