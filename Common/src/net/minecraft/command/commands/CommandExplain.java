package net.minecraft.command.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.construction.CommandConstructable;
import net.minecraft.command.construction.CommandDescriptorDefault.CParserData;
import net.minecraft.command.parser.DebugParser;
import net.minecraft.command.parser.Parser;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

public class CommandExplain extends CommandArg<Integer>
{
	private static final CommandExplain command = new CommandExplain(true);
	private static final CommandExplain commandAll = new CommandExplain(false);
	
	public static final CommandConstructable constructable = new CommandConstructable()
	{
		@Override
		public CommandArg<Integer> construct(final CParserData data) throws SyntaxErrorException
		{
			return data.params.isEmpty() ? command : new Pos(true, data.get(TypeIDs.BlockPos));
		}
	};
	
	public static final CommandConstructable constructableAll = new CommandConstructable()
	{
		@Override
		public CommandArg<Integer> construct(final CParserData data) throws SyntaxErrorException
		{
			return data.params.isEmpty() ? commandAll : new Pos(false, data.get(TypeIDs.BlockPos));
		}
	};
	
	private final boolean reducedOutput;
	
	protected CommandExplain(final boolean reducedOutput)
	{
		this.reducedOutput = reducedOutput;
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
		
		return this.parseCommand(sender, command, 1);
	}
	
	protected Integer parseCommand(final ICommandSender sender, final String command, final int startIndex) throws CommandException
	{
		final Parser parser = new DebugParser(sender, command, startIndex, this.reducedOutput);
		
		try
		{
			parser.parseCommand();
		} catch (final SyntaxErrorException ex)
		{
			sender.addChatMessage(new ChatComponentText("Parsing failed at index " + parser.getIndex() + ": ").appendSibling(ParsingUtilities.location(parser)));
			return 1;
		}
		
		throw new CommandException(
			"The command was parsed without any error"
				+ (this.reducedOutput ? " (use '/explain all' for more detailed information)" : ""));
	}
	
	private static class Pos extends CommandExplain
	{
		private final CommandArg<BlockPos> pos;
		
		public Pos(final boolean reducedOutput, final CommandArg<BlockPos> pos)
		{
			super(reducedOutput);
			this.pos = pos;
		}
		
		@Override
		public Integer eval(final ICommandSender sender) throws CommandException
		{
			final BlockPos pos = this.pos.eval(sender);
			
			final TileEntity te = sender.getEntityWorld().getTileEntity(pos);
			
			if (te == null || !(te instanceof TileEntityCommandBlock))
				throw new CommandException("There is no command-block at '" + pos.getX() + " " + pos.getY() + " " + pos.getZ() + "'.");
			
			return this.parseCommand(sender, ((TileEntityCommandBlock) te).getCommandBlockLogic().getCustomName(), 0);
		}
	}
}
