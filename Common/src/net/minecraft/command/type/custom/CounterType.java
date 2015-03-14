package net.minecraft.command.type.custom;

import java.util.regex.Matcher;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.arg.CounterArg;
import net.minecraft.command.arg.Processable;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.CTypeParse;

public class CounterType extends CTypeParse<Integer>
{
	public static final CDataType<Integer> counterType = new CounterType();
	
	@Override
	public ArgWrapper<Integer> parse(final Parser parser) throws SyntaxErrorException, CompletionException
	{
		final Matcher m = parser.keyMatcher;
		
		if (!parser.findInc(m))
			throw parser.SEE("Unable to find counter name around index ");
		
		final String name = m.group(1);
		
		final CommandArg<Integer> start = ParserInt.intParser.parse(parser).arg;
		final CommandArg<Integer> end = ParserInt.intParser.parse(parser).arg;
		
		final ArgWrapper<Integer> ret = new ArgWrapper<>(TypeIDs.Integer, new CounterArg(start, end));
		
		parser.addLabel(name, ret);
		
		parser.addToProcess((Processable) ret.arg);
		parser.addIgnoreErrors(false);
		
		return ret;
	}
	
}
