package net.minecraft.command;

public interface IPermission
{
	public abstract boolean canCommandSenderUseCommand(ICommandSender sender);
	
	public static final IPermission unrestricted = new IPermission()
	{
		@Override
		public boolean canCommandSenderUseCommand(final ICommandSender sender)
		{
			return true;
		}
	};
	
	public static final IPermission level1 = new IPermission()
	{
		@Override
		public boolean canCommandSenderUseCommand(final ICommandSender sender)
		{
			return sender.canCommandSenderUseCommand(1, "");
		}
	};
	
	public static final IPermission level2 = new IPermission()
	{
		@Override
		public boolean canCommandSenderUseCommand(final ICommandSender sender)
		{
			return sender.canCommandSenderUseCommand(2, "");
		}
	};
	
	public static final IPermission level3 = new IPermission()
	{
		@Override
		public boolean canCommandSenderUseCommand(final ICommandSender sender)
		{
			return sender.canCommandSenderUseCommand(3, "");
		}
	};
	
	public static final IPermission level4 = new IPermission()
	{
		@Override
		public boolean canCommandSenderUseCommand(final ICommandSender sender)
		{
			return sender.canCommandSenderUseCommand(4, "");
		}
	};
}
