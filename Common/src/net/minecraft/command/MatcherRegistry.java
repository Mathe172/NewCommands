package net.minecraft.command;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MatcherRegistry
{
	private static int count = 0;
	private static final Set<MatcherRegistry> matchers = new HashSet<>();
	
	public final Pattern pattern;
	
	public MatcherRegistry(final Pattern pattern)
	{
		this.pattern = pattern;
	}
	
	public MatcherRegistry(final String pattern)
	{
		this.pattern = Pattern.compile(pattern);
	}
	
	private int id = -1;
	
	public int getId()
	{
		return this.id;
	}
	
	public Matcher matcher(final String toMatch)
	{
		return this.pattern.matcher(toMatch);
	}
	
	public static int getCount()
	{
		return count;
	}
	
	public void init()
	{
		if (matchers.add(this))
			this.id = count++;
	}
	
	public void aliasId(final MatcherRegistry m)
	{
		this.id = m.id;
		matchers.add(this);
	}
	
	public static void clear()
	{
		count = 0;
		for (final MatcherRegistry m : matchers)
			m.id = -1;
		
		matchers.clear();
	}
}
