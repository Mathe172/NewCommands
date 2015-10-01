package net.minecraft.command.type.custom.json;

import net.minecraft.command.collections.JsonDescriptors;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.type.CDataType;
import net.minecraft.command.type.custom.json.JsonUtilities.DeserializationManager;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumTypeAdapterFactory;
import net.minecraft.util.IChatComponent;

public class TypeJsonText
{
	private TypeJsonText()
	{
	}
	
	public static final CDataType<IChatComponent> parser;
	
	static
	{
		final DeserializationManager.Builder builder = new DeserializationManager.Builder();
		builder.registerTypeHierarchyAdapter(IChatComponent.class, new IChatComponent.Serializer());
		builder.registerTypeHierarchyAdapter(ChatStyle.class, new ChatStyle.Serializer());
		builder.registerTypeAdapterFactory(new EnumTypeAdapterFactory());
		parser = new TypeJson<>(JsonDescriptors.IChatComponent, builder.create(IChatComponent.Serializer.GSON), IChatComponent.class, TypeIDs.IChatComponent);
	}
}
