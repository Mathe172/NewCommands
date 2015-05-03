package net.minecraft.command.commands;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.construction.CommandConstructable;
import net.minecraft.command.descriptors.CommandDescriptor.ParserData;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.custom.TypeStringLiteral;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.WorldSettings;

public abstract class CommandGameMode extends CommandArg<Integer>
{
	public static final CDataType<String> gamemodeParser = new TypeStringLiteral(
		WorldSettings.GameType.SURVIVAL.getName(),
		WorldSettings.GameType.CREATIVE.getName(),
		WorldSettings.GameType.ADVENTURE.getName(),
		WorldSettings.GameType.SPECTATOR.getName(),
		"s", "c", "a", "sp", "0", "1", "2", "3");
	
	public static final CommandConstructable constructable = new CommandConstructable()
	{
		@Override
		public CommandGameMode construct(final ParserData data) throws SyntaxErrorException
		{
			if (data.size() == 1)
				return new NoPlayer(getParam(TypeIDs.String, data));
			else
				return new Player(
					getParam(TypeIDs.String, data),
					getParam(TypeIDs.EntityList, data));
		}
	};
	
	protected final CommandArg<String> gamemode;
	
	public CommandGameMode(final CommandArg<String> gamemode)
	{
		this.gamemode = gamemode;
	}
	
	public void procCommand(final ICommandSender sender, final WorldSettings.GameType gameType, final EntityPlayerMP player)
	{
		player.setGameType(gameType);
		player.fallDistance = 0.0F;
		
		if (sender.getEntityWorld().getGameRules().getGameRuleBooleanValue("sendCommandFeedback"))
		{
			player.addChatMessage(new ChatComponentTranslation("gameMode.changed"));
		}
		
		final ChatComponentTranslation message = new ChatComponentTranslation("gameMode." + gameType.getName());
		
		if (player != sender)
		{
			CommandBase.notifyOperators(sender, 1, "commands.gamemode.success.other", player.getName(), message);
		}
		else
		{
			CommandBase.notifyOperators(sender, 1, "commands.gamemode.success.self", message);
		}
	}
	
	private static class Player extends CommandGameMode
	{
		private final CommandArg<List<Entity>> players;
		
		public Player(final CommandArg<String> gamemode, final CommandArg<List<Entity>> players)
		{
			super(gamemode);
			this.players = players;
		}
		
		@Override
		public Integer eval(final ICommandSender sender) throws CommandException
		{
			int successCount = 0;
			
			final WorldSettings.GameType gameType = getGameModeFromCommand(this.gamemode.eval(sender));
			
			final List<Entity> entities = this.players.eval(sender);
			
			for (final Entity entity : entities)
			{
				if (!(entity instanceof EntityPlayerMP))
					sender.addChatMessage(entity.getDisplayName().appendText(" is not a player"));
				else
				{
					this.procCommand(sender, gameType, (EntityPlayerMP) entity);
					++successCount;
				}
			}
			
			sender.func_174794_a(CommandResultStats.Type.AFFECTED_ENTITIES, entities.size());// Not successCount to conform with old behavior
			
			return successCount;
		}
	}
	
	private static class NoPlayer extends CommandGameMode
	{
		public NoPlayer(final CommandArg<String> gamemode)
		{
			super(gamemode);
		}
		
		@Override
		public Integer eval(final ICommandSender sender) throws CommandException
		{
			this.procCommand(sender, getGameModeFromCommand(this.gamemode.eval(sender)), ParsingUtilities.getCommandSenderAsPlayer(sender));
			sender.func_174794_a(CommandResultStats.Type.AFFECTED_ENTITIES, 1);
			return 1;
		}
	}
	
	/**
	 * Gets the Game Mode specified in the command.
	 */
	protected static WorldSettings.GameType getGameModeFromCommand(final String gamemode) throws CommandException
	{
		return !gamemode.equalsIgnoreCase(WorldSettings.GameType.SURVIVAL.getName()) && !gamemode.equalsIgnoreCase("s") ? (!gamemode.equalsIgnoreCase(WorldSettings.GameType.CREATIVE.getName()) && !gamemode.equalsIgnoreCase("c") ? (!gamemode.equalsIgnoreCase(WorldSettings.GameType.ADVENTURE.getName()) && !gamemode.equalsIgnoreCase("a") ? (!gamemode.equalsIgnoreCase(WorldSettings.GameType.SPECTATOR.getName()) && !gamemode.equalsIgnoreCase("sp") ? WorldSettings.getGameTypeById(CommandBase.parseInt(gamemode, 0, WorldSettings.GameType.values().length - 2)) : WorldSettings.GameType.SPECTATOR) : WorldSettings.GameType.ADVENTURE) : WorldSettings.GameType.CREATIVE) : WorldSettings.GameType.SURVIVAL;
	}
}
