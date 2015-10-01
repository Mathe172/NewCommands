package net.minecraft.command.construction;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.collections.Completers;
import net.minecraft.command.completion.ProviderCompleter;
import net.minecraft.command.type.IComplete;
import net.minecraft.command.type.custom.json.JsonDescriptor;
import net.minecraft.command.type.custom.json.JsonDescriptor.DefaultElement;
import net.minecraft.command.type.custom.json.JsonDescriptor.Element;
import net.minecraft.command.type.custom.json.JsonDescriptor.TypedImplementation;
import net.minecraft.command.type.custom.json.ParserJsonArray;
import net.minecraft.command.type.custom.json.ParserJsonElement;
import net.minecraft.command.type.custom.json.ParserJsonObject;

public final class JsonConstructorArray extends TypedImplementation implements JsonDescriptor.Array, Element
{
	private final ParserJsonElement elementParser = new ParserJsonElement(this, Completers.bracketCompleter);
	private final List<Element> itemDescriptors = new ArrayList<>();
	private final ParserJsonArray arrayParser = new ParserJsonArray(this);
	
	public JsonConstructorArray(final Type... type)
	{
		super(type);
	}
	
	public JsonConstructorArray(final Element itemDescriptor, final Type... type)
	{
		super(type);
		this.then(itemDescriptor);
	}
	
	public JsonConstructorArray(final IComplete completer, final Type... type)
	{
		super(type);
		this.then(completer);
	}
	
	public JsonConstructorArray(final String[] completions, final Type... type)
	{
		super(type);
		this.then(completions);
	}
	
	public final JsonConstructorArray then(final Element itemDescriptor)
	{
		this.itemDescriptors.add(itemDescriptor);
		return this;
	}
	
	public final JsonConstructorArray then(final IComplete completer)
	{
		return this.then(new DefaultElement(completer));
	}
	
	public final JsonConstructorArray then(final String... completions)
	{
		return this.then(new ProviderCompleter(completions));
	}
	
	@Override
	public ParserJsonElement getElementParser()
	{
		return this.elementParser;
	}
	
	@Override
	public ParserJsonArray getArrayParser()
	{
		return this.arrayParser;
	}
	
	@Override
	public ParserJsonObject getObjectParser()
	{
		return JsonDescriptor.defaultObject.getObjectParser();
	}
	
	@Override
	public Element getElementDescriptor(final int index)
	{
		if (this.itemDescriptors.isEmpty())
			return JsonDescriptor.defaultElement;
		
		return this.itemDescriptors.get(Math.min(index, this.itemDescriptors.size() - 1));
	}
}
