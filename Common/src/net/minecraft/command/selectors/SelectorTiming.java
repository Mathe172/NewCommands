package net.minecraft.command.selectors;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.construction.SelectorConstructable;
import net.minecraft.command.type.custom.TypeSelectorContent.ParserData;

public class SelectorTiming extends CommandArg<Integer>
{
	public static final SelectorConstructable constructable = new SelectorConstructable()
	{
		@Override
		public ArgWrapper<Integer> construct(final ParserData parserData) throws SyntaxErrorException
		{
			return TypeIDs.Integer.wrap(
				new SelectorTiming(
					getRequiredParam(TypeIDs.Integer, 0, "cmd", parserData)));
		}
		
	};
	
	private final CommandArg<Integer> command;
	
	public SelectorTiming(final CommandArg<Integer> command)
	{
		this.command = command;
	}
	
	@Override
	public Integer eval(final ICommandSender sender) throws CommandException
	{
		final long start = System.nanoTime();
		this.command.eval(sender);
		return (int) ((System.nanoTime() - start) / 1000);
	}
}