package net.minecraft.command.construction;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.IPermission;
import net.minecraft.command.arg.ArgWrapper;

public interface CommandConstructable
{
	public CommandBase construct(List<ArgWrapper<?>> params, IPermission permission);
	
	public static CommandConstructable emptyConstructable = new CommandConstructable()
	{
		@Override
		public CommandBase construct(final List<ArgWrapper<?>> params, final IPermission permission)
		{
			return null;
		}
	};
}
