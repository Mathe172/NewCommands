package net.minecraft.command.type.custom;

import java.util.Set;
import java.util.regex.Matcher;

import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.completion.ProviderCompleter;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CListProvider;
import net.minecraft.command.type.base.CustomCompletable;

public class TypeUntypedLabel extends CustomCompletable<ArgWrapper<?>>
{
	private final ProviderCompleter completer;
	
	public TypeUntypedLabel()
	{
		this.completer = new ProviderCompleter(new CListProvider()
		{
			@Override
			public Set<ITabCompletion> getList(final Parser parser)
			{
				return parser.getLabelCompletions();
			}
		});
	}
	
	@Override
	public ArgWrapper<?> iParse(final Parser parser, final Context context) throws SyntaxErrorException
	{
		return parseLabel(parser);
	}
	
	public static ArgWrapper<?> parseLabel(final Parser parser) throws SyntaxErrorException
	{
		final Matcher m = parser.getMatcher(ParsingUtilities.nameMatcher);
		
		if (!parser.find(m))
			throw parser.SEE("No label name found ");
		
		final ArgWrapper<?> label = parser.getLabel(m.group());
		
		if (label == null)
			throw parser.SEE("Unknown label '" + m.group() + "' ");
		
		parser.incIndex(m);
		
		return label;
	}
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
	{
		this.completer.complete(tcDataSet, parser, startIndex, cData);
	}
}
