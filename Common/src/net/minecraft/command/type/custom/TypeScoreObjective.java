package net.minecraft.command.type.custom;

import java.util.Collection;
import java.util.HashSet;

import net.minecraft.command.CommandException;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.completion.DataRequest;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.IComplete;
import net.minecraft.command.type.base.CompoundType;
import net.minecraft.command.type.management.CConverter;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.server.MinecraftServer;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public final class TypeScoreObjective extends CompoundType<ScoreObjective>
{
	public static final CConverter<String, ScoreObjective> StringToObjective = new CConverter<String, ScoreObjective>()
	{
		@Override
		public ScoreObjective convert(final String toConvert) throws CommandException
		{
			final ScoreObjective ret = MinecraftServer.getServer().worldServerForDimension(0).getScoreboard().getObjective(toConvert);
			
			if (ret != null)
				return ret;
			
			throw new CommandException("Objective not found: " + toConvert);
		}
	};
	
	private static final CDataType<ScoreObjective> parser = new ParserName.CustomType<>("score name", TypeIDs.ScoreObjective, StringToObjective);
	
	public static final CDataType<ScoreObjective> type = new TypeScoreObjective(Predicates.<ScoreObjective> alwaysTrue());
	public static final CDataType<ScoreObjective> typeWriteable = new TypeScoreObjective(new Predicate<ScoreObjective>()
	{
		@Override
		public boolean apply(final ScoreObjective objective)
		{
			return !objective.getCriteria().isReadOnly();
		}
	});
	
	public static final CDataType<ScoreObjective> parserTrigger = new TypeScoreObjective(new Predicate<ScoreObjective>()
	{
		@Override
		public boolean apply(final ScoreObjective objective)
		{
			return objective.getCriteria() == IScoreObjectiveCriteria.field_178791_c;
		}
	});
	
	private TypeScoreObjective(final Predicate<ScoreObjective> filter)
	{
		super(parser, new IComplete()
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
		});
	}
}
