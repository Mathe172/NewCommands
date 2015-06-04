package net.minecraft.command.type.custom.coordinate;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.arg.PrimitiveParameter;
import net.minecraft.command.type.custom.coordinate.TypeCoordinates.Shift;
import net.minecraft.util.Vec3;

public class CoordinatesShift extends CommandArg<Shift>
{
	public final Coordinate x;
	public final Coordinate y;
	public final Coordinate z;
	
	public CoordinatesShift(final Coordinate x, final Coordinate y, final Coordinate z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	protected static Shift getShift(final Coordinate x, final Coordinate y, final Coordinate z, final Vec3 shift)
	{
		return new Shift()
		{
			@Override
			public boolean xRelative()
			{
				return x.isRelative();
			}
			
			@Override
			public boolean yRelative()
			{
				return y.isRelative();
			}
			
			@Override
			public boolean zRelative()
			{
				return z.isRelative();
			}
			
			@Override
			public Vec3 getShiftValues()
			{
				return shift;
			}
			
			@Override
			public Vec3 addBase(final Vec3 base) throws CommandException
			{
				return new Vec3(
					x.addBase(shift.xCoord, base.xCoord),
					y.addBase(shift.yCoord, base.yCoord),
					z.addBase(shift.zCoord, base.zCoord));
			}
		};
	}
	
	public static CommandArg<Shift> create(final Coordinate x, final Coordinate y, final Coordinate z)
	{
		if (x.getValue().isConstant()
			&& y.getValue().isConstant()
			&& z.getValue().isConstant())
			return new PrimitiveParameter<>(
				CoordinatesShift.getShift(x, y, z, new Vec3(
					x.getValue().getConstant(),
					y.getValue().getConstant(),
					z.getValue().getConstant())));
		
		return new CoordinatesShift(x, y, z);
	}
	
	@Override
	public Shift eval(final ICommandSender sender) throws CommandException
	{
		return getShift(this.x, this.y, this.z, new Vec3(
			CoordinatesShift.this.x.eval(sender),
			CoordinatesShift.this.y.eval(sender),
			CoordinatesShift.this.z.eval(sender)));
	}
}