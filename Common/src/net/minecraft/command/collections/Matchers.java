package net.minecraft.command.collections;

import static net.minecraft.command.ParsingUtilities.aKeyMatcher;
import static net.minecraft.command.ParsingUtilities.endingMatcher;
import static net.minecraft.command.ParsingUtilities.endingMatcherCompletion;
import static net.minecraft.command.ParsingUtilities.escapedMatcher;
import static net.minecraft.command.ParsingUtilities.generalMatcher;
import static net.minecraft.command.ParsingUtilities.idMatcher;
import static net.minecraft.command.ParsingUtilities.keyMatcher;
import static net.minecraft.command.ParsingUtilities.listEndMatcher;
import static net.minecraft.command.ParsingUtilities.nameMatcher;
import static net.minecraft.command.ParsingUtilities.oParenthMatcher;
import static net.minecraft.command.ParsingUtilities.quoteMatcher;
import static net.minecraft.command.ParsingUtilities.spaceMatcher;
import static net.minecraft.command.ParsingUtilities.stringMatcher;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.commands.CommandScoreboard;
import net.minecraft.command.selectors.entity.FilterList;
import net.minecraft.command.type.custom.CompleterResourcePath;
import net.minecraft.command.type.custom.ParserDouble;
import net.minecraft.command.type.custom.ParserInt;
import net.minecraft.command.type.custom.ParserLazyString;
import net.minecraft.command.type.custom.ParserUUID;
import net.minecraft.command.type.custom.TypeLabelDeclaration;
import net.minecraft.command.type.custom.TypeList;
import net.minecraft.command.type.custom.TypeSayString;
import net.minecraft.command.type.custom.TypeUntypedOperator;
import net.minecraft.command.type.custom.coordinate.TypeCoordinateBase;
import net.minecraft.command.type.custom.nbt.NBTPair;
import net.minecraft.command.type.custom.nbt.NBTUtilities;
import net.minecraft.command.type.custom.nbt.ParserNBTTag;

public final class Matchers
{
	private Matchers()
	{
	}
	
	public static final void init()
	{
		aKeyMatcher.init();
		listEndMatcher.init();
		nameMatcher.init();
		keyMatcher.init();
		endingMatcher.init();
		endingMatcherCompletion.aliasId(endingMatcher);
		idMatcher.init();
		oParenthMatcher.init();
		generalMatcher.init();
		spaceMatcher.init();
		stringMatcher.init();
		escapedMatcher.init();
		quoteMatcher.init();
		ParserLazyString.lazyStringMatcher.init();
		FilterList.inverterMatcher.init();
		ParserDouble.doubleMatcher.init();
		TypeList.listDelimMatcher.init();
		TypeSayString.sayStringMatcher.init();
		TypeUntypedOperator.operatorMatcher.init();
		ParserInt.intMatcher.init();
		ParserInt.Defaulted.intDefMatcher.init();
		TypeCoordinateBase.coordMatcher.init();
		NBTPair.nbtKeyMatcher.init();
		NBTUtilities.numberIDMatcher.init();
		ParserNBTTag.specialMatcher.init();
		ParserNBTTag.baseMatcher.init();
		ParsingUtilities.baseMatcher.init();
		ParsingUtilities.stackedMatcher.init();
		ParserNBTTag.numberMatcher.init();
		ParserUUID.sharpMatcher.init();
		ParserUUID.wildcardMatcher.init();
		CommandScoreboard.operationMatcher.init();
		ParsingUtilities.whitespaceMatcher.init();
		CompleterResourcePath.pathMatcher.init();
		TypeLabelDeclaration.labelMatcher.init();
	}
}
