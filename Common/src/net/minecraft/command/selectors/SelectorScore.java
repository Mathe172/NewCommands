package net.minecraft.command.selectors;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.arg.TypedWrapper.Getter;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.construction.SelectorConstructable;
import net.minecraft.command.descriptors.SelectorDescriptorDefault.DefaultParserData;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.server.MinecraftServer;

public class SelectorScore extends CommandArg<Integer>
{
	public static final SelectorConstructable constructable = new SelectorConstructable()
	{
		@Override
		public ArgWrapper<?> construct(final DefaultParserData parserData) throws SyntaxErrorException
		{
			return TypeIDs.Integer.wrap(
				new SelectorScore(
					getRequiredParam(TypeIDs.ScoreObjective, 0, "o", parserData),
					getParam(TypeIDs.UUID, 1, "t", parserData)));
		}
	};
	
	private final Getter<ScoreObjective> objective;
	private final Getter<String> target;
	
	public SelectorScore(final Getter<ScoreObjective> objective, final Getter<String> target)
	{
		this.objective = objective;
		this.target = target;
	}
	
	@Override
	public Integer eval(final ICommandSender sender) throws CommandException
	{
		return MinecraftServer
			.getServer()
			.worldServerForDimension(0)
			.getScoreboard()
			.getValueFromObjective(
				this.target == null
					? ParsingUtilities.getEntityIdentifier(sender.getCommandSenderEntity())
					: this.target.get(),
				this.objective.get())
			.getScorePoints();
	}
}
