package net.minecraft.command.type.custom;

import net.minecraft.command.EntityNotFoundException;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.management.CConverter;
import net.minecraft.entity.Entity;

public final class ParserEntity
{
	private ParserEntity()
	{
	}
	
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
	
	public static final CDataType<Entity> parser = new ParserName.CustomType<>("UUID/player name", TypeIDs.Entity, UUIDToEntity);
}
