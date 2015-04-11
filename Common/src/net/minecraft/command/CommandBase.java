package net.minecraft.command;

import net.minecraft.block.Block;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

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
		notifyOperators(sender, 0, msgFormat, msgParams);
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
		final int ret = this.procCommandChecked(sender);
		sender.func_174794_a(CommandResultStats.Type.SUCCESS_COUNT, ret);
		
		return ret;
	}
	
	public static int parseInt(final String input) throws NumberInvalidException
	{
		try
		{
			return Integer.parseInt(input);
		} catch (final NumberFormatException e)
		{
			throw new NumberInvalidException("commands.generic.num.invalid", new Object[] { input });
		}
	}
	
	public static int parseInt(final String input, final int min) throws NumberInvalidException
	{
		return parseInt(input, min, Integer.MAX_VALUE);
	}
	
	public static int parseInt(final String input, final int min, final int max) throws NumberInvalidException
	{
		final int ret = parseInt(input);
		
		if (ret < min)
		{
			throw new NumberInvalidException("commands.generic.num.tooSmall", new Object[] { Integer.valueOf(ret), Integer.valueOf(min) });
		}
		else if (ret > max)
		{
			throw new NumberInvalidException("commands.generic.num.tooBig", new Object[] { Integer.valueOf(ret), Integer.valueOf(max) });
		}
		else
		{
			return ret;
		}
	}
	
	public static int parseInt(final int input, final int min) throws NumberInvalidException
	{
		return parseInt(input, min, Integer.MAX_VALUE);
	}
	
	public static int parseInt(final int input, final int min, final int max) throws NumberInvalidException
	{
		if (input < min)
		{
			throw new NumberInvalidException("commands.generic.num.tooSmall", new Object[] { Integer.valueOf(input), Integer.valueOf(min) });
		}
		else if (input > max)
		{
			throw new NumberInvalidException("commands.generic.num.tooBig", new Object[] { Integer.valueOf(input), Integer.valueOf(max) });
		}
		else
		{
			return input;
		}
	}
	
	/**
	 * Gets the Block specified by the given text string. First checks the block registry, then tries by parsing the string as an integer ID (deprecated). Warns the sender if we matched by parsing the ID. Throws if the block wasn't found. Returns the block if it was found.
	 */
	public static Block getBlockByText(final String id) throws NumberInvalidException
	{
		final ResourceLocation resource = new ResourceLocation(id);
		
		if (!Block.blockRegistry.containsKey(resource))
			throw new NumberInvalidException("commands.give.notFound", new Object[] { resource });
		
		final Block block = (Block) Block.blockRegistry.getObject(resource);
		
		if (block == null)
			throw new NumberInvalidException("commands.give.notFound", new Object[] { resource });
		
		return block;
	}
}
