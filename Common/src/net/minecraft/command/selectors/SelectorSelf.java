package net.minecraft.command.selectors;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.descriptors.SelectorDescriptorNoContent.PrimitiveConstructable;
import net.minecraft.command.descriptors.SelectorDescriptorNoContent.PrimitiveData;

public final class SelectorSelf extends CommandArg<ICommandSender>
{
	private static final SelectorSelf selfSelector = new SelectorSelf();
	
	private SelectorSelf()
	{
	}
	
	public static final PrimitiveConstructable constructable = new PrimitiveConstructable()
	{
		@Override
		public ArgWrapper<ICommandSender> construct(final PrimitiveData parserData)
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
