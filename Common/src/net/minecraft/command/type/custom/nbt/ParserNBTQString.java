package net.minecraft.command.type.custom.nbt;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.arg.PrimitiveParameter;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IParse;
import net.minecraft.command.type.base.CompositeString;
import net.minecraft.command.type.custom.TypeIDs;
import net.minecraft.command.type.custom.nbt.NBTUtilities.NBTData;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagString;

public class ParserNBTQString
{
	public static final Pattern escapedPattern = Pattern.compile("\\G([^\\\\\"]*+)(?:\"|\\\\(.))");
	
	public static void parse(final Parser parser, final NBTData parserData) throws SyntaxErrorException, CompletionException
	{
		
		final Matcher m = parser.escapedMatcher;
		
		StringBuilder sb = new StringBuilder();
		
		final List<CommandArg<String>> parts = new ArrayList<>();
		
		final IParse<ArgWrapper<String>> selectorParser = TypeIDs.String.selectorParser;
		final IParse<ArgWrapper<String>> labelParser = TypeIDs.String.labelParser;
		
		while (parser.findInc(m))
		{
			sb.append(m.group(1));
			
			if (m.group(2) == null)
			{
				if (parts.isEmpty())
					parserData.put(new NBTTagString(sb.toString()));
				else
				{
					if (sb.length() != 0)
						parts.add(new PrimitiveParameter<>(sb.toString()));
					
					final CompositeString cs = new CompositeString(parts);
					parserData.put(new CommandArg<NBTBase>()
					{
						@Override
						public NBTTagString eval(final ICommandSender sender) throws CommandException
						{
							return new NBTTagString(cs.eval(sender));
						}
					});
				}
				return;
			}
			
			switch (m.group(2).charAt(0))
			{
			case '@':
				sb = resetSB(parts, sb);
				
				parts.add(selectorParser.parse(parser).arg);
				continue;
			case '$':
				sb = resetSB(parts, sb);
				
				parts.add(labelParser.parse(parser).arg);
				continue;
			case '"':
				sb.append('"');
				continue;
			case '\\':
				sb.append('\\');
				continue;
			default:
				sb.append('\\');
				sb.append(m.group(2));
			}
		}
		
		throw parser.SEE("Missing '\"' around index ");
	}
	
	private static final StringBuilder resetSB(final List<CommandArg<String>> parts, final StringBuilder sb)
	{
		if (sb.length() != 0)
		{
			parts.add(new PrimitiveParameter<>(sb.toString()));
			return new StringBuilder();
		}
		
		return sb;
	}
}
