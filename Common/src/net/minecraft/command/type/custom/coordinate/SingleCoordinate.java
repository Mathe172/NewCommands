package net.minecraft.command.type.custom.coordinate;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.Vec3;

public class SingleCoordinate
{
	public static abstract class Centered extends Coordinate
	{
		public Centered(final boolean relative, final CoordValue comp)
		{
			super(relative, comp);
		}
		
		@Override
		public Double evalCoord(final ICommandSender sender, final double base) throws CommandException
		{
			return this.evalCoord(sender, base, true);
		}
	}
	
	public static abstract class NonCentered extends Coordinate
	{
		public NonCentered(final boolean relative, final CoordValue comp)
		{
			super(relative, comp);
		}
		
		@Override
		public Double evalCoord(final ICommandSender sender, final double base) throws CommandException
		{
			return this.evalCoord(sender, base, false);
		}
	}
	
	public static class xCentered extends Centered
	{
		public xCentered(final boolean relative, final CoordValue comp)
		{
			super(relative, comp);
		}
		
		@Override
		public Double evalCoord(final ICommandSender sender, final Vec3 base) throws CommandException
		{
			return this.evalCoord(sender, base.xCoord);
		}
	}
	
	public static class xNonCentered extends NonCentered
	{
		public xNonCentered(final boolean relative, final CoordValue comp)
		{
			super(relative, comp);
		}
		
		@Override
		public Double evalCoord(final ICommandSender sender, final Vec3 base) throws CommandException
		{
			return this.evalCoord(sender, base.xCoord);
		}
	}
	
	public static class yCentered extends Centered
	{
		public yCentered(final boolean relative, final CoordValue comp)
		{
			super(relative, comp);
		}
		
		@Override
		public Double evalCoord(final ICommandSender sender, final Vec3 base) throws CommandException
		{
			return this.evalCoord(sender, base.yCoord);
		}
	}
	
	public static class yNonCentered extends NonCentered
	{
		public yNonCentered(final boolean relative, final CoordValue comp)
		{
			super(relative, comp);
		}
		
		@Override
		public Double evalCoord(final ICommandSender sender, final Vec3 base) throws CommandException
		{
			return this.evalCoord(sender, base.yCoord);
		}
	}
	
	public static class zCentered extends Centered
	{
		public zCentered(final boolean relative, final CoordValue comp)
		{
			super(relative, comp);
		}
		
		@Override
		public Double evalCoord(final ICommandSender sender, final Vec3 base) throws CommandException
		{
			return this.evalCoord(sender, base.zCoord);
		}
	}
	
	public static class zNonCentered extends NonCentered
	{
		public zNonCentered(final boolean relative, final CoordValue comp)
		{
			super(relative, comp);
		}
		
		@Override
		public Double evalCoord(final ICommandSender sender, final Vec3 base) throws CommandException
		{
			return this.evalCoord(sender, base.zCoord);
		}
	}
	
}
