package net.minecraft.command.descriptors;

import java.util.Set;

import net.minecraft.command.IPermission;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.descriptors.SelectorDescriptorNoContent.PrimitiveData;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.management.TypeID;

public class SelectorDescriptorNoContent extends SelectorDescriptor<PrimitiveData>
{
	public static class PrimitiveData extends SParserData
	{
		public PrimitiveData(final Parser parser)
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
	}
	
	public static abstract class PrimitiveConstructable
	{
		public abstract ArgWrapper<?> construct(PrimitiveData data);
	}
	
	private final PrimitiveConstructable constructable;
	
	public SelectorDescriptorNoContent(final Set<TypeID<?>> resultTypes, final IPermission permission, final PrimitiveConstructable constructable)
	{
		super(resultTypes, permission);
		this.constructable = constructable;
	}
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData, final PrimitiveData data)
	{
	}
	
	@Override
	public ArgWrapper<?> construct(final PrimitiveData data) throws SyntaxErrorException
	{
		return this.constructable.construct(data);
	}
	
	@Override
	public void parse(final Parser parser, final String key, final PrimitiveData data) throws SyntaxErrorException
	{
		throw parser.SEE("Selector does not have any parameters ");
	}
	
	@Override
	public void parse(final Parser parser, final PrimitiveData data) throws SyntaxErrorException
	{
		throw parser.SEE("Selector does not have any parameters ");
	}
	
	@Override
	public PrimitiveData newParserData(final Parser parser)
	{
		return new PrimitiveData(parser);
	}
}
