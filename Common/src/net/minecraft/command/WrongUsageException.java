package net.minecraft.command;

@SuppressWarnings("serial")
public class WrongUsageException extends SyntaxErrorException
{
	@SuppressWarnings("unused")
	private static final String __OBFID = "CL_00001192";
	public static final WrongUsageException wue = new WrongUsageException("commands.generic.syntax", null, false, false);
	
	public WrongUsageException(final String message, final Object... errorObjects)
	{
		super(message, errorObjects);
	}
	
	public WrongUsageException(final String message, final boolean enableSuppression, final boolean writableStackTrace, final Object... errorObjects)
	{
		super(message, null, enableSuppression, writableStackTrace, errorObjects);
	}
}
