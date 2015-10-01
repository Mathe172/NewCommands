package net.minecraft.command.arg;

import net.minecraft.command.CommandException;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.Setter.SetterProvider;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.custom.TypeLabelDeclaration.LabelRegistration;
import net.minecraft.command.type.management.Converter;
import net.minecraft.command.type.management.TypeID;

public class LabelWrapper<R> extends ExArgWrapper<R, CachedArg<R>> implements SetterProvider<R>, LabelRegistration<R>
{
	public LabelWrapper(final TypeID<R> type, final String name)
	{
		super(type, new CachedArg<R>(name));
	}
	
	// This is checked...
	@SuppressWarnings("unchecked")
	@Override
	public <T> Setter<T> getSetter(final TypeID<T> type)
	{
		this.checkTypes(type);
		
		return (Setter<T>) this.arg();
	}
	
	public static class LabelSetter<R> extends AbstractWrapper<R> implements SetterProvider<R>
	{
		private final Setter<R> setter;
		
		public LabelSetter(final TypeID<R> type, final Setter<R> setter)
		{
			super(type);
			this.setter = setter;
		}
		
		// This is checked...
		@Override
		@SuppressWarnings("unchecked")
		public <T> Setter<T> getSetter(final TypeID<T> type)
		{
			this.checkTypes(type);
			
			return (Setter<T>) this.setter;
		}
	}
	
	@Override
	public SetterProvider<R> register(final Parser parser) throws SyntaxErrorException
	{
		parser.addLabel(this.arg().name, this);
		return this;
	}
	
	// This is checked
	@SuppressWarnings("unchecked")
	public <T> Setter<T> getLabelSetter(final Parser parser, final TypeID<T> type, final boolean allowConversion) throws SyntaxErrorException
	{
		if (this.type == type)
			return (Setter<T>) this.arg();
		
		return this.iGetLabelSetter(parser, type, allowConversion);
	}
	
	// This is checked
	@SuppressWarnings("unchecked")
	public <T> SetterProvider<T> getLabelSetterTyped(final Parser parser, final TypeID<T> type, final boolean allowConversion) throws SyntaxErrorException
	{
		if (this.type == type)
			return (SetterProvider<T>) this;
		
		return new LabelSetter<>(type, this.iGetLabelSetter(parser, type, allowConversion));
	}
	
	private <T> Setter<T> iGetLabelSetter(final Parser parser, final TypeID<T> type, final boolean allowConversion) throws SyntaxErrorException
	{
		if (!allowConversion)
			throw parser.SEE("Label '" + this.arg().name + "' is not of correct type ('" + this.type.name + "' instead of '" + type.name + "')' ");
		
		final Converter<T, R, ?> converter = type.primitive.getConverter(this.type.primitive);
		
		if (converter == null)
			throw parser.SEE("Label '" + this.arg().name + "' of incorrect type, can't convert from '" + type.name + "' to '" + this.type.name + "' ");
		
		return new Setter<T>()
		{
			@Override
			public void set(final T value) throws CommandException
			{
				LabelWrapper.this.arg.set(converter.convert(value));
			}
		};
	}
}
