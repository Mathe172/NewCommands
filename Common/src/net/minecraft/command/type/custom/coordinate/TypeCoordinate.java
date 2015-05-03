package net.minecraft.command.type.custom.coordinate;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.custom.coordinate.TypeCoordinateBase.metaCoordinate;

public class TypeCoordinate
{
	public static final CDataType<Double> parserxC = TypeIDs.Double.wrap(TypeCoordinateBase.parserxC);
	public static final CDataType<Double> parserxNC = TypeIDs.Double.wrap(TypeCoordinateBase.parserxNC);
	public static final CDataType<Double> parseryC = TypeIDs.Double.wrap(TypeCoordinateBase.parseryC);
	public static final CDataType<Double> parseryNC = TypeIDs.Double.wrap(TypeCoordinateBase.parseryNC);
	public static final CDataType<Double> parserzC = TypeIDs.Double.wrap(TypeCoordinateBase.parserzC);
	public static final CDataType<Double> parserzNC = TypeIDs.Double.wrap(TypeCoordinateBase.parserzNC);
	
	public static final CDataType<Double> parserMetaC = TypeIDs.Double.wrap(TypeCoordinateBase.parserMetaC);
	public static final CDataType<Double> parserMetaNC = TypeIDs.Double.wrap(TypeCoordinateBase.parserMetaNC);
	
	public static abstract class Shift
	{
		public boolean relative()
		{
			return false;
		}
		
		public double getShiftValue()
		{
			return 0.;
		}
		
		public abstract double addBase(final double base) throws CommandException;
	}
	
	public static Shift getShift(final CommandArg<Double> arg, final ICommandSender sender) throws CommandException
	{
		if (arg == null)
			return new Shift()
			{
				@Override
				public boolean relative()
				{
					return true;
				}
				
				@Override
				public double addBase(final double base) throws CommandException
				{
					return base;
				}
			};
		
		final double shift = arg.eval(sender);
		if (arg instanceof metaCoordinate)
		{
			final metaCoordinate argMeta = (metaCoordinate) arg;
			return new Shift()
			{
				@Override
				public boolean relative()
				{
					return argMeta.isRelative();
				}
				
				@Override
				public double getShiftValue()
				{
					return shift;
				}
				
				@Override
				public double addBase(final double base) throws CommandException
				{
					return argMeta.addBase(shift, base);
				}
			};
		}
		else
			return new Shift()
			{
				@Override
				public double getShiftValue()
				{
					return shift;
				}
				
				@Override
				public double addBase(final double base) throws CommandException
				{
					return shift;
				}
				
			};
	}
}
