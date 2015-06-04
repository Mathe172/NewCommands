package net.minecraft.command.type.custom;

import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.ParsingUtilities.PrimitiveCallback;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.ArgWrapper;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Context;
import net.minecraft.command.parser.MatcherRegistry;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.CTypeParse;
import net.minecraft.command.type.management.Converter;
import net.minecraft.command.type.management.TypeID;

public final class ParserName extends CTypeParse<String>
{
	public static final ParserName parser = new ParserName();
	private final MatcherRegistry m;
	private final String error;
	private final TypeID<String> target;
	
	public static final PrimitiveCallback<String> callback = ParsingUtilities.callbackNonImmediate(ParsingUtilities.idStringConverter);
	
	public ParserName(final MatcherRegistry m, final String contentName, final TypeID<String> target)
	{
		this.m = m;
		this.error = "Expected " + contentName + " around index ";
		this.target = target;
	}
	
	public ParserName(final MatcherRegistry m, final TypeID<String> target)
	{
		this.m = m;
		this.error = "Expected identifier around index ";
		this.target = target;
	}
	
	public ParserName(final MatcherRegistry m)
	{
		this(m, TypeIDs.String);
	}
	
	public ParserName(final TypeID<String> target)
	{
		this(ParsingUtilities.stringMatcher, target);
	}
	
	public ParserName(final MatcherRegistry m, final String contentName)
	{
		this(m, contentName, TypeIDs.String);
	}
	
	public ParserName(final String contentName, final TypeID<String> target)
	{
		this(ParsingUtilities.stringMatcher, contentName, target);
	}
	
	public ParserName(final String contentName)
	{
		this(contentName, TypeIDs.String);
	}
	
	private ParserName()
	{
		this(ParsingUtilities.stringMatcher);
	}
	
	@Override
	public ArgWrapper<String> parse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
	{
		final ArgWrapper<String> ret = ParsingUtilities.parseString(parser, context, this.target, this.m, callback);
		
		if (ret != null)
			return ret;
		
		throw parser.SEE(this.error);
	}
	
	public static class CustomType<T> extends CTypeParse<T>
	{
		private final MatcherRegistry m;
		private final String error;
		private final TypeID<T> target;
		private final Converter<String, T, ?> converter;
		private final PrimitiveCallback<T> callback;
		
		public CustomType(final MatcherRegistry m, final String contentName, final TypeID<T> target, final Converter<String, T, ? super SyntaxErrorException> converter)
		{
			this(m, contentName, target, converter, false);
		}
		
		public CustomType(final String contentName, final TypeID<T> target, final Converter<String, T, ? super SyntaxErrorException> converter)
		{
			this(ParsingUtilities.stringMatcher, contentName, target, converter);
		}
		
		public CustomType(final MatcherRegistry m, final String contentName, final TypeID<T> target, final Converter<String, T, ?> converter, final PrimitiveCallback<T> callback)
		{
			this.m = m;
			this.error = "Expected " + contentName + " ";
			this.target = target;
			this.converter = converter;
			this.callback = callback;
		}
		
		public CustomType(final String contentName, final TypeID<T> target, final Converter<String, T, ?> converter, final PrimitiveCallback<T> callback)
		{
			this(ParsingUtilities.stringMatcher, contentName, target, converter, callback);
		}
		
		public CustomType(final MatcherRegistry m, final String contentName, final TypeID<T> target, final Converter<String, T, ? super SyntaxErrorException> converter, final boolean immediate)
		{
			this(m, contentName, target, converter,
				immediate ? ParsingUtilities.callbackImmediate(converter) : ParsingUtilities.callbackNonImmediate(converter));
		}
		
		public CustomType(final String contentName, final TypeID<T> target, final Converter<String, T, ? super SyntaxErrorException> converter, final boolean immediate)
		{
			this(ParsingUtilities.stringMatcher, contentName, target, converter, immediate);
		}
		
		@Override
		public ArgWrapper<T> parse(final Parser parser, final Context context) throws SyntaxErrorException, CompletionException
		{
			final ArgWrapper<T> ret = ParsingUtilities.parseString(parser, context, this.target, this.converter, this.m, this.callback);
			
			if (ret != null)
				return ret;
			
			throw parser.SEE(this.error);
		}
	}
}
