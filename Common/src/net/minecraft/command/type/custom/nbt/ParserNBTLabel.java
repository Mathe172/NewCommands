package net.minecraft.command.type.custom.nbt;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.custom.ParserUntypedLabel;
import net.minecraft.nbt.NBTBase;

public class ParserNBTLabel
{
	
	public static CommandArg<NBTBase> parse(final Parser parser) throws SyntaxErrorException, CompletionException
	{
		final ArgWrapper<?> label = ParserUntypedLabel.parseLabel(parser);
		
		return NBTUtilities.procIdentifier(parser, label);
	}
}
