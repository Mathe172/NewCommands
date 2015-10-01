package net.minecraft.command.legacy;

import java.util.List;
import java.util.Set;

import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.completion.DataRequest;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletionData.Weighted;
import net.minecraft.command.legacy.LegacyCommand.LegacyParserData;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.base.ExCustomCompletable;

/**
 * Legacy support
 */
@Deprecated
public class TypeLegacyArg extends ExCustomCompletable<Void, LegacyParserData>
{
	private TypeLegacyArg()
	{
	}
	
	public static final TypeLegacyArg parser = new TypeLegacyArg();
	
	@Override
	public Void iParse(final Parser parser, final LegacyParserData parserData) throws SyntaxErrorException
	{
		if (!parser.find(ParsingUtilities.spaceMatcher))
		{
			parserData.argString = null;
			return null;
		}
		
		int startIndex = parser.getIndex();
		final StringBuilder sb = new StringBuilder();
		
		int backslashCount = 0;
		
		outerLoop: while (!parser.endReached())
		{
			final char nextChar = parser.consumeNextChar();
			
			switch (nextChar)
			{
			case '\\':
				++backslashCount;
				continue;
			case '0':
				sb.append(parser.toParse, startIndex, parser.getIndex() - (3 + backslashCount) / 2);
				startIndex = parser.getIndex() - 1;
				
				if (backslashCount % 2 == 1)
					break outerLoop;
			default:
				backslashCount = 0;
			}
		}
		
		sb.append(parser.toParse, startIndex, parser.getIndex());
		
		parserData.argString = sb.toString();
		
		return null;
	}
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData, final LegacyParserData parserData)
	{
		final String[] args = parserData.argString != null ? LegacyCommand.dropFirstString(parserData.argString.split(" ", -1)) : new String[0];
		
		tcDataSet.add(new DataRequest()
		{
			private List<String> completions;
			
			@Override
			public void process()
			{
				this.completions = parserData.command.addTabCompletionOptions(cData.sender, args, cData.hovered);
			}
			
			@Override
			public void createCompletions(final Set<Weighted> tcDataSet)
			{
				if (this.completions == null)
					return;
				
				final int oldLen = args.length > 0 ? args[args.length - 1].length() : 0;
				
				for (final String completion : this.completions)
					tcDataSet.add(new Weighted(completion, parser.getIndex(), parser.getIndex(), completion.substring(oldLen), parser.getIndex() + completion.length() - oldLen, 0));
			}
		});
	}
}
