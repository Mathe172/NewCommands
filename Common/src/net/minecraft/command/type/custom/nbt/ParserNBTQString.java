package net.minecraft.command.type.custom.nbt;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.arg.PrimitiveParameter;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.custom.nbt.NBTUtilities.NBTData;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagString;

public class ParserNBTQString
{
	public static void parse(final Parser parser, final NBTData parserData) throws SyntaxErrorException, CompletionException
	{
		final CommandArg<String> parsed = ParsingUtilities.parseQuotedString(parser);
		
		if (parsed instanceof PrimitiveParameter<?>)
			parserData.put(new NBTTagString(((PrimitiveParameter<String>) parsed).value));
		else
			parserData.put(new CommandArg<NBTBase>()
			{
				@Override
				public NBTTagString eval(final ICommandSender sender) throws CommandException
				{
					return new NBTTagString(parsed.eval(sender));
				}
			});
	}
}
