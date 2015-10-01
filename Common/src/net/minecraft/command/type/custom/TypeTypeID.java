package net.minecraft.command.type.custom;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.completion.TCDSet;
import net.minecraft.command.completion.TabCompletion;
import net.minecraft.command.completion.TabCompletionData;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.base.CustomCompletable;
import net.minecraft.command.type.management.Convertable;
import net.minecraft.command.type.management.TypeID;

public class TypeTypeID extends CustomCompletable<TypeID<?>>
{
	private final Set<TypeID<?>> targets;
	
	public TypeTypeID(final Set<TypeID<?>> targets)
	{
		this.targets = targets;
	}
	
	@Override
	public TypeID<?> iParse(final Parser parser, final Context context) throws SyntaxErrorException
	{
		final String name = ParsingUtilities.parseLiteralString(parser, "Expected TypeID ");
		
		final TypeID<?> typeID = TypeID.get(name);
		
		if (typeID == null)
			throw parser.SEE("Unknown TypeID '" + name + "' ");
		
		return typeID;
	}
	
	@Override
	public void complete(final TCDSet tcDataSet, final Parser parser, final int startIndex, final CompletionData cData)
	{
		if (this.targets.size() > 1)
		{
			final Set<TabCompletion> completions = new HashSet<>();
			
			for (final TypeID<?> typeID : this.targets)
				for (final Convertable<?, ?, ?> convertable : typeID.convertableTo())
					if (convertable instanceof TypeID<?>)
						completions.add(((TypeID<?>) convertable).completion);
			
			TabCompletionData.addToSet(tcDataSet, startIndex, cData, completions);
			return;
		}
		
		for (final TypeID<?> typeID : this.targets)
			for (final Convertable<?, ?, ?> convertable : typeID.convertableTo())
				if (convertable instanceof TypeID<?>)
					TabCompletionData.addToSet(tcDataSet, startIndex, cData, ((TypeID<?>) convertable).completion);
		
	}
}
