package net.minecraft.command.type.custom.coordinate;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.arg.PrimitiveParameter;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletion;
import net.minecraft.command.completion.TabCompletionData;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.CTypeParse;
import net.minecraft.command.type.IComplete;
import net.minecraft.command.type.IParse;
import net.minecraft.command.type.custom.coordinate.Coordinate.CoordValue;

public class TypeCoordinate
{
	public static final IParse<Coordinate> xC = new TypeCoordinate.x(true);
	public static final IParse<Coordinate> xNC = new TypeCoordinate.x(false);
	public static final IParse<Coordinate> yC = new TypeCoordinate.y(true);
	public static final IParse<Coordinate> yNC = new TypeCoordinate.y(false);
	public static final IParse<Coordinate> zC = new TypeCoordinate.z(true);
	public static final IParse<Coordinate> zNC = new TypeCoordinate.z(false);
	
	public static final IParse<Coordinate> shiftXC = new TypeCoordinate.shift(true, TypeCoordinate.xCompletion);
	public static final IParse<Coordinate> shiftXNC = new TypeCoordinate.shift(false, TypeCoordinate.xCompletion);
	public static final IParse<Coordinate> shiftYC = new TypeCoordinate.shift(true, TypeCoordinate.yCompletion);
	public static final IParse<Coordinate> shiftYNC = new TypeCoordinate.shift(false, TypeCoordinate.yCompletion);
	public static final IParse<Coordinate> shiftZC = new TypeCoordinate.shift(true, TypeCoordinate.zCompletion);
	public static final IParse<Coordinate> shiftZNC = new TypeCoordinate.shift(false, TypeCoordinate.zCompletion);
	public static final IParse<Coordinate> shiftC = new TypeCoordinate.shift(true, null);
	public static final IParse<Coordinate> shiftNC = new TypeCoordinate.shift(false, null);
	
	public static final CDataType<Double> typeXC = TypeIDs.Double.wrap(xC);
	public static final CDataType<Double> typeXNC = TypeIDs.Double.wrap(xNC);
	public static final CDataType<Double> typeYC = TypeIDs.Double.wrap(yC);
	public static final CDataType<Double> typeYNC = TypeIDs.Double.wrap(yNC);
	public static final CDataType<Double> typeZC = TypeIDs.Double.wrap(zC);
	public static final CDataType<Double> typeZNC = TypeIDs.Double.wrap(zNC);
	
	public static final CDataType<SingleShift> typeShiftXC = new TypeShift(shiftXC);
	public static final CDataType<SingleShift> typeShiftXNC = new TypeShift(shiftXNC);
	public static final CDataType<SingleShift> typeShiftYC = new TypeShift(shiftYC);
	public static final CDataType<SingleShift> typeShiftYNC = new TypeShift(shiftYNC);
	public static final CDataType<SingleShift> typeShiftZC = new TypeShift(shiftZC);
	public static final CDataType<SingleShift> typeShiftZNC = new TypeShift(shiftZNC);
	public static final CDataType<SingleShift> typeShiftC = new TypeShift(shiftC);
	public static final CDataType<SingleShift> typeShiftNC = new TypeShift(shiftNC);
	
	private TypeCoordinate()
	{
	}
	
	public static final IComplete xCompletion = new IComplete()
	{
		@Override
		public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
		{
			if (cData.hovered != null)
				TabCompletionData.addToSet(tcDataSet, startIndex, cData, new TabCompletion(String.valueOf(cData.hovered.getX())));
		}
	};
	public static final IComplete yCompletion = new IComplete()
	{
		@Override
		public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
		{
			if (cData.hovered != null)
				TabCompletionData.addToSet(tcDataSet, startIndex, cData, new TabCompletion(String.valueOf(cData.hovered.getY())));
		}
	};
	public static final IComplete zCompletion = new IComplete()
	{
		@Override
		public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
		{
			if (cData.hovered != null)
				TabCompletionData.addToSet(tcDataSet, startIndex, cData, new TabCompletion(String.valueOf(cData.hovered.getZ())));
		}
	};
	
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
			xCompletion.complete(tcDataSet, parser, startIndex, cData);
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
			yCompletion.complete(tcDataSet, parser, startIndex, cData);
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
			zCompletion.complete(tcDataSet, parser, startIndex, cData);
		}
	}
	
	public static class shift extends TypeCoordinateBase
	{
		private final IComplete completion;
		
		public shift(final boolean centerBlock, final IComplete completion)
		{
			super(centerBlock, new SingleCoordinate.shift(SingleCoordinate.tildeCoord, true, centerBlock));
			this.completion = completion;
		}
		
		@Override
		public Coordinate coord(final CoordValue param, final boolean relative)
		{
			return new SingleCoordinate.shift(param, relative, this.center);
		}
		
		@Override
		public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
		{
			super.complete(tcDataSet, parser, startIndex, cData);
			if (this.completion != null)
				this.completion.complete(tcDataSet, parser, startIndex, cData);
		}
	}
	
	public static class TypeShift extends CTypeParse<SingleShift>
	{
		private final IParse<Coordinate> parser;
		
		public TypeShift(final IParse<Coordinate> parser)
		{
			this.parser = parser;
		}
		
		private static SingleShift getShift(final Coordinate coord, final double shift)
		{
			return new SingleShift()
			{
				@Override
				public boolean relative()
				{
					return coord.isRelative();
				}
				
				@Override
				public double getShiftValue()
				{
					return shift;
				}
				
				@Override
				public double addBase(final double base) throws CommandException
				{
					return coord.addBase(shift, base);
				}
			};
		}
		
		@Override
		public ArgWrapper<SingleShift> parse(final Parser parser, final Context parserData) throws SyntaxErrorException, CompletionException
		{
			final Coordinate coord = TypeShift.this.parser.parse(parser, parserData);
			
			return TypeIDs.SingleShift.wrap(
				coord.getValue().isConstant()
					? new PrimitiveParameter<>(getShift(coord, coord.getValue().getConstant()))
					: new CommandArg<SingleShift>()
					{
						@Override
						public SingleShift eval(final ICommandSender sender) throws CommandException
						{
							return getShift(coord, coord.eval(sender));
						}
					});
		}
	}
	
	public static final CommandArg<SingleShift> trivialShift = new PrimitiveParameter<SingleShift>(new SingleShift()
	{
		@Override
		public boolean relative()
		{
			return true;
		}
		
		@Override
		public double getShiftValue()
		{
			return 0;
		}
		
		@Override
		public double addBase(final double base)
		{
			return base;
		}
	});
	
	public static interface SingleShift
	{
		public boolean relative();
		
		public double getShiftValue();
		
		public double addBase(final double base) throws CommandException;
	}
}