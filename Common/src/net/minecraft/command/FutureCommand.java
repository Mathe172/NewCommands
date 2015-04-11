package net.minecraft.command;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.parser.ParsingManager;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;

public class FutureCommand extends IFutureCommand
{
	Future<CommandArg<Integer>> fCommand;
	CommandArg<Integer> command;
	
	public FutureCommand()
	{
		super("");
		this.fCommand = null;
		this.command = initCommand;
	}
	
	public FutureCommand(final String command)
	{
		super(command);
		this.command = null;
		this.fCommand = ParsingManager.submit(command);
	}
	
	@Override
	public void set(final String command)
	{
		super.set(command);
		this.command = null;
		this.fCommand = ParsingManager.submit(command);
	}
	
	@Override
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
					this.command = Parser.parseCommand(get());
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
