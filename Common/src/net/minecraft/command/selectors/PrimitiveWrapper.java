package net.minecraft.command.selectors;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.descriptors.SelectorDescriptorSingleArg.ParserDataSingleArg;
import net.minecraft.command.descriptors.SelectorDescriptorSingleArg.SingleArgConstructable;

public class PrimitiveWrapper
{
	public static final SingleArgConstructable constructable = new SingleArgConstructable()
	{
		@Override
		public ArgWrapper<?> construct(final ParserDataSingleArg parserData) throws SyntaxErrorException
		{
			return parserData.getRequiredArg();
		}
	};
}
