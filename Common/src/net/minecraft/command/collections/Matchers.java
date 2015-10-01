package net.minecraft.command.collections;

import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.commands.CommandScoreboard;
import net.minecraft.command.parser.MatcherRegistry;
import net.minecraft.command.selectors.entity.FilterList;
import net.minecraft.command.type.custom.CompleterResourcePath;
import net.minecraft.command.type.custom.ParserLazyString;
import net.minecraft.command.type.custom.TypeLabelDeclaration;
import net.minecraft.command.type.custom.TypeList;
import net.minecraft.command.type.custom.TypeUntypedOperator;
import net.minecraft.command.type.custom.command.ParserCommand;
import net.minecraft.command.type.custom.coordinate.TypeCoordinateBase;
import net.minecraft.command.type.custom.json.ParserJsonElement;
import net.minecraft.command.type.custom.json.TypeJsonPair;
import net.minecraft.command.type.custom.nbt.NBTUtilities;
import net.minecraft.command.type.custom.nbt.ParserNBTTag;
import net.minecraft.command.type.custom.nbt.TypeNBTPair;

public final class Matchers
{
	public static final MatcherRegistry doubleMatcher = new MatcherRegistry("\\G\\s*+([+-]?+(?=\\.?+\\d)\\d*+\\.?+\\d*+)");
	public static final MatcherRegistry intMatcher = new MatcherRegistry("\\G\\s*+([+-]?+\\d++)");
	public static final MatcherRegistry sharpMatcher = new MatcherRegistry("\\G\\s*+(#?+[\\w\\.:-]++)");
	public static final MatcherRegistry wildcardMatcher = new MatcherRegistry("\\G\\s*+(\\*|#?+[\\w\\.:-]++)");
	public static final MatcherRegistry intDefMatcher = new MatcherRegistry("\\G\\s*+(\\*|[+-]?+\\d++)");
	
	private Matchers()
	{
	}
	
	public static final void init()
	{
		ParsingUtilities.aKeyMatcher.init();
		ParsingUtilities.listEndMatcher.init();
		ParsingUtilities.nameMatcher.init();
		ParsingUtilities.keyMatcher.init();
		ParsingUtilities.endingMatcher.init();
		ParsingUtilities.endingMatcherCompletion.aliasId(ParsingUtilities.endingMatcher);
		ParsingUtilities.idMatcher.init();
		ParsingUtilities.oParenthMatcher.init();
		ParsingUtilities.generalMatcher.init();
		ParsingUtilities.spaceMatcher.init();
		ParsingUtilities.stringMatcher.init();
		ParsingUtilities.quoteMatcher.init();
		ParserLazyString.lazyStringMatcher.init();
		FilterList.inverterMatcher.init();
		Matchers.doubleMatcher.init();
		TypeList.listDelimMatcher.init();
		TypeUntypedOperator.operatorMatcher.init();
		Matchers.intMatcher.init();
		Matchers.intDefMatcher.init();
		ParserCommand.commandNameMatcher.init();
		TypeCoordinateBase.coordMatcher.init();
		TypeNBTPair.keyMatcher.init();
		TypeJsonPair.keyMatcher.init();
		NBTUtilities.numberIDMatcher.init();
		ParserNBTTag.specialMatcher.init();
		ParserNBTTag.baseMatcher.init();
		ParsingUtilities.baseMatcher.init();
		ParsingUtilities.stackedMatcher.init();
		ParserNBTTag.numberMatcher.init();
		Matchers.sharpMatcher.init();
		Matchers.wildcardMatcher.init();
		CommandScoreboard.operationMatcher.init();
		ParsingUtilities.whitespaceMatcher.init();
		CompleterResourcePath.pathMatcher.init();
		TypeLabelDeclaration.labelMatcher.init();
		ParserJsonElement.specialMatcher.init();
		ParserJsonElement.literalMatcher.init();
	}
}
