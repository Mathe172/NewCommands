package net.minecraft.command.type.custom.coordinate;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.type.management.CConvertableUnwrapped;

public abstract class Coordinate extends CommandArg<Double>
{
	public static final CConvertableUnwrapped<CoordValue> typeCoord = new CConvertableUnwrapped<>("CoordValue");
	
	public static class CoordValue extends CommandArg<Double>
	{
		private final CommandArg<Double> param;
		public final boolean hasDecimal;
		
		public CoordValue(final CommandArg<Double> param, final boolean hasDecimal)
		{
			this.param = param;
			this.hasDecimal = hasDecimal;
		}
		
		@Override
		public Double eval(final ICommandSender sender) throws CommandException
		{
			return this.param.eval(sender);
		}
	}
	
	private final CoordValue comp;
	private final boolean relative;
	private final boolean center;
	
	public Coordinate(final CoordValue comp, final boolean relative, final boolean tryCenter)
	{
		this.comp = comp;
		this.relative = relative;
		this.center = !this.comp.hasDecimal && tryCenter;
	}
	
	public final Double evalCoord(final ICommandSender sender, final double base) throws CommandException
	{
		return this.addBase(this.evalShift(sender), base);
	}
	
	public double evalShift(final ICommandSender sender) throws CommandException
	{
		return (!this.relative && this.center ? 0.5 : 0.) + this.comp.eval(sender);
	}
	
	public void validate(final double result) throws CommandException
	{
		if (result < -30000000.)
			throw new NumberInvalidException("commands.generic.double.tooSmall", result, -30000000);
		
		if (result > 30000000.)
			throw new NumberInvalidException("commands.generic.double.tooBig", result, 30000000);
	}
	
	public double addBase(final double shift, final double base) throws CommandException
	{
		if (this.relative && Double.isNaN(base))
			throw new NumberInvalidException("commands.generic.num.invalid", base);
		
		final double ret = (this.relative ? base : 0.) + shift;
		
		this.validate(ret);
		
		return ret;
	}
	
	public final boolean isRelative()
	{
		return this.relative;
	}
	
	public final CoordValue getValue()
	{
		return this.comp;
	}
}
