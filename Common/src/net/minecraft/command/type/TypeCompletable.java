package net.minecraft.command.type;

import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.type.base.CustomCompletable;

public abstract class TypeCompletable<R extends ArgWrapper<?>> extends CustomCompletable<R> implements IDataType<R>
{
}