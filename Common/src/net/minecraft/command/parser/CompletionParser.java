package net.minecraft.command.parser;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.type.IExParse;
import net.minecraft.command.type.IType;
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
	private boolean proposeCompletion = false;
	private boolean snapshot = false;
	
	private final TCDSet tcDataSet = new TCDSet();
	
	public CompletionParser(final String toParse, final int startIndex, final CompletionData cData)
	{
		super(toParse, startIndex, true);
		this.cData = cData;
	}
	
	public CompletionParser(final String toParse, final CompletionData cData)
	{
		super(toParse, 0);
		this.cData = cData;
	}
	
	@Override
	public <R, D> R parse(final IType<R, D> target, final D parserData) throws SyntaxErrorException, CompletionException
	{
		final int startIndex = this.getIndex();
		
		final boolean terminateCompletion = this.terminateCompletion;
		this.terminateCompletion = false;
		
		final boolean proposeCompletion = this.proposeCompletion;
		this.proposeCompletion = false;
		
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
			else
				throw new CompletionException();
		}
		
		if (this.getIndex() >= this.cData.cursorIndex - 1 || this.proposeCompletion)
			this.complete(target, startIndex, parserData);
		
		this.cleanup(terminateCompletion, proposeCompletion);
		
		return ret;
	}
	
	private void cleanup(final boolean terminateCompletion, final boolean proposeCompletion)
	{
		this.terminateCompletion |= terminateCompletion;
		this.proposeCompletion = proposeCompletion;
	}
	
	@Override
	public <R, D> R parseSnapshot(final IExParse<R, D> target, final D parserData) throws SyntaxErrorException, CompletionException
	{
		final boolean saveSnapshot = this.snapshot;
		this.snapshot = true;
		
		try
		{
			return super.parseSnapshot(target, parserData);
		} finally
		{
			this.snapshot = saveSnapshot;
		}
	}
	
	private <D> void complete(final IType<?, D> target, final int startIndex, final D parserData)
	{
		if (!this.terminateCompletion && startIndex <= this.cData.cursorIndex)
			target.complete(this.tcDataSet, this, startIndex, this.cData, parserData);
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
}
