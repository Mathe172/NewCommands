package net.minecraft.command.type.custom;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.command.completion.DataRequest;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletion;
import net.minecraft.command.completion.TabCompletionData;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CListProvider;
import net.minecraft.command.type.IComplete;
import net.minecraft.command.type.ProviderCompleter;
import net.minecraft.server.MinecraftServer;

public class Completers
{
	public static final IComplete userCompleter = new ProviderCompleter(new CListProvider()
	{
		@Override
		public Set<TabCompletion> getList(final Parser parser)
		{
			return MinecraftServer.getServer().getConfigurationManager().playerCompletions;
		}
	});
	
	public static final IComplete nonOppedOnline = new IComplete()
	{
		@Override
		public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
		{
			tcDataSet.add(new DataRequest()
			{
				String[] ops = null;
				
				@Override
				public void process()
				{
					this.ops = MinecraftServer.getServer().getConfigurationManager().getOppedPlayerNames();
				}
				
				@Override
				public void createCompletions(final Set<TabCompletionData> tcDataSet)
				{
					final Set<String> opSet = new HashSet<>(this.ops.length);
					for (final String op : this.ops)
						opSet.add(op);
					
					for (final TabCompletion tc : MinecraftServer.getServer().getConfigurationManager().playerCompletions)
						if (!opSet.contains(tc.name))
							TabCompletionData.addToSet(tcDataSet, parser.toParse, startIndex, cData, tc);
				}
			});
		}
	};
	
	public static final IComplete opName = new IComplete()
	{
		@Override
		public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
		{
			tcDataSet.add(new DataRequest()
			{
				String[] ops = null;
				
				@Override
				public void process()
				{
					this.ops = MinecraftServer.getServer().getConfigurationManager().getOppedPlayerNames();
				}
				
				@Override
				public void createCompletions(final Set<TabCompletionData> tcDataSet)
				{
					for (final String op : this.ops)
						TabCompletionData.addToSet(tcDataSet, parser.toParse, startIndex, cData, new TabCompletion(op));
				}
			});
		}
	};
}
