package net.minecraft.command.type.management;

import java.util.HashSet;
import java.util.Set;

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
	
	private final Set<ITabCompletion> directSelectors = new HashSet<>();
	private final Set<ITabCompletion> possibleSelectors = new HashSet<>();
	
	private final Set<ITabCompletion> directOperators = new HashSet<>();
	private final Set<ITabCompletion> possibleOperators = new HashSet<>();
	
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
	
	public final void addSelector(final ITabCompletion selector)
	{
		this.directSelectors.add(selector);
		
		this.possibleSelectors.add(selector);
		
		for (final Convertable<?, ?, ?> convertable : this.convertableTo)
			convertable.addPossibleSelector(selector);
	}
	
	public final void addOperator(final ITabCompletion operator)
	{
		this.directOperators.add(operator);
		
		this.possibleOperators.add(operator);
		
		for (final Convertable<?, ?, ?> convertable : this.convertableTo)
			convertable.addPossibleOperator(operator);
	}
	
	public final Set<ITabCompletion> getSelectorCompletions()
	{
		return this.possibleSelectors;
	}
	
	public final Set<ITabCompletion> getOperatorCompletions()
	{
		return this.possibleOperators;
	}
	
	@Override
	public void addPossibleSelector(final ITabCompletion tc)
	{
		this.possibleSelectors.add(tc);
	}
	
	@Override
	public void addPossibleOperator(final ITabCompletion tc)
	{
		this.possibleOperators.add(tc);
	}
	
	@Override
	public void adjustCompletions(final Convertable<?, ?, ?> target)
	{
		for (final ITabCompletion selector : this.directSelectors)
			target.addPossibleSelector(selector);
		
		for (final ITabCompletion operator : this.directOperators)
			target.addPossibleOperator(operator);
	}
}
