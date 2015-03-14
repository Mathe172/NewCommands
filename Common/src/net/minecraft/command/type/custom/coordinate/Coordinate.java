package net.minecraft.command.type.custom.coordinate;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.util.Vec3;

public abstract class Coordinate extends CommandArg<Double>
{
	public abstract static class CoordValue extends CommandArg<Double>
	{
		public final CommandArg<Double> param;
		
		public CoordValue(final CommandArg<Double> param)
		{
			this.param = param;
		}
		
		public abstract boolean hasDecimal();
		
		@Override
		public Double eval(final ICommandSender sender) throws CommandException
		{
			return this.param.eval(sender);
		}
	}
	
	public static class CoordValueDec extends CoordValue
	{
		public CoordValueDec(final CommandArg<Double> param)
		{
			super(param);
		}
		
		@Override
		public boolean hasDecimal()
		{
			return true;
		}
	}
	
	public static class CoordValueInt extends CoordValue
	{
		public CoordValueInt(final CommandArg<Double> param)
		{
			super(param);
		}
		
		@Override
		public boolean hasDecimal()
		{
			return false;
		}
	}
	
	private final CoordValue comp;
	
	private final boolean relative;
	
	public Coordinate(final boolean relative, final CoordValue comp)
	{
		this.comp = comp;
		this.relative = relative;
	}
	
	public abstract Double evalCoord(final ICommandSender sender, final double base) throws CommandException;
	
	public abstract Double evalCoord(final ICommandSender sender, final Vec3 base) throws CommandException;
	
	@Override
	public Double eval(final ICommandSender sender) throws CommandException
	{
		return this.evalCoord(sender, sender.getPositionVector());
	}
	
	public final Double evalCoord(final ICommandSender sender, final double base, final boolean centerBlock) throws CommandException
	{
		if (this.relative && Double.isNaN(base))
			throw new NumberInvalidException("commands.generic.num.invalid", new Object[] { Double.valueOf(base) });
		
		double result = this.relative ? (this.comp.hasDecimal() ? base : Math.floor(base)) : 0.;
		
		if (!this.comp.hasDecimal() && centerBlock) // && !this.relative
			result += 0.5;
		
		result += this.comp.param.eval(sender);
		
		if (result < -30000000.)
			throw new NumberInvalidException("commands.generic.double.tooSmall", new Object[] { Double.valueOf(result), Integer.valueOf(-30000000) });
		
		if (result > 30000000.)
			throw new NumberInvalidException("commands.generic.double.tooBig", new Object[] { Double.valueOf(result), Integer.valueOf(30000000) });
		
		return result;
	}
}
