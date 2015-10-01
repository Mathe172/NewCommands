package net.minecraft.command.type.metadata;

public class MetaID<T>
{
	private final int id;
	public final MetaType type;
	
	public MetaID(final MetaType type)
	{
		this.type = type;
		this.id = type.count++;
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public int getCount()
	{
		return this.type.count;
	}
	
	public static class MetaType
	{
		private int count = 0;
		public final String name;
		
		public MetaType(final String name)
		{
			this.name = name;
		}
		
		public int getCount()
		{
			return this.count;
		}
	}
}