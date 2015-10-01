package net.minecraft.command.parser;

import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.type.management.TypeID;

public interface Context
{
	public <R> ArgWrapper<R> generalParse(final Parser parser, final TypeID<R> target) throws SyntaxErrorException;
	
	public static final Context defContext = new Context()
	{
		@Override
		public final <R> ArgWrapper<R> generalParse(final Parser parser, final TypeID<R> target) throws SyntaxErrorException
		{
			return ParsingUtilities.generalParse(parser, target, parser.getMatcher(ParsingUtilities.generalMatcher));
		}
	};
}
