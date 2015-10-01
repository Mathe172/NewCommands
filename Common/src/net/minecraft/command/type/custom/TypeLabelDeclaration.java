package net.minecraft.command.type.custom;

import java.util.regex.Matcher;

import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.LabelWrapper;
import net.minecraft.command.arg.Setter.SetterProvider;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.construction.CommandDescriptorDefault.CParserData;
import net.minecraft.command.parser.MatcherRegistry;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.base.ExCustomParse;
import net.minecraft.command.type.management.TypeID;
import net.minecraft.entity.Entity;

public class TypeLabelDeclaration<T> extends ExCustomParse<Void, CParserData>
{
	public static final MatcherRegistry labelMatcher = new MatcherRegistry("\\G\\s*+([*\\^])?+([\\w-]++)(:)?+");
	
	public static final TypeLabelDeclaration<Entity> entityNonInstant = new TypeLabelDeclaration<>(TypeIDs.Entity, false);
	public static final TypeLabelDeclaration<Integer> intNonInstant = new TypeLabelDeclaration<>(TypeIDs.Integer, false);
	
	private final TypeID<T> typeID;
	private final boolean instant;
	
	public TypeLabelDeclaration(final TypeID<T> typeID, final boolean instant)
	{
		this.typeID = typeID;
		this.instant = instant;
	}
	
	@Override
	public Void iParse(final Parser parser, final CParserData parserData) throws SyntaxErrorException
	{
		final Matcher m = parser.getMatcher(labelMatcher);
		
		if (!parser.findInc(m))
			throw parser.SEE("Unable to find label name ");
		
		final String name = m.group(2);
		
		if (m.group(1) == null)
		{
			final boolean customType = m.group(3) != null;
			
			final LabelWrapper<?> label = new LabelWrapper<>(customType ? this.typeID.typeIDParser.parse(parser) : this.typeID, name);
			
			if (this.instant)
			{
				parser.addLabel(name, label);
				parserData.addLabel(label.getLabelSetterTyped(parser, this.typeID, true));
			}
			else
				parserData.addLabelRegistration(
					customType
						? new LabelRegistration<T>()
						{
							@Override
							public SetterProvider<T> register(final Parser parser) throws SyntaxErrorException
							{
								label.register(parser);
								return label.getLabelSetterTyped(parser, TypeLabelDeclaration.this.typeID, true);
							}
						}
						: label);
			
			return null;
		}
		
		if (m.group(3) != null)
			throw parser.SEE("Can't specify type for label '" + name + "' (already defined) ");
		
		final boolean allowConversion = "^".equals(m.group(1));
		
		if (this.instant)
			parserData.addLabel(parser.getLabelSetterTyped(name, this.typeID, allowConversion));
		else
			parserData.addLabelRegistration(new LabelRegistration<T>()
			{
				@Override
				public SetterProvider<T> register(final Parser parser) throws SyntaxErrorException
				{
					return parser.getLabelSetterTyped(name, TypeLabelDeclaration.this.typeID, allowConversion);
				}
			});
		
		return null;
	}
	
	public static interface LabelRegistration<T>
	{
		public SetterProvider<T> register(Parser parser) throws SyntaxErrorException;
	}
	
	public static class ProvideLabel extends ExCustomParse<Void, CParserData>
	{
		private final int index;
		
		public ProvideLabel(final int index)
		{
			this.index = index;
		}
		
		@Override
		public Void iParse(final Parser parser, final CParserData parserData) throws SyntaxErrorException
		{
			parserData.registerLabel(parser, this.index);
			
			return null;
		}
	}
	
	public static class ProvideLastLabel extends ExCustomParse<Void, CParserData>
	{
		public static final ProvideLastLabel parser = new ProvideLastLabel();
		
		private ProvideLastLabel()
		{
		}
		
		@Override
		public Void iParse(final Parser parser, final CParserData parserData) throws SyntaxErrorException
		{
			parserData.registerLabel(parser);
			
			return null;
		}
	}
}
