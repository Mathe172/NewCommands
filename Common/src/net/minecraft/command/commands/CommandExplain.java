package net.minecraft.command.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.construction.CommandConstructable;
import net.minecraft.command.descriptors.CommandDescriptor.CParserData;
import net.minecraft.command.parser.Parser;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.event.HoverEvent;
import net.minecraft.event.HoverEvent.Action;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public class CommandExplain extends CommandArg<Integer>
{
	private static final CommandExplain command = new CommandExplain();
	private static final CommandExplain commandAll = new All();
	
	public static final CommandConstructable constructable = new CommandConstructable()
	{
		@Override
		public CommandArg<Integer> construct(final CParserData data) throws SyntaxErrorException
		{
			return data.params.isEmpty()
				? command
				: new Pos(data.get(TypeIDs.BlockPos));
		}
	};
	
	public static final CommandConstructable constructableAll = new CommandConstructable()
	{
		@Override
		public CommandArg<Integer> construct(final CParserData data) throws SyntaxErrorException
		{
			return data.params.isEmpty()
				? commandAll
				: new Pos.All(data.get(TypeIDs.BlockPos));
		}
	};
	
	private CommandExplain()
	{
	}
	
	private static class ExplainAllParser extends Parser
	{
		private final ICommandSender sender;
		
		public ExplainAllParser(final ICommandSender sender, final String toParse, final int startIndex)
		{
			super(toParse, startIndex, false, false, true);
			this.sender = sender;
		}
		
		@Override
		public SyntaxErrorException SEE(final String s, final String postfix, final Throwable cause, final Object... errorObjects)
		{
			sendFancyMessage(new ChatComponentText("- " + s + "around index " + this.index + postfix));
			
			return SyntaxErrorException.see;
		}
		
		@Override
		public SyntaxErrorException SEE(final String s, final boolean appendIndex, final Throwable cause, final Object... errorObjects)
		{
			sendFancyMessage(appendIndex
				? new ChatComponentText("- " + s + ("around index " + this.index))
				: new ChatComponentText("- ").appendSibling(new ChatComponentTranslation(s, errorObjects)));
			
			return SyntaxErrorException.see;
		}
		
		@Override
		public WrongUsageException WUE(final String s, final Object... errorObjects)
		{
			sendFancyMessage(new ChatComponentTranslation("- " + s, errorObjects));
			
			return WrongUsageException.wue;
		}
		
		private void sendFancyMessage(final IChatComponent message)
		{
			message.getChatStyle().setColor(EnumChatFormatting.GRAY).setChatHoverEvent(new HoverEvent(Action.SHOW_TEXT, location(this)));
			this.sender.addChatMessage(message);
		}
	}
	
	@Override
	public Integer eval(final ICommandSender sender) throws CommandException
	{
		if (!(sender.getCommandSenderEntity() instanceof EntityPlayerMP))
			throw new CommandException("Only players can use '/explain' without arguments");
		
		final EntityPlayerMP player = (EntityPlayerMP) sender.getCommandSenderEntity();
		
		final String command = player.playerNetServerHandler.lastCommand;
		
		if (command == null)
			throw new CommandException("No previously executed command found");
		
		return parseCommand(sender, command, 1);
	}
	
	private static void printException(final ICommandSender sender, final SyntaxErrorException exception, final Parser parser)
	{
		sender.addChatMessage(new ChatComponentText("Parsing failed at index " + parser.getIndex() + ": ").appendSibling(location(parser)));
		iPrintException(sender, exception, "- ");
	}
	
	private static void iPrintException(final ICommandSender sender, final Throwable throwable, final String indent)
	{
		if (!(throwable instanceof CommandException))
		{
			sender.addChatMessage(new ChatComponentText(indent + "Fatal error: " + throwable.getMessage()));
			return;
		}
		
		final CommandException exception = (CommandException) throwable;
		
		final IChatComponent message = new ChatComponentText(indent)
			.appendSibling(new ChatComponentTranslation(exception.getMessage(), exception.getErrorOjbects()));
		
		message.getChatStyle().setColor(EnumChatFormatting.GRAY);
		
		sender.addChatMessage(message);
		
		final String newIndent = "  " + indent;
		
		for (final Throwable ex : exception.getSuppressed())
			iPrintException(sender, ex, newIndent);
	}
	
	private static IChatComponent location(final Parser parser)
	{
		final int start = parser.getIndex() > 19 ? parser.getIndex() - 20 : 0;
		
		final int end = parser.getIndex() < parser.toParse.length() - 20 ? parser.getIndex() + 19 : parser.toParse.length();
		
		final IChatComponent prefix = new ChatComponentText((start > 0 ? "…" : "") + parser.toParse.substring(start, parser.getIndex()));
		
		final IChatComponent cursor = new ChatComponentText("|");
		
		final IChatComponent postfix = new ChatComponentText(
			parser.toParse.substring(parser.getIndex(), end)
				+ (end < parser.toParse.length() ? "…" : ""));
		
		prefix.getChatStyle().setColor(EnumChatFormatting.GRAY);
		cursor.getChatStyle().setColor(EnumChatFormatting.RED);
		postfix.getChatStyle().setColor(EnumChatFormatting.GRAY);
		
		final IChatComponent message = prefix.appendSibling(cursor).appendSibling(postfix);
		return message;
	}
	
	protected Integer parseCommand(final ICommandSender sender, final String command, final int startIndex) throws CommandException
	{
		final Parser parser = new Parser(command, startIndex, false, false, true);
		
		try
		{
			parser.parseCommand();
		} catch (final SyntaxErrorException ex)
		{
			printException(sender, ex, parser);
			return 1;
		}
		
		throw new CommandException("The command was parsed without any error (use '/explain all' for more detailed information)");
	}
	
	protected Integer parseCommandAll(final ICommandSender sender, final String command, final int startIndex) throws CommandException
	{
		final Parser parser = new ExplainAllParser(sender, command, startIndex);
		
		try
		{
			parser.parseCommand();
		} catch (final SyntaxErrorException ex)
		{
			sender.addChatMessage(new ChatComponentText("Parsing failed at index " + parser.getIndex() + ": ").appendSibling(location(parser)));
			return 1;
		}
		
		throw new CommandException("The command was parsed without any error");
	}
	
	private static class All extends CommandExplain
	{
		@Override
		protected Integer parseCommand(final ICommandSender sender, final String command, final int startIndex) throws CommandException
		{
			return parseCommandAll(sender, command, startIndex);
		}
	}
	
	private static class Pos extends CommandExplain
	{
		private final CommandArg<BlockPos> pos;
		
		public Pos(final CommandArg<BlockPos> pos)
		{
			this.pos = pos;
		}
		
		@Override
		public Integer eval(final ICommandSender sender) throws CommandException
		{
			final BlockPos pos = this.pos.eval(sender);
			
			final TileEntity te = sender.getEntityWorld().getTileEntity(pos);
			
			if (te == null || !(te instanceof TileEntityCommandBlock))
				throw new CommandException("There is no command-block at '" + pos.getX() + " " + pos.getY() + " " + pos.getZ() + "'.");
			
			return parseCommand(sender, ((TileEntityCommandBlock) te).getCommandBlockLogic().getCustomName(), 0);
		}
		
		private static class All extends Pos
		{
			public All(final CommandArg<BlockPos> pos)
			{
				super(pos);
			}
			
			@Override
			protected Integer parseCommand(final ICommandSender sender, final String command, final int startIndex) throws CommandException
			{
				return parseCommandAll(sender, command, startIndex);
			}
		}
	}
}
