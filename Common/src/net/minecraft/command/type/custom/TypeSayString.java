package net.minecraft.command.type.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.ChatComponentList;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.arg.PrimitiveParameter;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.MatcherRegistry;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.CTypeParse;
import net.minecraft.command.type.IParse;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

public final class TypeSayString extends CTypeParse<IChatComponent>
{
	public static final CDataType<IChatComponent> sayStringType = new TypeSayString();
	
	public static final MatcherRegistry sayStringMatcher = new MatcherRegistry("\\G[^@\\$]*+([@\\$])");
	
	private TypeSayString()
	{
	}
	
	@Override
	public ArgWrapper<IChatComponent> parse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
	{
		parser.incIndex(1);
		
		final List<CommandArg<IChatComponent>> parts = new ArrayList<>();
		
		final Matcher m = parser.getMatcher(sayStringMatcher);
		
		final IParse<ArgWrapper<IChatComponent>> selectorParser = TypeIDs.IChatComponent.selectorParser;
		final IParse<ArgWrapper<IChatComponent>> labelParser = TypeIDs.IChatComponent.labelParser;
		
		int startIndex = parser.getIndex();
		
		while (parser.findInc(m))
		{
			try
			{
				final int index = parser.getIndex();
				CommandArg<IChatComponent> ret;
				
				if ("@".equals(m.group(1)))
					ret = selectorParser.parseSnapshot(parser).arg();
				else
					ret = labelParser.parseSnapshot(parser).arg();
				
				parts.add(new PrimitiveParameter<IChatComponent>(new ChatComponentText(parser.toParse.substring(startIndex, index - 1))));
				
				parts.add(ret);
				
				startIndex = parser.getIndex();
				
			} catch (final SyntaxErrorException e)
			{
			}
			
		}
		
		parser.setIndexEnd();
		
		if (parts.isEmpty())
			return TypeIDs.IChatComponent.wrap(new ChatComponentText(parser.toParse.substring(startIndex)));
		
		if (startIndex != parser.len)
			parts.add(new PrimitiveParameter<IChatComponent>(new ChatComponentText(parser.toParse.substring(startIndex))));
		
		return TypeIDs.IChatComponent.wrap(new ChatComponentList(parts));
	}
}
