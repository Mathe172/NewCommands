package net.minecraft.command.type.custom.nbt;

import java.util.Set;
import java.util.regex.Matcher;

import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletionData;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.MatcherRegistry;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.base.ExCustomCompletable;
import net.minecraft.command.type.custom.nbt.NBTDescriptor.Compound;
import net.minecraft.command.type.custom.nbt.ParserNBTCompound.CompoundData;

public class TypeNBTPair extends ExCustomCompletable<Void, CompoundData>
{
	private final Compound descriptor;
	public static final MatcherRegistry keyMatcher = new MatcherRegistry("\\G\\s*+([\\w-]*+)\\s*+:");
	
	public TypeNBTPair(final Compound descriptor)
	{
		this.descriptor = descriptor;
	}
	
	@Override
	public Void iParse(final Parser parser, final CompoundData parserData) throws SyntaxErrorException
	{
		final Matcher m = parser.getMatcher(keyMatcher);
		
		if (!parser.findInc(m))
			throw parser.SEE("Missing tag name ");
		
		ParsingUtilities.terminateCompletion(parser);
		
		final String name = m.group(1);
		
		parserData.name = name;
		
		this.descriptor.getSubDescriptor(name).getTagParser().parse(parser, parserData);
		
		return null;
	}
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData, final CompoundData parserData)
	{
		final Set<String> keySet = parserData.keySet();
		
		for (final ITabCompletion tc : this.descriptor.getKeyCompletions())
			if (!keySet.contains(tc.name))
				TabCompletionData.addToSet(tcDataSet, startIndex, cData, tc);
	}
}
