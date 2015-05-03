package net.minecraft.command.type.custom.coordinate;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
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
	
	public static final CDataType<Vec3> metaC = new TypeAlternatives.Typed<>(new metaC(), Types.generalType(TypeIDs.Coordinates));
	public static final CDataType<Vec3> metaNC = new TypeAlternatives.Typed<>(new metaNC(), Types.generalType(TypeIDs.Coordinates));
	
	/**
	 * y-Coordinate NOT centered
	 */
	private static class Centered extends CTypeParse<Vec3>
	{
		@Override
		public ArgWrapper<Vec3> parse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
		{
			final Coordinate x = TypeCoordinateBase.parserxC.parse(parser);
			final Coordinate y = TypeCoordinateBase.parseryNC.parse(parser);
			final Coordinate z = TypeCoordinateBase.parserzC.parse(parser);
			
			return TypeIDs.Coordinates.wrap(new Coordinates(x, y, z));
		}
	}
	
	private static class NonCentered extends CTypeParse<Vec3>
	{
		@Override
		public ArgWrapper<Vec3> parse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
		{
			final Coordinate x = TypeCoordinateBase.parserxNC.parse(parser);
			final Coordinate y = TypeCoordinateBase.parseryNC.parse(parser);
			final Coordinate z = TypeCoordinateBase.parserzNC.parse(parser);
			
			return TypeIDs.Coordinates.wrap(new Coordinates(x, y, z));
		}
	}
	
	public static class meta extends CommandArg<Vec3>
	{
		public final Coordinate x;
		public final Coordinate y;
		public final Coordinate z;
		
		private meta(final Coordinate x, final Coordinate y, final Coordinate z)
		{
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		@Override
		public Vec3 eval(final ICommandSender sender) throws CommandException
		{
			return new Vec3(this.x.eval(sender), this.y.eval(sender), this.z.eval(sender));
		}
		
		public Vec3 addBase(final Vec3 shift, final Vec3 base) throws CommandException
		{
			return new Vec3(this.x.addBase(shift.xCoord, base.xCoord), this.y.addBase(shift.yCoord, base.yCoord), this.z.addBase(shift.zCoord, base.zCoord));
		}
	}
	
	/**
	 * y-Coordinate NOT centered
	 */
	private static class metaC extends CTypeParse<Vec3>
	{
		@Override
		public ArgWrapper<Vec3> parse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
		{
			final Coordinate x = TypeCoordinateBase.parserMetaC.parse(parser);
			final Coordinate y = TypeCoordinateBase.parserMetaNC.parse(parser);
			final Coordinate z = TypeCoordinateBase.parserMetaC.parse(parser);
			
			return TypeIDs.Coordinates.wrap(new meta(x, y, z));
		}
	}
	
	private static class metaNC extends CTypeParse<Vec3>
	{
		@Override
		public ArgWrapper<Vec3> parse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
		{
			final Coordinate x = TypeCoordinateBase.parserMetaNC.parse(parser);
			final Coordinate y = TypeCoordinateBase.parserMetaNC.parse(parser);
			final Coordinate z = TypeCoordinateBase.parserMetaNC.parse(parser);
			
			return TypeIDs.Coordinates.wrap(new meta(x, y, z));
		}
	}
	
	public static abstract class Shift
	{
		public boolean xRelative()
		{
			return false;
		}
		
		public boolean yRelative()
		{
			return false;
		}
		
		public boolean zRelative()
		{
			return false;
		}
		
		private static final Vec3 zeroVec = new Vec3(0, 0, 0);
		
		public Vec3 getShiftValues()
		{
			return zeroVec;
		}
		
		public abstract Vec3 addBase(final Vec3 base) throws CommandException;
	}
	
	public static Shift getShift(final CommandArg<Vec3> arg, final ICommandSender sender) throws CommandException
	{
		if (arg == null)
			return new Shift()
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
				
				@Override
				public Vec3 addBase(final Vec3 base) throws CommandException
				{
					return base;
				}
				
			};
		
		final Vec3 shift = arg.eval(sender);
		if (arg instanceof meta)
		{
			final meta argMeta = (meta) arg;
			return new Shift()
			{
				@Override
				public boolean xRelative()
				{
					return argMeta.x.isRelative();
				}
				
				@Override
				public boolean yRelative()
				{
					return argMeta.y.isRelative();
				}
				
				@Override
				public boolean zRelative()
				{
					return argMeta.z.isRelative();
				}
				
				@Override
				public Vec3 getShiftValues()
				{
					return shift;
				}
				
				@Override
				public Vec3 addBase(final Vec3 base) throws CommandException
				{
					return argMeta.addBase(shift, base);
				}
			};
		}
		else
			return new Shift()
			{
				@Override
				public Vec3 getShiftValues()
				{
					return shift;
				}
				
				@Override
				public Vec3 addBase(final Vec3 base) throws CommandException
				{
					return shift;
				}
			};
	}
}
