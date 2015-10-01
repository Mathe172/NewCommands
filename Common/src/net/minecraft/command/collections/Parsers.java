package net.minecraft.command.collections;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.base.CompoundType;
import net.minecraft.command.type.custom.ParserName;
import net.minecraft.command.type.custom.TypeList;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.scoreboard.ScoreObjective;

public class Parsers
{
	private Parsers()
	{
	}
	
	public static final CDataType<Integer> defaultedIntMin = Types.defaultedInt(Integer.MIN_VALUE);
	public static final CDataType<Integer> defaultedIntMax = Types.defaultedInt(Integer.MAX_VALUE);
	
	public static final CDataType<Block> blockID = new ParserName.CustomType<>("BlockID", TypeIDs.BlockID, Converters.stringToBlock, true);
	
	public static final CDataType<Double> dbl = new ParserName.CustomType<>(Matchers.doubleMatcher, "double", TypeIDs.Double, Converters.stringToDouble, true);
	
	public static final CDataType<Entity> entity = new ParserName.CustomType<>("UUID/player name", TypeIDs.Entity, Converters.UUIDToEntity);
	
	public static final CDataType<ICommandSender> iCmdSender = new ParserName.CustomType<>("UUID", TypeIDs.ICmdSender, Converters.UUIDToICmdSender);
	
	public static final CDataType<Integer> integer = new ParserName.CustomType<>(Matchers.intMatcher, "int", TypeIDs.Integer, Converters.stringToInt, true);
	
	public static final CDataType<List<Integer>> integerList = new TypeList.GParsed<>(TypeIDs.IntList, integer);
	
	public static final CDataType<Item> itemID = new ParserName.CustomType<>("ItemID", TypeIDs.ItemID, Converters.stringToItem, true);
	
	public static final CDataType<String> uuid = new ParserName("UUID", TypeIDs.UUID);
	
	public static final CDataType<String> scoreHolder = new ParserName(Matchers.sharpMatcher, "UUID or variable name", TypeIDs.UUID);
	
	public static final CDataType<String> scoreHolderWC = new ParserName(Matchers.wildcardMatcher, "UUID, variable name or '*'", TypeIDs.UUID);
	
	public static final CDataType<Boolean> bool = new CompoundType<>(
		new ParserName.CustomType<>("boolean", TypeIDs.Boolean, Converters.stringToBoolean, true),
		Completers.bool);
	
	public static final CDataType<ScoreObjective> scoreObjective = new ParserName.CustomType<>("score name", TypeIDs.ScoreObjective, Converters.StringToObjective);
}
