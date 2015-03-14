package net.minecraft.command.type;

import net.minecraft.command.arg.ArgWrapper;

public abstract class CTypeCompletable<R> extends TypeCompletable<ArgWrapper<R>> implements CDataType<R>
{
}