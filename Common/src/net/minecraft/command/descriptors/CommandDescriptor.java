package net.minecraft.command.descriptors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.command.CommandBase;
import net.minecraft.command.IPermission;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.completion.TabCompletion;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IDataType;
import net.minecraft.command.type.TypeID;
import net.minecraft.command.type.custom.ParserKeyword;

import org.apache.commons.lang3.tuple.Pair;

public abstract class CommandDescriptor
{
	private final List<IDataType<?>> paramTypes;
	private final Map<String, CommandDescriptor> keywords = new HashMap<>();
	
	private final Set<TabCompletion> keywordCompletions = new HashSet<>();
	
	private final ParserKeyword kp = new ParserKeyword(this);
	
	public final String usage;
	public final IPermission permission;
	
	public static final Map<TabCompletion, CommandDescriptor> commandCompletions = new HashMap<>();
	private static final Map<String, CommandDescriptor> commands = new HashMap<>();
	
	public CommandDescriptor(final IPermission permission, final String usage, final List<IDataType<?>> paramTypes)
	{
		this.permission = permission;
		this.usage = usage;
		this.paramTypes = paramTypes;
	}
	
	public CommandDescriptor(final IPermission permission, final String usage, final List<IDataType<?>> paramTypes, final Map<String, CommandDescriptor> keywords)
	{
		this(permission, usage, paramTypes);
		
		for (final Entry<String, CommandDescriptor> entry : keywords.entrySet())
			this.addSubType(entry.getKey(), entry.getValue());
	}
	
	public CommandDescriptor(final IPermission permission, final String usage, final List<IDataType<?>> paramTypes, final Set<Pair<Set<String>, CommandDescriptor>> descriptors)
	{
		this(permission, usage, paramTypes);
		
		for (final Pair<Set<String>, CommandDescriptor> descriptor : descriptors)
			for (final String name : descriptor.getKey())
				this.addSubType(name, descriptor.getValue());
	}
	
	public static final void clear()
	{
		commandCompletions.clear();
		commands.clear();
	}
	
	public abstract CommandBase construct(final List<ArgWrapper<?>> params, final IPermission permission);
	
	public static void registerCommand(final String name, final CommandDescriptor descriptor)
	{
		if (descriptor.usage == null)
			throw new IllegalArgumentException("Base command requires usage information");
		
		if (commands.put(name, descriptor) != null)
			throw new IllegalArgumentException("Command with name '" + name + "' already registered");
		
		commandCompletions.put(new TabCompletion(name), descriptor);
	}
	
	public static void registerCommand(final Pair<Set<String>, CommandDescriptor> descriptor)
	{
		final CommandDescriptor baseDescriptor = descriptor.getValue();
		
		for (final String name : descriptor.getKey())
			registerCommand(name, baseDescriptor);
	}
	
	public void addSubType(final String key, final CommandDescriptor descriptor)
	{
		if (this.keywords.put(key, descriptor) != null)
			throw new IllegalArgumentException("Keyword '" + key + "' already registered");
		
		if ("".equals(key))
			return;
		
		this.keywordCompletions.add(new TabCompletion(key));
	}
	
	public static final CommandDescriptor getDescriptor(final String name)
	{
		return commands.get(name);
	}
	
	public CommandDescriptor getSubType(final Parser parser) throws SyntaxErrorException, CompletionException
	{
		return this.kp.parse(parser);
	}
	
	public CommandDescriptor getSubType(final String keyword)
	{
		return this.keywords.get(keyword);
	}
	
	public int getParamCount()
	{
		return this.paramTypes.size();
	}
	
	public IDataType<?> getRequiredType(final int index)
	{
		return this.paramTypes.get(index);
	}
	
	public final Set<TabCompletion> getKeywordCompletions()
	{
		return this.keywordCompletions;
	}
	
	public ArgWrapper<?> parse(final Parser parser, final int i) throws SyntaxErrorException, CompletionException
	{
		return this.getRequiredType(i).parse(parser);
	}
	
	public static final Set<TabCompletion> getCompletions()
	{
		return commandCompletions.keySet();
	}
	
	public static void addAlias(final String name, final String... aliases)
	{
		final CommandDescriptor descriptor = getDescriptor(name);
		
		for (final String alias : aliases)
			registerCommand(alias, descriptor);
	}
	
	public static final <T> CommandArg<T> getParam(final TypeID<T> type, final int index, final List<ArgWrapper<?>> params)
	{
		
		if (index < params.size())
			return params.get(index).get(type);
		
		return null;
	}
	
	public static final <T> CommandArg<T> getRequiredParam(final TypeID<T> type, final int index, final String name, final List<ArgWrapper<?>> params) throws SyntaxErrorException
	{
		final CommandArg<T> ret = getParam(type, index, params);
		
		if (ret != null)
			return ret;
		
		throw ParsingUtilities.SEE("Missing parameter for command: " + (name != null ? name : index));
	}
}