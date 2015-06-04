package net.minecraft.command.descriptors;

import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.management.TypeID;

public abstract class SParserData
{
	public final Parser parser;
	public String label = null;
	public TypeID<?> labelType = null;
	public char labelModifier = 0;
	
	public SParserData(final Parser parser)
	{
		this.parser = parser;
	}
	
	public abstract ArgWrapper<?> finalize(final ArgWrapper<?> selector);
	
	public abstract boolean requiresKey();
}
