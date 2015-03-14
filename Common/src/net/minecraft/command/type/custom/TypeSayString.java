package net.minecraft.command.type.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.arg.PrimitiveParameter;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.IParse;
import net.minecraft.command.type.CTypeParse;
import net.minecraft.command.type.base.CompositeString;

public class TypeSayString extends CTypeParse<String>
{
	public static final CDataType<String> sayStringType = new TypeSayString();
	
	public static final Pattern sayStringPattern = Pattern.compile("\\G[^@\\$]*+([@\\$])");
	
	@Override
	public ArgWrapper<String> parse(final Parser parser) throws SyntaxErrorException, CompletionException
	{
		final List<CommandArg<String>> parts = new ArrayList<>();
		
		final Matcher m = parser.sayStringMatcher;
		
		final IParse<ArgWrapper<String>> selectorParser = TypeIDs.String.selectorParser;
		final IParse<ArgWrapper<String>> labelParser = TypeIDs.String.labelParser;
		
		int startIndex = parser.getIndex();
		
		while (parser.findInc(m))
		{
			try
			{
				final int index = parser.getIndex();
				CommandArg<String> ret;
				
				if ("@".equals(m.group(1)))
					ret = selectorParser.parseSnapshot(parser).arg;
				else
					ret = labelParser.parseSnapshot(parser).arg;
				
				parts.add(new PrimitiveParameter<>(parser.toParse.substring(startIndex, index - 1)));
				
				parts.add(ret);
				
				startIndex = parser.getIndex();
				
			} catch (final SyntaxErrorException e)
			{
			}
			
		}
		
		parser.setIndexEnd();
		
		if (parts.isEmpty())
			return new ArgWrapper<>(TypeIDs.String, parser.toParse.substring(startIndex));
		
		if (startIndex != parser.len - 1)
			parts.add(new PrimitiveParameter<>(parser.toParse.substring(startIndex)));
		
		return new ArgWrapper<>(TypeIDs.String, new CompositeString(parts));
	}
}
