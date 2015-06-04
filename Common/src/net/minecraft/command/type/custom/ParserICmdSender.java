package net.minecraft.command.type.custom;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.management.Converter;
import net.minecraft.command.type.management.SConverter;
import net.minecraft.entity.Entity;

public final class ParserICmdSender
{
	private ParserICmdSender()
	{
	}
	
	public static final SConverter<Entity, ICommandSender> EntityToICmdSender = Converter.<Entity, ICommandSender> primitiveConverter();
	private static final Converter<String, ICommandSender, CommandException> UUIDToICmdSender = ParserEntity.UUIDToEntity.chain(EntityToICmdSender);
	
	public static final CDataType<ICommandSender> parser = new ParserName.CustomType<>("UUID", TypeIDs.ICmdSender, UUIDToICmdSender);
}
