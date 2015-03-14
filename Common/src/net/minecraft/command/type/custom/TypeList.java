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
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletion;
import net.minecraft.command.completion.TabCompletionData;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CTypeCompletable;
import net.minecraft.command.type.IDataType;
import net.minecraft.command.type.TypeID;

public class TypeList<T> extends CTypeCompletable<List<T>>
{
	public static final Pattern listDelimPattern = Pattern.compile("\\G\\s*+([,)])");
	
	public final TypeID<List<T>> type;
	private final IDataType<ArgWrapper<T>> dataType;
	
	public TypeList(final TypeID<List<T>> type, final IDataType<ArgWrapper<T>> dataType)
	{
		this.type = type;
		this.dataType = dataType;
	}
	
	@Override
	public ArgWrapper<List<T>> iParse(final Parser parser) throws SyntaxErrorException, CompletionException
	{
		if (!parser.findInc(parser.oParenthMatcher))
		{
			final CommandArg<T> item = this.dataType.parse(parser).arg;
			return new ArgWrapper<List<T>>(this.type, new CommandArg<List<T>>()
			{
				@Override
				public List<T> eval(final ICommandSender sender) throws CommandException
				{
					return Arrays.asList(item.eval(sender));
				}
				
			});
		}
		
		final List<CommandArg<T>> items = new ArrayList<>();
		
		final Matcher m = parser.listDelimMatcher;
		
		while (true)
		{
			items.add(this.dataType.parse(parser).arg);
			
			if (!parser.findInc(m))
				throw parser.SEE("Expected ',' or ')' around index ");
			
			if (")".equals(m.group(1)))
				return new ArgWrapper<>(this.type, new CommandArg<List<T>>()
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
	
	public static final TabCompletion parenthCompletion = new TabCompletion(Pattern.compile("\\A(\\s*+)(\\(\\)?+)?+\\z"), "()", "()")
	{
		@Override
		public final int getCursorOffset(final Matcher m, final CompletionData cData)
		{
			return -1;
		};
	};
	
	@Override
	public final void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
	{
		TabCompletionData.addToSet(tcDataSet, parser.toParse, startIndex, cData, parenthCompletion);
	}
	
	public static class GParsed<T> extends TypeList<T>
	{
		
		public GParsed(TypeID<List<T>> type, IDataType<ArgWrapper<T>> dataType)
		{
			super(type, dataType);
		}
		
		@Override
		public final ArgWrapper<List<T>> iParse(Parser parser) throws SyntaxErrorException, CompletionException
		{
			final ArgWrapper<List<T>> ret = ParsingUtilities.generalParse(parser, this.type);
			
			if (ret != null)
				return ret;
			
			return super.iParse(parser);
		}
	}
}
