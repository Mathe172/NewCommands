package net.minecraft.command.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;

import net.minecraft.command.MatcherRegistry;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.arg.CompoundArg;
import net.minecraft.command.arg.Processable;
import net.minecraft.command.collections.Types;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletion;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.type.IExParse;
import net.minecraft.command.type.IParse;
import net.minecraft.command.type.IType;
import net.minecraft.command.type.base.ExCustomParse;
import net.minecraft.command.type.custom.command.ParserCommands;
import net.minecraft.command.type.custom.command.TypeCommand;
import net.minecraft.command.type.management.CConvertable;
import net.minecraft.command.util.SnapList;
import net.minecraft.command.util.SnapMap;
import net.minecraft.entity.Entity;

public class Parser
{
	public final String toParse;
	public final int len;
	
	protected int index;
	
	private final SnapMap<String, ArgWrapper<?>> labels = new SnapMap<>();
	
	private final Stack<SnapList<Processable>> toProcess = new Stack<>();
	private final Stack<SnapList<Boolean>> ignoreErrors = new Stack<>();
	
	private final List<Matcher> matchers;
	
	public Context defContext;
	
	public Parser(final String toParse, final int startIndex, final boolean completionPatterns)
	{
		this.defContext = Context.defContext;
		
		this.index = startIndex;
		
		this.toParse = toParse;
		this.len = toParse.length();
		
		this.matchers = new ArrayList<>(MatcherRegistry.getCount());
		
		// The Completion-version of the pattern tricks the parser into thinking that the end is not yet reached, thus calling the subparsers for completions
		this.getMatcher(completionPatterns ? ParsingUtilities.endingMatcherCompletion : ParsingUtilities.endingMatcher);
	}
	
	public Parser(final String toParse, final int startIndex)
	{
		this(toParse, startIndex, false);
	}
	
	public Parser(final String toParse)
	{
		this(toParse, 0);
	}
	
	public static CommandArg<Integer> parseCommand(final String toParse, final int startIndex) throws SyntaxErrorException
	{
		final Parser parser = new Parser(toParse, startIndex);
		final CommandArg<Integer> ret;
		try
		{
			ret = parser.parseInit(ParserCommands.parser);
		} catch (final SyntaxErrorException e)
		{
			throw e;
		} catch (final Throwable t)
		{
			throw (SyntaxErrorException) ParsingUtilities.SEE("Fatal error while parsing command: " + t.getMessage()).initCause(t);
		}
		
		if (parser.endReached())
			return ret;
		
		throw parser.SEE("Unexpected ')' around index ");
	}
	
	public static CommandArg<Integer> parseCommand(final String toParse) throws SyntaxErrorException
	{
		return parseCommand(toParse, 0);
	}
	
	public static TCDSet parseCompletion(final CompletionData cData, final int startIndex)
	{
		final CompletionParser completionParser = new CompletionParser(cData.toMatch.substring(0, cData.cursorIndex), startIndex, cData);
		
		try
		{
			TypeCommand.parser.parse(completionParser);
		} catch (final SyntaxErrorException e)
		{
		} catch (final CompletionException e)
		{
		}
		
		return completionParser.getTCDSet();
	}
	
	public static TCDSet parseCompletion(final CompletionData cData)
	{
		return parseCompletion(cData, 0);
	}
	
	public static CommandArg<List<String>> parseStatsTarget(final String toParse) throws SyntaxErrorException
	{
		final Parser parser = new Parser(toParse, 0);
		
		final CommandArg<List<String>> ret;
		try
		{
			ret = parser.parseInit(Types.IPScoreHolderList);
		} catch (final SyntaxErrorException e)
		{
			throw e;
		} catch (final Throwable t)
		{
			throw (SyntaxErrorException) ParsingUtilities.SEE("Fatal error while parsing UUID-List: " + t.getMessage()).initCause(t);
		}
		
		if (parser.endReached())
			return ret;
		
		throw parser.SEE("Parsing endend unexpectedly around index ");
	}
	
	public static CommandArg<String> parseScoreHolder(final String toParse) throws SyntaxErrorException
	{
		final Parser parser = new Parser(toParse, 0);
		
		final CommandArg<String> ret;
		try
		{
			ret = parser.parseInit(Types.IPScoreHolder);
		} catch (final SyntaxErrorException e)
		{
			throw e;
		} catch (final Throwable t)
		{
			throw (SyntaxErrorException) ParsingUtilities.SEE("Fatal error while parsing UUID: " + t.getMessage()).initCause(t);
		}
		
		if (parser.endReached())
			return ret;
		
		throw parser.SEE("Parsing endend unexpectedly around index ");
	}
	
	public static CommandArg<List<Entity>> parseEntityList(final String toParse) throws SyntaxErrorException
	{
		final Parser parser = new Parser(toParse, 0);
		
		final CommandArg<List<Entity>> ret;
		try
		{
			ret = parser.parseInit(Types.IPEntityList);
		} catch (final SyntaxErrorException e)
		{
			throw e;
		} catch (final Throwable t)
		{
			throw (SyntaxErrorException) ParsingUtilities.SEE("Fatal error while parsing Entity-List: " + t.getMessage()).initCause(t);
		}
		
		if (parser.endReached())
			return ret;
		
		throw parser.SEE("Parsing endend unexpectedly around index ");
	}
	
	public Matcher getMatcher(final MatcherRegistry m)
	{
		final int ind = m.getId();
		
		if (ind < this.matchers.size())
		{
			Matcher ret = this.matchers.get(ind);
			
			if (ret != null)
				return ret;
			
			ret = m.matcher(this.toParse);
			
			this.matchers.set(ind, ret);
			
			return ret;
		}
		else
		{
			for (int i = this.matchers.size(); i < ind; ++i)
				this.matchers.add(null);
			
			final Matcher ret = m.matcher(this.toParse);
			
			this.matchers.add(ret);
			
			return ret;
		}
	}
	
	public SyntaxErrorException SEE(final String s)
	{
		return ParsingUtilities.SEE(s + this.index);
	}
	
	public void setIndexEnd()
	{
		this.index = this.len;
	}
	
	public boolean find(final Matcher m)
	{
		return m.find(this.index);
	}
	
	public boolean find(final MatcherRegistry m)
	{
		return this.find(this.getMatcher(m));
	}
	
	public void incIndex(final int amount)
	{
		this.index += amount;
	}
	
	public void incIndex(final Matcher m)
	{
		this.index += m.group().length();
	}
	
	public boolean findInc(final Matcher m)
	{
		final boolean ret = m.find(this.index);
		if (ret)
			this.index += m.group().length();
		
		return ret;
	}
	
	public boolean findInc(final MatcherRegistry m)
	{
		return this.findInc(this.getMatcher(m));
	}
	
	public int getIndex()
	{
		return this.index;
	}
	
	public boolean endReached()
	{
		return this.index == this.len;
	}
	
	public <R, D> CommandArg<R> parseInit(final IExParse<? extends CommandArg<R>, D> target, final D parserData) throws SyntaxErrorException, CompletionException
	{
		final List<Processable> toProcess = new ArrayList<>();
		final List<Boolean> ignoreErrors = new ArrayList<>();
		
		this.toProcess.push(new SnapList<>(toProcess));
		this.ignoreErrors.push(new SnapList<>(ignoreErrors));
		
		CommandArg<R> parsed;
		
		try
		{
			parsed = target.parse(this, parserData);
		} finally
		{
			this.toProcess.pop();
			this.ignoreErrors.pop();
		}
		
		if (toProcess.isEmpty())
			return parsed;
		
		return new CompoundArg<>(toProcess, ignoreErrors, parsed);
	}
	
	public <R> CommandArg<R> parseInit(final IParse<? extends CommandArg<R>> target) throws SyntaxErrorException, CompletionException
	{
		return this.parseInit(new ExCustomParse<CommandArg<R>, Void>()
		{
			@Override
			public final CommandArg<R> parse(final Parser parser, final Void Null) throws SyntaxErrorException, CompletionException
			{
				return target.parse(parser);
			}
		}, null);
	}
	
	public void terminateCompletion()
	{
	}
	
	public void proposeCompletion()
	{
	}
	
	public Set<ITabCompletion> getLabelCompletions()
	{
		final Set<ITabCompletion> completions = new HashSet<>(this.labels.size());
		
		for (final String name : this.labels.keySet())
			completions.add(new TabCompletion(name));
		
		return completions;
	}
	
	public Set<ITabCompletion> getLabelCompletions(final CConvertable<?, ?> target)
	{
		final Set<ITabCompletion> completions = new HashSet<>();
		
		for (final Entry<String, ArgWrapper<?>> entry : this.labels)
			if (target.convertableFrom(entry.getValue().type))
				completions.add(new TabCompletion(entry.getKey()));
		
		return completions;
	}
	
	public <R, D> R parse(final IType<R, D> target, final D parserData) throws SyntaxErrorException, CompletionException
	{
		return target.iParse(this, parserData);
	}
	
	public <R, D> R parseSnapshot(final IExParse<R, D> target, final D parserData) throws SyntaxErrorException, CompletionException
	{
		final int startIndex = this.getIndex();
		final int saveLabels = this.saveLabels();
		final int saveToProcess = this.saveToProcess();
		final int saveIgnoreErrors = this.saveIgnoreErrors();
		
		final Context defContext = this.defContext;
		
		try
		{
			return target.parse(this, parserData);
			
		} catch (final SyntaxErrorException e)
		{
			this.index = startIndex;
			this.restoreLabels(saveLabels);
			this.restoreToProcess(saveToProcess);
			this.restoreIgnoreErrors(saveIgnoreErrors);
			
			this.defContext = defContext;
			
			throw e;
		}
	}
	
	public final boolean checkSpace()
	{
		return this.find(ParsingUtilities.spaceMatcher);
	}
	
	public void addToProcess(final Processable toProcess)
	{
		this.toProcess.peek().add(toProcess);
	}
	
	public void addToProcess(final CommandArg<?> toProcess)
	{
		this.toProcess.peek().add(toProcess.processable());
	}
	
	public void addIgnoreErrors(final boolean ignoreError)
	{
		this.ignoreErrors.peek().add(ignoreError);
	}
	
	public int saveLabels()
	{
		return this.labels.save();
	}
	
	public int saveToProcess()
	{
		return this.toProcess.peek().save();
	}
	
	public int saveIgnoreErrors()
	{
		return this.ignoreErrors.peek().save();
	}
	
	public void restoreLabels(final int snapId)
	{
		this.labels.restore(snapId);
	}
	
	public void restoreToProcess(final int snapId)
	{
		this.toProcess.peek().restore(snapId);
	}
	
	public void restoreIgnoreErrors(final int snapId)
	{
		this.ignoreErrors.peek().restore(snapId);
	}
	
	public void addLabel(final String label, final ArgWrapper<?> value) throws SyntaxErrorException
	{
		if (!this.labels.put(label, value))
			throw this.SEE("Label " + label + " already in use around index ");
	}
	
	public ArgWrapper<?> getLabel(final String label)
	{
		return this.labels.get(label);
	}
	
}
