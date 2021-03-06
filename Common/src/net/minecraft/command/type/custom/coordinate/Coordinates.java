package net.minecraft.command.type.custom.coordinate;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.arg.PrimitiveParameter;
import net.minecraft.util.Vec3;

public class Coordinates extends CommandArg<Vec3>
{
	public final CommandArg<Double> x;
	public final CommandArg<Double> y;
	public final CommandArg<Double> z;
	
	public Coordinates(final CommandArg<Double> x, final CommandArg<Double> y, final CommandArg<Double> z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public static CommandArg<Vec3> create(final Coordinate x, final Coordinate y, final Coordinate z)
	{
		if (x.getValue().isConstant()
			&& y.getValue().isConstant()
			&& z.getValue().isConstant()
			&& !x.isRelative()
			&& !y.isRelative()
			&& !z.isRelative())
			return new PrimitiveParameter<>(
				new Vec3(
					x.getValue().getConstant(),
					y.getValue().getConstant(),
					z.getValue().getConstant()));
		
		return new Coordinates(x, y, z);
	}
	
	@Override
	public Vec3 eval(final ICommandSender sender) throws CommandException
	{
		return new Vec3(this.x.eval(sender), this.y.eval(sender), this.z.eval(sender));
	}
}
