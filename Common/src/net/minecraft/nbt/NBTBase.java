package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;

public abstract class NBTBase
{
	public static final String[] NBT_TYPES = new String[] { "END", "BYTE", "SHORT", "INT", "LONG", "FLOAT", "DOUBLE", "BYTE[]", "STRING", "LIST", "COMPOUND", "INT[]" };
	private static final String __OBFID = "CL_00001229";
	
	/**
	 * Write the actual data contents of the tag, implemented in NBT extension classes
	 */
	abstract void write(DataOutput output) throws IOException;
	
	abstract void read(DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException;
	
	@Override
	public abstract String toString();
	
	/**
	 * Gets the type byte for the tag.
	 */
	public abstract byte getId();
	
	/**
	 * Creates a new NBTBase object that corresponds with the passed in id.
	 */
	protected static NBTBase createNewByType(byte id)
	{
		switch (id)
		{
		case 0:
			return new NBTTagEnd();
			
		case 1:
			return new NBTTagByte();
			
		case 2:
			return new NBTTagShort();
			
		case 3:
			return new NBTTagInt();
			
		case 4:
			return new NBTTagLong();
			
		case 5:
			return new NBTTagFloat();
			
		case 6:
			return new NBTTagDouble();
			
		case 7:
			return new NBTTagByteArray();
			
		case 8:
			return new NBTTagString();
			
		case 9:
			return new NBTTagList();
			
		case 10:
			return new NBTTagCompound();
			
		case 11:
			return new NBTTagIntArray();
			
		default:
			return null;
		}
	}
	
	/**
	 * Creates a clone of the tag.
	 */
	public abstract NBTBase copy();
	
	/**
	 * Return whether this compound has no tags.
	 */
	public boolean hasNoTags()
	{
		return false;
	}
	
	@Override
	public boolean equals(Object p_equals_1_)
	{
		if (!(p_equals_1_ instanceof NBTBase))
		{
			return false;
		}
		else
		{
			final NBTBase var2 = (NBTBase) p_equals_1_;
			return this.getId() == var2.getId();
		}
	}
	
	@Override
	public int hashCode()
	{
		return this.getId();
	}
	
	protected String getString()
	{
		return this.toString();
	}
	
	public abstract static class NBTPrimitive extends NBTBase
	{
		private static final String __OBFID = "CL_00001230";
		
		public abstract long getLong();
		
		public abstract int getInt();
		
		public abstract short getShort();
		
		public abstract byte getByte();
		
		public abstract double getDouble();
		
		public abstract float getFloat();
	}
	
	public static boolean compareTags(NBTBase p_175775_0_, NBTBase p_175775_1_, boolean p_175775_2_)
	{
		if (p_175775_0_ == p_175775_1_)
		{
			return true;
		}
		else if (p_175775_0_ == null)
		{
			return true;
		}
		else if (p_175775_1_ == null)
		{
			return false;
		}
		else if (!p_175775_0_.getClass().equals(p_175775_1_.getClass()))
		{
			return false;
		}
		else if (p_175775_0_ instanceof NBTTagCompound)
		{
			final NBTTagCompound var9 = (NBTTagCompound) p_175775_0_;
			final NBTTagCompound var10 = (NBTTagCompound) p_175775_1_;
			final Iterator var11 = var9.getKeySet().iterator();
			String var12;
			NBTBase var13;
			
			do
			{
				if (!var11.hasNext())
				{
					return true;
				}
				
				var12 = (String) var11.next();
				var13 = var9.getTag(var12);
			} while (compareTags(var13, var10.getTag(var12), p_175775_2_));
			
			return false;
		}
		else if (p_175775_0_ instanceof NBTTagList && p_175775_2_)
		{
			final NBTTagList var3 = (NBTTagList) p_175775_0_;
			final NBTTagList var4 = (NBTTagList) p_175775_1_;
			
			if (var3.tagCount() == 0)
			{
				return var4.tagCount() == 0;
			}
			else
			{
				int var5 = 0;
				
				while (var5 < var3.tagCount())
				{
					final NBTBase var6 = var3.get(var5);
					boolean var7 = false;
					int var8 = 0;
					
					while (true)
					{
						if (var8 < var4.tagCount())
						{
							if (!compareTags(var6, var4.get(var8), p_175775_2_))
							{
								++var8;
								continue;
							}
							
							var7 = true;
						}
						
						if (!var7)
						{
							return false;
						}
						
						++var5;
						break;
					}
				}
				
				return true;
			}
		}
		else
		{
			return p_175775_0_.equals(p_175775_1_);
		}
	}
}
