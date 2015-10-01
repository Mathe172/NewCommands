package net.minecraft.command;

import java.util.List;

import net.minecraft.util.BlockPos;

/**
 * Legacy support
 */
@Deprecated
public interface ICommand extends Comparable<ICommand>, IPermission
{
	String getCommandName();
	
	String getCommandUsage(ICommandSender sender);
	
	List<String> getCommandAliases();
	
	void processCommand(ICommandSender sender, String[] args) throws CommandException;
	
	List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos);
	
	/**
	 * Return whether the specified command parameter index is a username parameter.
	 */
	boolean isUsernameIndex(String[] args, int index);
}
