package net.minecraft.command.type.custom.nbt;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.arg.PrimitiveParameter;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.CTypeParse;
import net.minecraft.command.type.custom.nbt.NBTDescriptor.Tag;
import net.minecraft.command.type.custom.nbt.NBTUtilities.NBTData;
import net.minecraft.nbt.NBTBase;

public class TypeNBTBase extends CTypeParse<NBTBase>
{
	private final Tag baseDescriptor;
	
	public TypeNBTBase(final Tag baseDescriptor)
	{
		this.baseDescriptor = baseDescriptor;
	}
	
	public static final CDataType<NBTBase> parserDefault = new TypeNBTBase(NBTDescriptor.baseDescriptor);
	
	@Override
	public ArgWrapper<NBTBase> iParse(final Parser parser, final Context context) throws SyntaxErrorException
	{
		final ArgWrapper<NBTBase> ret = context.generalParse(parser, TypeIDs.NBTBase);
		
		if (ret != null)
			return ret;
		
		final NBTTagData data = new NBTTagData();
		
		this.baseDescriptor.getTagParser().parse(parser, data);
		
		return TypeIDs.NBTBase.wrap(data.tag);
	}
	
	private static class NBTTagData extends NBTData
	{
		public CommandArg<NBTBase> tag = null;
		
		@Override
		public void put(final NBTBase data)
		{
			this.tag = new PrimitiveParameter<>(data);
		}
		
		@Override
		public void add(final CommandArg<NBTBase> data)
		{
			this.tag = data;
		}
	}
}
