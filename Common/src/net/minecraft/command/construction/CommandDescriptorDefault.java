package net.minecraft.command.construction;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.command.IPermission;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.descriptors.CommandDescriptor;
import net.minecraft.command.descriptors.CommandDescriptor.CParserData;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.IExParse;

import org.apache.commons.lang3.tuple.Pair;

public abstract class CommandDescriptorDefault extends CommandDescriptor<CParserData>
{
	private final List<IExParse<Void, ? super CParserData>> types;
	
	public CommandDescriptorDefault(final IPermission permission, final WUEProvider usage, final List<IExParse<Void, ? super CParserData>> types)
	{
		super(permission, usage);
		
		this.types = types;
	}
	
	public CommandDescriptorDefault(final IPermission permission, final WUEProvider usage, final Map<String, CommandDescriptor<? super CParserData>> keywords, final List<IExParse<Void, ? super CParserData>> types)
	{
		super(permission, usage, keywords);
		
		this.types = types;
	}
	
	public CommandDescriptorDefault(final IPermission permission, final WUEProvider usage, final Set<Pair<Set<String>, CommandDescriptor<? super CParserData>>> descriptors, final List<IExParse<Void, ? super CParserData>> types)
	{
		super(permission, usage, descriptors);
		
		this.types = types;
	}
	
	@Override
	public void parse(final Parser parser, final CParserData parserData, final WUEProvider usage) throws SyntaxErrorException, CompletionException
	{
		for (final IExParse<Void, ? super CParserData> type : this.types)
		{
			if (!parser.checkSpace())
				throw usage.create(parserData);
			
			type.parse(parser, parserData);
		}
	}
	
	@Override
	public CParserData parserData(final Parser parser)
	{
		return new CParserData(parser, this.types.size());
	}
}
