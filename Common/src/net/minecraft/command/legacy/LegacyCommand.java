package net.minecraft.command.legacy;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.CommandResultStats.Type;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.arg.PermissionWrapper.Command;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.construction.RegistrationHelper;
import net.minecraft.command.descriptors.CommandDescriptor;
import net.minecraft.command.descriptors.ICommandDescriptor;
import net.minecraft.command.legacy.LegacyCommand.LegacyParserData;
import net.minecraft.command.parser.Parser;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/*
 * Legacy Support
 */
@Deprecated
public class LegacyCommand extends CommandDescriptor<LegacyParserData>
{
	private final ICommand command;
	
	private static ICommandSender legacyICS = new ICommandSender()
	{
		private static final String err = "Something accessed a method of the legacy command support ICommandSender.";
		
		@Override
		public boolean sendCommandFeedback()
		{
			MinecraftServer.getServer().logWarning(err);
			return false;
		}
		
		@Override
		public Vec3 getPositionVector()
		{
			MinecraftServer.getServer().logWarning(err);
			return new Vec3(0, 0, 0);
		}
		
		@Override
		public BlockPos getPosition()
		{
			MinecraftServer.getServer().logWarning(err);
			return new BlockPos(0, 0, 0);
		}
		
		@Override
		public String getName()
		{
			MinecraftServer.getServer().logWarning(err);
			return "Legacy command support";
		}
		
		@Override
		public World getEntityWorld()
		{
			MinecraftServer.getServer().logWarning(err);
			return MinecraftServer.getServer().getEntityWorld();
		}
		
		@Override
		public IChatComponent getDisplayName()
		{
			MinecraftServer.getServer().logWarning(err);
			return new ChatComponentText("Legacy command support");
		}
		
		@Override
		public Entity getCommandSenderEntity()
		{
			MinecraftServer.getServer().logWarning(err);
			return null;
		}
		
		@Override
		public void func_174794_a(final Type p_174794_1_, final int p_174794_2_)
		{
			MinecraftServer.getServer().logWarning(err);
		}
		
		@Override
		public boolean canCommandSenderUseCommand(final int permissionLevel, final String command)
		{
			MinecraftServer.getServer().logWarning(err);
			return false;
		}
		
		@Override
		public void addChatMessage(final IChatComponent message)
		{
			MinecraftServer.getServer().logWarning(err + "\nMessage sent: " + message.getUnformattedText());
		}
	};
	
	public LegacyCommand(final ICommand command)
	{
		super(command, RegistrationHelper.usage(command.getCommandUsage(legacyICS)));
		this.command = command;
	}
	
	/**
	 * Legacy support
	 */
	@Deprecated
	public static class LegacyParserData
	{
		public final ICommand command;
		public String argString;
		
		public LegacyParserData(final ICommand command)
		{
			this.command = command;
		}
	}
	
	@Override
	public CommandArg<Integer> construct(final LegacyParserData data) throws SyntaxErrorException
	{
		final String[] args = data.argString != null ? dropFirstString(data.argString.split(" ")) : new String[0];
		
		final int usernameIndex = this.getUsernameIndex(args);
		final CommandArg<List<Entity>> targets = usernameIndex < 0 ? null : Parser.parseEntityList(args[usernameIndex]);
		
		return new CommandArg<Integer>()
		{
			
			private final CommandArg<Integer> wrappedCommand = new CommandArg<Integer>()
			{
				@Override
				public Integer eval(final ICommandSender sender) throws CommandException
				{
					LegacyCommand.this.command.processCommand(sender, args);
					return 1;
				}
			};
			
			@Override
			public Integer eval(final ICommandSender sender) throws CommandException
			{
				if (targets == null)
				{
					sender.func_174794_a(CommandResultStats.Type.AFFECTED_ENTITIES, 1);
					return this.wrappedCommand.eval(sender);
				}
				
				final List<Entity> eTargets = targets.eval(sender);
				
				sender.func_174794_a(CommandResultStats.Type.AFFECTED_ENTITIES, eTargets.size());
				
				int ret = 0;
				for (final Entity target : eTargets)
				{
					args[usernameIndex] = target.getUniqueID().toString();
					
					ret += CommandHandler.executeCommand(sender, this.wrappedCommand);
				}
				
				return ret;
			}
		};
	}
	
	@Override
	public ICommandDescriptor<? super LegacyParserData> getSubDescriptor(final Parser parser, final LegacyParserData data) throws SyntaxErrorException
	{
		return null;
	}
	
	@Override
	public ICommandDescriptor<? super LegacyParserData> getSubDescriptor(final String keyword)
	{
		return null;
	}
	
	@Override
	public final Set<ITabCompletion> getKeywordCompletions()
	{
		return Collections.emptySet();
	}
	
	@Override
	public void addSubDescriptor(final String key, final ICommandDescriptor<? super LegacyParserData> descriptor)
	{
		throw new UnsupportedOperationException("Can't add sub-types to legacy-command");
	}
	
	protected int getUsernameIndex(final String[] args)
	{
		for (int i = 0; i < args.length; ++i)
			if (this.command.isUsernameIndex(args, i))
				return i;
		
		return -1;
	}
	
	@Override
	public void parse(final Parser parser, final LegacyParserData parserData, final UsageProvider usage) throws SyntaxErrorException
	{
		TypeLegacyArg.parser.parse(parser, parserData);
	}
	
	@Override
	public Command parse(final Parser parser) throws SyntaxErrorException
	{
		final LegacyParserData parserData = new LegacyParserData(this.command);
		TypeLegacyArg.parser.parse(parser, parserData);
		
		// CommandsParser REQUIRES that endingMatcher is in the following state: whitespaces processed + found match
		final Matcher endingMatcher = parser.getMatcher(ParsingUtilities.endingMatcher);
		
		if (parser.find(endingMatcher))
			parser.incIndex(endingMatcher.group(1).length());
		else
			throw parser.SEE("Expected ')' or end of string ");
		
		return new Command(this.construct(parserData), this.command);
	}
	
	/**
	 * creates a new array and sets elements 0..n-2 to be 0..n-1 of the input (n elements)
	 */
	@Deprecated
	protected static String[] dropFirstString(final String[] p_71559_0_)
	{
		if (p_71559_0_.length == 0)
			return p_71559_0_;
		
		final String[] var1 = new String[p_71559_0_.length - 1];
		System.arraycopy(p_71559_0_, 1, var1, 0, p_71559_0_.length - 1);
		return var1;
	}
}
