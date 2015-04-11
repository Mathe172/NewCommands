package net.minecraft.command.descriptors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.completion.TabCompletion.Escaped;
import net.minecraft.command.completion.TabCompletion.SingleChar;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IDataType;
import net.minecraft.command.type.management.TypeID;

public abstract class OperatorDescriptor
{
	private static final Map<String, OperatorDescriptor> operators = new HashMap<>();
	
	private static final Set<ITabCompletion> operatorCompletions = new HashSet<>();
	
	private final Set<TypeID<?>> resultTypes;
	
	private final List<IDataType<?>> operands;
	
	public static final OperatorDescriptor getDescriptor(final String name)
	{
		return operators.get(name);
	}
	
	public static final void clear()
	{
		operators.clear();
		operatorCompletions.clear();
	}
	
	public static final void register(final String name, final ITabCompletion completion, final OperatorDescriptor descriptor)
	{
		if (operators.put(name, descriptor) != null)
			throw new IllegalArgumentException("Operator already registerd: " + name);
		
		operatorCompletions.add(completion);
		
		for (final TypeID<?> resultType : descriptor.resultTypes)
			resultType.addOperator(completion);
	}
	
	public static final void register(final String name, final OperatorDescriptor descriptor)
	{
		register(name, name.length() == 1 ? new SingleChar(name.charAt(0)) : new Escaped(name), descriptor);
	}
	
	public OperatorDescriptor(final Set<TypeID<?>> resultTypes, final List<IDataType<?>> operands)
	{
		this.resultTypes = resultTypes;
		this.operands = operands;
	}
	
	public OperatorDescriptor(final Set<TypeID<?>> resultTypes, final IDataType<?>... operands)
	{
		this.resultTypes = resultTypes;
		this.operands = Arrays.asList(operands);
	}
	
	public static final Set<ITabCompletion> getCompletions()
	{
		return operatorCompletions;
	}
	
	public abstract ArgWrapper<?> construct(List<ArgWrapper<?>> operands);
	
	public final ArgWrapper<?> parse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
	{
		final List<ArgWrapper<?>> operands = new ArrayList<>(this.operands.size());
		
		for (final IDataType<?> operand : this.operands)
			operands.add(operand.parse(parser, context));
		
		return this.construct(operands);
	}
	
	public static class Primitive extends OperatorDescriptor
	{
		public Primitive(final Set<TypeID<?>> resultTypes, final IDataType<?> operand)
		{
			super(resultTypes, operand);
		}
		
		@Override
		public ArgWrapper<?> construct(final List<ArgWrapper<?>> operands)
		{
			return operands.get(0);
		}
	}
}
