package net.minecraft.command.type.management;

import net.minecraft.command.CommandException;

public abstract class Converter<F, T, E extends CommandException>
{
	public abstract T convert(F toConvert) throws E;
	
	public static <F, T, FT, E extends CommandException> Converter<F, FT, E> chain(final Converter<F, T, ? extends E> converter, final Converter<T, FT, ? extends E> newConverter)
	{
		return new Converter<F, FT, E>()
		{
			@Override
			public final FT convert(final F toConvert) throws E
			{
				return newConverter.convert(converter.convert(toConvert));
			}
		};
	}
	
	public <FT> Converter<F, FT, E> chain(final Converter<T, FT, ? extends E> newConverter)
	{
		return new Converter<F, FT, E>()
		{
			@Override
			public final FT convert(final F toConvert) throws E
			{
				return newConverter.convert(Converter.this.convert(toConvert));
			}
		};
	}
	
	public static final <F extends T, T> SConverter<F, T> primitiveConverter()
	{
		return new SConverter<F, T>()
		{
			@Override
			public T convert(final F toConvert)
			{
				return toConvert;
			}
		};
	}
	
	public static class Chained<F, T, E extends CommandException>
	{
		private final Convertable<F, ?, ?> baseType;
		private final Convertable<T, ?, E> type;
		
		private final Converter<F, T, ? extends E> converter;
		
		public Chained(final Convertable<F, ?, ?> baseType, final Convertable<T, ?, E> type, final Converter<F, T, ? extends E> converter)
		{
			this.baseType = baseType;
			this.type = type;
			
			this.converter = converter;
		}
		
		public Chained(final Convertable<F, ?, ?> from, final Convertable<T, ?, E> to)
		{
			final Converter<F, T, ? extends E> converter = from.getConverter(to);
			
			if (converter == null)
				throw new IllegalArgumentException("No converter from " + from.name + " to " + to.name + " known.");
			
			this.baseType = from;
			this.type = to;
			
			this.converter = converter;
		}
		
		public static <F, T, FT, E extends CommandException> Chained<F, FT, E> chain(final Chained<F, T, ? extends E> base, final Convertable<FT, ?, E> newType)
		{
			final Converter<T, FT, ? extends E> toChain = base.type.getConverter(newType);
			
			if (toChain == null)
				throw new IllegalArgumentException("No converter from " + base.type.name + " to " + newType.name + " known.");
			
			return new Chained<F, FT, E>(base.baseType, newType, Converter.<F, T, FT, E> chain(base.converter, toChain));
		}
		
		public <FT> Chained<F, FT, E> chain(final Convertable<FT, ?, E> newType)
		{
			final Converter<T, FT, ? extends E> toChain = this.type.getConverter(newType);
			
			if (toChain == null)
				throw new IllegalArgumentException("No converter from " + this.type.name + " to " + newType.name + " known.");
			
			return new Chained<>(this.baseType, newType, Converter.chain(this.converter, toChain));
		}
		
		public void register()
		{
			this.baseType.addConverter(this.type, this.converter);
		}
	}
	
}
