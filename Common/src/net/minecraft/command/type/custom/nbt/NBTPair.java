package net.minecraft.command.type.custom.nbt;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.completion.TabCompletionData;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.base.ExCustomCompletable;
import net.minecraft.command.type.custom.nbt.NBTDescriptor.Compound;
import net.minecraft.command.type.custom.nbt.ParserNBTCompound.CompoundData;

public class NBTPair extends ExCustomCompletable<Void, CompoundData>
{
	private final Compound descriptor;
	
	public NBTPair(final Compound descriptor)
	{
		this.descriptor = descriptor;
	}
	
	public static final Pattern nbtKeyPattern = Pattern.compile("\\G\\s*+([\\w-]*+)\\s*+:");
	
	@Override
	public Void iParse(final Parser parser, final CompoundData parserData) throws SyntaxErrorException, CompletionException
	{
		final Matcher m = parser.nbtKeyMatcher;
		
		if (!parser.findInc(m))
			throw parser.SEE("Missing tag name around index ");
		
		parser.terminateCompletion();
		
		final String name = m.group(1);
		
		parserData.name = name;
		
		this.descriptor.getSubDescriptor(name).getTagParser().parse(parser, parserData);
		
		return null;
	}
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData, final CompoundData parserData)
	{
		for (final ITabCompletion tc : this.descriptor.getKeyCompletions())
			if (!parserData.containsKey(tc.name))
				TabCompletionData.addToSet(tcDataSet, startIndex, cData, tc);
	}
	
}
