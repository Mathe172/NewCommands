package net.minecraft.command.type.custom.nbt;

import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.custom.nbt.NBTUtilities.NBTData;

public class ParserNBTQString
{
	public static void parse(final Parser parser, final NBTData parserData) throws SyntaxErrorException, CompletionException
	{
		final CommandArg<String> parsed = ParsingUtilities.parseQuotedString(parser, parserData);
		
		if (parsed != null)
			parserData.add(NBTUtilities.procIdentifier(parser, parsed.wrap(TypeIDs.String)));
	}
}
