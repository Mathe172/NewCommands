package net.minecraft.command.type.custom;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.command.completion.DataRequest;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletion;
import net.minecraft.command.completion.TabCompletionData;
import net.minecraft.command.completion.TabCompletionData.Weighted;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CListProvider;
import net.minecraft.command.type.IComplete;
import net.minecraft.command.type.ProviderCompleter;
import net.minecraft.entity.EntityList;
import net.minecraft.server.MinecraftServer;

public class Completers
{
	public static final IComplete userCompleter = new ProviderCompleter(new CListProvider()
	{
		@Override
		public Set<ITabCompletion> getList(final Parser parser)
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
				public void createCompletions(final Set<Weighted> tcDataSet)
				{
					final Set<String> opSet = new HashSet<>(this.ops.length);
					for (final String op : this.ops)
						opSet.add(op);
					
					for (final ITabCompletion tc : MinecraftServer.getServer().getConfigurationManager().playerCompletions)
						if (!opSet.contains(tc.name))
							TabCompletionData.addToSet(tcDataSet, startIndex, cData, tc);
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
				public void createCompletions(final Set<Weighted> tcDataSet)
				{
					for (final String op : this.ops)
						TabCompletionData.addToSet(tcDataSet, startIndex, cData, new TabCompletion(op));
				}
			});
		}
	};
	
	public static final IComplete teamName = new IComplete()
	{
		@Override
		public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
		{
			tcDataSet.add(new DataRequest()
			{
				Set<String> teams;
				
				@Override
				public void process()
				{
					final Collection<?> teamNames = MinecraftServer.getServer().worldServerForDimension(0).getScoreboard().getTeamNames();
					this.teams = new HashSet<>(teamNames.size());
					
					for (final Object team : teamNames)
						this.teams.add((String) team);
				}
				
				@Override
				public void createCompletions(final Set<Weighted> tcDataSet)
				{
					for (final String team : this.teams)
						TabCompletionData.addToSet(tcDataSet, startIndex, cData, new TabCompletion(team));
				}
			});
		}
	};
	
	public static final IComplete entityID = new IComplete()
	{
		@Override
		public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
		{
			TabCompletionData.addToSet(tcDataSet, startIndex, cData, EntityList.completions);
		}
	};
	
	public static final IComplete entityIDWPlayer = new IComplete()
	{
		private final ITabCompletion playerCompletion = new TabCompletion("Player");
		
		@Override
		public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
		{
			entityID.complete(tcDataSet, parser, startIndex, cData);
			TabCompletionData.addToSet(tcDataSet, startIndex, cData, this.playerCompletion);
		}
	};
	
	public static final CompleterResourcePath blockCompleter = new CompleterResourcePath();
	
	public static final CompleterResourcePath itemCompleter = new CompleterResourcePath();
}
