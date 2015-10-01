package net.minecraft.command.collections;

import net.minecraft.block.Block;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandUtilities;
import net.minecraft.command.EntityNotFoundException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.type.management.CConverter;
import net.minecraft.command.type.management.Converter;
import net.minecraft.command.type.management.SConverter;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.server.MinecraftServer;

public class Converters
{
	private Converters()
	{
	}
	
	public static final CConverter<Block, String> blockToString = new CConverter<Block, String>()
	{
		@Override
		public String convert(final Block toConvert) throws CommandException
		{
			return toConvert.getName();
		}
	};
	
	public static final CConverter<String, Block> stringToBlock = new CConverter<String, Block>()
	{
		@Override
		public Block convert(final String toConvert) throws CommandException
		{
			return CommandUtilities.getBlockByText(toConvert);
		}
	};
	
	public static final CConverter<String, Double> stringToDouble = new CConverter<String, Double>()
	{
		@Override
		public Double convert(final String toConvert) throws CommandException
		{
			try
			{
				return new Double(toConvert);
			} catch (final NumberFormatException e)
			{
				throw new NumberInvalidException("Cannot convert " + toConvert + " to double");
			}
		}
	};
	
	public static final CConverter<String, Entity> UUIDToEntity = new CConverter<String, Entity>()
	{
		@Override
		public Entity convert(final String toConvert) throws EntityNotFoundException
		{
			final Entity ret = ParsingUtilities.entiyFromIdentifier(toConvert);
			
			if (ret == null)
				throw new EntityNotFoundException("commands.generic.entity.invalidUuid");
			
			return ret;
		}
	};
	
	public static final SConverter<Entity, ICommandSender> EntityToICmdSender = Converter.<Entity, ICommandSender> primitiveConverter();
	
	public static final Converter<String, ICommandSender, CommandException> UUIDToICmdSender = UUIDToEntity.chain(EntityToICmdSender);
	
	public static final CConverter<String, Integer> stringToInt = new CConverter<String, Integer>()
	{
		@Override
		public Integer convert(final String toConvert) throws CommandException
		{
			try
			{
				return new Integer(toConvert);
			} catch (final NumberFormatException e)
			{
				throw new NumberInvalidException("Cannot convert " + toConvert + " to int");
			}
		}
	};
	
	public static final CConverter<String, Item> stringToItem = new CConverter<String, Item>()
	{
		@Override
		public Item convert(final String toConvert) throws CommandException
		{
			return CommandUtilities.getItemByText(toConvert);
		}
	};
	
	public static final CConverter<String, Boolean> stringToBoolean = new CConverter<String, Boolean>()
	{
		@Override
		public Boolean convert(final String toConvert) throws CommandException
		{
			if (ParsingUtilities.isTrue(toConvert))
				return true;
			if (ParsingUtilities.isFalse(toConvert))
				return false;
			
			throw new NumberInvalidException("'" + toConvert + "' cannot be converted to boolean");
		}
	};
	
	public static final CConverter<String, ScoreObjective> StringToObjective = new CConverter<String, ScoreObjective>()
	{
		@Override
		public ScoreObjective convert(final String toConvert) throws CommandException
		{
			final ScoreObjective ret = MinecraftServer.getServer().worldServerForDimension(0).getScoreboard().getObjective(toConvert);
			
			if (ret != null)
				return ret;
			
			throw new CommandException("Objective not found: " + toConvert);
		}
	};
}
