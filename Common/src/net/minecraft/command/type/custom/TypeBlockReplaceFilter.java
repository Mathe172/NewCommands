package net.minecraft.command.type.custom;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.collections.Parsers;
import net.minecraft.command.construction.CommandDescriptorDefault.CParserData;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IExParse;
import net.minecraft.command.type.base.ExCustomParse;
import net.minecraft.command.type.custom.nbt.TypeNBTArg;

public final class TypeBlockReplaceFilter extends ExCustomParse<Void, CParserData>
{
	public static final IExParse<Void, CParserData> parser = new TypeBlockReplaceFilter();
	
	private TypeBlockReplaceFilter()
	{
	}
	
	@Override
	public Void iParse(final Parser parser, final CParserData parserData) throws SyntaxErrorException
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
			parserData.add(Parsers.blockID.parse(parser));
			
			try
			{
				nbt = TypeNBTArg.parserBlock.parseSnapshot(parser);
				
				parserData.add((ArgWrapper<?>) null);
				
				parserData.add(nbt);
			} catch (final SyntaxErrorException e2)
			{
				parserData.add(Parsers.integer.parse(parser));
				
				try
				{
					parserData.add(Parsers.integer.parseSnapshot(parser));
				} catch (final SyntaxErrorException e3)
				{
					parserData.add((ArgWrapper<?>) null);
				}
			}
		}
		
		return null;
	}
}
