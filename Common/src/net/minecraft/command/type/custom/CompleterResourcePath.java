package net.minecraft.command.type.custom;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletion.Escaped;
import net.minecraft.command.completion.TabCompletionData;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.MatcherRegistry;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IComplete;
import net.minecraft.util.ResourceLocation;

public class CompleterResourcePath implements IComplete
{
	private final Map<String, CompleterResourcePath> subResources = new HashMap<>();
	private final Set<ITabCompletion> domainCompletions = new HashSet<>();
	private final Set<ITabCompletion> entryCompletions;
	
	public static final MatcherRegistry pathMatcher = new MatcherRegistry("\\G[\\w]*+[.:]");
	
	public CompleterResourcePath(final Set<ITabCompletion> completions)
	{
		this.entryCompletions = completions;
	}
	
	public CompleterResourcePath()
	{
		this.entryCompletions = new HashSet<>();
	}
	
	public CompleterResourcePath(final String... defaultDomains)
	{
		this();
		
		for (final String domain : defaultDomains)
		{
			final String domainPath = domain + ":";
			this.subResources.put(domainPath.toLowerCase(), new CompleterResourcePath(this.entryCompletions));
			this.addDomainCompletion(domainPath, domain);
		}
		
	}
	
	private final void addDomainCompletion(final String domainPath, final String domain)
	{
		this.domainCompletions.add(new Escaped(domainPath, domain, false)
		{
			@Override
			public double weightOffset(final Matcher m, final CompletionData cData)
			{
				return 1.0;
			}
		});
	}
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
	{
		final Matcher whitespaceMatcher = parser.getMatcher(ParsingUtilities.whitespaceMatcher);
		whitespaceMatcher.find(startIndex);
		
		final Matcher pathMatcher = parser.getMatcher(CompleterResourcePath.pathMatcher);
		
		int index = startIndex + whitespaceMatcher.group().length();
		CompleterResourcePath completerResourcePath = this;
		
		while (completerResourcePath != null)
		{
			if (pathMatcher.find(index))
			{
				completerResourcePath = completerResourcePath.subResources.get(pathMatcher.group().toLowerCase());
				
				index += pathMatcher.group().length();
			}
			else
			{
				TabCompletionData.addToSet(tcDataSet, index, cData, completerResourcePath.domainCompletions);
				TabCompletionData.addToSet(tcDataSet, index, cData, completerResourcePath.entryCompletions);
				
				return;
			}
		}
	}
	
	public CompleterResourcePath registerResource(final ResourceLocation... resources)
	{
		for (final ResourceLocation resource : resources)
			registerSingleResource(resource.toString());
		return this;
	}
	
	private final CompleterResourcePath registerSingleResource(final String resource)
	{
		final Matcher m = pathMatcher.matcher(resource);
		
		int index = 0;
		CompleterResourcePath resourcePath = this;
		
		while (m.find(index))
		{
			final String domainName = m.group();
			CompleterResourcePath resourcePathNew = resourcePath.subResources.get(domainName.toLowerCase());
			
			if (resourcePathNew == null)
			{
				resourcePathNew = new CompleterResourcePath();
				resourcePath.addDomainCompletion(domainName, domainName.substring(0, domainName.length() - 1));
				resourcePath.subResources.put(domainName.toLowerCase(), resourcePathNew);
			}
			
			resourcePath = resourcePathNew;
			index += domainName.length();
		}
		
		final String name = resource.substring(index);
		
		if (name.length() != 0)
			resourcePath.entryCompletions.add(new Escaped(name, false));
		return this;
	}
	
	public final CompleterResourcePath registerResource(final String... resources)
	{
		for (final String resource : resources)
			registerSingleResource(resource);
		
		return this;
	}
}
