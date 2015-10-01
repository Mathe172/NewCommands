package net.minecraft.command.type.custom;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.ChatComponentList;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.arg.PrimitiveParameter;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.CTypeParse;
import net.minecraft.command.type.IParse;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

public final class ParserSayString extends CTypeParse<IChatComponent>
{
	public static final CDataType<IChatComponent> parser = new ParserSayString();
	
	private ParserSayString()
	{
	}
	
	@Override
	public ArgWrapper<IChatComponent> iParse(final Parser parser, final Context context) throws SyntaxErrorException
	{
		parser.incIndex(1);
		
		final List<CommandArg<IChatComponent>> parts = new ArrayList<>();
		
		final IParse<ArgWrapper<IChatComponent>> selectorParser = TypeIDs.IChatComponent.selectorParser;
		final IParse<ArgWrapper<IChatComponent>> labelParser = TypeIDs.IChatComponent.labelParser;
		
		int startIndex = parser.getIndex();
		StringBuilder sb = new StringBuilder();
		
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
			case '!':
			case '@':
			case '$':
				sb.append(parser.toParse, startIndex, parser.getIndex() - (3 + backslashCount) / 2);
				startIndex = parser.getIndex();
				
				final boolean evenBackslashes = backslashCount % 2 == 0;
				backslashCount = 0;
				
				if (evenBackslashes)
				{
					if (nextChar == '@' || nextChar == '$')
						try
						{
							CommandArg<IChatComponent> ret;
							
							if (nextChar == '@')
								ret = selectorParser.parseSnapshot(parser).arg();
							else
								ret = labelParser.parseSnapshot(parser).arg();
							
							startIndex = parser.getIndex();
							sb = resetSB(parts, sb);
							parts.add(ret);
							continue;
						} catch (final SyntaxErrorException e)
						{
						}
				}
				else
				{
					if (nextChar == '0')
						break outerLoop;
					if (nextChar == '!')
						continue;
				}
				
				sb.append(nextChar);
				break;
			default:
				backslashCount = 0;
			}
		}
		
		sb.append(parser.toParse, startIndex, parser.getIndex());
		
		if (parts.isEmpty())
			return TypeIDs.IChatComponent.wrap(new ChatComponentText(sb.toString()));
		
		if (sb.length() != 0)
			parts.add(new PrimitiveParameter<IChatComponent>(new ChatComponentText(sb.toString())));
		
		return TypeIDs.IChatComponent.wrap(new ChatComponentList(parts));
	}
	
	private static StringBuilder resetSB(final List<CommandArg<IChatComponent>> parts, final StringBuilder sb)
	{
		if (sb.length() != 0)
		{
			parts.add(new PrimitiveParameter<IChatComponent>(new ChatComponentText(sb.toString())));
			return new StringBuilder();
		}
		
		return sb;
	}
}
