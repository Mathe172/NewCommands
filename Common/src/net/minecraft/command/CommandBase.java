package net.minecraft.command;

import net.minecraft.command.arg.CommandArg;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;

public abstract class CommandBase extends CommandArg<Integer>// ICommand
{
	private final IPermission permission;
	private static IAdminCommand theAdmin;
	@SuppressWarnings("unused")
	private static final String __OBFID = "CL_00001739";
	
	public CommandBase(final IPermission permission)
	{
		this.permission = permission;
	}
	
	/**
	 * Sets the static IAdminCommander.
	 */
	public static void setAdminCommander(final IAdminCommand command)
	{
		theAdmin = command;
	}
	
	public abstract int procCommand(ICommandSender sender) throws CommandException;
	
	private final int procCommandChecked(final ICommandSender sender) throws CommandException
	{
		if (this.permission.canCommandSenderUseCommand(sender))
			return this.procCommand(sender);
		
		final ChatComponentTranslation errorMessage = new ChatComponentTranslation("commands.generic.permission", new Object[0]);
		errorMessage.getChatStyle().setColor(EnumChatFormatting.RED);
		sender.addChatMessage(errorMessage);
		
		return 0;
	}
	
	public void notifyOperators(final ICommandSender sender, final String msgFormat, final Object... msgParams)
	{
		this.notifyOperators(sender, 0, msgFormat, msgParams);
	}
	
	public void notifyOperators(final ICommandSender sender, final int p_152374_2_, final String msgFormat, final Object... msgParams)
	{
		if (theAdmin != null)
		{
			theAdmin.notifyOperators(sender, this.permission, p_152374_2_, msgFormat, msgParams);
		}
	}
	
	@Override
	public final Integer eval(final ICommandSender sender) throws CommandException
	{
		return this.procCommandChecked(sender);
	}
}
