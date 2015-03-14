package net.minecraft.command.type.custom;

import java.util.regex.Matcher;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletionData;
import net.minecraft.command.descriptors.SelectorDescriptor;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IParse;
import net.minecraft.command.type.base.CustomCompletable;

public class ParserUntypedSelector extends CustomCompletable<ArgWrapper<?>>
{
	@Override
	public ArgWrapper<?> iParse(final Parser parser) throws SyntaxErrorException, CompletionException
	{
		final SelectorDescriptor descriptor = parseName(parser);
		
		return descriptor.getContentParser().parse(parser);
	}
	
	public static SelectorDescriptor parseName(final Parser parser) throws SyntaxErrorException
	{
		final Matcher m = parser.nameMatcher;
		
		parser.find(m);
		
		final SelectorDescriptor descriptor = SelectorDescriptor.getDescriptor(m.group());
		
		if (descriptor == null)
			throw parser.SEE("Unknown selector type: " + m.group() + " around index ");
		
		parser.incIndex(m);
		
		return descriptor;
	}
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
	{
		TabCompletionData.addToSet(tcDataSet, parser.toParse, startIndex, cData, SelectorDescriptor.getCompletions());
	}
	
	public static IParse<ArgWrapper<?>> parser = new ParserUntypedSelector();
}
