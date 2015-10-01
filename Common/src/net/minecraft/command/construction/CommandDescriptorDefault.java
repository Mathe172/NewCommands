package net.minecraft.command.construction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.command.IPermission;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.arg.PermissionWrapper;
import net.minecraft.command.arg.PermissionWrapper.Command;
import net.minecraft.command.arg.Setter;
import net.minecraft.command.arg.Setter.SetterProvider;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.completion.TabCompletion;
import net.minecraft.command.construction.CommandDescriptorDefault.CParserData;
import net.minecraft.command.descriptors.CommandDescriptor;
import net.minecraft.command.descriptors.ICommandDescriptor;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IExParse;
import net.minecraft.command.type.custom.TypeLabelDeclaration.LabelRegistration;
import net.minecraft.command.type.custom.command.ParserKeyword;
import net.minecraft.command.type.management.TypeID;

public abstract class CommandDescriptorDefault extends CommandDescriptor<CParserData>
{
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
	
	private final PatriciaTrie<ICommandDescriptor<? super CParserData>> keywords = new PatriciaTrie<>();
	
	private final Set<ITabCompletion> keywordCompletions = new HashSet<>();
	
	private final ParserKeyword kp = new ParserKeyword(this);
	
	private final List<IExParse<Void, ? super CParserData>> types;
	
	public CommandDescriptorDefault(final IPermission permission, final UsageProvider usage, final List<IExParse<Void, ? super CParserData>> types)
	{
		super(permission, usage);
		
		this.types = types;
	}
	
	public CommandDescriptorDefault(final IPermission permission, final UsageProvider usage, final Map<String, CommandDescriptor<? super CParserData>> keywords, final List<IExParse<Void, ? super CParserData>> types)
	{
		super(permission, usage);
		this.addSubDescriptors(keywords);
		
		this.types = types;
	}
	
	public CommandDescriptorDefault(final IPermission permission, final UsageProvider usage, final Set<Pair<Set<String>, CommandDescriptor<? super CParserData>>> descriptors, final List<IExParse<Void, ? super CParserData>> types)
	{
		super(permission, usage);
		this.addSubDescriptors(descriptors);
		
		this.types = types;
	}
	
	@Override
	public void parse(final Parser parser, final CParserData parserData, final UsageProvider usage) throws SyntaxErrorException
	{
		for (final IExParse<Void, ? super CParserData> type : this.types)
		{
			if (!parser.checkSpace())
				throw usage.createException(parser, parserData.path);
			
			type.parse(parser, parserData);
		}
	}
	
	@Override
	public Command parse(final Parser parser) throws SyntaxErrorException
	{
		ICommandDescriptor<? super CParserData> currDescriptor = this;
		
		IPermission permission = this.permission;
		UsageProvider usage = this.usage;
		
		final Matcher endingMatcher = parser.getMatcher(ParsingUtilities.endingMatcher);
		
		final CParserData data = new CParserData(parser, this.types.size());
		
		while (true)
		{
			currDescriptor.parse(parser, data, usage);
			
			if (parser.find(endingMatcher))
			{
				// CommandsParser REQUIRES that endingMatcher is in the following state: whitespaces processed + found match
				parser.incIndex(endingMatcher.group(1).length());
				
				final CommandArg<Integer> command = currDescriptor.construct(data);
				
				if (command == null)
					throw usage.createException(parser, data.path);
				
				return new PermissionWrapper.Command(command, permission);
			}
			
			if (!parser.checkSpace() || (currDescriptor = currDescriptor.getSubDescriptor(parser, data)) == null)
				throw usage.createException(parser, data.path);
			
			if (currDescriptor.permission != null)
				permission = currDescriptor.permission;
			
			if (currDescriptor.usage != null)
				usage = currDescriptor.usage;
		}
	}
	
	@Override
	public void addSubDescriptor(final String key, final ICommandDescriptor<? super CParserData> descriptor)
	{
		if (this.keywords.put(key, descriptor) != null)
			throw new IllegalArgumentException("Keyword '" + key + "' already registered");
		
		if ("".equals(key))
			return;
		
		this.keywordCompletions.add(new TabCompletion(key));
	}
	
	@Override
	public ICommandDescriptor<? super CParserData> getSubDescriptor(final String keyword)
	{
		return this.keywords.get(keyword);
	}
	
	@Override
	public ICommandDescriptor<? super CParserData> getSubDescriptor(final Parser parser, final CParserData data) throws SyntaxErrorException
	{
		return this.kp.parse(parser, data);
	}
	
	public final Set<ITabCompletion> getKeywordCompletions()
	{
		return this.keywordCompletions;
	}
}
