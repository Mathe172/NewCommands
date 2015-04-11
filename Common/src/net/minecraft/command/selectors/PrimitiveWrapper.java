package net.minecraft.command.selectors;

import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.construction.SelectorConstructable;
import net.minecraft.command.type.custom.TypeSelectorContent.ParserData;

public class PrimitiveWrapper
{
	public static final SelectorConstructable constructable = new SelectorConstructable()
	{
		@Override
		public ArgWrapper<?> construct(ParserData parserData) throws SyntaxErrorException
		{
			return ParsingUtilities.getRequiredParam(0, parserData);
		}
	};
}
