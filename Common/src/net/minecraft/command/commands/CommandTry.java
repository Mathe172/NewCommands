package net.minecraft.command.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.construction.CommandConstructable;
import net.minecraft.command.descriptors.CommandDescriptor.CParserData;

public class CommandTry extends CommandArg<Integer>
{
	public static final CommandConstructable constructable = new CommandConstructable()
	{
		@Override
		public CommandArg<Integer> construct(final CParserData data) throws SyntaxErrorException
		{
			return new CommandTry(
				data.get(TypeIDs.Integer),
				data.get(TypeIDs.Integer));
		}
	};
	
	private final CommandArg<Integer> toTry;
	private final CommandArg<Integer> fail;
	
	public CommandTry(final CommandArg<Integer> toTry, final CommandArg<Integer> fail)
	{
		this.toTry = toTry;
		this.fail = fail;
	}
	
	@Override
	public Integer eval(final ICommandSender sender) throws CommandException
	{
		try
		{
			return this.toTry.eval(sender);
		} catch (final CommandException e)
		{
			return this.fail.eval(sender);
		}
	}
}
