package net.minecraft.command;

@SuppressWarnings("serial")
public class CommandException extends Exception
{
	private final Object[] errorObjects;
	@SuppressWarnings("unused")
	private static final String __OBFID = "CL_00001187";
	
	public CommandException(final String message, final Throwable cause, final Object... errorObjects)
	{
		super(message, cause, true, CommandUtilities.catchStack);
		this.errorObjects = errorObjects;
	}
	
	public CommandException(final String message, final Object... errorObjects)
	{
		this(message, null, errorObjects);
	}
	
	public CommandException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace, final Object... errorObjects)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.errorObjects = errorObjects;
	}
	
	public Object[] getErrorOjbects()
	{
		return this.errorObjects;
	}
}
