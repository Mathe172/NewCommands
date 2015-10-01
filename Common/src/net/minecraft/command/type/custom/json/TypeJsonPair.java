package net.minecraft.command.type.custom.json;

import java.util.Set;
import java.util.regex.Matcher;

import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletionData;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.MatcherRegistry;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.base.ExCustomCompletable;
import net.minecraft.command.type.custom.json.ParserJsonObject.JsonObjectData;

public class TypeJsonPair extends ExCustomCompletable<Void, JsonObjectData>
{
	public static final MatcherRegistry keyMatcher = new MatcherRegistry("\\G\\s*+(?:([\"'])|((?:\\s*+[^,\\]}\\s:]++)++)\\s*+:)");
	
	private final JsonDescriptor.Object descriptor;
	
	public TypeJsonPair(final JsonDescriptor.Object descriptor)
	{
		this.descriptor = descriptor;
	}
	
	@Override
	public Void iParse(final Parser parser, final JsonObjectData parserData) throws SyntaxErrorException
	{
		final Matcher m = parser.getMatcher(keyMatcher);
		
		if (!parser.findInc(m))
			throw parser.SEE("Missing member name ");
		
		ParsingUtilities.terminateCompletion(parser);
		
		String name;
		
		if (m.group(1) != null)
		{
			name = ParsingUtilities.parseEscapedString(parser, m.group(1).charAt(0));
			parser.findInc(ParsingUtilities.whitespaceMatcher);
			
			if (parser.endReached() || parser.consumeNextChar() != ':')
				throw parser.SEE("Invalid syntax for Json-member-name ");
		}
		else
			name = m.group(2);
		
		parserData.name = name;
		
		this.descriptor.getSubDescriptor(name).getElementParser().parse(parser, parserData);
		
		return null;
	}
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData, final JsonObjectData parserData)
	{
		final Set<String> keySet = parserData.keySet();
		
		for (final ITabCompletion tc : this.descriptor.getKeyCompletions())
			if (!keySet.contains(tc.name))
				TabCompletionData.addToSet(tcDataSet, startIndex, cData, tc);
	}
}
