package net.minecraft.command.type.custom.nbt;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.command.MatcherRegistry;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.base.ExCustomParse;
import net.minecraft.command.type.custom.nbt.NBTDescriptor.Tag;
import net.minecraft.command.type.custom.nbt.NBTUtilities.NBTData;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;

public class ParserNBTTag extends ExCustomParse<Void, NBTData>
{
	public final static MatcherRegistry specialMatcher = new MatcherRegistry("\\G\\s*+([\"\\[{]|\\\\[@\\$])");
	
	public final static MatcherRegistry baseMatcher = new MatcherRegistry("\\G[^\\[,}\\]]*+(?:(\\[)|(?=,|}|\\]))");
	public final static MatcherRegistry stackedMatcher = new MatcherRegistry("\\G[^\\[\\]]*+(\\[|\\])");
	
	public final static MatcherRegistry numberMatcher = new MatcherRegistry(Pattern.compile("\\G\\s*+(?>(([+-]?+)(?=\\.?+\\d)(\\d*+)([bsl]|(\\.\\d*+)?+([df]?+))|true|false))(?=\\s*+[,\\]}])", Pattern.CASE_INSENSITIVE));
	
	private final Tag descriptor;
	
	public ParserNBTTag(final Tag descriptor)
	{
		this.descriptor = descriptor;
	}
	
	@Override
	public Void parse(final Parser parser, final NBTData parserData) throws SyntaxErrorException, CompletionException
	{
		final Matcher m = parser.getMatcher(specialMatcher);
		
		if (parser.findInc(m))
		{
			switch (m.group(1))
			{
			case "\"":
				ParserNBTQString.parse(parser, parserData);
				return null;
			case "\\@":
				parserData.put(ParserNBTSelector.parser.parse(parser));
				return null;
			case "\\$":
				parserData.put(ParserNBTLabel.parser.parse(parser));
				return null;
			case "[":
				this.descriptor.getListParser().parse(parser, parserData);
				return null;
			case "{":
				this.descriptor.getCompoundParser().parse(parser, parserData);
				return null;
			}
		}
		
		final Matcher nm = parser.getMatcher(numberMatcher);
		
		if (parser.findInc(nm))
		{
			if ("true".equalsIgnoreCase(nm.group(1)))
			{
				parserData.put(new NBTTagByte((byte) 1));
				return null;
			}
			if ("false".equalsIgnoreCase(nm.group(1)))
			{
				parserData.put(new NBTTagByte((byte) 0));
				return null;
			}
			
			String number = nm.group(2) + nm.group(3);
			
			final String id = nm.group(4);
			
			if ("".equals(id))
			{
				parserData.put(new NBTTagInt(Integer.parseInt(number)));
				return null;
			}
			if ("b".equalsIgnoreCase(id))
			{
				parserData.put(new NBTTagByte(Byte.parseByte(number)));
				return null;
			}
			if ("s".equalsIgnoreCase(id))
			{
				parserData.put(new NBTTagShort(Short.parseShort(number)));
				return null;
			}
			if ("l".equalsIgnoreCase(id))
			{
				parserData.put(new NBTTagLong(Long.parseLong(number)));
				return null;
			}
			
			final String dotNumber = nm.group(5);
			
			if (dotNumber != null)
				number += dotNumber;
			
			if ("f".equalsIgnoreCase(nm.group(6)))
			{
				parserData.put(new NBTTagFloat(Float.parseFloat(number)));
				return null;
			}
			
			parserData.put(new NBTTagDouble(Double.parseDouble(number)));
			return null;
		}
		
		parserData.put(parseString(parser));
		return null;
	}
	
	private static final NBTTagString parseString(final Parser parser) throws SyntaxErrorException
	{
		final int startIndex = parser.getIndex();
		int level = 0;
		
		final Matcher bm = parser.getMatcher(baseMatcher);
		final Matcher sm = parser.getMatcher(stackedMatcher);
		
		while (true)
		{
			if (level == 0)
			{
				if (!parser.findInc(bm))
					throw parser.SEE("Missing ']', '}' or ',' around index ");
				
				if (bm.group(1) == null)
					return new NBTTagString(parser.toParse.substring(startIndex, parser.getIndex()).trim());
				
				level = 1;
			}
			else
			{
				if (!parser.findInc(sm))
					throw parser.SEE("Missing ']' around index ");
				
				if ("[".equals(sm.group(1)))
					++level;
				else
					--level;
			}
		}
	}
}
