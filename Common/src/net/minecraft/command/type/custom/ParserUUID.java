package net.minecraft.command.type.custom;

import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.parser.MatcherRegistry;
import net.minecraft.command.type.CDataType;

public final class ParserUUID
{
	private ParserUUID()
	{
	}
	
	public static final MatcherRegistry sharpMatcher = new MatcherRegistry("\\G\\s*+(#?+[\\w\\.:-]++)");
	public static final MatcherRegistry wildcardMatcher = new MatcherRegistry("\\G\\s*+(\\*|#?+[\\w\\.:-]++)");
	
	public static final CDataType<String> parser = new ParserName("UUID", TypeIDs.UUID);
	public static final CDataType<String> scoreHolder = new ParserName(sharpMatcher, "UUID or variable name", TypeIDs.UUID);
	public static final CDataType<String> scoreHolderWC = new ParserName(wildcardMatcher, "UUID, variable name or '*'", TypeIDs.UUID);
}
