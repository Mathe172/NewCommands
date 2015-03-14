package net.minecraft.command.selectors;

import java.util.List;
import java.util.Map;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.construction.SelectorConstructable;
import net.minecraft.command.descriptors.SelectorDescriptor;
import net.minecraft.command.type.custom.TypeIDs;

public class SelectorTiming extends CommandArg<Integer>
{
	public static final SelectorConstructable constructable = new SelectorConstructable()
	{
		@Override
		public ArgWrapper<Integer> construct(final List<ArgWrapper<?>> unnamedParams, final Map<String, ArgWrapper<?>> namedParams) throws SyntaxErrorException
		{
			return new ArgWrapper<>(TypeIDs.Integer, new SelectorTiming(SelectorDescriptor.getRequiredParam(TypeIDs.Integer, 0, "cmd", unnamedParams, namedParams)));
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