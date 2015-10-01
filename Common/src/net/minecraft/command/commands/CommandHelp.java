package net.minecraft.command.commands;

import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandNotFoundException;
import net.minecraft.command.CommandUtilities;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.construction.CommandConstructable;
import net.minecraft.command.construction.CommandDescriptorDefault.CParserData;
import net.minecraft.command.descriptors.CommandDescriptor;
import net.minecraft.command.descriptors.ICommandDescriptor;
import net.minecraft.command.descriptors.ICommandDescriptor.UsageProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public class CommandHelp extends CommandArg<Integer>
{
	public static final CommandConstructable constructable = new CommandConstructable()
	{
		@Override
		public CommandArg<Integer> construct(final CParserData data) throws SyntaxErrorException
		{
			return new CommandHelp(data.get(TypeIDs.StringList));
		}
	};
	
	private final CommandArg<List<String>> path;
	
	public CommandHelp(final CommandArg<List<String>> path)
	{
		this.path = path;
	}
	
	@Override
	public Integer eval(final ICommandSender sender) throws CommandException
	{
		if (this.path == null)
		{
			printPage(sender, 1);
			return 1;
		}
		
		final List<String> path = this.path.eval(sender);
		
		if (path.size() == 1)
			try
			{
				printPage(sender, Integer.parseInt(path.get(0)));
				return 1;
			} catch (final NumberFormatException ex)
			{
			}
		
		final UsageProvider usage = getUsageFromPath(path);
		
		if (usage == null)
			throw new CommandNotFoundException();
		
		sender.addChatMessage(usage.createMessage(path.subList(1, path.size())));
		return 1;
	}
	
	private static UsageProvider getUsageFromPath(final List<String> path)
	{
		final ListIterator<String> it = path.listIterator();
		
		ICommandDescriptor<?> currDesc = CommandDescriptor.getDescriptor(it.next());
		
		if (currDesc == null)
			return null;
		
		UsageProvider ret = currDesc.usage;
		
		while (it.hasNext())
		{
			final String next = it.next();
			ICommandDescriptor<?> newDesc = currDesc.getSubDescriptor(next);
			
			if (newDesc == null)
			{
				it.previous();
				newDesc = currDesc.getSubDescriptor("");
				
				if (newDesc == null)
					return null;
			}
			
			if (newDesc.usage != null)
				ret = newDesc.usage;
			
			currDesc = newDesc;
		}
		
		return ret;
	}
	
	private static void printPage(final ICommandSender sender, final int pageNum) throws NumberInvalidException
	{
		final List<Entry<String, CommandDescriptor<?>>> commands = CommandDescriptor.getCommands();
		
		final int pageCount = (commands.size() - 1) / 7 + 1;
		
		CommandUtilities.checkInt(pageNum, 1, pageCount);
		
		CommandUtilities.message(sender, EnumChatFormatting.DARK_GREEN, "commands.help.header", pageNum, pageCount);
		
		final int end = Math.min(pageNum * 7, commands.size());
		for (final Entry<String, CommandDescriptor<?>> command : commands.subList((pageNum - 1) * 7, end))
		{
			final IChatComponent message = command.getValue().usage.createMessage(Collections.<String> emptyList());
			message.getChatStyle().setChatClickEvent(new ClickEvent(Action.SUGGEST_COMMAND, "/" + command.getKey() + " "));
			sender.addChatMessage(message);
		}
		
		if (pageNum == 1 && sender instanceof EntityPlayer)
			CommandUtilities.message(sender, EnumChatFormatting.GREEN, "commands.help.footer");
	}
}
