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
	private final boolean centerBlock;
	
	public Coordinate(final CoordValue comp, final boolean relative, final boolean centerBlock)
	{
		this.comp = comp;
		this.relative = relative;
		this.centerBlock = centerBlock;
	}
	
	public final Double evalCoord(final ICommandSender sender, final double base) throws CommandException
	{
		if (this.relative && Double.isNaN(base))
			throw new NumberInvalidException("commands.generic.num.invalid", new Object[] { Double.valueOf(base) });
		
		double result = this.relative ? (this.comp.hasDecimal ? base : Math.floor(base)) : 0.;
		
		if (!this.comp.hasDecimal && this.centerBlock) // && !this.relative
			result += 0.5;
		
		result += this.comp.eval(sender);
		
		if (result < -30000000.)
			throw new NumberInvalidException("commands.generic.double.tooSmall", new Object[] { Double.valueOf(result), Integer.valueOf(-30000000) });
		
		if (result > 30000000.)
			throw new NumberInvalidException("commands.generic.double.tooBig", new Object[] { Double.valueOf(result), Integer.valueOf(30000000) });
		
		return result;
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
