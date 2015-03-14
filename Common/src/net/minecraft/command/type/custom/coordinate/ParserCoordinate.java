package net.minecraft.command.type.custom.coordinate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.PrimitiveParameter;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletion;
import net.minecraft.command.completion.TabCompletionData;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.CTypeCompletable;
import net.minecraft.command.type.custom.TypeIDs;
import net.minecraft.command.type.custom.coordinate.Coordinate.CoordValue;
import net.minecraft.command.type.custom.coordinate.Coordinate.CoordValueDec;
import net.minecraft.command.type.custom.coordinate.Coordinate.CoordValueInt;

public abstract class ParserCoordinate extends CTypeCompletable<Double>
{
	public static final Pattern coordPattern = Pattern.compile("\\G\\s*+(~)?+(?:(@|\\$|[+-]?+(?=\\.?+\\d)\\d*+(\\.)?+\\d*+))?+");
	public static final TabCompletion tildeCompletion = new TabCompletion(Pattern.compile("\\G(\\s*+)(~)?+\\z"), "~", "~");
	public static final CoordValue tildeCoord = new CoordValueInt(new PrimitiveParameter<>(0.0));
	
	public static final CDataType<Double> parserxC = new xCentered();
	public static final CDataType<Double> parserxNC = new xNonCentered();
	public static final CDataType<Double> parseryC = new yCentered();
	public static final CDataType<Double> parseryNC = new yNonCentered();
	public static final CDataType<Double> parserzC = new zCentered();
	public static final CDataType<Double> parserzNC = new zNonCentered();
	
	@Override
	public ArgWrapper<Double> iParse(final Parser parser) throws SyntaxErrorException, CompletionException
	{
		final Matcher m = parser.coordMatcher;
		
		parser.findInc(m);
		
		final boolean relative = m.group(1) != null;
		
		final String s = m.group(2);
		
		if (relative)
			parser.terminateCompletion();
		else if (s == null)
			throw parser.SEE("'~' or coordinate expected around index ");
		
		if (s == null)
			return new ArgWrapper<>(TypeIDs.CoordValue, this.coord());
		
		if ("@".equals(s))
			return new ArgWrapper<>(TypeIDs.CoordValue, this.coord(relative, (CoordValue) TypeIDs.CoordValue.selectorParser.parse(parser).arg));
		
		if ("$".equals(s))
			return new ArgWrapper<>(TypeIDs.CoordValue, this.coord(relative, (CoordValue) TypeIDs.CoordValue.labelParser.parse(parser).arg));
		
		if (m.group(3) == null)
			return new ArgWrapper<>(TypeIDs.CoordValue, this.coord(relative, new CoordValueInt(new PrimitiveParameter<>(Double.parseDouble(s)))));
		
		return new ArgWrapper<>(TypeIDs.CoordValue, this.coord(relative, new CoordValueDec(new PrimitiveParameter<>(Double.parseDouble(s)))));
	}
	
	public abstract Coordinate coord();
	
	public abstract Coordinate coord(boolean relative, CoordValue param);
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
	{
		TabCompletionData.addToSet(tcDataSet, parser.toParse, startIndex, cData, tildeCompletion);
	}
	
	public static class xCentered extends ParserCoordinate
	{
		public static final Coordinate tildeCoord = new SingleCoordinate.xCentered(true, ParserCoordinate.tildeCoord);
		
		@Override
		public Coordinate coord()
		{
			return tildeCoord;
		}
		
		@Override
		public Coordinate coord(final boolean relative, final CoordValue param)
		{
			return new SingleCoordinate.xCentered(relative, param);
		}
		
		@Override
		public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
		{
			super.complete(tcDataSet, parser, startIndex, cData);
			if (cData.hovered != null)
				TabCompletionData.addToSet(tcDataSet, parser.toParse, startIndex, cData, new TabCompletion(String.valueOf(cData.hovered.getX())));
		}
	}
	
	public static class xNonCentered extends ParserCoordinate
	{
		public static final Coordinate tildeCoord = new SingleCoordinate.xNonCentered(true, ParserCoordinate.tildeCoord);
		
		@Override
		public Coordinate coord()
		{
			return tildeCoord;
		}
		
		@Override
		public Coordinate coord(final boolean relative, final CoordValue param)
		{
			return new SingleCoordinate.xNonCentered(relative, param);
		}
		
		@Override
		public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
		{
			super.complete(tcDataSet, parser, startIndex, cData);
			if (cData.hovered != null)
				TabCompletionData.addToSet(tcDataSet, parser.toParse, startIndex, cData, new TabCompletion(String.valueOf(cData.hovered.getX())));
		}
	}
	
	public static class yCentered extends ParserCoordinate
	{
		public static final Coordinate tildeCoord = new SingleCoordinate.yCentered(true, ParserCoordinate.tildeCoord);
		
		@Override
		public Coordinate coord()
		{
			return tildeCoord;
		}
		
		@Override
		public Coordinate coord(final boolean relative, final CoordValue param)
		{
			return new SingleCoordinate.yCentered(relative, param);
		}
		
		@Override
		public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
		{
			super.complete(tcDataSet, parser, startIndex, cData);
			if (cData.hovered != null)
				TabCompletionData.addToSet(tcDataSet, parser.toParse, startIndex, cData, new TabCompletion(String.valueOf(cData.hovered.getY())));
		}
	}
	
	public static class yNonCentered extends ParserCoordinate
	{
		public static final Coordinate tildeCoord = new SingleCoordinate.yNonCentered(true, ParserCoordinate.tildeCoord);
		
		@Override
		public Coordinate coord()
		{
			return tildeCoord;
		}
		
		@Override
		public Coordinate coord(final boolean relative, final CoordValue param)
		{
			return new SingleCoordinate.yNonCentered(relative, param);
		}
		
		@Override
		public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
		{
			super.complete(tcDataSet, parser, startIndex, cData);
			if (cData.hovered != null)
				TabCompletionData.addToSet(tcDataSet, parser.toParse, startIndex, cData, new TabCompletion(String.valueOf(cData.hovered.getY())));
		}
	}
	
	public static class zCentered extends ParserCoordinate
	{
		public static final Coordinate tildeCoord = new SingleCoordinate.zCentered(true, ParserCoordinate.tildeCoord);
		
		@Override
		public Coordinate coord()
		{
			return tildeCoord;
		}
		
		@Override
		public Coordinate coord(final boolean relative, final CoordValue param)
		{
			return new SingleCoordinate.zCentered(relative, param);
		}
		
		@Override
		public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
		{
			super.complete(tcDataSet, parser, startIndex, cData);
			if (cData.hovered != null)
				TabCompletionData.addToSet(tcDataSet, parser.toParse, startIndex, cData, new TabCompletion(String.valueOf(cData.hovered.getZ())));
		}
	}
	
	public static class zNonCentered extends ParserCoordinate
	{
		public static final Coordinate tildeCoord = new SingleCoordinate.zNonCentered(true, ParserCoordinate.tildeCoord);
		
		@Override
		public Coordinate coord()
		{
			return tildeCoord;
		}
		
		@Override
		public Coordinate coord(final boolean relative, final CoordValue param)
		{
			return new SingleCoordinate.zNonCentered(relative, param);
		}
		
		@Override
		public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
		{
			super.complete(tcDataSet, parser, startIndex, cData);
			if (cData.hovered != null)
				TabCompletionData.addToSet(tcDataSet, parser.toParse, startIndex, cData, new TabCompletion(String.valueOf(cData.hovered.getZ())));
		}
	}
}
