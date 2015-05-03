package net.minecraft.command.collections;

import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.CTypeParse;
import net.minecraft.command.type.IParse;
import net.minecraft.command.type.base.CompoundType;
import net.minecraft.command.type.custom.ParserEntity;
import net.minecraft.command.type.custom.ParserICmdSender;
import net.minecraft.command.type.custom.ParserName;
import net.minecraft.command.type.custom.ParserUUID;
import net.minecraft.command.type.custom.TypeList;
import net.minecraft.command.type.custom.TypeStringLiteral;
import net.minecraft.command.type.custom.nbt.TypeNBTArg;
import net.minecraft.command.type.management.TypeID;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;

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
	
	public static final IParse<CommandArg<String>> IPScoreHolder = ParsingUtilities.unwrap(scoreHolder);
	public static final IParse<CommandArg<List<String>>> IPScoreHolderList = ParsingUtilities.unwrap(scoreHolderList);
	
	public static final CDataType<String> scoreHolderWC = new CompoundType<>(ParserUUID.scoreHolderWC, Completers.scoreHolderWC);
	public static final CDataType<List<String>> scoreHolderListWC = new TypeList.GParsed<>(TypeIDs.UUIDList, scoreHolderWC);
	
	public static final CDataType<Entity> entity = new CompoundType<>(ParserEntity.parser, Completers.userCompleter);
	
	public static final CDataType<List<Entity>> entityList = new TypeList.GParsed<>(TypeIDs.EntityList, entity);
	public static final IParse<CommandArg<List<Entity>>> IPEntityList = ParsingUtilities.unwrap(entityList);
	
	public static final CDataType<String> teamName = new CompoundType<>(new ParserName("team name"), Completers.teamName);
	public static final CDataType<List<String>> teamNameList = new TypeList.GParsed<>(TypeIDs.StringList, teamName);
	
	public static final CDataType<String> entityID = new CompoundType<>(ParserName.parser, Completers.entityID);
	public static final CDataType<List<String>> entityIDList = new TypeList.GParsed<>(TypeIDs.StringList, entityID);
	
	public static final CDataType<ICommandSender> iCmdSender = new CompoundType<>(ParserICmdSender.parser, Completers.userCompleter);
	public static final CDataType<List<ICommandSender>> iCmdSenderList = new TypeList.GParsed<>(TypeIDs.ICmdSenderList, iCmdSender);
	
	public static final CDataType<String> entityIDWPlayer = new CompoundType<>(ParserName.parser, Completers.entityIDWPlayer);
	public static final CDataType<List<String>> entityIDWPlayerList = new TypeList.GParsed<>(TypeIDs.StringList, entityIDWPlayer);
	
	public static final IParse<CommandArg<NBTTagCompound>> IPNBT = ParsingUtilities.unwrap(TypeNBTArg.parserDefault);
	
	public static final CDataType<String> blockID = new CompoundType<>(new ParserName("block ID"), Completers.blockCompleter);
	
	public static final CDataType<String> stringBoolean = new TypeStringLiteral("true", "false");
	
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
				
				throw parser.SEE("Could not find any selector or label around index ");
			}
		};
	}
}
