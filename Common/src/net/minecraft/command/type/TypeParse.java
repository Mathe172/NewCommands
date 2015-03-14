package net.minecraft.command.type;

import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.type.base.CustomParse;

public abstract class TypeParse<R extends ArgWrapper<?>> extends CustomParse<R> implements IDataType<R>
{
}
