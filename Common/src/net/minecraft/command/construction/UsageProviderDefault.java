package net.minecraft.command.construction;

import java.util.List;

import net.minecraft.command.WrongUsageException;
import net.minecraft.command.descriptors.CommandDescriptor;
import net.minecraft.command.parser.Parser;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;

public abstract class UsageProviderDefault implements CommandDescriptor.UsageProvider
{
	protected abstract <R> R create(List<String> path, AbstractCreator<R> creator);
	
	protected abstract static class AbstractCreator<R>
	{
		public abstract R create(String usage, Object... errorObjects);
	}
	
	private static final AbstractCreator<WrongUsageException> ExceptionCreator(final Parser parser)
	{
		return new AbstractCreator<WrongUsageException>()
		{
			@Override
			public WrongUsageException create(final String usage, final Object... errorObjects)
			{
				return parser.WUE(usage, errorObjects);
			}
		};
	}
	
	private static final AbstractCreator<IChatComponent> MessageCreator = new AbstractCreator<IChatComponent>()
	{
		@Override
		public ChatComponentTranslation create(final String usage, final Object... errorObjects)
		{
			return new ChatComponentTranslation(usage, errorObjects);
		};
	};
	
	@Override
	public WrongUsageException createException(final Parser parser, final List<String> path)
	{
		return this.create(path, ExceptionCreator(parser));
	}
	
	@Override
	public IChatComponent createMessage(final List<String> path)
	{
		return this.create(path, MessageCreator);
	}
}
