package net.minecraft.command.type.custom;

import java.util.Set;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.completion.ProviderCompleter;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CListProvider;
import net.minecraft.command.type.base.CustomCompletable;
import net.minecraft.command.type.management.CConvertable;

public class TypeLabel<T> extends CustomCompletable<T>
{
	private final ProviderCompleter completer;
	
	private final CConvertable<?, T> target;
	
	public TypeLabel(final CConvertable<?, T> target)
	{
		this.target = target;
		this.completer = new ProviderCompleter(new CListProvider()
		{
			@Override
			public Set<ITabCompletion> getList(final Parser parser)
			{
				return parser.getLabelCompletions(target);
			}
		});
	}
	
	@Override
	public T iParse(final Parser parser, final Context context) throws SyntaxErrorException
	{
		return TypeUntypedLabel.parseLabel(parser).convertTo(parser, this.target);
	}
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
	{
		this.completer.complete(tcDataSet, parser, startIndex, cData);
	}
}
