package net.minecraft.command.type.custom.nbt;

import java.util.Map.Entry;
import java.util.regex.Matcher;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.CTypeCompletable;
import net.minecraft.command.type.custom.NBTDescriptors;
import net.minecraft.command.type.custom.TypeIDs;
import net.minecraft.command.type.custom.nbt.NBTDescriptor.Tag;
import net.minecraft.command.type.custom.nbt.ParserNBTCompound.CompoundData;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

public class TypeNBTArg extends CTypeCompletable<NBTTagCompound>
{
	private final Tag baseDescriptor;
	
	public TypeNBTArg(final Tag baseDescriptor)
	{
		this.baseDescriptor = baseDescriptor;
	}
	
	public static final CDataType<NBTTagCompound> parserDefault = new TypeNBTArg(NBTDescriptor.defaultTagCompound);
	public static final CDataType<NBTTagCompound> parserBlock = new TypeNBTArg(NBTDescriptors.block);
	public static final CDataType<NBTTagCompound> parserEntity = new TypeNBTArg(NBTDescriptors.entity);
	
	@Override
	public ArgWrapper<NBTTagCompound> iParse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
	{
		final ArgWrapper<NBTTagCompound> ret = context.generalParse(parser, TypeIDs.NBTCompound);
		
		if (ret != null)
			return ret;
		
		final Matcher m = parser.specialMatcher;
		
		if (!parser.findInc(m) || !"{".equals(m.group(1)))
			throw parser.SEE("Expected '{' around index ");
		
		parser.terminateCompletion();
		
		final CompoundData data = new CompoundData();
		
		this.baseDescriptor.getCompoundParser().parseItems(parser, data);
		
		if (data.data.isEmpty())
			return TypeIDs.NBTCompound.wrap(new NBTTagCompound(data.primitiveData));
		
		return TypeIDs.NBTCompound.wrap(new CommandArg<NBTTagCompound>()
		{
			final NBTTagCompound compound = new NBTTagCompound(data.primitiveData);
			
			@Override
			public NBTTagCompound eval(final ICommandSender sender) throws CommandException
			{
				for (final Entry<String, CommandArg<NBTBase>> tag : data.data.entrySet())
					data.primitiveData.put(tag.getKey(), tag.getValue().eval(sender));
				
				return this.compound;
			}
		});
	}
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
	{
		NBTUtilities.braceCompleter.complete(tcDataSet, parser, startIndex, cData);
	}
}