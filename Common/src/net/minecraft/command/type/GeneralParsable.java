package net.minecraft.command.type;

import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;

public interface GeneralParsable<T extends ArgWrapper<?>>
{
	public T parse(Parser parser, Context context, ArgWrapper<?> arg);
}
