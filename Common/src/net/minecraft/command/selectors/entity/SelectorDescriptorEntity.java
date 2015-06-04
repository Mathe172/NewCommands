package net.minecraft.command.selectors.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import net.minecraft.command.IPermission;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.TypedWrapper.Getter;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.collections.Types;
import net.minecraft.command.completion.DataRequest;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletion;
import net.minecraft.command.completion.TabCompletionData;
import net.minecraft.command.completion.TabCompletionData.Weighted;
import net.minecraft.command.descriptors.SelectorDescriptor;
import net.minecraft.command.descriptors.SelectorDescriptorDefault.DefaultParserData;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.selectors.entity.FilterList.InvertableArg;
import net.minecraft.command.selectors.entity.SelectorDescriptorEntity.ExParserData;
import net.minecraft.command.selectors.entity.SelectorEntity.SelectorType;
import net.minecraft.command.type.IDataType;
import net.minecraft.command.type.custom.ParserDouble;
import net.minecraft.command.type.custom.ParserInt;
import net.minecraft.command.type.custom.TypeNullable;
import net.minecraft.command.type.custom.coordinate.TypeCoordinate;
import net.minecraft.command.type.custom.coordinate.TypeCoordinates;
import net.minecraft.command.type.custom.nbt.TypeNBTArg;
import net.minecraft.command.type.management.TypeID;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.server.MinecraftServer;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

public final class SelectorDescriptorEntity extends SelectorDescriptor<ExParserData>
{
	public static class ExParserData extends DefaultParserData
	{
		public InvertableArg name = null;
		public InvertableArg team = null;
		public InvertableArg type = null;
		
		public Map<String, MutablePair<Getter<Integer>, Getter<Integer>>> primitiveScores = new HashMap<>();
		public boolean nullScoreAllowed = true;
		
		public ExParserData(final Parser parser)
		{
			super(parser);
		}
	}
	
	private static final List<IDataType<?>> unnamedTypes = new ArrayList<>(4);
	private static final Map<String, IDataType<?>> namedTypes = new HashMap<>(21);
	private static final Set<ITabCompletion> keyCompletions = new HashSet<>(18);
	
	private static final ITabCompletion nameCompletion = new TabCompletion("name=", "name=", "name");
	private static final ITabCompletion teamCompletion = new TabCompletion("team=", "team=", "team");
	private static final ITabCompletion typeCompletion = new TabCompletion("type=", "type=", "type");
	
	static
	{
		unnamedTypes.add(new TypeNullable<>(TypeCoordinate.typeXNC));
		unnamedTypes.add(new TypeNullable<>(TypeCoordinate.typeYNC));
		unnamedTypes.add(new TypeNullable<>(TypeCoordinate.typeZNC));
		unnamedTypes.add(ParserInt.parser);
		
		namedTypes.put("x", TypeCoordinate.typeXNC);
		namedTypes.put("y", TypeCoordinate.typeYNC);
		namedTypes.put("z", TypeCoordinate.typeZNC);
		namedTypes.put("r", ParserInt.parser);
		namedTypes.put("rm", ParserInt.parser);
		namedTypes.put("dx", ParserDouble.parser);
		namedTypes.put("dy", ParserDouble.parser);
		namedTypes.put("dz", ParserDouble.parser);
		namedTypes.put("dxyz", Types.generalType(TypeIDs.Coordinates));
		namedTypes.put("c", ParserInt.parser);
		namedTypes.put("m", ParserInt.parser);
		namedTypes.put("l", ParserInt.parser);
		namedTypes.put("lm", ParserInt.parser);
		namedTypes.put("rx", ParserDouble.parser);
		namedTypes.put("rxm", ParserDouble.parser);
		namedTypes.put("ry", ParserDouble.parser);
		namedTypes.put("rym", ParserDouble.parser);
		
		namedTypes.put("xyz", TypeCoordinates.nonCentered);
		namedTypes.put("nbt", TypeNBTArg.parserEntity);
		
		for (final String key : namedTypes.keySet())
		{
			final String s = key + "=";
			keyCompletions.add(new TabCompletion(s, s, key));
		}
		
	}
	
	private final SelectorType selType;
	
	public SelectorDescriptorEntity(final SelectorType selType)
	{
		super(Collections.<TypeID<?>> singleton(TypeIDs.EntityList), IPermission.unrestricted);
		
		this.selType = selType;
	}
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData, final ExParserData data)
	{
		for (final ITabCompletion tc : keyCompletions)
			if (!data.namedParams.containsKey(tc.name.toLowerCase()))
				TabCompletionData.addToSet(tcDataSet, startIndex, cData, tc);
		
		if (data.name == null)
			TabCompletionData.addToSet(tcDataSet, startIndex, cData, nameCompletion);
		if (data.team == null)
			TabCompletionData.addToSet(tcDataSet, startIndex, cData, teamCompletion);
		if (data.type == null)
			TabCompletionData.addToSet(tcDataSet, startIndex, cData, typeCompletion);
		
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
				{
					final Pair<Getter<Integer>, Getter<Integer>> score = data.primitiveScores.get(tc);
					
					if (score == null || score.getLeft() == null)
						TabCompletionData.addToSet(tcDataSet, startIndex, cData, new TabCompletion("score_" + tc + "_min")
						{
							@Override
							public double weightOffset(final Matcher m, final CompletionData cData)
							{
								return -1.0;
							};
						});
					
					if (score == null || score.getRight() == null)
						TabCompletionData.addToSet(tcDataSet, startIndex, cData, new TabCompletion("score_" + tc)
						{
							@Override
							public double weightOffset(final Matcher m, final CompletionData cData)
							{
								return -1.0;
							};
						});
				}
			}
		});
	}
	
	@Override
	public ArgWrapper<?> construct(final ExParserData data)
	{
		return TypeIDs.EntityList.wrap(new SelectorEntity(this.selType, data));
	}
	
	@Override
	public void parse(final Parser parser, final String key, final ExParserData data) throws SyntaxErrorException, CompletionException
	{
		final IDataType<?> valueType = namedTypes.get(key);
		
		if (valueType != null)
		{
			data.namedParams.put(key, valueType.parse(parser).addToProcess(data.toProcess));
			return;
		}
		
		if (key.startsWith("score_"))
		{
			final boolean min = key.endsWith("_min");
			
			this.parseScore(parser, key.substring(6, key.length() - (min ? 4 : 0)), min, data);
			return;
		}
		
		switch (key)
		{
		case "name":
			data.name = FilterList.name.parse(parser, data);
			return;
		case "team":
			data.team = FilterList.team.parse(parser, data);
			return;
		case "type":
			data.type = FilterList.type.parse(parser, data);
			return;
		}
		
		throw parser.SEE("Unknown parameter key '" + key + "' encountered ");
	}
	
	private final void parseScore(final Parser parser, final String name, final boolean min, final ExParserData data) throws SyntaxErrorException, CompletionException
	{
		final Getter<Integer> value = ParserInt.parser.parse(parser).addToProcess(data.toProcess).get();
		
		final MutablePair<Getter<Integer>, Getter<Integer>> scoreData = data.primitiveScores.get(name);
		
		if (scoreData == null)
		{
			data.primitiveScores.put(name, new MutablePair<>(min ? value : null, min ? null : value));
			return;
		}
		
		if (min)
			scoreData.left = value;
		else
			scoreData.right = value;
	}
	
	@Override
	public void parse(final Parser parser, final ExParserData data) throws SyntaxErrorException, CompletionException
	{
		if (unnamedTypes.size() <= data.unnamedParams.size())
			throw data.parser.SEE("Too many unnamed parameters encountered while parsing selector (", ")");
		
		final IDataType<?> valueType = unnamedTypes.get(data.unnamedParams.size());
		
		final ArgWrapper<?> value = valueType.parse(parser);
		
		data.unnamedParams.add(value.addToProcess(data.toProcess));
	}
	
	@Override
	public ExParserData newParserData(final Parser parser)
	{
		return new ExParserData(parser);
	}
}
