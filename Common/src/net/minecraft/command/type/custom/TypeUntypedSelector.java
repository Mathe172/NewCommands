package net.minecraft.command.type.custom;

import java.util.regex.Matcher;

import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.PermissionWrapper;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.descriptors.SelectorDescriptor;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IParse;
import net.minecraft.command.type.TypeCompletable;

public class TypeUntypedSelector extends TypeCompletable<ArgWrapper<?>>
{
	@Override
	public ArgWrapper<?> iParse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
	{
		return parseName(parser).parse(parser);
	}
	
	public static SelectorDescriptor<?> parseName(final Parser parser) throws SyntaxErrorException
	{
		final Matcher m = parser.getMatcher(ParsingUtilities.nameMatcher);
		
		parser.find(m);
		
		final SelectorDescriptor<?> descriptor = SelectorDescriptor.getDescriptor(m.group());
		
		if (descriptor == null)
			throw parser.SEE("Unknown selector type: " + m.group() + " around index ");
		
		parser.incIndex(m);
		
		return descriptor;
	}
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
	{
		PermissionWrapper.complete(tcDataSet, startIndex, cData, SelectorDescriptor.selectorCompletions);
	}
	
	public static IParse<ArgWrapper<?>> parser = new TypeUntypedSelector();
}
