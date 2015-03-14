package net.minecraft.command.type.custom;

import java.util.regex.Matcher;

import net.minecraft.command.CommandException;
import net.minecraft.command.EntityNotFoundException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.CTypeParse;
import net.minecraft.entity.Entity;

public class ParserEntity extends CTypeParse<Entity>
{
	public static final CDataType<Entity> parser = new ParserEntity();
	
	@Override
	public ArgWrapper<Entity> parse(Parser parser) throws SyntaxErrorException, CompletionException
	{
		final ArgWrapper<Entity> ret = ParsingUtilities.generalParse(parser, TypeIDs.Entity);
		
		if (ret != null)
			return ret;
		
		final Matcher m = parser.stringMatcher;
		
		if (!parser.findInc(m))
			throw parser.SEE("Expected UUID/player name around index ");
		
		final String identifier = m.group(1);
		
		return new ArgWrapper<>(TypeIDs.Entity, new CommandArg<Entity>()
		{
			@Override
			public Entity eval(ICommandSender sender) throws CommandException
			{
				final Entity ret = ParsingUtilities.entiyFromIdentifier(identifier);
				
				if (ret == null)
					throw new EntityNotFoundException("commands.generic.entity.invalidUuid", new Object[0]);
				
				return ret;
			}
		});
	}
}
