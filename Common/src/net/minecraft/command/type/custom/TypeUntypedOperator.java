package net.minecraft.command.type.custom;

import java.util.regex.Matcher;

import net.minecraft.command.MatcherRegistry;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.PermissionWrapper;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.descriptors.OperatorDescriptor;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.TypeCompletable;
import net.minecraft.command.type.management.TypeID;

public final class TypeUntypedOperator extends TypeCompletable<ArgWrapper<?>>
{
	public static final MatcherRegistry operatorMatcher = new MatcherRegistry("\\G\\s*+([^\\s]++)(?=\\s)");
	
	public static final Context mathContext = new Context()
	{
		@Override
		public <R> ArgWrapper<R> generalParse(final Parser parser, final TypeID<R> target) throws SyntaxErrorException, CompletionException
		{
			final ArgWrapper<R> ret = Context.defContext.generalParse(parser, target);
			
			if (ret != null)
				return ret;
			
			return target.operatorParser.parse(parser, this);
		}
	};
	
	public static final TypeUntypedOperator parser = new TypeUntypedOperator();
	
	private TypeUntypedOperator()
	{
	}
	
	@Override
	public ArgWrapper<?> iParse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
	{
		return parseOperator(parser);
	}
	
	public static ArgWrapper<?> parseOperator(final Parser parser) throws SyntaxErrorException, CompletionException
	{
		parser.proposeCompletion();
		
		final Matcher m = parser.getMatcher(operatorMatcher);
		
		if (!parser.find(m))
			return null;
		
		final OperatorDescriptor descriptor = OperatorDescriptor.getDescriptor(m.group(1));
		
		if (descriptor == null)
			return null;
		
		parser.incIndex(m);
		
		return descriptor.parse(parser, mathContext);
	}
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
	{
		PermissionWrapper.complete(tcDataSet, startIndex, cData, OperatorDescriptor.operatorCompletions);
	}
	
}
