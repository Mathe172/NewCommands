package net.minecraft.command.type.custom.json;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonPrimitive;
import com.google.gson.internal.LazilyParsedNumber;

import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.parser.MatcherRegistry;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IComplete;
import net.minecraft.command.type.base.ExCustomParse;
import net.minecraft.command.type.custom.json.JsonDescriptor.Element;
import net.minecraft.command.type.custom.json.JsonUtilities.JsonData;
import net.minecraft.command.type.metadata.ICompletable;

public class ParserJsonElement extends ExCustomParse<Void, JsonData>
{
	public static final MatcherRegistry specialMatcher = new MatcherRegistry("\\G\\s*+(['\"\\[{]|\\\\[@\\$])");
	public static final MatcherRegistry literalMatcher = new MatcherRegistry(Pattern.compile(
		"\\G\\s*+(?:(?:([+-]?+(?=\\.?+\\d)\\d*+\\.?+\\d*+(?:e[+-]?+\\d++)?+)|(true)|(false)|(null))(?=\\s*+(?:[,\\]}]|\\z))|((?:\\s*+[^,\\]}\\s]++)++))",
		Pattern.CASE_INSENSITIVE));
	
	private final JsonDescriptor.Element descriptor;
	
	public ParserJsonElement(final JsonDescriptor.Element descriptor)
	{
		this.descriptor = descriptor;
	}
	
	public ParserJsonElement(final Element descriptor, final IComplete completer)
	{
		this(descriptor);
		this.addEntry(new ICompletable.Default(completer));
	}
	
	@Override
	public Void iParse(final Parser parser, final JsonData parserData) throws SyntaxErrorException
	{
		final Matcher m = parser.getMatcher(specialMatcher);
		
		if (parser.findInc(m))
			switch (m.group(1))
			{
			case "'":
			case "\"":
				final CommandArg<String> ret = ParsingUtilities.parseQuotedString(parser, parserData, m.group(1).charAt(0));
				
				if (ret != null)
					parserData.add(JsonUtilities.tranfsorm(ret));
				
				return null;
			case "\\@":
				parserData.add(JsonUtilities.parseSelector(parser));
				return null;
			case "\\$":
				parserData.add(JsonUtilities.parseLabel(parser));
				return null;
			case "[":
				this.descriptor.getArrayParser().parse(parser, parserData);
				return null;
			case "{":
				this.descriptor.getObjectParser().parse(parser, parserData);
				return null;
			}
		
		final Matcher nm = parser.getMatcher(literalMatcher);
		
		parserData.put(
			parser.findInc(nm)
				? nm.group(1) == null
					? nm.group(2) == null
						? nm.group(3) == null
							? nm.group(4) == null
								? new JsonPrimitive(nm.group(5))
								: null
							: new JsonPrimitive(false)
						: new JsonPrimitive(true)
					: new JsonPrimitive(new LazilyParsedNumber(nm.group(1)))
				: null,
			this.descriptor.type());
		
		return null;
	}
}
