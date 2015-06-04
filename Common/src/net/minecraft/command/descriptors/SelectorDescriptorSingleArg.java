package net.minecraft.command.descriptors;

import java.util.Set;

import net.minecraft.command.IPermission;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletion;
import net.minecraft.command.completion.TabCompletionData;
import net.minecraft.command.descriptors.SelectorDescriptorSingleArg.ParserDataSingleArg;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IDataType;
import net.minecraft.command.type.management.TypeID;

public class SelectorDescriptorSingleArg extends SelectorDescriptor<ParserDataSingleArg>
{
	public static class ParserDataSingleArg extends SParserData
	{
		public ArgWrapper<?> arg;
		
		public ParserDataSingleArg(final Parser parser)
		{
			super(parser);
		}
		
		@Override
		public ArgWrapper<?> finalize(final ArgWrapper<?> selector)
		{
			return selector;
		}
		
		@Override
		public boolean requiresKey()
		{
			return false;
		}
		
		public ArgWrapper<?> getRequiredArg() throws SyntaxErrorException
		{
			if (this.arg == null)
				throw this.parser.SEE("Missing required argument for selector ");
			
			return this.arg;
		}
		
		public <T> CommandArg<T> getRequiredArg(final TypeID<T> type) throws SyntaxErrorException
		{
			return getRequiredArg().get(type);
		}
	}
	
	public static abstract class SingleArgConstructable
	{
		public abstract ArgWrapper<?> construct(ParserDataSingleArg data) throws SyntaxErrorException;
	}
	
	private final String key;
	private final TabCompletion keyCompletion;
	private final IDataType<?> arg;
	private final SingleArgConstructable constructable;
	
	public SelectorDescriptorSingleArg(final Set<TypeID<?>> resultTypes, final IPermission permission, final IDataType<?> arg, final SingleArgConstructable constructable)
	{
		super(resultTypes, permission);
		this.arg = arg;
		this.key = null;
		this.keyCompletion = null;
		this.constructable = constructable;
	}
	
	public SelectorDescriptorSingleArg(final Set<TypeID<?>> resultTypes, final IPermission permission, final String key, final IDataType<?> arg, final SingleArgConstructable constructable)
	{
		super(resultTypes, permission);
		this.arg = arg;
		this.key = key;
		this.keyCompletion = new TabCompletion(key);
		this.constructable = constructable;
	}
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData, final ParserDataSingleArg data)
	{
		if (this.key != null && data.arg == null)
			TabCompletionData.addToSet(tcDataSet, startIndex, cData, this.keyCompletion);
	}
	
	@Override
	public ArgWrapper<?> construct(final ParserDataSingleArg data) throws SyntaxErrorException
	{
		return this.constructable.construct(data);
	}
	
	@Override
	public void parse(final Parser parser, final String key, final ParserDataSingleArg data) throws SyntaxErrorException, CompletionException
	{
		if (!key.equals(this.key))
			throw parser.SEE("Unknown key '" + key + "' ");
		
		data.arg = this.arg.parse(parser);
	}
	
	@Override
	public void parse(final Parser parser, final ParserDataSingleArg data) throws SyntaxErrorException, CompletionException
	{
		if (data.arg != null)
			throw parser.SEE("Selector has only one argument ");
		
		data.arg = this.arg.parse(parser);
	}
	
	@Override
	public ParserDataSingleArg newParserData(final Parser parser)
	{
		return new ParserDataSingleArg(parser);
	}
}
