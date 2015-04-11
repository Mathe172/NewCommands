package net.minecraft.command.type.management;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.type.custom.Relations;

public class TypeID<T> extends CConvertable<CommandArg<T>, ArgWrapper<T>>
{
	public final Convertable<T, ?, CommandException> primitive;
	
	public TypeID(final String name)
	{
		super(name);
		this.primitive = new ConvertableUnwrapped<>(name + ".primitive");
	}
	
	@Override
	public void init()
	{
		this.primitive.init();
		super.init();
		Relations.commandArg.registerPair(this.primitive, this);
	}
	
	// checked...
	@SuppressWarnings("unchecked")
	@Override
	public ArgWrapper<T> convertFrom(final ArgWrapper<?> toConvert) throws SyntaxErrorException
	{
		if (toConvert.type == this)
			return (ArgWrapper<T>) toConvert;
		
		return this.wrap(toConvert.iConvertTo(this));
	}
	
	public ArgWrapper<T> wrap(final CommandArg<T> toWrap)
	{
		return new ArgWrapper<>(this, toWrap);
	}
	
	public ArgWrapper<T> wrap(final T toWrap)
	{
		return new ArgWrapper<>(this, toWrap);
	}
	
	public final <U> void addPrimitiveConverter(final TypeID<U> type, final Converter<T, U, ?> converter)
	{
		this.primitive.addConverter(type.primitive, converter);
	}
	
	public static interface ExceptionProvider
	{
		public CommandException create();
	}
	
	public final TypeID<List<T>> addList(final ExceptionProvider provider, final String listName)
	{
		final TypeID<List<T>> list = new TypeID<List<T>>(listName)
		{
			@Override
			public void init()
			{
				super.init();
				Relations.list.registerPair(TypeID.this.primitive, this.primitive);
				
				Relations.relDefault.registerPair(this.primitive, TypeID.this.primitive, new Converter<List<T>, T, CommandException>()
				{
					@Override
					public final T convert(final List<T> toConvert) throws CommandException
					{
						
						if (toConvert.size() != 1)
							throw provider.create();
						
						return toConvert.get(0);
					}
				});
				
				Relations.relDefault.registerPair(TypeID.this.primitive, this.primitive, new Converter<T, List<T>, SyntaxErrorException>()
				{
					@Override
					public List<T> convert(final T toConvert)
					{
						final List<T> list = new ArrayList<>();
						
						list.add(toConvert);
						
						return list;
					}
				});
			}
		};
		
		return list;
	}
	
	public final TypeID<List<T>> addList(final ExceptionProvider provider)
	{
		return addList(provider, this.name + ".list");
	}
	
	public final TypeID<List<T>> addList(final String listName)
	{
		return this.addList(listToItemProvider(), listName);
	}
	
	public final TypeID<List<T>> addList()
	{
		return this.addList(this.name + ".list");
	}
	
	public final ExceptionProvider listToItemProvider()
	{
		return new ExceptionProvider()
		{
			@Override
			public CommandException create()
			{
				return new CommandException("The " + TypeID.this.name + "-list does not contain a single element");
			}
		};
	}
	
	public final <R> void addDefaultConverter(final TypeID<R> target, final Converter<T, R, ?> converter)
	{
		Relations.relDefault.registerPair(this.primitive, target.primitive, converter);
	}
}
