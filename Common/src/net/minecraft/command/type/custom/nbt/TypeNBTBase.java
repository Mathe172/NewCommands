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
import net.minecraft.command.type.custom.TypeIDs;
import net.minecraft.command.type.custom.nbt.NBTDescriptor.Tag;
import net.minecraft.command.type.custom.nbt.ParserNBTCompound.CompoundData;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

public class TypeNBTBase extends CTypeCompletable<NBTBase>
{
	private final Tag baseDescriptor;
	
	public TypeNBTBase(final Tag baseDescriptor)
	{
		this.baseDescriptor = baseDescriptor;
	}
	
	public static final CDataType<NBTBase> parserDefault = new TypeNBTBase(NBTDescriptor.defaultTagCompound);
	
	/** Note: Nearly the same code as in TypeNBTArg, with the only difference being the target type (thanks, java generics for not allowing 'T super NBTTagCompound'...) */
	@Override
	public ArgWrapper<NBTBase> iParse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
	{
		final ArgWrapper<NBTBase> ret = context.generalParse(parser, TypeIDs.NBTBase);
		
		if (ret != null)
			return ret;
		
		final Matcher m = parser.specialMatcher;
		
		if (!parser.findInc(m) || !"{".equals(m.group(1)))
			throw parser.SEE("Expected '{' around index ");
		
		parser.terminateCompletion();
		
		final CompoundData data = new CompoundData();
		
		this.baseDescriptor.getCompoundParser().parseItems(parser, data);
		
		if (data.data.isEmpty())
			return TypeIDs.NBTBase.wrap(new NBTTagCompound(data.primitiveData));
		
		return TypeIDs.NBTBase.wrap(new CommandArg<NBTBase>()
		{
			final NBTBase compound = new NBTTagCompound(data.primitiveData);
			
			@Override
			public NBTBase eval(final ICommandSender sender) throws CommandException
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
