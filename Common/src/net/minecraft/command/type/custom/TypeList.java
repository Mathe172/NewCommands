package net.minecraft.command.type.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletion;
import net.minecraft.command.completion.TabCompletionData;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.MatcherRegistry;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CTypeCompletable;
import net.minecraft.command.type.IDataType;
import net.minecraft.command.type.management.TypeID;

public class TypeList<T> extends CTypeCompletable<List<T>>
{
	public static final MatcherRegistry listDelimMatcher = new MatcherRegistry("\\G\\s*+([,)])");
	
	public final TypeID<List<T>> type;
	private final IDataType<ArgWrapper<T>> dataType;
	
	public TypeList(final TypeID<List<T>> type, final IDataType<ArgWrapper<T>> dataType)
	{
		this.type = type;
		this.dataType = dataType;
	}
	
	@Override
	public ArgWrapper<List<T>> iParse(final Parser parser, final Context context) throws SyntaxErrorException
	{
		if (!parser.findInc(parser.getMatcher(ParsingUtilities.oParenthMatcher)))
		{
			final CommandArg<T> item = this.dataType.parse(parser).arg();
			return this.type.wrap(new CommandArg<List<T>>()
			{
				@Override
				public List<T> eval(final ICommandSender sender) throws CommandException
				{
					return Arrays.asList(item.eval(sender));
				}
				
			});
		}
		
		final List<CommandArg<T>> items = new ArrayList<>();
		
		final Matcher m = parser.getMatcher(listDelimMatcher);
		
		while (true)
		{
			items.add(this.dataType.parse(parser).arg());
			
			if (!parser.findInc(m))
				throw parser.SEE("Expected ',' or ')' ");
			
			if (")".equals(m.group(1)))
				return this.type.wrap(new CommandArg<List<T>>()
				{
					@Override
					public List<T> eval(final ICommandSender sender) throws CommandException
					{
						final List<T> ret = new ArrayList<>(items.size());
						for (final CommandArg<T> item : items)
							ret.add(item.eval(sender));
						
						return ret;
					}
				});
		}
	}
	
	public static final ITabCompletion parenthCompletion = new TabCompletion(Pattern.compile("\\A(\\s*+)\\(?+\\z"), "()", "()")
	{
		@Override
		public boolean complexFit()
		{
			return false;
		}
		
		@Override
		public int getCursorOffset(final Matcher m, final CompletionData cData)
		{
			return -1;
		};
		
		@Override
		public double weightOffset(final Matcher m, final CompletionData cData)
		{
			return -1.0;
		}
		
		@Override
		public boolean fullMatch(final Matcher m, final CompletionData cData, final String replacement)
		{
			return false;
		}
	};
	
	@Override
	public final void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
	{
		TabCompletionData.addToSet(tcDataSet, startIndex, cData, parenthCompletion);
	}
	
	public static class GParsed<T> extends TypeList<T>
	{
		
		public GParsed(final TypeID<List<T>> type, final IDataType<ArgWrapper<T>> dataType)
		{
			super(type, dataType);
		}
		
		@Override
		public final ArgWrapper<List<T>> iParse(final Parser parser, final Context context) throws SyntaxErrorException
		{
			final ArgWrapper<List<T>> ret = context.generalParse(parser, this.type);
			
			if (ret != null)
				return ret;
			
			return super.iParse(parser, context);
		}
	}
}
