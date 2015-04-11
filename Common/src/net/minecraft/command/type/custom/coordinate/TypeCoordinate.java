package net.minecraft.command.type.custom.coordinate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.PrimitiveParameter;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletion;
import net.minecraft.command.completion.TabCompletion.SingleChar;
import net.minecraft.command.completion.TabCompletionData;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.CTypeCompletable;
import net.minecraft.command.type.custom.TypeIDs;
import net.minecraft.command.type.custom.coordinate.Coordinate.CoordValue;

public abstract class TypeCoordinate extends CTypeCompletable<Double>
{
	public static final Pattern coordPattern = Pattern.compile("\\G\\s*+(~)?+(?:([@\\$]|[+-]?+(?=\\.?+\\d)\\d*+(\\.)?+\\d*+))?+");
	public static final ITabCompletion tildeCompletion = new SingleChar('~')
	{
		@Override
		public double weight()
		{
			return 1.0;
		};
	};
	
	public static final CDataType<Double> parserxC = new x(true);
	public static final CDataType<Double> parserxNC = new x(false);
	public static final CDataType<Double> parseryC = new y(true);
	public static final CDataType<Double> parseryNC = new y(false);
	public static final CDataType<Double> parserzC = new z(true);
	public static final CDataType<Double> parserzNC = new z(false);
	
	@Override
	public ArgWrapper<Double> iParse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
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
			return TypeIDs.Double.wrap(this.coord());
		
		if ("@".equals(s))
			return TypeIDs.Double.wrap(this.coord(relative, Coordinate.typeCoord.selectorParser.parse(parser)));
		
		if ("$".equals(s))
			return TypeIDs.Double.wrap(this.coord(relative, Coordinate.typeCoord.labelParser.parse(parser)));
		
		if (m.group(3) == null)
			return TypeIDs.Double.wrap(this.coord(relative, new CoordValue(new PrimitiveParameter<>(Double.parseDouble(s)), false)));
		
		return TypeIDs.Double.wrap(this.coord(relative, new CoordValue(new PrimitiveParameter<>(Double.parseDouble(s)), true)));
	}
	
	public abstract Coordinate coord();
	
	public abstract Coordinate coord(boolean relative, CoordValue param);
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
	{
		TabCompletionData.addToSet(tcDataSet, startIndex, cData, tildeCompletion);
	}
	
	public static class x extends TypeCoordinate
	{
		public final Coordinate tildeCoord;
		
		private final boolean centerBlock;
		
		public x(final boolean centerBlock)
		{
			this.centerBlock = centerBlock;
			this.tildeCoord = new SingleCoordinate.x(SingleCoordinate.tildeCoord, true, centerBlock);
		}
		
		@Override
		public Coordinate coord()
		{
			return this.tildeCoord;
		}
		
		@Override
		public Coordinate coord(final boolean relative, final CoordValue param)
		{
			return new SingleCoordinate.x(param, relative, this.centerBlock);
		}
		
		@Override
		public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
		{
			super.complete(tcDataSet, parser, startIndex, cData);
			if (cData.hovered != null)
				TabCompletionData.addToSet(tcDataSet, startIndex, cData, new TabCompletion(String.valueOf(cData.hovered.getX())));
		}
	}
	
	public static class y extends TypeCoordinate
	{
		public final Coordinate tildeCoord;
		
		private final boolean centerBlock;
		
		public y(final boolean centerBlock)
		{
			this.centerBlock = centerBlock;
			this.tildeCoord = new SingleCoordinate.y(SingleCoordinate.tildeCoord, true, centerBlock);
		}
		
		@Override
		public Coordinate coord()
		{
			return this.tildeCoord;
		}
		
		@Override
		public Coordinate coord(final boolean relative, final CoordValue param)
		{
			return new SingleCoordinate.y(param, relative, this.centerBlock);
		}
		
		@Override
		public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
		{
			super.complete(tcDataSet, parser, startIndex, cData);
			if (cData.hovered != null)
				TabCompletionData.addToSet(tcDataSet, startIndex, cData, new TabCompletion(String.valueOf(cData.hovered.getY())));
		}
	}
	
	public static class z extends TypeCoordinate
	{
		public final Coordinate tildeCoord;
		
		private final boolean centerBlock;
		
		public z(final boolean centerBlock)
		{
			this.centerBlock = centerBlock;
			this.tildeCoord = new SingleCoordinate.z(SingleCoordinate.tildeCoord, true, centerBlock);
		}
		
		@Override
		public Coordinate coord()
		{
			return this.tildeCoord;
		}
		
		@Override
		public Coordinate coord(final boolean relative, final CoordValue param)
		{
			return new SingleCoordinate.z(param, relative, this.centerBlock);
		}
		
		@Override
		public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
		{
			super.complete(tcDataSet, parser, startIndex, cData);
			if (cData.hovered != null)
				TabCompletionData.addToSet(tcDataSet, startIndex, cData, new TabCompletion(String.valueOf(cData.hovered.getZ())));
		}
	}
}
