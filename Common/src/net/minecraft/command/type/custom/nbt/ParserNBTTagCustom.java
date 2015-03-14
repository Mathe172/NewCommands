package net.minecraft.command.type.custom.nbt;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IComplete;
import net.minecraft.command.type.IType;
import net.minecraft.command.type.custom.nbt.NBTDescriptor.Tag;
import net.minecraft.command.type.custom.nbt.NBTUtilities.NBTData;

public class ParserNBTTagCustom extends ParserNBTTag implements IType<Void, NBTData>
{
	private final IComplete completer;
	
	public ParserNBTTagCustom(final Tag descriptor, final IComplete completer)
	{
		super(descriptor);
		this.completer = completer;
	}
	
	@Override
	public final Void parse(final Parser parser, final NBTData parserData) throws SyntaxErrorException, CompletionException
	{
		return parser.parse(this, parserData);
	}
	
	@Override
	public Void iParse(final Parser parser, final NBTData parserData) throws SyntaxErrorException, CompletionException
	{
		return super.parse(parser, parserData);
	}
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData, final NBTData parserData)
	{
		this.completer.complete(tcDataSet, parser, startIndex, cData);
	}
}
