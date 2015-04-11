package net.minecraft.command.type.custom;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.command.CommandException;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.completion.DataRequest;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletion;
import net.minecraft.command.completion.TabCompletionData;
import net.minecraft.command.completion.TabCompletionData.Weighted;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.CTypeCompletable;
import net.minecraft.command.type.management.CConverter;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.server.MinecraftServer;

public class TypeScoreObjective extends CTypeCompletable<ScoreObjective>
{
	public static final CDataType<ScoreObjective> parser = new TypeScoreObjective();
	
	@Override
	public ArgWrapper<ScoreObjective> iParse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
	{
		final ArgWrapper<ScoreObjective> ret = ParsingUtilities.parseString(parser, context, TypeIDs.ScoreObjective, StringToObjective);
		
		if (ret != null)
			return ret;
		
		throw parser.SEE("Expected score name around index ");
	}
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
	{
		tcDataSet.add(new DataRequest()
		{
			Set<String> tcSet;
			
			@Override
			public void process()
			{
				final Collection<?> objectives = MinecraftServer.getServer().worldServerForDimension(0).getScoreboard().getScoreObjectives();
				this.tcSet = new HashSet<>(objectives.size());
				
				for (final Object objective : objectives)
					this.tcSet.add(((ScoreObjective) objective).getDisplayName());
			}
			
			@Override
			public void createCompletions(final Set<Weighted> tcDataSet)
			{
				for (final String tc : this.tcSet)
					TabCompletionData.addToSet(tcDataSet, startIndex, cData, new TabCompletion(tc));
			}
		});
	}
	
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
}
