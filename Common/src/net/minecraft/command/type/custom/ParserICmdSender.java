package net.minecraft.command.type.custom;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CTypeParse;
import net.minecraft.command.type.management.Converter;
import net.minecraft.command.type.management.SConverter;
import net.minecraft.entity.Entity;

public class ParserICmdSender extends CTypeParse<ICommandSender>
{
	@Override
	public ArgWrapper<ICommandSender> parse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
	{
		final ArgWrapper<ICommandSender> ret = ParsingUtilities.parseString(parser, context, TypeIDs.ICmdSender, UUIDToICmdSender);
		
		if (ret != null)
			return ret;
		
		throw parser.SEE("Expected UUID around index ");
	}
	
	public static final SConverter<Entity, ICommandSender> EntityToICmdSender = Converter.<Entity, ICommandSender> primitiveConverter();
	private static final Converter<String, ICommandSender, ?> UUIDToICmdSender = ParserEntity.UUIDToEntity.chain(EntityToICmdSender);
}
