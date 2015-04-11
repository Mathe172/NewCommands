package net.minecraft.command.type.custom;

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
import net.minecraft.command.type.custom.nbt.TypeNBTArg;
import net.minecraft.command.type.management.TypeID;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;

public class Types
{
	public static final CDataType<String> opName = new CompoundType<>(ParserName.parser, Completers.opName);
	
	public static final CDataType<String> nonOppedOnline = new CompoundType<>(ParserName.parser, Completers.nonOppedOnline);
	
	public static final CDataType<String> name = new CompoundType<>(ParserName.parser, Completers.userCompleter);
	public static final CDataType<List<String>> nameList = new TypeList.GParsed<>(TypeIDs.StringList, name);
	
	public static final CDataType<String> UUID = new CompoundType<>(new ParserUUID(), Completers.userCompleter);
	public static final IParse<CommandArg<String>> IPUUID = ParsingUtilities.unwrap(UUID);
	
	public static final IParse<CommandArg<NBTTagCompound>> IPNBT = ParsingUtilities.unwrap(TypeNBTArg.parserDefault);
	
	public static final CDataType<List<String>> UUIDList = new TypeList.GParsed<>(TypeIDs.UUIDList, UUID);
	public static final IParse<CommandArg<List<String>>> IPUUIDList = ParsingUtilities.unwrap(UUIDList);
	
	public static final CDataType<Entity> entity = new CompoundType<>(new ParserEntity(), Completers.userCompleter);
	
	public static final CDataType<List<Entity>> entityList = new TypeList.GParsed<>(TypeIDs.EntityList, entity);
	public static final IParse<CommandArg<List<Entity>>> IPEntityList = ParsingUtilities.unwrap(entityList);
	
	public static final CDataType<String> teamName = new CompoundType<>(ParserName.parser, Completers.teamName);
	public static final CDataType<List<String>> teamNameList = new TypeList.GParsed<>(TypeIDs.StringList, teamName);
	
	public static final CDataType<String> type = new CompoundType<>(ParserName.parser, Completers.entityID);
	public static final CDataType<List<String>> typeList = new TypeList.GParsed<>(TypeIDs.StringList, type);
	
	public static final CDataType<ICommandSender> iCmdSender = new CompoundType<>(new ParserICmdSender(), Completers.userCompleter);
	public static final CDataType<List<ICommandSender>> iCmdSenderList = new TypeList.GParsed<>(TypeIDs.ICmdSenderList, iCmdSender);
	
	public static final CDataType<String> entityIDWPlayer = new CompoundType<>(ParserName.parser, Completers.entityIDWPlayer);
	public static final CDataType<List<String>> entityIDWPlayerList = new TypeList.GParsed<>(TypeIDs.StringList, entityIDWPlayer);
	
	public static final CDataType<String> blockID = new CompoundType<>(ParserName.parser, Completers.blockCompleter);
	
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
