package net.minecraft.command.selectors;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.construction.SelectorConstructable;
import net.minecraft.command.type.custom.TypeSelectorContent.ParserData;

public final class SelectorSelf extends CommandArg<ICommandSender>
{
	private static final SelectorSelf selfSelector = new SelectorSelf();
	
	private SelectorSelf()
	{
	}
	
	public static final SelectorConstructable constructable = new SelectorConstructable()
	{
		@Override
		public ArgWrapper<ICommandSender> construct(final ParserData parserData)
		{
			return TypeIDs.ICmdSender.wrap(selfSelector);
		}
		
	};
	
	@Override
	public ICommandSender eval(final ICommandSender sender)
	{
		return sender;
	}
}
