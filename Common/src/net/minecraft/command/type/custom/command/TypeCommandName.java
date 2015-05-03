package net.minecraft.command.type.custom.command;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.PermissionWrapper;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.descriptors.CommandDescriptor;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.CTypeCompletable;
import net.minecraft.command.type.custom.ParserName;

public final class TypeCommandName extends CTypeCompletable<String>
{
	public static final CDataType<String> parser = new TypeCommandName();
	
	private TypeCommandName()
	{
	}
	
	@Override
	public ArgWrapper<String> iParse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
	{
		return ParserName.parser.parse(parser, context);
	}
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
	{
		PermissionWrapper.complete(tcDataSet, startIndex, cData, CommandDescriptor.commandCompletions);
	}
}
