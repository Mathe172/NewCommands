package net.minecraft.command.descriptors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.command.IPermission;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.arg.Setter;
import net.minecraft.command.arg.Setter.SetterProvider;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.completion.TabCompletion;
import net.minecraft.command.descriptors.CommandDescriptor.CParserData;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.custom.TypeLabelDeclaration.LabelRegistration;
import net.minecraft.command.type.custom.command.ParserKeyword;
import net.minecraft.command.type.management.TypeID;

import org.apache.commons.lang3.tuple.Pair;

public abstract class CommandDescriptor<D extends CParserData>
{
	private final Map<String, CommandDescriptor<? super D>> keywords = new HashMap<>();
	
	private final Set<ITabCompletion> keywordCompletions = new HashSet<>();
	
	private final ParserKeyword<D> kp = new ParserKeyword<>(this);
	
	public static interface WUEProvider
	{
		public WrongUsageException create(CParserData data);
	}
	
	public final WUEProvider usage;
	public final IPermission permission;
	
	public static final Map<ITabCompletion, IPermission> commandCompletions = new HashMap<>();
	private static final Map<String, CommandDescriptor<?>> commands = new HashMap<>();
	
	public static class CParserData
	{
		public final Parser parser;
		
		public final List<ArgWrapper<?>> params;
		public final List<String> path = new ArrayList<>();
		
		public List<SetterProvider<?>> labels = null;
		private List<LabelRegistration<?>> labelRegistrations = null;
		
		public CParserData(final Parser parser, final int count)
		{
			this.parser = parser;
			this.params = new ArrayList<>(count);
		}
		
		public void add(final ArgWrapper<?> item)
		{
			this.params.add(item);
		}
		
		public void add(final String keyword)
		{
			this.path.add(keyword);
		}
		
		private int index = 0;
		
		public ArgWrapper<?> get()
		{
			if (this.index >= this.params.size())
				return null;
			
			return this.params.get(this.index++);
		}
		
		public ArgWrapper<?> get(final int id)
		{
			this.index = id;
			return this.get();
		}
		
		public <T> CommandArg<T> get(final TypeID<T> type, final int id)
		{
			return ArgWrapper.get(type, this.get(id));
		}
		
		public <T> CommandArg<T> get(final TypeID<T> type)
		{
			return ArgWrapper.get(type, this.get());
		}
		
		private int pathIndex = 0;
		
		public String getPath()
		{
			if (this.pathIndex >= this.path.size())
				return null;
			
			return this.path.get(this.pathIndex++);
		}
		
		public String getPath(final int id)
		{
			this.pathIndex = id;
			return this.getPath();
		}
		
		public int size()
		{
			return this.params.size();
		}
		
		public boolean isEmpty()
		{
			return this.params.isEmpty();
		}
		
		public void addLabel(final SetterProvider<?> label)
		{
			if (this.labels == null)
				this.labels = new ArrayList<>();
			
			this.labels.add(label);
		}
		
		private int labelIndex = 0;
		
		public <T> Setter<T> getLabel(final TypeID<T> type)
		{
			if (this.labels == null)
				return null;
			
			if (this.labelIndex >= this.labels.size())
				return null;
			
			return this.labels.get(this.labelIndex++).getSetter(type);
		}
		
		public <T> Setter<T> getLabel(final TypeID<T> type, final int index)
		{
			this.labelIndex = index;
			return this.getLabel(type);
		}
		
		public void addLabelRegistration(final LabelRegistration<?> registration)
		{
			if (this.labelRegistrations == null)
				this.labelRegistrations = new ArrayList<>();
			
			this.labelRegistrations.add(registration);
		}
		
		private int labelRegistrationIndex = 0;
		
		public void registerLabel(final Parser parser) throws SyntaxErrorException
		{
			if (this.labelRegistrations == null || this.labelRegistrationIndex >= this.labelRegistrations.size())
				throw new IllegalArgumentException("Trying to register non-existing label");
			
			this.addLabel(this.labelRegistrations.get(this.labelRegistrationIndex++).register(parser));
		}
		
		public void registerLabel(final Parser parser, final int index) throws SyntaxErrorException
		{
			this.labelRegistrationIndex = index;
			this.registerLabel(parser);
		}
	}
	
	public CommandDescriptor(final IPermission permission, final WUEProvider usage)
	{
		this.permission = permission;
		this.usage = usage;
	}
	
	public CommandDescriptor(final IPermission permission, final WUEProvider usage, final Map<String, CommandDescriptor<? super D>> keywords)
	{
		this(permission, usage);
		
		for (final Entry<String, CommandDescriptor<? super D>> entry : keywords.entrySet())
			this.addSubType(entry.getKey(), entry.getValue());
	}
	
	public CommandDescriptor(final IPermission permission, final WUEProvider usage, final Set<Pair<Set<String>, CommandDescriptor<? super D>>> descriptors)
	{
		this(permission, usage);
		
		for (final Pair<Set<String>, CommandDescriptor<? super D>> descriptor : descriptors)
			for (final String name : descriptor.getKey())
				this.addSubType(name, descriptor.getValue());
	}
	
	public static final void clear()
	{
		commandCompletions.clear();
		commands.clear();
	}
	
	public abstract CommandArg<Integer> construct(final D data) throws SyntaxErrorException;
	
	public static void registerCommand(final String name, final CommandDescriptor<?> descriptor)
	{
		if (commands.put(name.toLowerCase(), descriptor) != null)
			throw new IllegalArgumentException("Command with name '" + name + "' already registered");
		
		commandCompletions.put(new TabCompletion(name), descriptor.permission);
	}
	
	public static void registerCommand(final Pair<Set<String>, ? extends CommandDescriptor<?>> descriptor)
	{
		final CommandDescriptor<?> baseDescriptor = descriptor.getValue();
		
		for (final String name : descriptor.getKey())
			registerCommand(name, baseDescriptor);
	}
	
	public void addSubType(final String key, final CommandDescriptor<? super D> descriptor)
	{
		if (this.keywords.put(key, descriptor) != null)
			throw new IllegalArgumentException("Keyword '" + key + "' already registered");
		
		if ("".equals(key))
			return;
		
		this.keywordCompletions.add(new TabCompletion(key));
	}
	
	public static final CommandDescriptor<?> getDescriptor(final String name)
	{
		return commands.get(name.toLowerCase());
	}
	
	public CommandDescriptor<? super D> getSubType(final Parser parser, final D data) throws SyntaxErrorException, CompletionException
	{
		return this.kp.parse(parser, data);
	}
	
	public CommandDescriptor<? super D> getSubType(final String keyword)
	{
		return this.keywords.get(keyword);
	}
	
	public final Set<ITabCompletion> getKeywordCompletions()
	{
		return this.keywordCompletions;
	}
	
	public D parserData(@SuppressWarnings("unused") final Parser parser) // TODO:...
	{
		return null;
	}
	
	public abstract void parse(final Parser parser, final D parserData, final WUEProvider usage) throws SyntaxErrorException, CompletionException; // TODO:...
	
	public static final Set<ITabCompletion> getCompletions()
	{
		return commandCompletions.keySet();
	}
	
	public static void addAlias(final String name, final String... aliases)
	{
		final CommandDescriptor<?> descriptor = getDescriptor(name);
		
		for (final String alias : aliases)
			registerCommand(alias, descriptor);
	}
}