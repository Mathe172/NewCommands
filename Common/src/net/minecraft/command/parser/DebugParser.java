package net.minecraft.command.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.collections.MetaColl;
import net.minecraft.command.type.ICachedParse;
import net.minecraft.command.type.custom.command.ParserCommands;
import net.minecraft.command.type.metadata.MetaID;
import net.minecraft.command.type.metadata.MetaProvider;
import net.minecraft.event.HoverEvent;
import net.minecraft.event.HoverEvent.Action;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;

public class DebugParser extends Parser
{
	private final ICommandSender sender;
	private final boolean reducedOutput;
	
	public static final MetaID<IChatComponent> hintID = new MetaID<>(MetaColl.typeHint);
	
	public DebugParser(final ICommandSender sender, final String toParse, final int startIndex, final boolean reducedOutput)
	{
		super(toParse, startIndex, false);
		this.sender = sender;
		this.reducedOutput = reducedOutput;
	}
	
	private final Stack<AbstractTree> messages = new Stack<>();
	private AbstractTree newElement = null;
	private AbstractTree lastMessage;
	
	/**
	 * 
	 * @return Always <code>null</code>
	 */
	@Override
	public CommandArg<Integer> parseCommand() throws SyntaxErrorException
	{
		this.messages.push(this.reducedOutput ? new TopLevelMessageRed() : new TopLevelMessage(this.sender));
		
		try
		{
			ParserCommands.parse(this, false);
			
			if (this.endReached())
				return null;
			
			this.SEE("Unexpected ')' ");
		} catch (final SyntaxErrorException e)
		{
			if (this.reducedOutput)
				if (this.lastMessage != null)
					this.lastMessage.print("- ", this.sender);
			throw e;
		} catch (final Throwable t)
		{
			throw this.handleFatalError("Fatal error while parsing command: ", t);
		} finally
		{
			if (!this.reducedOutput)
				this.cleanStack(0);
		}
		
		return null;
	}
	
	@Override
	public SyntaxErrorException SEE(final String s, final String postfix, final Throwable cause, final Object... errorObjects)
	{
		this.newMessage(this.addLocation(new ChatComponentText(s + "around index " + this.index + postfix)));
		
		return SyntaxErrorException.see;
	}
	
	private void newMessage(final IChatComponent message)
	{
		final AbstractTree toAdd;
		if (this.newElement == null)
			toAdd = new MessageTree(message);
		else
		{
			toAdd = new MessageTreeWC(message, this.newElement);
			this.newElement = null;
		}
		
		this.lastMessage = toAdd;
		
		if (this.messages.peek().skipped == 0)
			this.messages.peek().add(toAdd);
		else
			this.messages.push(toAdd);
	}
	
	@Override
	public SyntaxErrorException SEE(final String s, final boolean appendIndex, final Throwable cause, final Object... errorObjects)
	{
		this.newMessage(this.addLocation(appendIndex ? new ChatComponentText(s + ("around index " + this.index)) : new ChatComponentTranslation(s, errorObjects)));
		
		return SyntaxErrorException.see;
	}
	
	@Override
	public WrongUsageException WUE(final String s, final Object... errorObjects)
	{
		this.newMessage(new ChatComponentTranslation(s, errorObjects));
		
		return WrongUsageException.wue;
	}
	
	@Override
	public <D> boolean pushMetadata(final MetaProvider<D> data, final D parserData)
	{
		if (this.newElement != null)
		{
			this.messages.push(this.newElement);
			this.newElement = null;
		}
		
		++this.messages.peek().skipped;
		
		return true;
	}
	
	@Override
	public void popMetadata(final MetaProvider<?> data)
	{
		if (this.messages.peek().skipped > 0)
		{
			--this.messages.peek().skipped;
			return;
		}
		
		final AbstractTree current = this.messages.pop();
		
		if (this.newElement != null)
			current.add(this.newElement);
		
		this.newElement = current;
		--this.messages.peek().skipped;
	}
	
	private void cleanStack(final int stackSize)
	{
		while (this.messages.size() > stackSize)
		{
			final AbstractTree current = DebugParser.this.messages.pop();
			
			if (DebugParser.this.newElement != null)
				current.add(DebugParser.this.newElement);
			
			DebugParser.this.newElement = current;
		}
	}
	
	@Override
	public <D> void supplyHint(final MetaProvider<D> hint, final D data)
	{
		final IChatComponent message = hint.getData(hintID, this, data);
		
		if (message == null)
			return;
		
		this.newMessage(this.addLocation(message));
	}
	
	private IChatComponent addLocation(final IChatComponent message)
	{
		message.getChatStyle().setChatHoverEvent(new HoverEvent(Action.SHOW_TEXT, ParsingUtilities.location(this)));
		return message;
	}
	
	private static abstract class AbstractTree
	{
		protected int skipped = 0;
		
		protected abstract void add(final AbstractTree next);
		
		protected abstract void print(String indent, ICommandSender sender);
	}
	
	private static class MessageTree extends AbstractTree
	{
		protected final IChatComponent message;
		public List<AbstractTree> neighbors;
		
		public MessageTree(final IChatComponent message)
		{
			this.message = message;
			this.neighbors = null;
		}
		
		@Override
		public void add(final AbstractTree next)
		{
			if (this.neighbors == null)
				this.neighbors = new ArrayList<>();
			
			this.neighbors.add(next);
		}
		
		@Override
		protected void print(final String indent, final ICommandSender sender)
		{
			sender.addChatMessage(new ChatComponentText(indent).appendSibling(this.message));
			
			if (this.neighbors == null)
				return;
			
			for (final AbstractTree next : this.neighbors)
				next.print(indent, sender);
		}
	}
	
	private static class MessageTreeWC extends MessageTree
	{
		private final AbstractTree child;
		
		public MessageTreeWC(final IChatComponent message, final AbstractTree newElement)
		{
			super(message);
			this.child = newElement;
		}
		
		@Override
		public void print(final String indent, final ICommandSender sender)
		{
			sender.addChatMessage(new ChatComponentText(indent).appendSibling(this.message));
			
			this.child.print("  " + indent, sender);
			
			if (this.neighbors == null)
				return;
			
			for (final AbstractTree next : this.neighbors)
				next.print(indent, sender);
		}
	}
	
	private static class TopLevelMessage extends AbstractTree
	{
		private final ICommandSender sender;
		
		public TopLevelMessage(final ICommandSender sender)
		{
			this.sender = sender;
		}
		
		@Override
		public void add(final AbstractTree next)
		{
			next.print("- ", this.sender);
		}
		
		@Override
		public void print(final String indent, final ICommandSender sender)
		{
		}
	}
	
	private static class TopLevelMessageRed extends AbstractTree
	{
		@Override
		protected void add(final AbstractTree next)
		{
		}
		
		@Override
		protected void print(final String indent, final ICommandSender sender)
		{
		}
	}
	
	@Override
	protected IVersionManager<?, ?> newVersionManager()
	{
		return new VersionManager();
	}
	
	private class VersionManager extends IVersionManager<ResParserState, SnapshotState>
	{
		@Override
		protected ResParserState parseFetchState(final ICachedParse target, final Context context) throws SyntaxErrorException
		{
			try
			{
				final ArgWrapper<?> res = target.iCachedParse(DebugParser.this, context);
				return new ResParserState.Success(DebugParser.this.getIndex(), DebugParser.this.defContext, this, res);
			} catch (final SyntaxErrorException ex)
			{
				return new ResParserState.Error(DebugParser.this.getIndex(), DebugParser.this.defContext, this, ex);
			}
		}
		
		@Override
		protected SnapshotState saveSnapshot()
		{
			return new SnapshotState(
				DebugParser.this.index,
				DebugParser.this.defContext,
				this,
				DebugParser.this.messages.size(),
				DebugParser.this.messages.peek().skipped);
		}
		
		@Override
		protected void restoreSnapshot(final SnapshotState state)
		{
			super.restoreSnapshot(state);
			
			DebugParser.this.cleanStack(state.stackSize);
			
			DebugParser.this.messages.peek().skipped = state.topSkipped;
			DebugParser.this.newElement.skipped = 0;
		}
	}
	
	private static class SnapshotState extends ISnapshotState<ResParserState>
	{
		public final int stackSize;
		public final int topSkipped;
		
		public SnapshotState(final int index, final Context defContext, final IVersionManager<ResParserState, ?> versionManager, final int stackSize, final int topSkipped)
		{
			super(index, defContext, versionManager);
			this.stackSize = stackSize;
			this.topSkipped = topSkipped;
		}
	}
}