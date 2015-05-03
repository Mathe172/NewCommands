package net.minecraft.command.selectors.entity;

import java.util.List;

import net.minecraft.command.MatcherRegistry;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.collections.Types;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IParse;
import net.minecraft.command.type.custom.TypeNullable;

public class FilterList
{
	public static final MatcherRegistry inverterMatcher = new MatcherRegistry("\\G\\s*+!");
	
	public static final FilterList name = new FilterList(Types.nameList);
	public static final FilterList team = new FilterList(new TypeNullable<ArgWrapper<List<String>>>(Types.teamNameList));
	public static final FilterList type = new FilterList(Types.entityIDWPlayerList);
	
	public static class InvertableArg
	{
		public final boolean inverted;
		public final CommandArg<List<String>> arg;
		
		public InvertableArg(final boolean inverted, final CommandArg<List<String>> arg)
		{
			this.inverted = inverted;
			this.arg = arg;
		}
	}
	
	private final IParse<ArgWrapper<List<String>>> listParser;
	
	public FilterList(final IParse<ArgWrapper<List<String>>> listParser)
	{
		this.listParser = listParser;
	}
	
	public InvertableArg parse(final Parser parser) throws SyntaxErrorException, CompletionException
	{
		return new InvertableArg(parser.findInc(inverterMatcher), this.listParser.parse(parser).get(TypeIDs.StringList));
	}
}
