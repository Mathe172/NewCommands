package net.minecraft.command.type;

import net.minecraft.command.arg.CommandArg;

public abstract class Converter<F, T>
{
	public abstract CommandArg<T> convert(CommandArg<F> toConvert);
	
	public <FT> Converter<F, FT> chain(final Converter<T, FT> newConverter)
	{
		final Converter<F, T> that = this;
		
		return new Converter<F, FT>()
		{
			@Override
			public final CommandArg<FT> convert(final CommandArg<F> toConvert)
			{
				return newConverter.convert(that.convert(toConvert));
			}
		};
	}
	
	public static class Chained<F, T>
	{
		private final TypeID<F> baseType;
		
		private final Converter<F, T> converter;
		private final TypeID<T> type;
		
		public Chained(TypeID<F> baseType, final Converter<F, T> converter, final TypeID<T> type)
		{
			this.baseType = baseType;
			this.converter = converter;
			this.type = type;
		}
		
		public Chained(final TypeID<F> from, final TypeID<T> to)
		{
			final Converter<F, T> converter = from.getConverter(to);
			
			if (converter == null)
				throw new IllegalArgumentException("No converter from " + from.name + " to " + to.name + " known.");
			
			this.baseType = from;
			this.converter = converter;
			this.type = to;
		}
		
		public <FT> Chained<F, FT> chain(final TypeID<FT> newType)
		{
			final Converter<T, FT> toChain = this.type.getConverter(newType);
			
			if (toChain == null)
				throw new IllegalArgumentException("No converter from " + this.type.name + " to " + newType.name + " known.");
			
			return new Chained<>(this.baseType, this.converter.chain(toChain), newType);
		}
		
		public void register()
		{
			this.baseType.addConverter(this.type, this.converter);
		}
	}
}
