package net.minecraft.command.type.custom.coordinate;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.arg.PrimitiveParameter;
import net.minecraft.command.type.custom.coordinate.Coordinate.CoordValue;

public class SingleCoordinate
{
	public static final CoordValue tildeCoord = new CoordValue(new PrimitiveParameter<>(0.0), false);
	
	public static final CommandArg<Double> tildexC = new x(tildeCoord, true, true);
	public static final CommandArg<Double> tildexNC = new x(tildeCoord, true, false);
	public static final CommandArg<Double> tildeyC = new y(tildeCoord, true, true);
	public static final CommandArg<Double> tildeyNC = new y(tildeCoord, true, false);
	public static final CommandArg<Double> tildezC = new z(tildeCoord, true, true);
	public static final CommandArg<Double> tildezNC = new z(tildeCoord, true, false);
	
	public static class x extends Coordinate
	{
		
		public x(final CoordValue comp, final boolean relative, final boolean centerBlock)
		{
			super(comp, relative, centerBlock);
		}
		
		@Override
		public Double eval(final ICommandSender sender) throws CommandException
		{
			return this.evalCoord(sender, sender.getPositionVector().xCoord);
		}
	}
	
	public static class y extends Coordinate
	{
		
		public y(final CoordValue comp, final boolean relative, final boolean centerBlock)
		{
			super(comp, relative, centerBlock);
		}
		
		@Override
		public Double eval(final ICommandSender sender) throws CommandException
		{
			return this.evalCoord(sender, sender.getPositionVector().yCoord);
		}
	}
	
	public static class z extends Coordinate
	{
		
		public z(final CoordValue comp, final boolean relative, final boolean centerBlock)
		{
			super(comp, relative, centerBlock);
		}
		
		@Override
		public Double eval(final ICommandSender sender) throws CommandException
		{
			return this.evalCoord(sender, sender.getPositionVector().zCoord);
		}
	}
	
}
