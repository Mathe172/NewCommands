package net.minecraft.command;

public interface IPermission
{
	public abstract boolean canCommandSenderUseCommand(ICommandSender sender);
	
	public static final IPermission PermissionUnrestricted = new IPermission()
	{
		@Override
		public boolean canCommandSenderUseCommand(final ICommandSender sender)
		{
			return true;
		}
	};
	
	public static final IPermission PermissionLevel1 = new IPermission()
	{
		@Override
		public boolean canCommandSenderUseCommand(final ICommandSender sender)
		{
			return sender.canCommandSenderUseCommand(1);
		}
	};
	
	public static final IPermission PermissionLevel2 = new IPermission()
	{
		@Override
		public boolean canCommandSenderUseCommand(final ICommandSender sender)
		{
			return sender.canCommandSenderUseCommand(2);
		}
	};
	
	public static final IPermission PermissionLevel3 = new IPermission()
	{
		@Override
		public boolean canCommandSenderUseCommand(final ICommandSender sender)
		{
			return sender.canCommandSenderUseCommand(3);
		}
	};
	
	public static final IPermission PermissionLevel4 = new IPermission()
	{
		@Override
		public boolean canCommandSenderUseCommand(final ICommandSender sender)
		{
			return sender.canCommandSenderUseCommand(4);
		}
	};
}
