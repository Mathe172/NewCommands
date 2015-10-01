package net.minecraft.command.selectors.entity;

import java.util.List;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.TypedWrapper.Getter;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.collections.Types;
import net.minecraft.command.descriptors.SelectorDescriptorDefault.DefaultParserData;
import net.minecraft.command.parser.MatcherRegistry;
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
		public final Getter<List<String>> arg;
		
		public InvertableArg(final boolean inverted, final Getter<List<String>> getter)
		{
			this.inverted = inverted;
			this.arg = getter;
		}
	}
	
	private final IParse<ArgWrapper<List<String>>> listParser;
	
	public FilterList(final IParse<ArgWrapper<List<String>>> listParser)
	{
		this.listParser = listParser;
	}
	
	public InvertableArg parse(final Parser parser, final DefaultParserData data) throws SyntaxErrorException
	{
		return new InvertableArg(parser.findInc(inverterMatcher), this.listParser.parse(parser).addToProcess(data.toProcess).get(TypeIDs.StringList));
	}
}
