package net.minecraft.command;

import net.minecraft.block.Block;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

public final class CommandBase
{
	@SuppressWarnings("unused")
	private static final String __OBFID = "CL_00001739";
	
	private CommandBase()
	{
	}
	
	public static void notifyOperators(final ICommandSender sender, final String msgFormat, final Object... msgParams)
	{
		notifyOperators(sender, 0, msgFormat, msgParams);
	}
	
	public static void notifyOperators(final ICommandSender sender, final int flags, final String msgFormat, final Object... msgParams)
	{
		ServerCommandManager.notifyOperators(sender, flags, msgFormat, msgParams);
	}
	
	public static int parseInt(final String input) throws NumberInvalidException
	{
		try
		{
			return Integer.parseInt(input);
		} catch (final NumberFormatException e)
		{
			throw new NumberInvalidException("commands.generic.num.invalid", input);
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
			throw new NumberInvalidException("commands.generic.num.tooSmall", ret, min);
		
		if (ret > max)
			throw new NumberInvalidException("commands.generic.num.tooBig", ret, max);
		
		return ret;
	}
	
	public static int parseInt(final int input, final int min) throws NumberInvalidException
	{
		return parseInt(input, min, Integer.MAX_VALUE);
	}
	
	public static int parseInt(final int input, final int min, final int max) throws NumberInvalidException
	{
		if (input < min)
			throw new NumberInvalidException("commands.generic.num.tooSmall", input, min);
		
		if (input > max)
			throw new NumberInvalidException("commands.generic.num.tooBig", input, max);
		
		return input;
	}
	
	/**
	 * Gets the Block specified by the given text string. First checks the block registry, then tries by parsing the string as an integer ID (deprecated). Warns the sender if we matched by parsing the ID. Throws if the block wasn't found. Returns the block if it was found.
	 */
	public static Block getBlockByText(final String id) throws NumberInvalidException
	{
		final ResourceLocation resource = new ResourceLocation(id);
		
		if (!Block.blockRegistry.containsKey(resource))
			throw new NumberInvalidException("commands.give.notFound", resource);
		
		final Block block = (Block) Block.blockRegistry.getObject(resource);
		
		if (block == null)
			throw new NumberInvalidException("commands.give.notFound", resource);
		
		return block;
	}
	
	public static void message(final ICommandSender sender, final EnumChatFormatting format, final String message, final Object... args)
	{
		final ChatComponentTranslation chatComponent = new ChatComponentTranslation(message, args);
		chatComponent.getChatStyle().setColor(format);
		sender.addChatMessage(chatComponent);
	}
	
	public static void errorMessage(final ICommandSender sender, final String message, final Object... args)
	{
		message(sender, EnumChatFormatting.RED, message, args);
	}
}
