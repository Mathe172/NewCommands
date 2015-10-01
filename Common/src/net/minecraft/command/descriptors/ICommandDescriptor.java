package net.minecraft.command.descriptors;

import java.util.List;
import java.util.Set;

import net.minecraft.command.IPermission;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.parser.Parser;
import net.minecraft.util.IChatComponent;

public abstract class ICommandDescriptor<D>
{
	public static interface UsageProvider
	{
		public WrongUsageException createException(Parser parser, List<String> path);
		
		public IChatComponent createMessage(List<String> path);
	}
	
	public final UsageProvider usage;
	public final IPermission permission;
	
	public ICommandDescriptor(final UsageProvider usage, final IPermission permission)
	{
		this.usage = usage;
		this.permission = permission;
	}
	
	public abstract void addSubDescriptor(final String key, final ICommandDescriptor<? super D> descriptor);
	
	public abstract CommandArg<Integer> construct(final D data) throws SyntaxErrorException;
	
	public abstract void parse(final Parser parser, final D parserData, final UsageProvider usage) throws SyntaxErrorException;
	
	public abstract ICommandDescriptor<? super D> getSubDescriptor(final String keyword);
	
	public abstract ICommandDescriptor<? super D> getSubDescriptor(final Parser parser, final D data) throws SyntaxErrorException;
	
	public abstract Set<ITabCompletion> getKeywordCompletions();
}