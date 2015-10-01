package net.minecraft.command.selectors;

import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.arg.TypedWrapper.Getter;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.construction.SelectorConstructable;
import net.minecraft.command.descriptors.SelectorDescriptorDefault.DefaultParserData;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class SelectorBlock extends CommandArg<IBlockState>
{
	public static final SelectorConstructable constructable = new SelectorConstructable()
	{
		@Override
		public ArgWrapper<?> construct(final DefaultParserData parserData) throws SyntaxErrorException
		{
			return TypeIDs.BlockState.wrap(new SelectorBlock(getParam(TypeIDs.BlockPos, 0, parserData)));
		}
	};
	
	private final Getter<BlockPos> pos;
	
	public SelectorBlock(final Getter<BlockPos> pos)
	{
		this.pos = pos;
	}
	
	@Override
	public IBlockState eval(final ICommandSender sender) throws CommandException
	{
		final BlockPos pos = this.pos == null ? sender.getPosition() : this.pos.get();
		
		final World world = sender.getEntityWorld();
		
		if (!world.isBlockLoaded(pos))
			throw new CommandException("Out of world"); // TODO:...
			
		return world.getBlockState(pos);
	}
}
