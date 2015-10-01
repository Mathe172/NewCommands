package net.minecraft.command.type.custom;

import java.util.Collection;
import java.util.HashSet;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.minecraft.command.collections.Parsers;
import net.minecraft.command.completion.DataRequest;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.IComplete;
import net.minecraft.command.type.base.CompoundType;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.server.MinecraftServer;

public final class TypeScoreObjective extends CompoundType<ScoreObjective>
{
	private TypeScoreObjective(final IComplete completer)
	{
		super(Parsers.scoreObjective, completer);
	}
	
	private TypeScoreObjective(final Predicate<ScoreObjective> filter)
	{
		super(Parsers.scoreObjective, getCompleter(filter));
	}
	
	public static final IComplete completer = getCompleter(Predicates.<ScoreObjective> alwaysTrue());
	public static final IComplete writeableCompleter = getCompleter(new Predicate<ScoreObjective>()
	{
		@Override
		public boolean apply(final ScoreObjective objective)
		{
			return !objective.getCriteria().isReadOnly();
		}
	});
	
	public static final CDataType<ScoreObjective> type = new TypeScoreObjective(completer);
	public static final CDataType<ScoreObjective> typeWriteable = new TypeScoreObjective(writeableCompleter);
	public static final CDataType<ScoreObjective> typeTrigger = new TypeScoreObjective(new Predicate<ScoreObjective>()
	{
		@Override
		public boolean apply(final ScoreObjective objective)
		{
			return objective.getCriteria() == IScoreObjectiveCriteria.field_178791_c;
		}
	});
	
	public static final CDataType<String> typeWriteableString = new CompoundType<>(new ParserName("score name"), writeableCompleter);
	
	private static IComplete getCompleter(final Predicate<ScoreObjective> filter)
	{
		return new IComplete()
		{
			@Override
			public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
			{
				tcDataSet.add(new DataRequest.SimpleAdd(startIndex, cData)
				{
					@Override
					public void process()
					{
						final Collection<?> objectives = MinecraftServer.getServer().worldServerForDimension(0).getScoreboard().getScoreObjectives();
						this.tcSet = new HashSet<>(objectives.size());
						
						for (final Object objective : objectives)
							if (filter.apply((ScoreObjective) objective))
								this.tcSet.add(((ScoreObjective) objective).getDisplayName());
					}
				});
			}
		};
	}
}
