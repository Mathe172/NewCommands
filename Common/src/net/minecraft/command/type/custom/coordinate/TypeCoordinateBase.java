package net.minecraft.command.type.custom.coordinate;

import java.util.regex.Matcher;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.MatcherRegistry;
import net.minecraft.command.SyntaxErrorException;
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
import net.minecraft.command.type.IParse;
import net.minecraft.command.type.base.CustomCompletable;
import net.minecraft.command.type.custom.coordinate.Coordinate.CoordValue;

public abstract class TypeCoordinateBase extends CustomCompletable<Coordinate>
{
	public static final MatcherRegistry coordMatcher = new MatcherRegistry("\\G\\s*+(~)?+(?:([@\\$]|[+-]?+(?=\\.?+\\d)\\d*+(\\.)?+\\d*+))?+");
	public static final ITabCompletion tildeCompletion = new SingleChar('~')
	{
		@Override
		public double weight()
		{
			return 1.0;
		};
	};
	
	public static final IParse<Coordinate> parserxC = new x(true);
	public static final IParse<Coordinate> parserxNC = new x(false);
	public static final IParse<Coordinate> parseryC = new y(true);
	public static final IParse<Coordinate> parseryNC = new y(false);
	public static final IParse<Coordinate> parserzC = new z(true);
	public static final IParse<Coordinate> parserzNC = new z(false);
	
	public static final IParse<Coordinate> parserMetaC = new meta(true);
	public static final IParse<Coordinate> parserMetaNC = new meta(false);
	
	@Override
	public Coordinate iParse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
	{
		final Matcher m = parser.getMatcher(coordMatcher);
		
		parser.findInc(m);
		
		final boolean relative = m.group(1) != null;
		
		final String s = m.group(2);
		
		if (relative)
			parser.terminateCompletion();
		else if (s == null)
			throw parser.SEE("'~' or coordinate expected around index ");
		
		if (s == null)
			return this.coord();
		
		if ("@".equals(s))
			return this.coord(Coordinate.typeCoord.selectorParser.parse(parser), relative);
		
		if ("$".equals(s))
			return this.coord(Coordinate.typeCoord.labelParser.parse(parser), relative);
		
		if (m.group(3) == null)
			return this.coord(new CoordValue(new PrimitiveParameter<>(Double.parseDouble(s)), false), relative);
		
		return this.coord(new CoordValue(new PrimitiveParameter<>(Double.parseDouble(s)), true), relative);
	}
	
	public final boolean center;
	public final Coordinate tildeCoord;
	
	public TypeCoordinateBase(final boolean center, final Coordinate tildeCoord)
	{
		this.center = center;
		this.tildeCoord = tildeCoord;
	}
	
	public Coordinate coord()
	{
		return this.tildeCoord;
	}
	
	public abstract Coordinate coord(CoordValue param, boolean relative);
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
	{
		TabCompletionData.addToSet(tcDataSet, startIndex, cData, tildeCompletion);
	}
	
	public static class metaCoordinate extends Coordinate
	{
		private metaCoordinate(final CoordValue comp, final boolean relative, final boolean centerBlock)
		{
			super(comp, relative, centerBlock);
		}
		
		@Override
		public Double eval(final ICommandSender sender) throws CommandException
		{
			return this.evalShift(sender);
		}
	}
	
	public static class meta extends TypeCoordinateBase
	{
		private meta(final boolean centerBlock)
		{
			super(centerBlock, new metaCoordinate(SingleCoordinate.tildeCoord, true, centerBlock));
		}
		
		@Override
		public Coordinate coord(final CoordValue param, final boolean relative)
		{
			return new metaCoordinate(param, relative, this.center);
		}
	}
	
	public static class x extends TypeCoordinateBase
	{
		public x(final boolean centerBlock)
		{
			super(centerBlock, centerBlock ? SingleCoordinate.tildexC : SingleCoordinate.tildexNC);
		}
		
		@Override
		public Coordinate coord()
		{
			return this.tildeCoord;
		}
		
		@Override
		public Coordinate coord(final CoordValue param, final boolean relative)
		{
			return new SingleCoordinate.x(param, relative, this.center);
		}
		
		@Override
		public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
		{
			super.complete(tcDataSet, parser, startIndex, cData);
			if (cData.hovered != null)
				TabCompletionData.addToSet(tcDataSet, startIndex, cData, new TabCompletion(String.valueOf(cData.hovered.getX())));
		}
	}
	
	public static class y extends TypeCoordinateBase
	{
		public y(final boolean centerBlock)
		{
			super(centerBlock, centerBlock ? SingleCoordinate.tildeyC : SingleCoordinate.tildeyNC);
		}
		
		@Override
		public Coordinate coord()
		{
			return this.tildeCoord;
		}
		
		@Override
		public Coordinate coord(final CoordValue param, final boolean relative)
		{
			return new SingleCoordinate.y(param, relative, this.center);
		}
		
		@Override
		public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
		{
			super.complete(tcDataSet, parser, startIndex, cData);
			if (cData.hovered != null)
				TabCompletionData.addToSet(tcDataSet, startIndex, cData, new TabCompletion(String.valueOf(cData.hovered.getY())));
		}
	}
	
	public static class z extends TypeCoordinateBase
	{
		public z(final boolean centerBlock)
		{
			super(centerBlock, centerBlock ? SingleCoordinate.tildezC : SingleCoordinate.tildezNC);
		}
		
		@Override
		public Coordinate coord()
		{
			return this.tildeCoord;
		}
		
		@Override
		public Coordinate coord(final CoordValue param, final boolean relative)
		{
			return new SingleCoordinate.z(param, relative, this.center);
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
