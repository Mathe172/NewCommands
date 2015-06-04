package net.minecraft.command.type.management;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.command.IPermission;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.type.IParse;
import net.minecraft.command.type.custom.TypeLabel;
import net.minecraft.command.type.custom.TypeOperator;
import net.minecraft.command.type.custom.TypeSelector;

public abstract class CConvertable<T, W> extends Convertable<T, W, SyntaxErrorException>
{
	public final IParse<W> selectorParser = new TypeSelector<>(this);
	public final IParse<W> labelParser = new TypeLabel<>(this);
	public final IParse<W> operatorParser = new TypeOperator<>(this);
	
	private final Map<ITabCompletion, IPermission> directSelectors = new HashMap<>();
	private final Map<ITabCompletion, IPermission> possibleSelectors = new HashMap<>();
	
	private final Map<ITabCompletion, IPermission> directOperators = new HashMap<>();
	private final Map<ITabCompletion, IPermission> possibleOperators = new HashMap<>();
	
	public CConvertable(final String name)
	{
		super(name);
	}
	
	@Override
	public void clear()
	{
		super.clear();
		
		this.directSelectors.clear();
		this.possibleSelectors.clear();
		
		this.directOperators.clear();
		this.possibleOperators.clear();
	}
	
	public final void addSelector(final ITabCompletion selector, final IPermission permission)
	{
		this.directSelectors.put(selector, permission);
		
		this.possibleSelectors.put(selector, permission);
		
		for (final Convertable<?, ?, ?> convertable : this.convertableTo.keySet())
			convertable.addPossibleSelector(selector, permission);
	}
	
	public final void addOperator(final ITabCompletion operator, final IPermission permission)
	{
		this.directOperators.put(operator, permission);
		
		this.possibleOperators.put(operator, permission);
		
		for (final Convertable<?, ?, ?> convertable : this.convertableTo.keySet())
			convertable.addPossibleOperator(operator, permission);
	}
	
	public final Map<ITabCompletion, IPermission> getSelectorCompletions()
	{
		return this.possibleSelectors;
	}
	
	public final Map<ITabCompletion, IPermission> getOperatorCompletions()
	{
		return this.possibleOperators;
	}
	
	@Override
	public void addPossibleSelector(final ITabCompletion tc, final IPermission permission)
	{
		this.possibleSelectors.put(tc, permission);
	}
	
	@Override
	public void addPossibleOperator(final ITabCompletion tc, final IPermission permission)
	{
		this.possibleOperators.put(tc, permission);
	}
	
	@Override
	public void adjustCompletions(final Convertable<?, ?, ?> target)
	{
		for (final Entry<ITabCompletion, IPermission> selector : this.directSelectors.entrySet())
			target.addPossibleSelector(selector.getKey(), selector.getValue());
		
		for (final Entry<ITabCompletion, IPermission> operator : this.directOperators.entrySet())
			target.addPossibleOperator(operator.getKey(), operator.getValue());
	}
}
