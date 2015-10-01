package net.minecraft.command.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandNotFoundException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.construction.CommandConstructable;
import net.minecraft.command.construction.CommandDescriptorDefault.CParserData;
import net.minecraft.command.descriptors.CommandDescriptor;
import net.minecraft.command.legacy.LegacyCommand;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

@SuppressWarnings("deprecation")
public class CommandLegacy extends CommandArg<Integer>
{
	public static final CommandConstructable constructable = new CommandConstructable()
	{
		@Override
		public CommandArg<Integer> construct(final CParserData data) throws SyntaxErrorException
		{
			return new CommandLegacy(data.get(TypeIDs.String));
		}
	};
	
	private final CommandArg<String> command;
	
	private CommandLegacy(final CommandArg<String> command)
	{
		this.command = command;
	}
	
	@Override
	public Integer eval(final ICommandSender sender) throws CommandException
	{
		final String command = this.command.eval(sender);
		final CommandDescriptor<?> descriptor = CommandDescriptor.getDescriptor(command);
		
		if (descriptor == null)
			throw new CommandNotFoundException();
		
		if (!descriptor.permission.canCommandSenderUseCommand(sender))
			throw new CommandException("commands.generic.permission");
		
		if (descriptor instanceof LegacyCommand)
		{
			final IChatComponent msg = new ChatComponentText("The command '" + command + "' is a legacy command, new features may not work");
			msg.getChatStyle().setColor(EnumChatFormatting.RED);
			sender.addChatMessage(msg);
		}
		else
		{
			final IChatComponent msg = new ChatComponentText("The command '" + command + "' is not a legacy command, everything is ok");
			msg.getChatStyle().setColor(EnumChatFormatting.GREEN);
			sender.addChatMessage(msg);
		}
		
		return 1;
	}
}
