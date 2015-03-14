package net.minecraft.command.type.custom.coordinate;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.CTypeParse;
import net.minecraft.command.type.custom.TypeIDs;
import net.minecraft.util.Vec3;

public abstract class ParserCoordinates
{
	public static final CDataType<Vec3> centered = new Centered();
	public static final CDataType<Vec3> nonCentered = new NonCentered();
	
	public static class Centered extends CTypeParse<Vec3>
	{
		@Override
		public ArgWrapper<Vec3> parse(final Parser parser) throws SyntaxErrorException, CompletionException
		{
			// These parsers always return something of type Coordinate
			final Coordinate x = (Coordinate) ParserCoordinate.parserxC.parse(parser).arg;
			final Coordinate y = (Coordinate) ParserCoordinate.parseryC.parse(parser).arg;
			final Coordinate z = (Coordinate) ParserCoordinate.parserzC.parse(parser).arg;
			
			return new ArgWrapper<>(TypeIDs.Coordinates, new Coordinates(x, y, z));
		}
	}
	
	public static class NonCentered extends CTypeParse<Vec3>
	{
		@Override
		public ArgWrapper<Vec3> parse(final Parser parser) throws SyntaxErrorException, CompletionException
		{
			// These parsers always return something of type Coordinate
			final Coordinate x = (Coordinate) ParserCoordinate.parserxNC.parse(parser).arg;
			final Coordinate y = (Coordinate) ParserCoordinate.parseryNC.parse(parser).arg;
			final Coordinate z = (Coordinate) ParserCoordinate.parserzNC.parse(parser).arg;
			
			return new ArgWrapper<>(TypeIDs.Coordinates, new Coordinates(x, y, z));
		}
	}
}
