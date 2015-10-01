package net.minecraft.command.type.base;

import net.minecraft.command.type.IComplete;
import net.minecraft.command.type.metadata.ICompletable;

public abstract class CustomCompletable<R> extends CustomParse<R> implements IComplete
{
	public CustomCompletable()
	{
		this.addEntry(new ICompletable.Default(this));
	}
}