package net.minecraft.command.type.custom.coordinate;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.CTypeParse;
import net.minecraft.command.type.custom.TypeAlternatives;
import net.minecraft.command.type.custom.TypeIDs;
import net.minecraft.command.type.custom.Types;
import net.minecraft.util.Vec3;

public abstract class TypeCoordinates
{
	public static final CDataType<Vec3> centered = new TypeAlternatives.Typed<>(new Centered(), Types.generalType(TypeIDs.Coordinates));
	public static final CDataType<Vec3> nonCentered = new TypeAlternatives.Typed<>(new NonCentered(), Types.generalType(TypeIDs.Coordinates));
	
	private static class Centered extends CTypeParse<Vec3>
	{
		@Override
		public ArgWrapper<Vec3> parse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
		{
			final CommandArg<Double> x = TypeCoordinate.parserxC.parse(parser).arg;
			final CommandArg<Double> y = TypeCoordinate.parseryC.parse(parser).arg;
			final CommandArg<Double> z = TypeCoordinate.parserzC.parse(parser).arg;
			
			return TypeIDs.Coordinates.wrap(new Coordinates(x, y, z));
		}
	}
	
	private static class NonCentered extends CTypeParse<Vec3>
	{
		@Override
		public ArgWrapper<Vec3> parse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
		{
			final CommandArg<Double> x = TypeCoordinate.parserxNC.parse(parser).arg;
			final CommandArg<Double> y = TypeCoordinate.parseryNC.parse(parser).arg;
			final CommandArg<Double> z = TypeCoordinate.parserzNC.parse(parser).arg;
			
			return TypeIDs.Coordinates.wrap(new Coordinates(x, y, z));
		}
	}
}
