package net.minecraft.command.type.custom;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletionData;
import net.minecraft.command.descriptors.SelectorDescriptor;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CTypeCompletable;
import net.minecraft.command.type.TypeID;

public class ParserSelector<T> extends CTypeCompletable<T>
{
	private final TypeID<T> target;
	
	public ParserSelector(final TypeID<T> target)
	{
		this.target = target;
	}
	
	@Override
	public ArgWrapper<T> iParse(final Parser parser) throws SyntaxErrorException, net.minecraft.command.parser.CompletionException
	{
		final SelectorDescriptor descriptor = ParserUntypedSelector.parseName(parser);
		
		// return descriptor.resultType.convertTo(descriptor.getContentParser().parse(parser), this.target);
		return descriptor.getContentParser().parse(parser).convertTo(this.target);
	}
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
	{
		TabCompletionData.addToSet(tcDataSet, parser.toParse, startIndex, cData, this.target.getCompletions());
	}
}
