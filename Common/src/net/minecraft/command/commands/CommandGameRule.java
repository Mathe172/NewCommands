package net.minecraft.command.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.CommandResultStats.Type;
import net.minecraft.command.CommandUtilities;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.construction.CommandConstructable;
import net.minecraft.command.descriptors.CommandDescriptor.CParserData;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.custom.TypeStringLiteral;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.GameRules;

public final class CommandGameRule
{
	private CommandGameRule()
	{
	}
	
	public static final CDataType<String> typeRules = new TypeStringLiteral(
		"commandBlockOutput",
		"doDaylightCycle",
		"doEntityDrops",
		"doFireTick",
		"doMobLoot",
		"doMobSpawning",
		"doTileDrops",
		"keepInventory",
		"logAdminCommands",
		"mobGriefing",
		"naturalRegeneration",
		"randomTickSpeed",
		"reducedDebugInfo",
		"sendCommandFeedback",
		"showDeathMessages"); // TODO:......
	
	public static final CommandConstructable constructable = new CommandConstructable()
	{
		@Override
		public CommandArg<Integer> construct(final CParserData data) throws SyntaxErrorException
		{
			switch (data.size())
			{
			case 0:
				return Empty.command;
				
			case 1:
				return new Rule(
					data.get(TypeIDs.String));
				
			default:
				return new RuleVal(
					data.get(TypeIDs.String),
					data.get(TypeIDs.String));
			}
		}
	};
	
	private static class Empty extends CommandArg<Integer>
	{
		private static final Empty command = new Empty();
		
		@Override
		public Integer eval(final ICommandSender sender) throws CommandException
		{
			final Object[] rules = getGameRules().getRules();
			sender.addChatMessage(new ChatComponentText(ParsingUtilities.joinNiceString(rules)));
			
			sender.func_174794_a(Type.QUERY_RESULT, rules.length);
			return rules.length;
		}
	}
	
	private static class Rule extends CommandArg<Integer>
	{
		public final CommandArg<String> rule;
		
		private Rule(final CommandArg<String> rule)
		{
			this.rule = rule;
		}
		
		@Override
		public Integer eval(final ICommandSender sender) throws CommandException
		{
			final String rule = this.rule.eval(sender);
			
			final GameRules gamerules = getGameRules();
			
			if (!gamerules.hasRule(rule))
				throw new CommandException("commands.gamerule.norule", rule);
			
			final String val = gamerules.getGameRuleStringValue(rule);
			
			sender.addChatMessage((new ChatComponentText(rule)).appendText(" = ").appendText(val));
			
			final int intVal = gamerules.getInt(val);
			
			sender.func_174794_a(CommandResultStats.Type.QUERY_RESULT, intVal);
			
			return intVal;
		}
	}
	
	private static class RuleVal extends Rule
	{
		private final CommandArg<String> val;
		
		private RuleVal(final CommandArg<String> rule, final CommandArg<String> val)
		{
			super(rule);
			this.val = val;
		}
		
		@Override
		public Integer eval(final ICommandSender sender) throws CommandException
		{
			final String rule = this.rule.eval(sender);
			final String val = this.val.eval(sender);
			
			final GameRules gamerules = getGameRules();
			
			if (gamerules.areSameType(rule, GameRules.ValueType.BOOLEAN_VALUE) && !ParsingUtilities.isTrue(val) && !ParsingUtilities.isFalse(val))
				throw new CommandException("commands.generic.boolean.invalid", val);
			
			gamerules.setOrCreateGameRule(rule, val);
			func_175773_a(gamerules, rule);
			
			CommandUtilities.notifyOperators(sender, "commands.gamerule.success");
			
			return 1;
		}
	}
	
	public static void func_175773_a(final GameRules gameRules, final String rule)
	{
		if ("reducedDebugInfo".equals(rule))
		{
			final int var2 = gameRules.getGameRuleBooleanValue(rule) ? 22 : 23;
			
			for (final Object o : MinecraftServer.getServer().getConfigurationManager().playerEntityList)
			{
				final EntityPlayerMP player = (EntityPlayerMP) o;
				player.playerNetServerHandler.sendPacket(new S19PacketEntityStatus(player, (byte) var2));
			}
		}
	}
	
	/**
	 * Return the game rule set this command should be able to manipulate.
	 */
	private static GameRules getGameRules()
	{
		return MinecraftServer.getServer().worldServerForDimension(0).getGameRules();
	}
}
