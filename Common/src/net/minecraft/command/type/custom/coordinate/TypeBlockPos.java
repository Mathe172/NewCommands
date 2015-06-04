package net.minecraft.command.type.custom.coordinate;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.arg.PrimitiveParameter;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.collections.Types;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.CTypeParse;
import net.minecraft.command.type.custom.TypeAlternatives.Typed;
import net.minecraft.util.BlockPos;

public class TypeBlockPos extends CTypeParse<BlockPos>
{
	public static final CDataType<BlockPos> parser = new Typed<>(new TypeBlockPos(), Types.generalType(TypeIDs.BlockPos));
	
	private TypeBlockPos()
	{
	}
	
	@Override
	public ArgWrapper<BlockPos> parse(final Parser parser, final Context parserData) throws SyntaxErrorException, CompletionException
	{
		final Coordinate x = TypeCoordinate.xNC.parse(parser);
		final Coordinate y = TypeCoordinate.yNC.parse(parser);
		final Coordinate z = TypeCoordinate.zNC.parse(parser);
		
		return TypeIDs.BlockPos.wrap(Pos.create(x, y, z));
	}
	
	private static class Pos extends CommandArg<BlockPos>
	{
		private final CommandArg<Double> x;
		private final CommandArg<Double> y;
		private final CommandArg<Double> z;
		
		public Pos(final CommandArg<Double> x, final CommandArg<Double> y, final CommandArg<Double> z)
		{
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		public static CommandArg<BlockPos> create(final Coordinate x, final Coordinate y, final Coordinate z)
		{
			if (x.getValue().isConstant()
				&& y.getValue().isConstant()
				&& z.getValue().isConstant()
				&& !x.isRelative()
				&& !y.isRelative()
				&& !z.isRelative())
				return new PrimitiveParameter<>(
					new BlockPos(
						x.getValue().getConstant(),
						y.getValue().getConstant(),
						z.getValue().getConstant()));
			
			return new Pos(x, y, z);
		}
		
		@Override
		public BlockPos eval(final ICommandSender sender) throws CommandException
		{
			return new BlockPos(this.x.eval(sender), this.y.eval(sender), this.z.eval(sender));
		}
	}
}
