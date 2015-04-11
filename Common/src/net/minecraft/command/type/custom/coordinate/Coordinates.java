package net.minecraft.command.type.custom.coordinate;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.util.Vec3;

public class Coordinates extends CommandArg<Vec3>
{
	private final CommandArg<Double> x;
	private final CommandArg<Double> y;
	private final CommandArg<Double> z;
	
	public Coordinates(final CommandArg<Double> x, final CommandArg<Double> y, final CommandArg<Double> z)
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
}
