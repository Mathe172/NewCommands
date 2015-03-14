package net.minecraft.command.completion;

public class TabCompleter implements Comparable<TabCompleter>
{
	private final TabCompletionData tcData;
	private String result = null;
	
	public TabCompleter(final TabCompletionData tcData)
	{
		this.tcData = tcData;
	}
	
	public String matchInto(final String toMatch, final int cursorIndex)
	{
		return this.result == null ? this.result = this.iMatchInto(toMatch, cursorIndex) : this.result;
	}
	
	private String iMatchInto(final String toMatch, final int cursorIndex)
	{
		final int startIndex = this.tcData.startIndex;
		final String replacement = this.tcData.replacement;
		
		final StringBuilder sb = new StringBuilder(toMatch.length() + replacement.length() + startIndex - cursorIndex);
		
		sb.append(toMatch, 0, startIndex);
		
		int index = startIndex;
		
		if (!this.tcData.primitiveFit)
		{
			int fitted = 0;
			while (fitted < replacement.length() && index < toMatch.length())
			{
				fitted = replacement.indexOf(toMatch.charAt(index), fitted) + 1;
				
				if (fitted == 0)
				{
					if (index >= cursorIndex)
						break;
					
					index = cursorIndex;
				}
				else
					++index;
			}
		}
		
		sb.append(replacement);
		
		if (index < toMatch.length())
			sb.append(toMatch, Math.max(cursorIndex, index), toMatch.length());
		
		return sb.toString();
	}
	
	public final String name()
	{
		return this.tcData.name;
	}
	
	public final int newCursorIndex()
	{
		return this.tcData.newCursorIndex;
	}
	
	@Override
	public final int compareTo(final TabCompleter completer)
	{
		return this.tcData.name.compareToIgnoreCase(completer.tcData.name);
	}
	
}
