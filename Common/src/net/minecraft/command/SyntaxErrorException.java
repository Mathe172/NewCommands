package net.minecraft.command;

@SuppressWarnings("serial")
public class SyntaxErrorException extends CommandException
{
	@SuppressWarnings("unused")
	private static final String __OBFID = "CL_00001189";
	
	public static final SyntaxErrorException see = new SyntaxErrorException("commands.generic.syntax", (Throwable) null, false, false, new Object[0]);
	
	public SyntaxErrorException()
	{
		this("commands.generic.snytax");
	}
	
	public SyntaxErrorException(final String message, final Object... errorObjects)
	{
		super(message, errorObjects);
	}
	
	public SyntaxErrorException(final String message, final Throwable cause, final Object... errorObjects)
	{
		super(message, cause, errorObjects);
	}
	
	public SyntaxErrorException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace, final Object... errorObjects)
	{
		super(message, cause, enableSuppression, writableStackTrace, errorObjects);
	}
}
