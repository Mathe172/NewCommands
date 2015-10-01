package net.minecraft.command.collections;

import java.util.Arrays;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import net.minecraft.command.completion.ProviderCompleter;
import net.minecraft.command.construction.JsonConstructor;
import net.minecraft.command.construction.JsonConstructor.ConstructionHelper;
import net.minecraft.command.construction.JsonConstructorArray;
import net.minecraft.command.type.custom.TypeScoreObjective;
import net.minecraft.command.type.custom.json.JsonDescriptor.Element;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.IChatComponent;

public class JsonDescriptors extends ConstructionHelper
{
	public static final Element IChatComponent;
	
	private JsonDescriptors()
	{
	}
	
	static
	{
		final JsonConstructor SingleIChatComponent = object(IChatComponent.class);
		final JsonConstructorArray ArrayIChatComponent = array();
		
		IChatComponent = merge(SingleIChatComponent, ArrayIChatComponent);
		ArrayIChatComponent.then(IChatComponent);
		
		SingleIChatComponent.key(
			"text",
			"translate",
			"selector",
			"insertion")
			.key("with", IChatComponent)
			.key("score", object()
				.key("name", Completers.scoreHolder)
				.key("objective", TypeScoreObjective.completer))
			.key("color", ProviderCompleter.create(Completers.chatColors))
			.key("bold", Completers.bool)
			.key("italic", Completers.bool)
			.key("underlined", Completers.bool)
			.key("strikethrough", Completers.bool)
			.key("obfuscated", Completers.bool)
			.key("hoverEvent", object()
				.key("action", ProviderCompleter.create(Lists.transform(Arrays.asList(HoverEvent.Action.values()), new Function<HoverEvent.Action, String>()
				{
					@Override
					public String apply(final HoverEvent.Action input)
					{
						return input.getCanonicalName();
					}
				})))
				.key("value"))
			.key("clickEvent", object()
				.key("action", ProviderCompleter.create(Lists.transform(Arrays.asList(ClickEvent.Action.values()), new Function<ClickEvent.Action, String>()
				{
					@Override
					public String apply(final ClickEvent.Action input)
					{
						return input.getCanonicalName();
					}
				})))
				.key("value"))
			.key("extra", IChatComponent);
	}
}
