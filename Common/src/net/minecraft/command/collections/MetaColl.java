package net.minecraft.command.collections;

import net.minecraft.command.type.metadata.MetaID.MetaType;

public class MetaColl
{
	private MetaColl()
	{
	}
	
	public static final MetaType typeSPA = new MetaType("sub-parser data");
	public static final MetaType typeHint = new MetaType("hint");
}
