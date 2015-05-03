package net.minecraft.command.type.custom.command;

import java.util.regex.Matcher;

import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletionData;
import net.minecraft.command.descriptors.CommandDescriptor;
import net.minecraft.command.descriptors.CommandDescriptor.ParserData;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.base.ExCustomCompletable;

public class ParserKeyword extends ExCustomCompletable<CommandDescriptor, ParserData>
{
	private final CommandDescriptor descriptor;
	
	public ParserKeyword(final CommandDescriptor descriptor)
	{
		this.descriptor = descriptor;
	}
	
	@Override
	public CommandDescriptor iParse(final Parser parser, final ParserData data) throws SyntaxErrorException
	{
		// No parser.checkEnd() needed
		final Matcher m = parser.getMatcher(ParsingUtilities.keyMatcher);
		
		if (parser.find(m))
		{
			final String keyword = m.group(1);
			
			final CommandDescriptor ret = this.descriptor.getSubType(keyword);
			
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
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData, final ParserData data)
	{
		TabCompletionData.addToSet(tcDataSet, startIndex, cData, this.descriptor.getKeywordCompletions());
	}
}
