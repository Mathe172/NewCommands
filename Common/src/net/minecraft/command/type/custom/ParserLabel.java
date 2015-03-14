package net.minecraft.command.type.custom;

import java.util.Set;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletion;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CListProvider;
import net.minecraft.command.type.ProviderCompleter;
import net.minecraft.command.type.CTypeCompletable;
import net.minecraft.command.type.TypeID;

public class ParserLabel<T> extends CTypeCompletable<T>
{
	private final ProviderCompleter completer;
	
	private final TypeID<T> target;
	
	public ParserLabel(final TypeID<T> target)
	{
		this.target = target;
		this.completer = new ProviderCompleter(new CListProvider()
		{
			@Override
			public Set<TabCompletion> getList(final Parser parser)
			{
				return parser.getLabelCompletions(target);
			}
		});
	}
	
	@Override
	public ArgWrapper<T> iParse(final Parser parser) throws SyntaxErrorException
	{
		return ParserUntypedLabel.parseLabel(parser).convertTo(this.target);
	}
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
	{
		this.completer.complete(tcDataSet, parser, startIndex, cData);
	}
}
