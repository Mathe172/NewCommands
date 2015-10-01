package net.minecraft.command.type.base;

import net.minecraft.command.type.IExComplete;
import net.minecraft.command.type.metadata.ICompletable;

public abstract class ExCustomCompletable<R, D> extends ExCustomParse<R, D> implements IExComplete<D>
{
	public ExCustomCompletable()
	{
		this.addEntry(new ICompletable.Capturing<>(this));
	}
}