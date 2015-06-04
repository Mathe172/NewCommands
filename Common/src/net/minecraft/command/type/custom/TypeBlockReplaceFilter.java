package net.minecraft.command.type.custom;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.descriptors.CommandDescriptor.CParserData;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IExParse;
import net.minecraft.command.type.base.ExCustomParse;
import net.minecraft.command.type.custom.nbt.TypeNBTArg;

public final class TypeBlockReplaceFilter extends ExCustomParse<Void, CParserData>// TODO:....
{
	public static final IExParse<Void, CParserData> parser = new TypeBlockReplaceFilter();
	
	private TypeBlockReplaceFilter()
	{
	}
	
	@Override
	public Void parse(final Parser parser, final CParserData parserData) throws SyntaxErrorException, CompletionException
	{
		ArgWrapper<?> nbt;
		
		try
		{
			nbt = TypeNBTArg.parserBlock.parseSnapshot(parser);
			
			parserData.add((ArgWrapper<?>) null);
			parserData.add((ArgWrapper<?>) null);
			
			parserData.add(nbt);
		} catch (final SyntaxErrorException e1)
		{
			parserData.add(ParserBlockID.parser.parse(parser));
			
			try
			{
				nbt = TypeNBTArg.parserBlock.parseSnapshot(parser);
				
				parserData.add((ArgWrapper<?>) null);
				
				parserData.add(nbt);
			} catch (final SyntaxErrorException e2)
			{
				parserData.add(ParserInt.parser.parse(parser));
				
				try
				{
					parserData.add(ParserInt.parser.parseSnapshot(parser));
				} catch (final SyntaxErrorException e3)
				{
					parserData.add((ArgWrapper<?>) null);
				}
			}
		}
		
		return null;
	}
}
