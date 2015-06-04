package net.minecraft.command.type;

import net.minecraft.command.arg.ArgWrapper;

public interface ITypeGeneral<T extends ArgWrapper<?>> extends IDataType<T>, GeneralParsable<T> // TODO: RENAME....
{
}
