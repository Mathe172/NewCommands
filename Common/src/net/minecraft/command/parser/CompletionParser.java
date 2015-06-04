package net.minecraft.command.parser;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.type.IType;
import net.minecraft.util.BlockPos;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

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
	private boolean proposeCompletion = false;
	
	private boolean terminateCompletionTriggered = false;
	private boolean completionTriggered = false;
	
	private final TCDSet tcDataSet = new TCDSet();
	
	public CompletionParser(final String toParse, final int startIndex, final CompletionData cData)
	{
		super(toParse, startIndex, true, false, false);
		this.cData = cData;
		
		this.suppressEx = true;
	}
	
	public CompletionParser(final String toParse, final CompletionData cData)
	{
		super(toParse, 0);
		this.cData = cData;
		
		this.suppressEx = true;
	}
	
	@Override
	public <R, D> R parse(final IType<R, D> target, final D parserData) throws SyntaxErrorException, CompletionException
	{
		final int startIndex = this.getIndex();
		
		final boolean terminateCompletion = this.terminateCompletion;
		this.terminateCompletion = false;
		
		final boolean proposeCompletion = this.proposeCompletion;
		this.proposeCompletion = false;
		
		this.completionTriggered = false;
		
		R ret;
		
		try
		{
			ret = target.iParse(this, parserData);
			
		} catch (final CompletionException e)
		{
			this.complete(target, startIndex, parserData);
			
			this.cleanup(terminateCompletion, proposeCompletion);
			
			throw e;
		} catch (final SyntaxErrorException e)
		{
			this.complete(target, startIndex, parserData);
			
			this.cleanup(terminateCompletion, proposeCompletion);
			
			if (this.snapshot)
				throw e;
			
			throw CompletionException.ex;
		}
		
		if (this.getIndex() >= this.cData.cursorIndex - 1 || this.proposeCompletion)
			this.complete(target, startIndex, parserData);
		
		this.cleanup(terminateCompletion, proposeCompletion);
		
		return ret;
	}
	
	private void cleanup(final boolean terminateCompletion, final boolean proposeCompletion)
	{
		this.terminateCompletionTriggered = this.terminateCompletion;
		
		this.terminateCompletion |= terminateCompletion;
		this.proposeCompletion = proposeCompletion;
	}
	
	private <D> void complete(final IType<?, D> target, final int startIndex, final D parserData)
	{
		if (!this.terminateCompletion && startIndex <= this.cData.cursorIndex)
		{
			this.completionTriggered = true;
			target.complete(this.tcDataSet, this, startIndex, this.cData, parserData);
		}
	}
	
	@Override
	public void terminateCompletion()
	{
		this.terminateCompletion = true;
	}
	
	@Override
	public void proposeCompletion()
	{
		this.proposeCompletion = true;
	}
	
	public TCDSet getTCDSet()
	{
		return this.tcDataSet;
	}
	
	private static class ParserStateRes extends Parser.IParserStateRes<ParserStateRes>
	{
		public final boolean terminateCompletionTriggered;
		public final boolean completionTriggered;
		
		public ParserStateRes(final int index, final Context defContext, final IVersionManager<ParserStateRes>.Version version, final boolean terminateCompletionTriggered, final boolean completionTriggered)
		{
			super(index, defContext, version);
			this.terminateCompletionTriggered = terminateCompletionTriggered;
			this.completionTriggered = completionTriggered;
		}
	}
	
	@Override
	protected IVersionManager<?> newVersionManager()
	{
		return new VersionManager();
	}
	
	private class VersionManager extends Parser.IVersionManager<ParserStateRes>
	{
		@Override
		public Pair<ParserStateRes, ArgWrapper<?>> parseFetchState(final IType<? extends ArgWrapper<?>, Context> target, final Context context) throws SyntaxErrorException, CompletionException
		{
			final ArgWrapper<?> res = CompletionParser.this.parse(target, context);
			
			this.saveSnapshot();
			
			final ParserStateRes state = new ParserStateRes(
				CompletionParser.this.getIndex(),
				CompletionParser.this.defContext,
				this.version,
				CompletionParser.this.terminateCompletionTriggered,
				CompletionParser.this.completionTriggered);
			
			return new ImmutablePair<ParserStateRes, ArgWrapper<?>>(state, res);
		}
		
		@Override
		public void applyCompletion(final IType<? extends ArgWrapper<?>, Context> target, final ParserState initialState, final ParserStateRes newState)
		{
			if (newState.completionTriggered)
				target.complete(CompletionParser.this.tcDataSet, CompletionParser.this, initialState.index, CompletionParser.this.cData, initialState.context);
		}
		
		@Override
		public void setState(final ParserStateRes state)
		{
			super.setState(state);
			
			CompletionParser.this.terminateCompletion |= state.terminateCompletionTriggered;
		}
	}
}
