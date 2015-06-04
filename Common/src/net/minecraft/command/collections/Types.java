package net.minecraft.command.collections;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.CTypeParse;
import net.minecraft.command.type.base.CompoundType;
import net.minecraft.command.type.custom.ParserBlockID;
import net.minecraft.command.type.custom.ParserEntity;
import net.minecraft.command.type.custom.ParserICmdSender;
import net.minecraft.command.type.custom.ParserItemID;
import net.minecraft.command.type.custom.ParserName;
import net.minecraft.command.type.custom.ParserUUID;
import net.minecraft.command.type.custom.TypeList;
import net.minecraft.command.type.custom.TypeStringLiteral;
import net.minecraft.command.type.management.TypeID;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;

public final class Types
{
	private Types()
	{
	}
	
	public static final CDataType<String> opName = new CompoundType<>(ParserName.parser, Completers.opName);
	
	public static final CDataType<String> nonOppedOnline = new CompoundType<>(ParserName.parser, Completers.nonOppedOnline);
	
	public static final CDataType<String> name = new CompoundType<>(ParserName.parser, Completers.userCompleter);
	public static final CDataType<List<String>> nameList = new TypeList.GParsed<>(TypeIDs.StringList, name);
	
	public static final CDataType<String> UUID = new CompoundType<>(ParserUUID.parser, Completers.userCompleter);
	public static final CDataType<List<String>> UUIDList = new TypeList.GParsed<>(TypeIDs.UUIDList, UUID);
	
	public static final CDataType<String> scoreHolder = new CompoundType<>(ParserUUID.scoreHolder, Completers.scoreHolder);
	public static final CDataType<List<String>> scoreHolderList = new TypeList.GParsed<>(TypeIDs.UUIDList, scoreHolder);
	
	public static final CDataType<String> scoreHolderWC = new CompoundType<>(ParserUUID.scoreHolderWC, Completers.scoreHolderWC);
	public static final CDataType<List<String>> scoreHolderListWC = new TypeList.GParsed<>(TypeIDs.UUIDList, scoreHolderWC);
	
	public static final CDataType<Entity> entity = new CompoundType<>(ParserEntity.parser, Completers.userCompleter);
	public static final CDataType<List<Entity>> entityList = new TypeList.GParsed<>(TypeIDs.EntityList, entity);
	
	public static final CDataType<String> teamName = new CompoundType<>(new ParserName("team name"), Completers.teamName);
	public static final CDataType<List<String>> teamNameList = new TypeList.GParsed<>(TypeIDs.StringList, teamName);
	
	public static final CDataType<String> entityID = new CompoundType<>(ParserName.parser, Completers.entityID);
	public static final CDataType<List<String>> entityIDList = new TypeList.GParsed<>(TypeIDs.StringList, entityID);
	
	public static final CDataType<ICommandSender> ICmdSender = new CompoundType<>(ParserICmdSender.parser, Completers.userCompleter);
	public static final CDataType<List<ICommandSender>> iCmdSenderList = new TypeList.GParsed<>(TypeIDs.ICmdSenderList, ICmdSender);
	
	public static final CDataType<String> entityIDWPlayer = new CompoundType<>(ParserName.parser, Completers.entityIDWPlayer);
	public static final CDataType<List<String>> entityIDWPlayerList = new TypeList.GParsed<>(TypeIDs.StringList, entityIDWPlayer);
	
	public static final CDataType<Block> blockID = new CompoundType<>(ParserBlockID.parser, Completers.blockCompleter);
	public static final CDataType<IBlockState> blockState = generalType(TypeIDs.BlockState);
	
	public static final CDataType<String> stringBoolean = new TypeStringLiteral("true", "false");
	
	public static final CDataType<Item> itemID = new CompoundType<>(ParserItemID.parser, Completers.itemCompleter);
	
	public static final <T> CDataType<T> generalType(final TypeID<T> target)
	{
		return new CTypeParse<T>()
		{
			@Override
			public ArgWrapper<T> parse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
			{
				final ArgWrapper<T> ret = context.generalParse(parser, target);
				
				if (ret != null)
					return ret;
				
				throw parser.SEE("Could not find any selector or label ");
			}
		};
	}
}
