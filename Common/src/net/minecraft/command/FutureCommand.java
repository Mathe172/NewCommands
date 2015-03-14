package net.minecraft.command;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.parser.ParsingManager;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;

public class FutureCommand
{
	private String commandStored;
	private Future<CommandArg<Integer>> fCommand;
	private CommandArg<Integer> command;
	
	public FutureCommand()
	{
		this.commandStored = "";
		this.fCommand = null;
		this.command = initCommand;
	}
	
	public FutureCommand(final String command)
	{
		this.commandStored = command;
		this.command = null;
		this.fCommand = ParsingManager.submit(this.commandStored);
	}
	
	public final String get()
	{
		return this.commandStored;
	}
	
	public final void set(final String command)
	{
		this.commandStored = command;
		this.command = null;
		this.fCommand = ParsingManager.submit(this.commandStored);
	}
	
	public CommandArg<Integer> getCommand()
	{
		if (this.command == null)
		{
			try
			{
				try
				{
					this.command = this.fCommand.get();
				} catch (final InterruptedException e)
				{
					this.command = Parser.parseCommand(this.commandStored);
				}
			} catch (final ExecutionException | SyntaxErrorException e)
			{
				final ChatComponentTranslation message = new ChatComponentTranslation((e instanceof ExecutionException ? e.getCause() : e).getMessage());
				message.getChatStyle().setColor(EnumChatFormatting.RED);
				
				this.command = new CommandArg<Integer>()
				{
					@Override
					public Integer eval(final ICommandSender sender) throws CommandException
					{
						sender.addChatMessage(message);
						return 0;
					}
				};
			}
			
			this.fCommand = null;
		}
		
		return this.command;
	}
	
	private static final CommandArg<Integer> initCommand = new CommandArg<Integer>()
	{
		@Override
		public Integer eval(final ICommandSender sender) throws CommandException
		{
			return 0;
		}
	};
}
