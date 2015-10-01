package net.minecraft.command.type.metadata;

import net.minecraft.command.collections.MetaColl;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IComplete;
import net.minecraft.command.type.IExComplete;

public class ICompletable
{
	public static interface CompletionCallback
	{
		public void complete(TCDSet tcDataSet, Parser parser, CompletionData cData);
	}
	
	public static final MetaID<CompletionCallback> metaID = new MetaID<>(MetaColl.typeSPA);
	
	private ICompletable()
	{
	}
	
	public static class Default extends MetaEntry<CompletionCallback, Object>
	{
		private final IComplete target;
		
		public Default(final IComplete target)
		{
			super(metaID);
			
			this.target = target;
		}
		
		@Override
		public CompletionCallback get(final Parser parser, final Object parserData)
		{
			return new CompletionCallback()
			{
				private final int index = parser.getIndex();
				
				@Override
				public void complete(final TCDSet tcDataSet, final Parser parser, final CompletionData cData)
				{
					Default.this.target.complete(tcDataSet, parser, this.index, cData);
				}
			};
		}
	}
	
	public static class Capturing<D> extends MetaEntry<CompletionCallback, D>
	{
		private final IExComplete<D> target;
		
		public Capturing(final IExComplete<D> target)
		{
			super(metaID);
			
			this.target = target;
		}
		
		@Override
		public CompletionCallback get(final Parser parser, final D parserData)
		{
			return new CompletionCallback()
			{
				private final int index = parser.getIndex();
				
				@Override
				public void complete(final TCDSet tcDataSet, final Parser parser, final CompletionData cData)
				{
					Capturing.this.target.complete(tcDataSet, parser, this.index, cData, parserData);
				}
			};
		}
	}
}
