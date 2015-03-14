package net.minecraft.command.type.custom;

import java.util.List;

import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.IParse;
import net.minecraft.command.type.base.CompoundType;
import net.minecraft.entity.Entity;

public class Types
{
	public static final CDataType<String> opName = new CompoundType<>(ParserName.parser, Completers.opName);
	
	public static final CDataType<String> nonOppedOnline = new CompoundType<>(ParserName.parser, Completers.nonOppedOnline);
	
	public static final CDataType<String> name = new CompoundType<>(ParserName.parser, Completers.userCompleter);
	
	public static final CDataType<String> UUID = new CompoundType<>(ParserName.parser, Completers.userCompleter);
	public static final IParse<CommandArg<String>> IPUUID = ParsingUtilities.unwrap(UUID);
	
	public static final CDataType<List<String>> UUIDList = new TypeList.GParsed<>(TypeIDs.UUIDList, name);
	public static final IParse<CommandArg<List<String>>> IPUUIDList = ParsingUtilities.unwrap(UUIDList);
	
	public static final CDataType<Entity> entity = new CompoundType<>(ParserEntity.parser, Completers.userCompleter);
	
	public static final CDataType<List<Entity>> entityList = new TypeList.GParsed<>(TypeIDs.EntityList, entity);
	public static final IParse<CommandArg<List<Entity>>> IPEntityList = ParsingUtilities.unwrap(entityList);
	
}
