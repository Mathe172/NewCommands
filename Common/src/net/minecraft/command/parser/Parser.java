package net.minecraft.command.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;

import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.arg.CompoundArg;
import net.minecraft.command.arg.Processable;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletion;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.type.IExParse;
import net.minecraft.command.type.IParse;
import net.minecraft.command.type.IType;
import net.minecraft.command.type.TypeID;
import net.minecraft.command.type.base.ExCustomParse;
import net.minecraft.command.type.custom.ParserCommands;
import net.minecraft.command.type.custom.ParserInt;
import net.minecraft.command.type.custom.TypeCommand;
import net.minecraft.command.type.custom.TypeList;
import net.minecraft.command.type.custom.TypeSayString;
import net.minecraft.command.type.custom.Types;
import net.minecraft.command.type.custom.coordinate.ParserCoordinate;
import net.minecraft.command.type.custom.nbt.NBTPair;
import net.minecraft.command.type.custom.nbt.NBTUtilities;
import net.minecraft.command.type.custom.nbt.ParserNBTQString;
import net.minecraft.command.type.custom.nbt.ParserNBTTag;
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
	
	public final Matcher idMatcher;
	public final Matcher oParenthMatcher;
	public final Matcher endingMatcher;
	public final Matcher keyMatcher;
	public final Matcher aKeyMatcher;
	public final Matcher listEndMatcher;
	public final Matcher generalMatcher;
	public final Matcher nameMatcher;
	public final Matcher spaceMatcher;
	
	public final Matcher numberIDMatcher;
	public final Matcher specialMatcher;
	
	public final Matcher nbtKeyMatcher;
	
	public final Matcher escapedMatcher;
	
	public final Matcher baseMatcher;
	public final Matcher stackedMatcher;
	public final Matcher numberMatcher;
	
	public final Matcher intMatcher;
	public final Matcher sayStringMatcher;
	
	public final Matcher coordMatcher;
	
	public final Matcher stringMatcher;
	
	public final Matcher listDelimMatcher;
	
	public Parser(final String toParse, final int startIndex, final boolean completionPatterns)
	{
		this.index = startIndex;
		
		this.toParse = toParse;
		this.len = this.toParse.length();
		
		this.idMatcher = ParsingUtilities.identifierPattern.matcher(this.toParse);
		this.oParenthMatcher = ParsingUtilities.oParenthPattern.matcher(this.toParse);
		
		// The Completion-version of the pattern tricks the parser into thinking that the end is not yet reached, thus calling the subparsers for completions
		this.endingMatcher = completionPatterns ? ParsingUtilities.endingPatternCompletion.matcher(this.toParse) : ParsingUtilities.endingPattern.matcher(this.toParse);
		
		this.keyMatcher = ParsingUtilities.keyPattern.matcher(this.toParse);
		this.aKeyMatcher = ParsingUtilities.assignedKeyPattern.matcher(this.toParse);
		this.listEndMatcher = ParsingUtilities.listEndPattern.matcher(this.toParse);
		this.generalMatcher = ParsingUtilities.generalPattern.matcher(this.toParse);
		this.nameMatcher = ParsingUtilities.namePattern.matcher(this.toParse);
		this.spaceMatcher = ParsingUtilities.spacePattern.matcher(this.toParse);
		
		this.numberIDMatcher = NBTUtilities.numberIDPattern.matcher(this.toParse);
		this.specialMatcher = ParserNBTTag.specialPattern.matcher(this.toParse);
		
		this.nbtKeyMatcher = NBTPair.nbtKeyPattern.matcher(this.toParse);
		
		this.escapedMatcher = ParserNBTQString.escapedPattern.matcher(this.toParse);
		
		this.baseMatcher = ParserNBTTag.basePattern.matcher(this.toParse);
		this.stackedMatcher = ParserNBTTag.stackedPattern.matcher(this.toParse);
		this.numberMatcher = ParserNBTTag.numberPattern.matcher(this.toParse);
		
		this.intMatcher = ParserInt.IntegerPatternConverter.matcher(this.toParse);
		this.sayStringMatcher = TypeSayString.sayStringPattern.matcher(this.toParse);
		
		this.coordMatcher = ParserCoordinate.coordPattern.matcher(this.toParse);
		
		this.stringMatcher = ParsingUtilities.stringPattern.matcher(this.toParse);
		
		this.listDelimMatcher = TypeList.listDelimPattern.matcher(this.toParse);
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
	
	public static TCDSet parseCompletion(final String toParse, final CompletionData cData, final int startIndex)
	{
		final CompletionParser completionParser = new CompletionParser(toParse.substring(0, cData.cursorIndex), startIndex, cData);
		
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
	
	public static TCDSet parseCompletion(final String toParse, final CompletionData cData)
	{
		return parseCompletion(toParse, cData, 0);
	}
	
	public static CommandArg<List<String>> parseStatsTarget(final String toParse) throws SyntaxErrorException
	{
		final Parser parser = new Parser(toParse, 0);
		
		final CommandArg<List<String>> ret;
		try
		{
			ret = parser.parseInit(Types.IPUUIDList);
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
	
	public static CommandArg<String> parseUUID(final String toParse) throws SyntaxErrorException
	{
		final Parser parser = new Parser(toParse, 0);
		
		final CommandArg<String> ret;
		try
		{
			ret = parser.parseInit(Types.IPUUID);
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
	
	public Set<TabCompletion> getLabelCompletions()
	{
		final Set<TabCompletion> completions = new HashSet<>();
		
		for (final String name : this.labels.keySet())
			completions.add(new TabCompletion(name));
		
		return completions;
	}
	
	public Set<TabCompletion> getLabelCompletions(final TypeID<?> target)
	{
		final Set<TabCompletion> completions = new HashSet<>();
		
		for (final Entry<String, ArgWrapper<?>> entry : this.labels)
			if (target.convertableFrom.contains(entry.getValue().type))
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
		
		try
		{
			return target.parse(this, parserData);
			
		} catch (final SyntaxErrorException e)
		{
			this.index = startIndex;
			this.restoreLabels(saveLabels);
			this.restoreToProcess(saveToProcess);
			this.restoreIgnoreErrors(saveIgnoreErrors);
			
			throw e;
		}
	}
	
	public final boolean checkSpace()
	{
		return this.find(this.spaceMatcher);
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
