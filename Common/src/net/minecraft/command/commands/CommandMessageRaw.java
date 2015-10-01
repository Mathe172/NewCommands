package net.minecraft.command.commands;

import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandUtilities;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.construction.CommandConstructable;
import net.minecraft.command.construction.CommandDescriptorDefault.CParserData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentProcessor;
import net.minecraft.util.IChatComponent;

public class CommandMessageRaw extends CommandArg<Integer>
{
	public static final CommandConstructable constructable = new CommandConstructable()
	{
		@Override
		public CommandArg<Integer> construct(final CParserData data) throws SyntaxErrorException
		{
			return new CommandMessageRaw(data.get(TypeIDs.EntityList), data.get(TypeIDs.IChatComponent));
		}
	};
	
	private final CommandArg<List<Entity>> targets;
	private final CommandArg<IChatComponent> message;
	
	public CommandMessageRaw(final CommandArg<List<Entity>> targets, final CommandArg<IChatComponent> message)
	{
		this.targets = targets;
		this.message = message;
	}
	
	@Override
	public Integer eval(final ICommandSender sender) throws CommandException
	{
		final List<Entity> targets = this.targets.eval(sender);
		final IChatComponent message = this.message.eval(sender);
		
		int success = 0;
		
		for (final Entity target : targets)
			if (target instanceof EntityPlayerMP)
			{
				target.addChatMessage(ChatComponentProcessor.func_179985_a(sender, message, target));
				++success;
			}
			else
				CommandUtilities.errorMessage(sender, target.getName() + " is not a player");
		
		return success;
	}
}
