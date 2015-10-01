package net.minecraft.command.type.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.trie.PatriciaTrie;

import net.minecraft.command.CommandException;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.arg.PrimitiveParameter;
import net.minecraft.command.collections.Relations;
import net.minecraft.command.completion.TabCompletion;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.CTypeParse;
import net.minecraft.command.type.IParse;
import net.minecraft.command.type.custom.TypeTypeID;

public class TypeID<T> extends CConvertable<CommandArg<T>, ArgWrapper<T>>
{
	private static final PatriciaTrie<TypeID<?>> typeIDs = new PatriciaTrie<>();
	
	public final Convertable<T, ?, CommandException> primitive;
	public final TabCompletion completion;
	public final IParse<TypeID<?>> typeIDParser;
	
	public TypeID(final String name)
	{
		super(name);
		
		if (ParsingUtilities.spaceMatcher.matcher(name).matches() || !ParsingUtilities.stringMatcher.matcher(name).matches())
			throw new IllegalArgumentException("Invalid name for TypeID: '" + name + "'");
		
		this.typeIDParser = new TypeTypeID(Collections.<TypeID<?>> singleton(this));
		
		this.primitive = new ConvertableUnwrapped<>(name + ".primitive");
		this.completion = new TabCompletion.Escaped(name);
	}
	
	@Override
	public void init()
	{
		typeIDs.put(this.name, this);
		this.primitive.init();
		super.init();
		Relations.commandArg.registerPair(this.primitive, this);
	}
	
	public static TypeID<?> get(final String name)
	{
		return typeIDs.get(name);
	}
	
	public static void clearAll()
	{
		typeIDs.clear();
	}
	
	// checked...
	@SuppressWarnings("unchecked")
	@Override
	public ArgWrapper<T> convertFrom(final Parser parser, final ArgWrapper<?> toConvert) throws SyntaxErrorException
	{
		if (toConvert.type == this)
			return (ArgWrapper<T>) toConvert;
		
		return this.wrap(toConvert.iConvertTo(parser, this));
	}
	
	public ArgWrapper<T> wrap(final CommandArg<T> toWrap)
	{
		return toWrap.wrap(this);
	}
	
	public ArgWrapper<T> wrap(final T toWrap)
	{
		return new PrimitiveParameter<>(toWrap).wrap(this);
	}
	
	public CDataType<T> wrap(final IParse<? extends CommandArg<T>> toWrap)
	{
		return new CTypeParse<T>()
		{
			@Override
			public ArgWrapper<T> iParse(final Parser parser, final Context context) throws SyntaxErrorException
			{
				return TypeID.this.wrap(toWrap.parse(parser, context));
			}
		};
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
		return this.addList(provider, this.name + ".list");
	}
	
	public final TypeID<List<T>> addList(final String listName)
	{
		return this.addList(this.listToItemProvider(), listName);
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
	
	public final <R> void addChild(final TypeID<R> child, final Converter<R, T, ?> converter)
	{
		child.addDefaultConverter(this, converter);
		Relations.relSuper.registerPair(this.primitive, child.primitive, converter);
	}
}
