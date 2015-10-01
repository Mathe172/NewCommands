package net.minecraft.command.parser;

import java.util.ArrayDeque;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.collections.MetaColl;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.parser.CompletionParser.ResParserState.CompletionModifier;
import net.minecraft.command.type.ICachedParse;
import net.minecraft.command.type.metadata.ICompletable;
import net.minecraft.command.type.metadata.ICompletable.CompletionCallback;
import net.minecraft.command.type.metadata.MetaEntry.PrimitiveHint;
import net.minecraft.command.type.metadata.MetaID;
import net.minecraft.command.type.metadata.MetaProvider;
import net.minecraft.util.BlockPos;

public class CompletionParser extends Parser
{
	public static class CompletionData
	{
		public final String toMatch;
		public final String lowerToMatch;
		public final int cursorIndex;
		public final ICommandSender sender;
		public final BlockPos hovered;
		
		public CompletionData(final String toMatch, final int cursorIndex, final ICommandSender sender, final BlockPos hovered)
		{
			this.toMatch = toMatch;
			this.lowerToMatch = toMatch.toLowerCase();
			this.cursorIndex = cursorIndex;
			this.sender = sender;
			this.hovered = hovered;
		}
	}
	
	private final CompletionData cData;
	
	private boolean terminateCompletion = false;
	
	private final TCDSet tcDataSet = new TCDSet();
	
	private final ArrayDeque<CompletionCallback> completers = new ArrayDeque<>();
	private final ArrayDeque<CompletionCallback> proposed = new ArrayDeque<>();
	
	public CompletionParser(final String toParse, final int startIndex, final CompletionData cData)
	{
		super(toParse, startIndex, false);
		
		// The Completion-version of the pattern tricks the parser into thinking that the end is not yet reached, thus calling the subparsers for completions
		this.getMatcher(ParsingUtilities.endingMatcherCompletion);
		
		this.cData = cData;
		
		this.suppressEx = true;
	}
	
	public CompletionParser(final String toParse, final CompletionData cData)
	{
		super(toParse, 0);
		this.cData = cData;
		
		this.suppressEx = true;
	}
	
	public TCDSet getTCDSet()
	{
		return this.tcDataSet;
	}
	
	public static final MetaID<PrimitiveHint> hintID = new MetaID<>(MetaColl.typeHint);
	
	public static final PrimitiveHint propose = new PrimitiveHint(hintID);
	public static final PrimitiveHint terminate = new PrimitiveHint(hintID);
	
	@Override
	public <D> void supplyHint(final MetaProvider<D> hint, final D data)
	{
		final PrimitiveHint eHint = hint.getData(hintID, this, data);
		
		if (eHint == null)
			return;
		
		if (eHint == propose)
			this.proposeCompletion();
		else
			this.terminateCompletion();
	}
	
	protected void terminateCompletion()
	{
		this.terminateCompletion = true;
		this.completers.clear();
		this.proposed.clear();
	}
	
	protected void proposeCompletion()
	{
		if (this.completers.isEmpty())
			return;
		
		this.proposed.push(this.completers.pop());
		this.completers.push(NULL);
	}
	
	@Override
	public <D> boolean pushMetadata(final MetaProvider<D> data, final D parserData)
	{
		final CompletionCallback completer = data.getData(ICompletable.metaID, this, parserData);
		
		if (completer == null)
			return false;
		
		this.completers.push(completer);
		return true;
	}
	
	@Override
	public void popMetadata(final MetaProvider<?> data)
	{
		if (!data.canProvide(ICompletable.metaID) || this.completers.isEmpty())
			return;
		
		this.complete(false);
	}
	
	protected void complete(final boolean forceCompletion)
	{
		final CompletionCallback completer = this.completers.pop();
		
		if (completer == NULL)
			this.proposed.pop().complete(this.tcDataSet, this, this.cData);
		else if (forceCompletion || this.getIndex() == this.cData.cursorIndex)
			completer.complete(this.tcDataSet, this, this.cData);
	}
	
	protected void complete(final boolean forceCompletion, final int endCount)
	{
		for (int i = CompletionParser.this.completers.size(); i > endCount; --i)
			this.complete(forceCompletion);
	}
	
	protected static abstract class ResParserState extends Parser.IResParserState<ResParserState>
	{
		public final CompletionModifier modifier;
		
		public ResParserState(
			final int index,
			final Context defContext,
			final IVersionManager<ResParserState, ?> versionManager,
			final CompletionModifier modifier)
		{
			super(index, defContext, versionManager);
			this.modifier = modifier;
		}
		
		public static enum CompletionModifier
		{
			none, terminate, propose;
		}
		
		protected static class Success extends ResParserState
		{
			private final ArgWrapper<?> res;
			
			public Success(final int index, final Context defContext, final IVersionManager<ResParserState, ?> versionManager, final CompletionModifier modifier, final ArgWrapper<?> res)
			{
				super(index, defContext, versionManager, modifier);
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
			
			public Error(final int index, final Context defContext, final IVersionManager<ResParserState, ?> versionManager, final CompletionModifier modifier, final SyntaxErrorException ex)
			{
				super(index, defContext, versionManager, modifier);
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
		public final int completersCount;
		public final boolean terminateCompletionTriggered;
		
		public SnapshotState(final int index, final Context defContext, final IVersionManager<ResParserState, ?> versionManager, final int completersCount, final boolean terminateCompletionTriggered)
		{
			super(index, defContext, versionManager);
			this.completersCount = completersCount;
			this.terminateCompletionTriggered = terminateCompletionTriggered;
		}
	}
	
	@Override
	protected IVersionManager<?, ?> newVersionManager()
	{
		return new VersionManager();
	}
	
	private static final CompletionCallback markerCompleter = new CompletionCallback()
	{
		@Override
		public void complete(final TCDSet tcDataSet, final Parser parser, final CompletionData cData)
		{
		}
	};
	
	private static final CompletionCallback NULL = new CompletionCallback()
	{
		@Override
		public void complete(final TCDSet tcDataSet, final Parser parser, final CompletionData cData)
		{
		}
	};
	
	private class VersionManager extends IVersionManager<ResParserState, SnapshotState>
	{
		@Override
		public void setState(final ResParserState state)
		{
			super.setState(state);
			
			switch (state.modifier)
			{
			case terminate:
				CompletionParser.this.terminateCompletion();
				return;
			case propose:
				CompletionParser.this.proposeCompletion();
			case none:
			}
		}
		
		@Override
		protected ResParserState parseFetchState(final ICachedParse target, final Context context) throws SyntaxErrorException
		{
			final boolean useMarker = CompletionParser.this.completers.isEmpty() || CompletionParser.this.completers.peek() == NULL;
			
			if (useMarker)
				CompletionParser.this.completers.push(markerCompleter);
			
			final boolean terminateCompletion = CompletionParser.this.terminateCompletion;
			CompletionParser.this.terminateCompletion = false;
			
			try
			{
				final ArgWrapper<?> res = target.iCachedParse(CompletionParser.this, context);
				
				final CompletionModifier modifier =
					CompletionParser.this.terminateCompletion
						? CompletionModifier.terminate
						: (useMarker ? CompletionParser.this.completers.pop() : CompletionParser.this.completers.peek()) == NULL
							? CompletionModifier.propose
							: CompletionModifier.none;
				
				if (modifier == CompletionModifier.propose && useMarker)
				{
					CompletionParser.this.proposed.pop();
					CompletionParser.this.proposeCompletion();
				}
				
				return new ResParserState.Success(CompletionParser.this.getIndex(), CompletionParser.this.defContext, this, modifier, res);
			} catch (final SyntaxErrorException ex)
			{
				final CompletionModifier modifier =
					CompletionParser.this.terminateCompletion
						? CompletionModifier.terminate
						: CompletionModifier.none;
				
				return new ResParserState.Error(CompletionParser.this.getIndex(), CompletionParser.this.defContext, this, modifier, ex);
			} finally
			{
				CompletionParser.this.terminateCompletion |= terminateCompletion;
			}
		}
		
		@Override
		protected SnapshotState saveSnapshot()
		{
			final boolean terminateCompletion = CompletionParser.this.terminateCompletion;
			
			CompletionParser.this.terminateCompletion = false;
			
			return new SnapshotState(
				CompletionParser.this.getIndex(),
				CompletionParser.this.defContext,
				this,
				CompletionParser.this.completers.size(),
				terminateCompletion);
		}
		
		@Override
		protected void restoreSnapshot(final SnapshotState state)
		{
			super.restoreSnapshot(state);
			
			final int endCount = CompletionParser.this.terminateCompletion ? 0 : state.completersCount;
			CompletionParser.this.complete(true, endCount);
		}
		
		@Override
		protected void finalizeSnapshot(final SnapshotState state)
		{
			CompletionParser.this.terminateCompletion |= state.terminateCompletionTriggered;
		}
	}
}
