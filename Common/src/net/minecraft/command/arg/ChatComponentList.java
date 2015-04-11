package net.minecraft.command.arg;

import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

public class ChatComponentList extends CommandArg<IChatComponent>
{
	List<CommandArg<IChatComponent>> parts;
	
	public ChatComponentList(final List<CommandArg<IChatComponent>> parts)
	{
		this.parts = parts;
	}
	
	@Override
	public IChatComponent eval(final ICommandSender sender) throws CommandException
	{
		// TODO: Clone?
		final IChatComponent ret = new ChatComponentText("");
		
		for (final CommandArg<IChatComponent> part : this.parts)
			ret.appendSibling(part.eval(sender));
		
		return ret;
	}
	
}
