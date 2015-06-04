package net.minecraft.command.type.custom.coordinate;

import net.minecraft.command.CommandException;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.arg.PrimitiveParameter;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.collections.Types;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.CTypeParse;
import net.minecraft.command.type.custom.TypeAlternatives;
import net.minecraft.util.Vec3;

public abstract class TypeCoordinates
{
	public static final CDataType<Vec3> centered = new TypeAlternatives.Typed<>(new Centered(), Types.generalType(TypeIDs.Coordinates));
	public static final CDataType<Vec3> nonCentered = new TypeAlternatives.Typed<>(new NonCentered(), Types.generalType(TypeIDs.Coordinates));
	
	public static final CDataType<Shift> shiftC = new TypeAlternatives.Typed<>(new shiftC(), Types.generalType(TypeIDs.Shift));
	public static final CDataType<Shift> shiftNC = new TypeAlternatives.Typed<>(new shiftNC(), Types.generalType(TypeIDs.Shift));
	
	/**
	 * y-Coordinate NOT centered
	 */
	private static class Centered extends CTypeParse<Vec3>
	{
		@Override
		public ArgWrapper<Vec3> parse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
		{
			final Coordinate x = TypeCoordinate.xC.parse(parser);
			final Coordinate y = TypeCoordinate.yNC.parse(parser);
			final Coordinate z = TypeCoordinate.zC.parse(parser);
			
			return TypeIDs.Coordinates.wrap(Coordinates.create(x, y, z));
		}
	}
	
	private static class NonCentered extends CTypeParse<Vec3>
	{
		@Override
		public ArgWrapper<Vec3> parse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
		{
			final Coordinate x = TypeCoordinate.xNC.parse(parser);
			final Coordinate y = TypeCoordinate.yNC.parse(parser);
			final Coordinate z = TypeCoordinate.zNC.parse(parser);
			
			return TypeIDs.Coordinates.wrap(Coordinates.create(x, y, z));
		}
	}
	
	/**
	 * y-Coordinate NOT centered
	 */
	private static class shiftC extends CTypeParse<Shift>
	{
		@Override
		public ArgWrapper<Shift> parse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
		{
			final Coordinate x = TypeCoordinate.shiftXC.parse(parser);
			final Coordinate y = TypeCoordinate.shiftYNC.parse(parser);
			final Coordinate z = TypeCoordinate.shiftZC.parse(parser);
			
			return TypeIDs.Shift.wrap(CoordinatesShift.create(x, y, z));
		}
	}
	
	private static class shiftNC extends CTypeParse<Shift>
	{
		@Override
		public ArgWrapper<Shift> parse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
		{
			final Coordinate x = TypeCoordinate.shiftXNC.parse(parser);
			final Coordinate y = TypeCoordinate.shiftYNC.parse(parser);
			final Coordinate z = TypeCoordinate.shiftZNC.parse(parser);
			
			return TypeIDs.Shift.wrap(CoordinatesShift.create(x, y, z));
		}
	}
	
	public static final CommandArg<Shift> trivialShift = new PrimitiveParameter<Shift>(new Shift()
	{
		@Override
		public boolean xRelative()
		{
			return true;
		}
		
		@Override
		public boolean yRelative()
		{
			return true;
		}
		
		@Override
		public boolean zRelative()
		{
			return true;
		}
		
		private final Vec3 zeroVec = new Vec3(0, 0, 0);
		
		@Override
		public Vec3 getShiftValues()
		{
			return this.zeroVec;
		}
		
		@Override
		public Vec3 addBase(final Vec3 base)
		{
			return base;
		}
		
	});
	
	public static interface Shift
	{
		public boolean xRelative();
		
		public boolean yRelative();
		
		public boolean zRelative();
		
		public Vec3 getShiftValues();
		
		public Vec3 addBase(final Vec3 base) throws CommandException;
	}
}
