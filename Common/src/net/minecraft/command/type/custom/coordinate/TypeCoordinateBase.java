package net.minecraft.command.type.custom.coordinate;

import java.util.regex.Matcher;

import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletion.SingleChar;
import net.minecraft.command.completion.TabCompletionData;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.MatcherRegistry;
import net.minecraft.command.parser.Parser;
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
	
	@Override
	public Coordinate iParse(final Parser parser, final Context context) throws SyntaxErrorException
	{
		final Matcher m = parser.getMatcher(coordMatcher);
		
		parser.findInc(m);
		
		final boolean relative = m.group(1) != null;
		
		final String s = m.group(2);
		
		if (relative)
			ParsingUtilities.terminateCompletion(parser);
		else if (s == null)
			throw parser.SEE("'~' or coordinate expected ");
		
		if (s == null)
			return this.coord();
		
		if ("@".equals(s))
			return this.coord(Coordinate.typeCoord.selectorParser.parse(parser), relative);
		
		if ("$".equals(s))
			return this.coord(Coordinate.typeCoord.labelParser.parse(parser), relative);
		
		if (m.group(3) == null)
			return this.coord(new CoordValue.Constant(Double.parseDouble(s), false), relative);
		
		return this.coord(new CoordValue.Constant(Double.parseDouble(s), true), relative);
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
}
