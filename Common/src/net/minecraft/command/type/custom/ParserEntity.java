package net.minecraft.command.type.custom;

import net.minecraft.command.EntityNotFoundException;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CTypeParse;
import net.minecraft.command.type.management.CConverter;
import net.minecraft.entity.Entity;

public class ParserEntity extends CTypeParse<Entity>
{
	@Override
	public ArgWrapper<Entity> parse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
	{
		final ArgWrapper<Entity> ret = ParsingUtilities.parseString(parser, context, TypeIDs.Entity, UUIDToEntity);
		
		if (ret != null)
			return ret;
		
		throw parser.SEE("Expected UUID/player name around index ");
	}
	
	public static final CConverter<String, Entity> UUIDToEntity = new CConverter<String, Entity>()
	{
		@Override
		public Entity convert(final String toConvert) throws EntityNotFoundException
		{
			final Entity ret = ParsingUtilities.entiyFromIdentifier(toConvert);
			
			if (ret == null)
				throw new EntityNotFoundException("commands.generic.entity.invalidUuid");
			
			return ret;
		}
	};
}
