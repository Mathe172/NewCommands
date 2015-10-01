package net.minecraft.command.descriptors;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.command.IPermission;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.PermissionWrapper.Command;
import net.minecraft.command.collections.Commands;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.completion.TabCompletion;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.custom.command.ParserCommand;

public abstract class CommandDescriptor<D> extends ICommandDescriptor<D>
{
	public static final Map<ITabCompletion, IPermission> commandCompletions = new IdentityHashMap<>();
	private static final PatriciaTrie<CommandDescriptor<?>> commands = new PatriciaTrie<>();
	
	public CommandDescriptor(final IPermission permission, final UsageProvider usage)
	{
		super(usage, permission);
	}
	
	protected void addSubDescriptors(final Map<String, CommandDescriptor<? super D>> keywords)
	{
		for (final Entry<String, CommandDescriptor<? super D>> entry : keywords.entrySet())
			this.addSubDescriptor(entry.getKey(), entry.getValue());
	}
	
	protected void addSubDescriptors(final Set<Pair<Set<String>, CommandDescriptor<? super D>>> descriptors)
	{
		for (final Pair<Set<String>, CommandDescriptor<? super D>> descriptor : descriptors)
			for (final String name : descriptor.getKey())
				this.addSubDescriptor(name, descriptor.getValue());
	}
	
	public static final void init()
	{
		commands.put("?", Commands.helpDescriptor.getRight());
		commandCompletions.put(new TabCompletion.Escaped("?"), Commands.helpDescriptor.getRight().permission);
	}
	
	public static final void clear()
	{
		commandCompletions.clear();
		commands.clear();
	}
	
	public static void registerCommand(final String name, final CommandDescriptor<?> descriptor)
	{
		if (!ParsingUtilities.nameMatcher.pattern.matcher(name).matches())
			throw new IllegalArgumentException("Illegal command name '" + name + "'");
		
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
	
	public static final CommandDescriptor<?> getDescriptor(final String name)
	{
		return commands.get(name.toLowerCase());
	}
	
	/**
	 * Do NOT call outside {@link ParserCommand}
	 * {@link ParsingUtilities#endingMatcher endingMatcher} has to be in the following state after this call: whitespaces processed + found match
	 */
	public abstract Command parse(final Parser parser) throws SyntaxErrorException;
	
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
	
	public static List<Entry<String, CommandDescriptor<?>>> getCommands()
	{
		return new ArrayList<>(commands.entrySet());
	}
}