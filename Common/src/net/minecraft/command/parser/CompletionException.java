package net.minecraft.command.parser;

@SuppressWarnings("serial")
public class CompletionException extends Exception
{
	public static final CompletionException ex = new CompletionException();
	
	private CompletionException()
	{
		super("", null, false, false);
	}
}
