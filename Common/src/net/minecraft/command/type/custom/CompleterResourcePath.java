package net.minecraft.command.type.custom;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletion.Escaped;
import net.minecraft.command.completion.TabCompletionData;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IComplete;
import net.minecraft.util.ResourceLocation;

public class CompleterResourcePath implements IComplete
{
	private final Map<String, Set<ITabCompletion>> resources = new HashMap<>();
	private final Set<ITabCompletion> domainCompletions = new HashSet<>();
	
	private final String defaultDomain;
	
	public CompleterResourcePath(final String defaultDomain)
	{
		this.defaultDomain = defaultDomain;
		
		this.resources.put(defaultDomain, new HashSet<ITabCompletion>());
		this.domainCompletions.add(new Escaped(defaultDomain + ":", defaultDomain));
	}
	
	public CompleterResourcePath()
	{
		this("minecraft");
	}
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
	{
		final int index = parser.toParse.indexOf(':', startIndex);
		
		if (index == -1 || index == cData.cursorIndex - 1)
			TabCompletionData.addToSet(tcDataSet, startIndex, cData, this.domainCompletions);
		
		if (index == -1)
			TabCompletionData.addToSet(tcDataSet, startIndex, cData, this.resources.get(this.defaultDomain));
		else
		{
			final Set<ITabCompletion> completions = this.resources.get(parser.toParse.substring(startIndex, index).trim());
			
			if (completions != null)
				TabCompletionData.addToSet(tcDataSet, index + 1, cData, completions);
		}
	}
	
	public void registerResource(final ResourceLocation resource)
	{
		final String resourceDomain = resource.getResourceDomain();
		Set<ITabCompletion> completions = this.resources.get(resourceDomain);
		
		if (completions == null)
		{
			completions = new HashSet<>();
			this.resources.put(resourceDomain, completions);
			
			this.domainCompletions.add(new Escaped(resourceDomain + ":", resourceDomain));
		}
		
		completions.add(new Escaped(resource.getResourcePath()));
	}
}
