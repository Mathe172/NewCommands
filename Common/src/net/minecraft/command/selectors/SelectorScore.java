package net.minecraft.command.selectors;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.construction.SelectorConstructable;
import net.minecraft.command.type.custom.TypeIDs;
import net.minecraft.command.type.custom.TypeSelectorContent.ParserData;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.server.MinecraftServer;

public class SelectorScore extends CommandArg<Integer>
{
	public static final SelectorConstructable constructable = new SelectorConstructable()
	{
		@Override
		public ArgWrapper<?> construct(final ParserData parserData) throws SyntaxErrorException
		{
			return TypeIDs.Integer.wrap(
				new SelectorScore(
					ParsingUtilities.getRequiredParam(TypeIDs.ScoreObjective, 0, "objective", parserData),
					ParsingUtilities.getRequiredParam(TypeIDs.UUID, 1, "target", parserData)));
		}
	};
	
	private final CommandArg<ScoreObjective> objective;
	private final CommandArg<String> target;
	
	public SelectorScore(final CommandArg<ScoreObjective> objective, final CommandArg<String> target)
	{
		this.objective = objective;
		this.target = target;
	}
	
	@Override
	public Integer eval(final ICommandSender sender) throws CommandException
	{
		return MinecraftServer.getServer().worldServerForDimension(0).getScoreboard().getValueFromObjective(this.target.eval(sender), this.objective.eval(sender)).getScorePoints();
	}
}
