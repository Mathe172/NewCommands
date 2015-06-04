package net.minecraft.command.descriptors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.command.IPermission;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.arg.PermissionWrapper;
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
	
	public static final Map<ITabCompletion, IPermission> operatorCompletions = new HashMap<>();
	
	private final Set<TypeID<?>> resultTypes;
	private final IPermission permission;
	
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
		
		operatorCompletions.put(completion, descriptor.permission);
		
		for (final TypeID<?> resultType : descriptor.resultTypes)
			resultType.addOperator(completion, descriptor.permission);
	}
	
	public static final void register(final String name, final OperatorDescriptor descriptor)
	{
		register(name, name.length() == 1 ? new SingleChar(name.charAt(0)) : new Escaped(name), descriptor);
	}
	
	public OperatorDescriptor(final Set<TypeID<?>> resultTypes, final IPermission permission, final List<IDataType<?>> operands)
	{
		this.resultTypes = resultTypes;
		this.operands = operands;
		this.permission = permission;
	}
	
	public OperatorDescriptor(final Set<TypeID<?>> resultTypes, final IPermission permission, final IDataType<?>... operands)
	{
		this.resultTypes = resultTypes;
		this.permission = permission;
		this.operands = Arrays.asList(operands);
	}
	
	public abstract ArgWrapper<?> construct(ListOperands operands) throws SyntaxErrorException;
	
	public final ArgWrapper<?> parse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
	{
		final ListOperands operands = this.operands.isEmpty() ? null : new ListOperands(this.operands.size());
		
		for (final IDataType<?> operand : this.operands)
			operands.add(operand.parse(parser, context));
		
		return PermissionWrapper.wrap(this.construct(operands), this.permission);
	}
	
	public static class Primitive extends OperatorDescriptor
	{
		public Primitive(final Set<TypeID<?>> resultTypes, final IPermission permission, final IDataType<?> operand)
		{
			super(resultTypes, permission, operand);
		}
		
		@Override
		public ArgWrapper<?> construct(final ListOperands operands)
		{
			return operands.get();
		}
	}
	
	public static class ListOperands
	{
		private final List<ArgWrapper<?>> operands;
		
		public ListOperands()
		{
			this.operands = new ArrayList<>();
		}
		
		public ListOperands(final int initialSize)
		{
			this.operands = new ArrayList<>(initialSize);
		}
		
		public ArgWrapper<?> get()
		{
			return this.operands.get(this.index++);
		}
		
		public void add(final ArgWrapper<?> operand)
		{
			this.operands.add(operand);
		}
		
		private int index = 0;
		
		public <T> CommandArg<T> get(final TypeID<T> type)
		{
			return this.operands.get(this.index++).get(type);
		}
	}
}
