package net.minecraft.command.type.custom.nbt;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.descriptors.SelectorDescriptor;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.custom.ParserUntypedSelector;
import net.minecraft.nbt.NBTBase;

public class ParserNBTSelector
{
	
	public static CommandArg<NBTBase> parse(final Parser parser) throws SyntaxErrorException, CompletionException
	{
		final SelectorDescriptor descriptor = ParserUntypedSelector.parseName(parser);
		
		final ArgWrapper<?> arg = descriptor.getContentParser().parse(parser);
		
		return NBTUtilities.procIdentifier(parser, arg);
	}
}
