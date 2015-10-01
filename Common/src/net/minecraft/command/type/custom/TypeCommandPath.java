package net.minecraft.command.type.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;

import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletionData;
import net.minecraft.command.descriptors.CommandDescriptor;
import net.minecraft.command.descriptors.ICommandDescriptor;
import net.minecraft.command.parser.CompletionParser;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CTypeParse;
import net.minecraft.command.type.base.ExCustomCompletable;
import net.minecraft.command.type.custom.command.ParserCommand;
import net.minecraft.command.type.metadata.MetaEntry;
import net.minecraft.command.type.metadata.MetaEntry.PrimitiveHint;

public class TypeCommandPath extends CTypeParse<List<String>>
{
	public static final TypeCommandPath parser = new TypeCommandPath();
	
	private TypeCommandPath()
	{
	}
	
	@Override
	public ArgWrapper<List<String>> iParse(final Parser parser, final Context parserData) throws SyntaxErrorException
	{
		final List<String> data = new ArrayList<>();
		
		while (ITypeCommandPath.parser.parse(parser, data) != null && !parser.endReached())
		{
		}
		
		return TypeIDs.StringList.wrap(data);
	}
	
	/**
	 * @param path
	 *            Must NOT be empty
	 */
	private static final ICommandDescriptor<?> descFromPath(final List<String> path)
	{
		final ListIterator<String> it = path.listIterator();
		
		ICommandDescriptor<?> currDesc = CommandDescriptor.getDescriptor(it.next());
		
		if (currDesc == null)
			return null;
		
		while (it.hasNext())
		{
			final String next = it.next();
			ICommandDescriptor<?> newDesc = currDesc.getSubDescriptor(next);
			
			if (newDesc == null)
			{
				it.previous();
				newDesc = currDesc.getSubDescriptor("");
				
				if (newDesc == null)
					return null;
			}
			
			currDesc = newDesc;
		}
		
		return currDesc;
	}
	
	private static final MetaEntry<PrimitiveHint, Void> hint = new MetaEntry<PrimitiveHint, Void>(CompletionParser.hintID)
	{
		@Override
		public PrimitiveHint get(final Parser parser, final Void parserData)
		{
			final Matcher wm = parser.getMatcher(ParsingUtilities.whitespaceMatcher);
			
			parser.find(wm);
			
			return wm.group().length() + parser.getIndex() == parser.len ? CompletionParser.propose : null;
		}
	};
	
	private static class ITypeCommandPath extends ExCustomCompletable<String, List<String>>
	{
		private static final ITypeCommandPath parser = new ITypeCommandPath();
		
		@Override
		public String iParse(final Parser parser, final List<String> parserData) throws SyntaxErrorException
		{
			final Matcher m = parser.getMatcher(ParsingUtilities.keyMatcher);
			
			if (!parser.findInc(m))
			{
				parser.supplyHint(hint);
				return null;
			}
			
			final String key = m.group(1);
			
			parserData.add(key);
			
			return key;
		}
		
		@Override
		public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData, final List<String> parserData)
		{
			final List<String> path = startIndex == parser.getIndex() ? parserData : parserData.subList(0, parserData.size() - 1);
			
			if (path.isEmpty())
			{
				ParserCommand.nameCompleter.complete(tcDataSet, parser, startIndex, cData);
				return;
			}
			
			ICommandDescriptor<?> desc = descFromPath(path);
			
			//TODO: Permission?
			while (desc != null)
			{
				TabCompletionData.addToSet(tcDataSet, startIndex, cData, desc.getKeywordCompletions());
				desc = desc.getSubDescriptor("");
			}
		}
	}
}
