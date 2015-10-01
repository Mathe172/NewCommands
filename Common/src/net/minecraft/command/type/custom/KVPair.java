package net.minecraft.command.type.custom;

import java.util.regex.Matcher;

import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletion;
import net.minecraft.command.completion.TabCompletionData;
import net.minecraft.command.descriptors.SParserData;
import net.minecraft.command.descriptors.SelectorDescriptor;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IParse;
import net.minecraft.command.type.base.ExCustomCompletable;
import net.minecraft.command.type.management.TypeID;

public class KVPair<D extends SParserData> extends ExCustomCompletable<Void, D>
{
	private final SelectorDescriptor<D> descriptor;
	private final IParse<TypeID<?>> labelTypeParser;
	
	public KVPair(final SelectorDescriptor<D> descriptor)
	{
		this.descriptor = descriptor;
		this.labelTypeParser = new TypeTypeID(descriptor.getResultTypes());
	}
	
	@Override
	public Void iParse(final Parser parser, final D data) throws SyntaxErrorException
	{
		final Matcher m = parser.getMatcher(ParsingUtilities.aKeyMatcher);
		
		if (parser.findInc(m))
		{
			ParsingUtilities.terminateCompletion(parser);
			final String key = m.group(1).toLowerCase();
			
			if ("label".equals(key))
			{
				if (data.label != null)
					throw parser.SEE("Multiple labels encountered while parsing selector ");
				
				final Matcher lm = parser.getMatcher(TypeLabelDeclaration.labelMatcher);
				
				if (!parser.findInc(lm))
					throw parser.SEE("Expected label name ");
				
				data.label = lm.group(2);
				
				if (lm.group(1) == null)
				{
					if (lm.group(3) != null)
						data.labelType = this.labelTypeParser.parse(parser);
				}
				else
				{
					if (lm.group(3) != null)
						throw parser.SEE("Can't specify type for label '" + data.label + "' (already defined) ");
					
					data.labelModifier = lm.group(1).charAt(0);
				}
				
				return null;
			}
			
			this.descriptor.parse(parser, key, data);
			
			return null;
		}
		
		if (data.requiresKey())
			throw parser.SEE("Missing key for parameter ");
		
		this.descriptor.parse(parser, data);
		
		return null;
	}
	
	private static final ITabCompletion labelCompletion = new TabCompletion("label=", "label");
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData, final D data)
	{
		this.descriptor.complete(tcDataSet, parser, startIndex, cData, data);
		
		if (data.label == null)
			TabCompletionData.addToSet(tcDataSet, startIndex, cData, labelCompletion);
	}
}
