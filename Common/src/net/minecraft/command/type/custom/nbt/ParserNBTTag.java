package net.minecraft.command.type.custom.nbt;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.parser.MatcherRegistry;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IComplete;
import net.minecraft.command.type.base.ExCustomParse;
import net.minecraft.command.type.custom.nbt.NBTDescriptor.Tag;
import net.minecraft.command.type.custom.nbt.NBTUtilities.NBTData;
import net.minecraft.command.type.metadata.ICompletable;
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
	public final static MatcherRegistry baseMatcher = new MatcherRegistry("\\G[^\\[,}\\]]*+(?:(\\[)|(?=[,}\\]]))");
	public final static MatcherRegistry numberMatcher = new MatcherRegistry(Pattern.compile("\\G\\s*+(?>(([+-]?+)(?=\\.?+\\d)(\\d*+)([bsil]|(\\.\\d*+)?+([df]?+))|true|false))(?=\\s*+[,\\]}])", Pattern.CASE_INSENSITIVE));
	
	private final Tag descriptor;
	
	public ParserNBTTag(final Tag descriptor)
	{
		this.descriptor = descriptor;
	}
	
	public ParserNBTTag(final Tag descriptor, final IComplete completer)
	{
		this(descriptor);
		this.addEntry(new ICompletable.Default(completer));
	}
	
	@Override
	public Void iParse(final Parser parser, final NBTData parserData) throws SyntaxErrorException
	{
		final Matcher m = parser.getMatcher(specialMatcher);
		
		if (parser.findInc(m))
			switch (m.group(1))
			{
			case "\"":
				ParserNBTQString.parse(parser, parserData);
				return null;
			case "\\@":
				parserData.add(ParserNBTSelector.parser.parse(parser));
				return null;
			case "\\$":
				parserData.add(ParserNBTLabel.parser.parse(parser));
				return null;
			case "[":
				this.descriptor.getListParser().parse(parser, parserData);
				return null;
			case "{":
				this.descriptor.getCompoundParser().parse(parser, parserData);
				return null;
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
			
			final char idc = id.isEmpty() ? 0 : Character.toLowerCase(id.charAt(0));
			
			switch (idc)
			{
			case 0:
			case 'i':
				parserData.put(new NBTTagInt(Integer.parseInt(number)));
				return null;
			case 'b':
				parserData.put(new NBTTagByte(Byte.parseByte(number)));
				return null;
			case 's':
				parserData.put(new NBTTagShort(Short.parseShort(number)));
				return null;
			case 'l':
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
		
		parserData.put(new NBTTagString(parseString(parser)));
		
		return null;
	}
	
	private static final String parseString(final Parser parser) throws SyntaxErrorException
	{
		return ParsingUtilities.parseLazyString(parser, baseMatcher);
	}
}
