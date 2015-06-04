package net.minecraft.command.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;

import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.arg.LabelWrapper;
import net.minecraft.command.arg.Setter;
import net.minecraft.command.arg.Setter.SetterProvider;
import net.minecraft.command.collections.Types;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletion;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.type.IExParse;
import net.minecraft.command.type.IType;
import net.minecraft.command.type.custom.command.ParserCommands;
import net.minecraft.command.type.management.CConvertable;
import net.minecraft.command.type.management.TypeID;
import net.minecraft.entity.Entity;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.UnmodifiableIterator;

public class Parser
{
	public final String toParse;
	public final int len;
	
	protected int index;
	
	private final IVersionManager<?> versionManager;
	
	private final List<Matcher> matchers;
	
	public Context defContext;
	
	public final boolean catchStack;
	public boolean suppressEx;
	public final boolean debug;
	
	public Parser(final String toParse, final int startIndex, final boolean completionPatterns, final boolean catchStack, final boolean debug)
	{
		this.defContext = Context.defContext;
		
		this.index = startIndex;
		
		this.toParse = toParse;
		this.len = toParse.length();
		
		this.versionManager = this.newVersionManager();
		
		this.matchers = new ArrayList<>(MatcherRegistry.getCount());
		
		// The Completion-version of the pattern tricks the parser into thinking that the end is not yet reached, thus calling the subparsers for completions
		this.getMatcher(completionPatterns ? ParsingUtilities.endingMatcherCompletion : ParsingUtilities.endingMatcher);
		
		this.suppressEx = false;
		this.catchStack = catchStack;
		this.debug = debug;
	}
	
	public Parser(final String toParse, final int startIndex)
	{
		this(toParse, startIndex, false, false, false);
	}
	
	public Parser(final String toParse)
	{
		this(toParse, 0);
	}
	
	private SyntaxErrorException handleFatalError(final String messageStart, final Throwable t)
	{
		if (t instanceof StackOverflowError)
			return this.SEE(messageStart + " Too recursive", false);
		
		return this.SEE(messageStart + t.getMessage() + " ", t);
	}
	
	public static CommandArg<Integer> parseCommand(final String toParse, final int startIndex) throws SyntaxErrorException
	{
		return parseCommand(toParse, startIndex, false);
	}
	
	public static CommandArg<Integer> parseCommand(final String toParse, final int startIndex, final boolean debug) throws SyntaxErrorException
	{
		return new Parser(toParse, startIndex, false, false, debug).parseCommand();
	}
	
	public CommandArg<Integer> parseCommand() throws SyntaxErrorException
	{
		final CommandArg<Integer> ret;
		try
		{
			ret = ParserCommands.parse(this, false);
		} catch (final SyntaxErrorException e)
		{
			throw e;
		} catch (final Throwable t)
		{
			throw this.handleFatalError("Fatal error while parsing command: ", t);
		}
		
		if (this.endReached())
			return ret;
		
		throw this.SEE("Unexpected ')' ");
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
			ParserCommands.parse(completionParser, false);
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
			ret = Types.scoreHolderList.parse(parser).arg();
		} catch (final SyntaxErrorException e)
		{
			throw e;
		} catch (final Throwable t)
		{
			throw parser.handleFatalError("Fatal error while parsing UUID-List: ", t);
		}
		
		if (parser.endReached())
			return ret;
		
		throw parser.SEE("Parsing endend unexpectedly ");
	}
	
	public static CommandArg<String> parseScoreHolder(final String toParse) throws SyntaxErrorException
	{
		final Parser parser = new Parser(toParse, 0);
		
		final CommandArg<String> ret;
		try
		{
			ret = Types.scoreHolder.parse(parser).arg();
		} catch (final SyntaxErrorException e)
		{
			throw e;
		} catch (final Throwable t)
		{
			throw parser.handleFatalError("Fatal error while parsing UUID: ", t);
		}
		
		if (parser.endReached())
			return ret;
		
		throw parser.SEE("Parsing endend unexpectedly ");
	}
	
	public static CommandArg<List<Entity>> parseEntityList(final String toParse) throws SyntaxErrorException
	{
		final Parser parser = new Parser(toParse, 0);
		
		final CommandArg<List<Entity>> ret;
		try
		{
			ret = Types.entityList.parse(parser).arg();
		} catch (final SyntaxErrorException e)
		{
			throw e;
		} catch (final Throwable t)
		{
			throw parser.handleFatalError("Fatal error while parsing Entity-List: ", t);
		}
		
		if (parser.endReached())
			return ret;
		
		throw parser.SEE("Parsing endend unexpectedly ");
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
		
		for (int i = this.matchers.size(); i < ind; ++i)
			this.matchers.add(null);
		
		final Matcher ret = m.matcher(this.toParse);
		
		this.matchers.add(ret);
		
		return ret;
	}
	
	public SyntaxErrorException SEE(final Object... errorObjects)
	{
		return this.SEE("commands.generic.syntax", false, null, errorObjects);
	}
	
	public SyntaxErrorException SEE(final String s, final Object... errorObjects)
	{
		return this.SEE(s, true, null, errorObjects);
	}
	
	public SyntaxErrorException SEE(final String s, final Throwable cause, final Object... errorObjects)
	{
		return this.SEE(s, true, cause, errorObjects);
	}
	
	public SyntaxErrorException SEE(final String s, final boolean appendIndex, final Object... errorObjects)
	{
		return this.SEE(s, appendIndex, null, errorObjects);
	}
	
	public SyntaxErrorException SEE(final String s, final boolean appendIndex)
	{
		return this.SEE(s, appendIndex, null, new Object[0]);
	}
	
	public SyntaxErrorException SEE(final String s, final String postfix, final Object... errorObjects)
	{
		return this.SEE(s, postfix, null, errorObjects);
	}
	
	public SyntaxErrorException SEE(final String s, final String postfix)
	{
		return this.SEE(s, postfix, null, new Object[0]);
	}
	
	public SyntaxErrorException SEE(final String s, final String postfix, final Throwable cause, final Object... errorObjects)
	{
		final int start = this.index > 3 ? this.index - 4 : 0;
		
		final int end = this.index < this.toParse.length() - 4 ? this.index + 3 : this.toParse.length();
		
		return this.createSEE(
			s
				+ "around index "
				+ this.index
				+ postfix
				+ (start > 0 ? " (…" : " (")
				+ this.toParse.substring(start, this.index)
				+ "|"
				+ this.toParse.substring(this.index, end)
				+ (end < this.toParse.length() ? "…)" : ")"),
			cause,
			errorObjects);
	}
	
	public SyntaxErrorException SEE(final String s, final boolean appendIndex, final Throwable cause, final Object... errorObjects)
	{
		if (!appendIndex)
			return this.createSEE(s, cause, errorObjects);
		
		return this.SEE(s, "", cause, errorObjects);
	}
	
	private SyntaxErrorException createSEE(final String s, final Throwable cause, final Object... errorObjects)
	{
		if (this.suppressEx)
			return SyntaxErrorException.see;
		
		return new SyntaxErrorException(s, cause, true, this.catchStack, errorObjects);
	}
	
	public WrongUsageException WUE(final String message, final Object... errorObjects)
	{
		if (this.suppressEx)
			return WrongUsageException.wue;
		
		return new WrongUsageException(message, true, this.catchStack, errorObjects);
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
	
	public void terminateCompletion()
	{
	}
	
	public void proposeCompletion()
	{
	}
	
	public Set<ITabCompletion> getLabelCompletions()
	{
		final Set<ITabCompletion> completions = new HashSet<>();
		
		for (final String name : this.versionManager.labelKeysIterable())
			completions.add(new TabCompletion(name));
		
		return completions;
	}
	
	public Set<ITabCompletion> getLabelCompletions(final CConvertable<?, ?> target)
	{
		final Set<ITabCompletion> completions = new HashSet<>();
		
		for (final Entry<String, LabelWrapper<?>> entry : this.versionManager.labelIterable())
			if (target.convertableFrom(entry.getValue().type))
				completions.add(new TabCompletion(entry.getKey()));
		
		return completions;
	}
	
	public <R, D> R parse(final IType<R, D> target, final D parserData) throws SyntaxErrorException, CompletionException
	{
		return target.iParse(this, parserData);
	}
	
	protected boolean snapshot = false;
	
	public <R, D> R parseSnapshot(final IExParse<R, D> target, final D parserData) throws SyntaxErrorException, CompletionException
	{
		final boolean saveSnapshot = this.snapshot;
		this.snapshot = true;
		
		final int startIndex = this.getIndex();
		
		final IVersionManager<?>.Version version = this.versionManager.saveSnapshot();
		
		final Context defContext = this.defContext;
		
		try
		{
			return target.parse(this, parserData);
			
		} catch (final SyntaxErrorException e)
		{
			this.index = startIndex;
			
			version.restore();
			
			this.defContext = defContext;
			
			throw e;
		} finally
		{
			this.snapshot = saveSnapshot;
		}
	}
	
	public void addLabel(final String label, final LabelWrapper<?> value) throws SyntaxErrorException
	{
		this.versionManager.addLabel(label, value);
	}
	
	public <T> SetterProvider<T> getLabelSetterTyped(final String label, final TypeID<T> type, final boolean allowConversion) throws SyntaxErrorException
	{
		return this.getLabelSafe(label).getLabelSetterTyped(this, type, allowConversion);
	}
	
	public <T> Setter<T> getLabelSetter(final String label, final TypeID<T> type, final boolean allowConversion) throws SyntaxErrorException
	{
		return this.getLabelSafe(label).getLabelSetter(this, type, allowConversion);
	}
	
	public LabelWrapper<?> getLabel(final String label)
	{
		return this.versionManager.getLabel(label);
	}
	
	public LabelWrapper<?> getLabelSafe(final String label) throws SyntaxErrorException
	{
		final LabelWrapper<?> ret = this.getLabel(label);
		
		if (ret == null)
			throw this.SEE("Label '" + label + "' not registered ");
		
		return ret;
	}
	
	public ArgWrapper<?> parseCached(final IType<? extends ArgWrapper<?>, Context> target, final Context context, final CacheID cacheID) throws SyntaxErrorException, CompletionException
	{
		return this.versionManager.parseCached(target, context, cacheID);
	}
	
	public final boolean checkSpace()
	{
		return this.find(ParsingUtilities.spaceMatcher);
	}
	
	protected static abstract class IParserState
	{
		public final int index;
		public final Context defContext;
		
		public IParserState(final int index, final Context defContext)
		{
			this.index = index;
			this.defContext = defContext;
		}
	}
	
	protected static class ParserState extends IParserState
	{
		public final Context context;
		public final CacheID id;
		
		public ParserState(final int index, final Context defContext, final Context context, final CacheID id)
		{
			super(index, defContext);
			this.context = context;
			this.id = id;
		}
		
		@Override
		public boolean equals(final Object other)
		{
			if (!(other instanceof ParserState))
				return false;
			
			final ParserState state = (ParserState) other;
			
			return this.index == state.index && this.defContext == state.defContext && this.context == state.context && this.id == state.id;
		}
		
		@Override
		public int hashCode()
		{
			return this.index ^ this.defContext.hashCode() ^ this.context.hashCode() ^ this.id.hashCode();
		}
	}
	
	protected static class IParserStateRes<S extends IParserStateRes<S>> extends IParserState
	{
		public final IVersionManager<S>.Version version;
		
		public IParserStateRes(final int index, final Context defContext, final IVersionManager<S>.Version version)
		{
			super(index, defContext);
			this.version = version;
		}
	}
	
	protected static class ParserStateRes extends IParserStateRes<ParserStateRes>
	{
		public ParserStateRes(final int index, final Context defContext, final IVersionManager<ParserStateRes>.Version version)
		{
			super(index, defContext, version);
		}
	}
	
	protected IVersionManager<?> newVersionManager()
	{
		return new VersionManager();
	}
	
	public abstract class IVersionManager<S extends IParserStateRes<S>>
	{
		protected final class Version
		{
			protected int versionNumber;
			
			public Version(final int versionNumber)
			{
				this.versionNumber = versionNumber;
			}
			
			Map<ParserState, Pair<S, ArgWrapper<?>>> cachedResults = null;
			
			protected Version next = null;
			
			protected Pair<S, ArgWrapper<?>> getCachedResult(final ParserState state)
			{
				return this.cachedResults == null ? null : this.cachedResults.get(state);
			}
			
			protected void addCachedResult(final ParserState initialState, final Pair<S, ArgWrapper<?>> result)
			{
				if (this.cachedResults == null)
					this.cachedResults = new HashMap<>();
				
				this.cachedResults.put(initialState, result);
			}
			
			protected boolean invalidateTail()
			{
				Version curr = this.next;
				
				if (curr == null)
					return false;
				
				do
				{
					curr.versionNumber = Integer.MAX_VALUE;
					curr.cachedResults = null;
				} while ((curr = curr.next) != null);
				
				this.next = null;
				
				return true;
			}
			
			protected boolean valid()
			{
				return this.versionNumber != Integer.MAX_VALUE;
			}
			
			protected boolean valid(final int versionNumber)
			{
				return this.versionNumber <= versionNumber;
			}
			
			protected void restore()
			{
				IVersionManager.this.restoreSnapshot(this);
			}
		}
		
		private int versionNumber;
		
		protected Version version;
		
		protected boolean changed = true;
		
		private final Map<String, Pair<Version, LabelWrapper<?>>> labels;
		
		public IVersionManager()
		{
			this.versionNumber = 0;
			
			this.version = new Version(0);
			
			this.labels = new HashMap<>();
		}
		
		public void applyChange()
		{
			if (this.changed)
				return;
			
			this.changed = true;
			
			this.version.invalidateTail();
			
			this.version = (this.version.next = new Version(++this.versionNumber));
		}
		
		public ParserState getInitState(final Context context, final CacheID id)
		{
			return new ParserState(Parser.this.index, Parser.this.defContext, context, id);
		}
		
		public void setState(final S state)
		{
			this.version = state.version;
			this.versionNumber = this.version.versionNumber;
			
			this.changed = false;
			Parser.this.index = state.index;
			Parser.this.defContext = state.defContext;
		}
		
		public abstract Pair<S, ArgWrapper<?>> parseFetchState(IType<? extends ArgWrapper<?>, Context> target, Context context) throws SyntaxErrorException, CompletionException;
		
		public ArgWrapper<?> parseCached(final IType<? extends ArgWrapper<?>, Context> target, final Context context, final CacheID cacheID) throws SyntaxErrorException, CompletionException
		{
			if (this.changed || !Parser.this.snapshot)
				return Parser.this.parse(target, context);
			
			final Version initVersion = this.version;
			
			final ParserState initialState = this.getInitState(context, cacheID);
			
			Pair<S, ArgWrapper<?>> res = initVersion.getCachedResult(initialState);
			
			if (res != null && res.getLeft().version.valid())
			{
				this.applyCompletion(target, initialState, res.getLeft());
				
				this.setState(res.getLeft());
				
				return res.getRight();
			}
			
			res = this.parseFetchState(target, context);
			
			initVersion.addCachedResult(initialState, res);
			
			return res.getRight();
		}
		
		@SuppressWarnings("unused")
		protected void applyCompletion(final IType<? extends ArgWrapper<?>, Context> target, final ParserState initialState, final S newState)
		{
		}
		
		protected Version saveSnapshot()
		{
			this.changed = false;
			
			return this.version;
		}
		
		protected void restoreSnapshot(final Version version)
		{
			this.version = version;
			this.versionNumber = version.versionNumber;
			
			this.changed = false;
		}
		
		private boolean putLabel(final String label, final LabelWrapper<?> value)
		{
			final Pair<Version, LabelWrapper<?>> arg = this.labels.get(label);
			
			if (arg != null && arg.getLeft().valid(this.versionNumber))
				return false;
			
			this.labels.put(label, new ImmutablePair<Version, LabelWrapper<?>>(this.version, value));
			
			return true;
		}
		
		public void addLabel(final String label, final LabelWrapper<?> value) throws SyntaxErrorException
		{
			if (!this.putLabel(label, value))
				throw Parser.this.SEE("Label '" + label + "' already in use (", ")");
		}
		
		public LabelWrapper<?> getLabel(final String label)
		{
			final Pair<Version, LabelWrapper<?>> ret = this.labels.get(label);
			
			if (ret == null || !ret.getLeft().valid(this.versionNumber))
				return null;
			
			return ret.getRight();
		}
		
		public Iterable<Entry<String, LabelWrapper<?>>> labelIterable()
		{
			return new Iterable<Map.Entry<String, LabelWrapper<?>>>()
			{
				@Override
				public Iterator<Entry<String, LabelWrapper<?>>> iterator()
				{
					return IVersionManager.this.labelIterator();
				}
			};
		}
		
		public Iterable<String> labelKeysIterable()
		{
			return new Iterable<String>()
			{
				@Override
				public Iterator<String> iterator()
				{
					return IVersionManager.this.labelKeysIterator();
				}
			};
		}
		
		public UnmodifiableIterator<Entry<String, LabelWrapper<?>>> labelIterator()
		{
			final Iterator<Entry<String, Pair<IVersionManager<S>.Version, LabelWrapper<?>>>> it = this.labelEntryIterator();
			
			return new UnmodifiableIterator<Map.Entry<String, LabelWrapper<?>>>()
			{
				@Override
				public boolean hasNext()
				{
					return it.hasNext();
				}
				
				@Override
				public Entry<String, LabelWrapper<?>> next()
				{
					final Entry<String, Pair<IVersionManager<S>.Version, LabelWrapper<?>>> value = it.next();
					
					return new ImmutablePair<String, LabelWrapper<?>>(value.getKey(), value.getValue().getRight());
				}
			};
		}
		
		public UnmodifiableIterator<String> labelKeysIterator()
		{
			final Iterator<Entry<String, Pair<IVersionManager<S>.Version, LabelWrapper<?>>>> it = this.labelEntryIterator();
			
			return new UnmodifiableIterator<String>()
			{
				@Override
				public boolean hasNext()
				{
					return it.hasNext();
				}
				
				@Override
				public String next()
				{
					final Entry<String, Pair<IVersionManager<S>.Version, LabelWrapper<?>>> value = it.next();
					
					return value.getKey();
				}
			};
		}
		
		private Iterator<Entry<String, Pair<IVersionManager<S>.Version, LabelWrapper<?>>>> labelEntryIterator()
		{
			final Iterator<Entry<String, Pair<IVersionManager<S>.Version, LabelWrapper<?>>>> it = this.labels.entrySet().iterator();
			
			final int versionNumber = this.versionNumber;
			
			return new UnmodifiableIterator<Entry<String, Pair<IVersionManager<S>.Version, LabelWrapper<?>>>>()
			{
				private Entry<String, Pair<IVersionManager<S>.Version, LabelWrapper<?>>> next = this.getNext();
				
				private Entry<String, Pair<IVersionManager<S>.Version, LabelWrapper<?>>> getNext()
				{
					while (it.hasNext())
					{
						final Entry<String, Pair<IVersionManager<S>.Version, LabelWrapper<?>>> entry = it.next();
						
						if (entry.getValue().getLeft().valid(versionNumber))
							return entry;
					}
					
					return null;
				}
				
				@Override
				public boolean hasNext()
				{
					return this.next != null;
				}
				
				@Override
				public Entry<String, Pair<IVersionManager<S>.Version, LabelWrapper<?>>> next()
				{
					final Entry<String, Pair<IVersionManager<S>.Version, LabelWrapper<?>>> ret = this.next;
					this.next = this.getNext();
					return ret;
				}
			};
		}
	}
	
	private class VersionManager extends IVersionManager<ParserStateRes>
	{
		@Override
		public Pair<ParserStateRes, ArgWrapper<?>> parseFetchState(final IType<? extends ArgWrapper<?>, Context> target, final Context context) throws SyntaxErrorException, CompletionException
		{
			final ArgWrapper<?> res = Parser.this.parse(target, context);
			
			this.saveSnapshot();
			
			final ParserStateRes state = new ParserStateRes(Parser.this.getIndex(), Parser.this.defContext, this.version);
			
			return new ImmutablePair<ParserStateRes, ArgWrapper<?>>(state, res);
		}
	}
}
