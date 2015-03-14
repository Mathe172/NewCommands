package net.minecraft.command.type.custom.coordinate;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.util.Vec3;

public class Coordinates extends CommandArg<Vec3>
{
	private final Coordinate x;
	private final Coordinate y;
	private final Coordinate z;
	
	public Coordinates(final Coordinate x, final Coordinate y, final Coordinate z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vec3 eval(final ICommandSender sender, final ICommandSender origin) throws CommandException
	{
		final Vec3 base = origin.getPositionVector();
		return new Vec3(this.x.evalCoord(sender, base), this.y.evalCoord(sender, base), this.z.evalCoord(sender, base));
	}
	
	@Override
	public Vec3 eval(final ICommandSender sender) throws CommandException
	{
		return this.eval(sender, sender);
	}
}
