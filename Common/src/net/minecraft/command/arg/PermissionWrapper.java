package net.minecraft.command.arg;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.IPermission;
import net.minecraft.command.completion.DataRequest;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletionData.Weighted;
import net.minecraft.command.parser.CompletionParser.CompletionData;

public class PermissionWrapper<T> extends CommandArg<T>
{
	private final CommandArg<T> arg;
	private final IPermission permission;
	
	public PermissionWrapper(final CommandArg<T> arg, final IPermission permission)
	{
		this.arg = arg;
		this.permission = permission;
	}
	
	@Override
	public T eval(final ICommandSender sender) throws CommandException
	{
		if (this.permission.canCommandSenderUseCommand(sender))
			return this.arg.eval(sender);
		
		throw new CommandException("commands.generic.permission");
	}
	
	public static <T> ArgWrapper<T> wrap(final ArgWrapper<T> toWrap, final IPermission permission)
	{
		return toWrap.type.wrap(new PermissionWrapper<>(toWrap.arg(), permission));
	}
	
	public static class Command extends PermissionWrapper<Integer>
	{
		public Command(final CommandArg<Integer> arg, final IPermission permission)
		{
			super(arg, permission);
		}
		
		@Override
		public final Integer eval(final ICommandSender sender) throws CommandException
		{
			final int ret = super.eval(sender);
			sender.func_174794_a(CommandResultStats.Type.SUCCESS_COUNT, ret);
			
			return ret;
		}
	}
	
	public static void complete(final TCDSet tcDataSet, final int startIndex, final CompletionData cData, final Map<ITabCompletion, IPermission> completions)
	{
		final Map<Weighted, IPermission> filtered = new IdentityHashMap<>();
		
		for (final Entry<ITabCompletion, IPermission> e : completions.entrySet())
		{
			final Weighted tcData = e.getKey().getMatchData(startIndex, cData);
			
			if (tcData != null)
				filtered.put(tcData, e.getValue());
		}
		
		tcDataSet.add(new DataRequest()
		{
			@Override
			public void process()
			{
				for (final Entry<Weighted, IPermission> e : filtered.entrySet())
				{
					final IPermission permission = e.getValue();
					if (permission == null || permission.canCommandSenderUseCommand(cData.sender))
						tcDataSet.add(e.getKey());
				}
			}
			
			@Override
			public void createCompletions(final Set<Weighted> tcDataSet)
			{
			}
		});
	}
}
