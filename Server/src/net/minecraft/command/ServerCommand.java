package net.minecraft.command;

public class ServerCommand
{
	public final FutureCommand command;
	public final ICommandSender sender;
	private static final String __OBFID = "CL_00001779";
	
	public ServerCommand(final String command, final ICommandSender sender)
	{
		this.command = new FutureCommand(command);
		this.sender = sender;
	}
}
