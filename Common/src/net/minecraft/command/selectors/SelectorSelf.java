package net.minecraft.command.selectors;

import java.util.List;
import java.util.Map;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.construction.SelectorConstructable;
import net.minecraft.command.type.custom.TypeIDs;

public class SelectorSelf extends CommandArg<ICommandSender>
{
	private static final SelectorSelf selfSelector = new SelectorSelf();
	
	public static final SelectorConstructable constructable = new SelectorConstructable()
	{
		@Override
		public ArgWrapper<ICommandSender> construct(final List<ArgWrapper<?>> unnamedParams, final Map<String, ArgWrapper<?>> namedParams)
		{
			return new ArgWrapper<>(TypeIDs.ICmdSender, selfSelector);
		}
		
	};
	
	@Override
	public ICommandSender eval(final ICommandSender sender)
	{
		return sender;
	}
}
