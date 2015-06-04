package net.minecraft.command;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public final class CommandUtilities
{
	public static boolean catchStack = false;
	
	private CommandUtilities()
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
	
	public static void checkInt(final int input, final int min) throws NumberInvalidException
	{
		checkInt(input, min, Integer.MAX_VALUE);
	}
	
	public static void checkInt(final int input, final int min, final int max) throws NumberInvalidException
	{
		if (input < min)
			throw new NumberInvalidException("commands.generic.num.tooSmall", input, min);
		
		if (input > max)
			throw new NumberInvalidException("commands.generic.num.tooBig", input, max);
	}
	
	/**
	 * Gets the Block specified by the given text string. First checks the block registry, then tries by parsing the string as an integer ID (deprecated). Warns the sender if we matched by parsing the ID. Throws if the block wasn't found. Returns the block if it was found.
	 */
	public static Block getBlockByText(final String id) throws NumberInvalidException
	{
		final ResourceLocation resource = new ResourceLocation(id);
		
		final Block block = (Block) Block.blockRegistry.getObject(resource);
		
		if (block == null)
			throw new NumberInvalidException("commands.give.notFound", resource);
		
		return block;
	}
	
	/**
	 * Gets the Item specified by the given text string. First checks the item registry, then tries by parsing the string as an integer ID (deprecated). Warns the sender if we matched by parsing the ID. Throws if the item wasn't found. Returns the item if it was found.
	 */
	public static Item getItemByText(final String toConvert) throws NumberInvalidException
	{
		final ResourceLocation resource = new ResourceLocation(toConvert);
		
		final Item item = (Item) Item.itemRegistry.getObject(resource);
		
		if (item == null)
			throw new NumberInvalidException("commands.give.notFound", resource);
		
		return item;
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
	
	public static void setNBT(final World world, final BlockPos pos, final NBTTagCompound nbt)
	{
		final TileEntity te = world.getTileEntity(pos);
		
		if (te == null)
			return;
		
		nbt.setInteger("x", pos.getX());
		nbt.setInteger("y", pos.getY());
		nbt.setInteger("z", pos.getZ());
		
		te.readFromNBT(nbt);
		
		te.markDirty();
		world.markBlockForUpdate(pos);
	}
	
	/**
	 * Returns the temp-block (barrier or bedrock) with the opacity closest to either state1 or state2 (tries to minimize the load on the lightning engine)
	 */
	public static IBlockState getTempState(final IBlockState state1, final IBlockState state2)
	{
		return (state1.getBlock().getLightOpacity() + state2.getBlock().getLightOpacity() < 15 ? Blocks.barrier : Blocks.bedrock).getDefaultState();
	}
	
	/**
	 * Returns the temp-block (barrier or bedrock) with the opacity closest to state (tries to minimize the load on the lightning engine)
	 */
	public static IBlockState getTempState(final IBlockState state)
	{
		return (state.getBlock().getLightOpacity() < 8 ? Blocks.barrier : Blocks.bedrock).getDefaultState();
	}
}
