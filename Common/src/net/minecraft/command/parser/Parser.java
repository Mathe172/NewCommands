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

import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.UnmodifiableIterator;

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
import net.minecraft.command.type.ICachedParse;
import net.minecraft.command.type.IExParse;
import net.minecraft.command.type.custom.command.ParserCommands;
import net.minecraft.command.type.management.CConvertable;
import net.minecraft.command.type.management.TypeID;
import net.minecraft.command.type.metadata.MetaProvider;
import net.minecraft.entity.Entity;

public class Parser
{
	public final String toParse;
	public final int len;
	
	protected int index;
	
	private final IVersionManager<?, ?> versionManager;
	
	private final List<Matcher> matchers;
	
	public Context defContext;
	
	public final boolean catchStack;
	public boolean suppressEx;
	
	public Parser(final String toParse, final int startIndex, final boolean catchStack)
	{
		this.defContext = Context.defContext;
		
		this.index = startIndex;
		
		this.toParse = toParse;
		this.len = toParse.length();
		
		this.versionManager = this.newVersionManager();
		
		this.matchers = new ArrayList<>(MatcherRegistry.getCount());
		
		this.suppressEx = false;
		this.catchStack = catchStack;
	}
	
	public Parser(final String toParse, final int startIndex)
	{
		this(toParse, startIndex, false);
	}
	
	public Parser(final String toParse)
	{
		this(toParse, 0);
	}
	
	@Override
	public String toString()
	{
		return this.toParse + '\n' + StringUtils.repeat(' ', this.index) + '^';
	}
	
	protected SyntaxErrorException handleFatalError(final String messageStart, final Throwable t)
	{
		if (t instanceof StackOverflowError)
			return this.SEE(messageStart + " Too recursive", false);
		
		return this.SEE(messageStart + t.getMessage() + " ", t);
	}
	
	public static CommandArg<Integer> parseCommand(final String toParse, final int startIndex) throws SyntaxErrorException
	{
		return new Parser(toParse, startIndex, false).parseCommand();
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
			completionParser.complete(true, 0);
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
		final int id = m.getId();
		
		if (id < this.matchers.size())
		{
			Matcher ret = this.matchers.get(id);
			
			if (ret != null)
				return ret;
			
			ret = m.matcher(this.toParse);
			
			this.matchers.set(id, ret);
			
			return ret;
		}
		
		for (int i = this.matchers.size(); i < id; ++i)
			this.matchers.add(null);
		
		final Matcher ret = m.matcher(this.toParse);
		
		this.matchers.add(ret);
		
		return ret;
	}
	
	@SuppressWarnings("unused")
	public <D> boolean pushMetadata(final MetaProvider<D> data, final D parserData)
	{
		return false;
	}
	
	@SuppressWarnings("unused")
	public void popMetadata(final MetaProvider<?> data)
	{
	}
	
	/**
	 * Calls {@link #supplyHint(Hint, D)} with <code>null</code> as second argument
	 */
	public void supplyHint(final MetaProvider<?> hint)
	{
		this.supplyHint(hint, null);
	}
	
	@SuppressWarnings("unused")
	public <D> void supplyHint(final MetaProvider<D> hint, final D data)
	{
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
	
	public boolean find(final Matcher m)
	{
		return m.find(this.index);
	}
	
	public boolean find(final MatcherRegistry m)
	{
		return this.find(this.getMatcher(m));
	}
	
	public void incIndex()
	{
		++this.index;
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
	
	/**
	 * Does not check if index valid
	 */
	public char consumeNextChar()
	{
		return this.toParse.charAt(this.index++);
	}
	
	public int getIndex()
	{
		return this.index;
	}
	
	public boolean endReached()
	{
		return this.index == this.len;
	}
	
	public final boolean checkSpace()
	{
		return this.find(ParsingUtilities.spaceMatcher);
	}
	
	public boolean isSnapshot()
	{
		return this.versionManager.isSnapshot();
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
	
	public <T, D> T parseSnapshot(final IExParse<T, D> target, final D parserData) throws SyntaxErrorException
	{
		return this.versionManager.parseSnapshot(target, parserData);
	}
	
	public ArgWrapper<?> parseCached(final ICachedParse target, final Context context, final CacheID cacheID) throws SyntaxErrorException
	{
		return this.versionManager.parseCached(target, context, cacheID);
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
		
		@Override
		public boolean equals(final Object other)
		{
			if (!(other instanceof ParserState))
				return false;
			
			final ParserState state = (ParserState) other;
			
			return this.index == state.index && this.defContext.equals(state.defContext);
		}
		
		@Override
		public int hashCode()
		{
			return this.index ^ this.defContext.hashCode();
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
			
			return super.equals(state) && this.context.equals(state.context) && this.id == state.id;
		}
		
		@Override
		public int hashCode()
		{
			return super.hashCode() ^ this.context.hashCode() ^ this.id.hashCode();
		}
	}
	
	protected static abstract class IResParserState<S extends IResParserState<S>> extends IParserState
	{
		public abstract ArgWrapper<?> res() throws SyntaxErrorException;
		
		public final Version<S> version;
		
		public IResParserState(final int index, final Context defContext, final IVersionManager<S, ?> versionManager)
		{
			super(index, defContext);
			
			this.version = versionManager.useVersion();
		}
	}
	
	protected static abstract class ISnapshotState<R extends IResParserState<R>> extends IParserState
	{
		public final Version<R> version;
		
		public ISnapshotState(final int index, final Context defContext, final IVersionManager<R, ?> versionManager)
		{
			super(index, defContext);
			this.version = versionManager.useVersion();
		}
	}
	
	protected IVersionManager<?, ?> newVersionManager()
	{
		return new VersionManager();
	}
	
	protected static final class Version<R extends IResParserState<R>>
	{
		protected int versionNumber;
		
		public Version(final int versionNumber)
		{
			this.versionNumber = versionNumber;
		}
		
		Map<ParserState, R> cachedResults = null;
		
		protected Version<R> next = null;
		
		protected R getCachedResult(final ParserState state)
		{
			return this.cachedResults == null ? null : this.cachedResults.get(state);
		}
		
		protected void addCachedResult(final ParserState initialState, final R result)
		{
			if (this.cachedResults == null)
				this.cachedResults = new HashMap<>();
			
			this.cachedResults.put(initialState, result);
		}
		
		protected boolean invalidateTail()
		{
			Version<R> curr = this.next;
			
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
	}
	
	public abstract class IVersionManager<R extends IResParserState<R>, S extends ISnapshotState<R>>
	{
		private int versionNumber;
		
		protected Version<R> version;
		
		protected boolean changed = true;
		protected int snapshotCount = 0;
		
		private final PatriciaTrie<Pair<Version<R>, LabelWrapper<?>>> labels;
		
		public IVersionManager()
		{
			this.versionNumber = 0;
			
			this.version = new Version<>(0);
			
			this.labels = new PatriciaTrie<>();
		}
		
		public Version<R> useVersion()
		{
			this.changed = false;
			
			return this.version;
		}
		
		public void changed()
		{
			if (this.changed)
				return;
			
			this.changed = true;
			
			this.version.invalidateTail();
			
			this.version = (this.version.next = new Version<>(++this.versionNumber));
		}
		
		protected ParserState getInitState(final Context context, final CacheID id)
		{
			return new ParserState(Parser.this.index, Parser.this.defContext, context, id);
		}
		
		public void setState(final R state)
		{
			this.changed = false;
			this.version = state.version;
			this.versionNumber = this.version.versionNumber;
			
			Parser.this.index = state.index;
			Parser.this.defContext = state.defContext;
		}
		
		public ArgWrapper<?> parseCached(final ICachedParse target, final Context context, final CacheID cacheID) throws SyntaxErrorException
		{
			if (this.changed || !this.isSnapshot())
				return target.iCachedParse(Parser.this, context);
			
			final Version<R> initVersion = this.version;
			
			final ParserState initialState = this.getInitState(context, cacheID);
			
			if (initialState == null)
				return target.iCachedParse(Parser.this, context);
			
			R res = initVersion.getCachedResult(initialState);
			
			if (res != null && res.version.valid())
			{
				this.setState(res);
				
				return res.res();
			}
			
			res = this.parseFetchState(target, context);
			
			initVersion.addCachedResult(initialState, res);
			
			return res.res();
		}
		
		public boolean isSnapshot()
		{
			return this.snapshotCount > 0;
		}
		
		/**
		 * Exceptions can be caught, as long as they are rethrown by the {@link ResParserState#res()} method of the return value
		 */
		protected abstract R parseFetchState(final ICachedParse target, final Context context) throws SyntaxErrorException;
		
		protected abstract S saveSnapshot();
		
		protected void restoreSnapshot(final S state)
		{
			Parser.this.index = state.index;
			Parser.this.defContext = state.defContext;
			
			this.restoreSnapshot(state.version);
		}
		
		@SuppressWarnings("unused")
		protected void passSnapshot(final S state)
		{
		}
		
		@SuppressWarnings("unused")
		protected void finalizeSnapshot(final S state)
		{
		}
		
		protected void restoreSnapshot(final Version<R> version)
		{
			this.version = version;
			this.versionNumber = version.versionNumber;
			
			this.changed = false;
		}
		
		public <T, D> T parseSnapshot(final IExParse<T, D> target, final D parserData) throws SyntaxErrorException
		{
			final S snapshot = this.saveSnapshot();
			++this.snapshotCount;
			
			try
			{
				final T ret = target.parse(Parser.this, parserData);
				this.passSnapshot(snapshot);
				return ret;
				
			} catch (final SyntaxErrorException e)
			{
				this.restoreSnapshot(snapshot);
				
				throw e;
			} finally
			{
				this.finalizeSnapshot(snapshot);
				--this.snapshotCount;
			}
		}
		
		private boolean putLabel(final String label, final LabelWrapper<?> value)
		{
			final Pair<Version<R>, LabelWrapper<?>> arg = this.labels.get(label);
			
			if (arg != null && arg.getLeft().valid(this.versionNumber))
				return false;
			
			this.labels.put(label, new ImmutablePair<Version<R>, LabelWrapper<?>>(this.version, value));
			
			return true;
		}
		
		public void addLabel(final String label, final LabelWrapper<?> value) throws SyntaxErrorException
		{
			if (!this.putLabel(label, value))
				throw Parser.this.SEE("Label '" + label + "' already in use (", ")");
		}
		
		public LabelWrapper<?> getLabel(final String label)
		{
			final Pair<Version<R>, LabelWrapper<?>> ret = this.labels.get(label);
			
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
			final Iterator<Entry<String, Pair<Version<R>, LabelWrapper<?>>>> it = this.labelEntryIterator();
			
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
					final Entry<String, Pair<Version<R>, LabelWrapper<?>>> value = it.next();
					
					return new ImmutablePair<String, LabelWrapper<?>>(value.getKey(), value.getValue().getRight());
				}
			};
		}
		
		public UnmodifiableIterator<String> labelKeysIterator()
		{
			final Iterator<Entry<String, Pair<Version<R>, LabelWrapper<?>>>> it = this.labelEntryIterator();
			
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
					final Entry<String, Pair<Version<R>, LabelWrapper<?>>> value = it.next();
					
					return value.getKey();
				}
			};
		}
		
		private UnmodifiableIterator<Entry<String, Pair<Version<R>, LabelWrapper<?>>>> labelEntryIterator()
		{
			final Iterator<Entry<String, Pair<Version<R>, LabelWrapper<?>>>> it = this.labels.entrySet().iterator();
			
			final int versionNumber = this.versionNumber;
			
			return new UnmodifiableIterator<Entry<String, Pair<Version<R>, LabelWrapper<?>>>>()
			{
				private Entry<String, Pair<Version<R>, LabelWrapper<?>>> next = this.getNext();
				
				private Entry<String, Pair<Version<R>, LabelWrapper<?>>> getNext()
				{
					while (it.hasNext())
					{
						final Entry<String, Pair<Version<R>, LabelWrapper<?>>> entry = it.next();
						
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
				public Entry<String, Pair<Version<R>, LabelWrapper<?>>> next()
				{
					final Entry<String, Pair<Version<R>, LabelWrapper<?>>> ret = this.next;
					this.next = this.getNext();
					return ret;
				}
			};
		}
	}
	
	protected static abstract class ResParserState extends IResParserState<ResParserState>
	{
		public ResParserState(final int index, final Context defContext, final IVersionManager<ResParserState, ?> versionManager)
		{
			super(index, defContext, versionManager);
		}
		
		protected static class Success extends ResParserState
		{
			private final ArgWrapper<?> res;
			
			public Success(final int index, final Context defContext, final IVersionManager<ResParserState, ?> versionManager, final ArgWrapper<?> res)
			{
				super(index, defContext, versionManager);
				this.res = res;
			}
			
			@Override
			public ArgWrapper<?> res() throws SyntaxErrorException
			{
				return this.res;
			}
		}
		
		protected static class Error extends ResParserState
		{
			private final SyntaxErrorException ex;
			
			public Error(final int index, final Context defContext, final IVersionManager<ResParserState, ?> versionManager, final SyntaxErrorException ex)
			{
				super(index, defContext, versionManager);
				this.ex = ex;
			}
			
			@Override
			public ArgWrapper<?> res() throws SyntaxErrorException
			{
				throw this.ex;
			}
		}
	}
	
	protected static class SnapshotState extends ISnapshotState<ResParserState>
	{
		public SnapshotState(final int index, final Context defContext, final IVersionManager<ResParserState, ?> versionManager)
		{
			super(index, defContext, versionManager);
		}
	}
	
	private class VersionManager extends IVersionManager<ResParserState, SnapshotState>
	{
		@Override
		protected ResParserState parseFetchState(final ICachedParse target, final Context context) throws SyntaxErrorException
		{
			try
			{
				final ArgWrapper<?> res = target.iCachedParse(Parser.this, context);
				return new ResParserState.Success(Parser.this.getIndex(), Parser.this.defContext, this, res);
			} catch (final SyntaxErrorException ex)
			{
				return new ResParserState.Error(Parser.this.getIndex(), Parser.this.defContext, this, ex);
			}
		}
		
		@Override
		protected SnapshotState saveSnapshot()
		{
			return new SnapshotState(Parser.this.getIndex(), Parser.this.defContext, this);
		}
	}
}
