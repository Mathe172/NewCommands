package net.minecraft.command.type.custom;

import java.util.regex.Matcher;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletionData;
import net.minecraft.command.descriptors.CommandDescriptor;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.base.CustomCompletable;

public class ParserKeyword extends CustomCompletable<CommandDescriptor>
{
	private final CommandDescriptor descriptor;
	
	public ParserKeyword(final CommandDescriptor descriptor)
	{
		this.descriptor = descriptor;
	}
	
	@Override
	public CommandDescriptor iParse(final Parser parser) throws SyntaxErrorException
	{
		// No parser.checkEnd() needed
		final Matcher m = parser.keyMatcher;
		
		if (parser.find(m))
		{
			final CommandDescriptor ret = this.descriptor.getSubType(m.group(1));
			
			if (ret != null)
			{
				parser.incIndex(m);
				
				return ret;
			}
		}
		
		parser.proposeCompletion();
		
		return this.descriptor.getSubType("");
	}
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
	{
		TabCompletionData.addToSet(tcDataSet, parser.toParse, startIndex, cData, this.descriptor.getKeywordCompletions());
	}
}
