package net.minecraft.command.selectors.entity;

import java.util.List;
import java.util.regex.Pattern;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IParse;
import net.minecraft.command.type.custom.TypeIDs;
import net.minecraft.command.type.custom.TypeNullable;
import net.minecraft.command.type.custom.Types;

public class FilterList
{
	public static final Pattern inverterPattern = Pattern.compile("\\G\\s*+!");
	
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
		return new InvertableArg(parser.findInc(parser.inverterMatcher), this.listParser.parse(parser).get(TypeIDs.StringList));
	}
}
