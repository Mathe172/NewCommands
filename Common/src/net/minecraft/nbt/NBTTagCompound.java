package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Maps;

public class NBTTagCompound extends NBTBase
{
	private static final Logger logger = LogManager.getLogger();
	
	public NBTTagCompound()
	{
		this.tagMap = Maps.newHashMap();
	}
	
	public NBTTagCompound(final Map<String, NBTBase> data)
	{
		this.tagMap = data;
	}
	
	/**
	 * The key-value pairs for the tag. Each key is a UTF string, each value is a tag.
	 */
	Map<String, NBTBase> tagMap;
	private static final String __OBFID = "CL_00001215";
	
	/**
	 * Write the actual data contents of the tag, implemented in NBT extension classes
	 */
	@Override
	void write(final DataOutput output) throws IOException
	{
		final Iterator<String> var2 = this.tagMap.keySet().iterator();
		
		while (var2.hasNext())
		{
			final String var3 = var2.next();
			final NBTBase var4 = this.tagMap.get(var3);
			writeEntry(var3, var4, output);
		}
		
		output.writeByte(0);
	}
	
	@Override
	void read(final DataInput input, final int depth, final NBTSizeTracker sizeTracker) throws IOException
	{
		if (depth > 512)
		{
			throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
		}
		else
		{
			this.tagMap.clear();
			byte var4;
			
			while ((var4 = readType(input, sizeTracker)) != 0)
			{
				final String var5 = readKey(input, sizeTracker);
				sizeTracker.read(16 * var5.length());
				final NBTBase var6 = readNBT(var4, var5, input, depth + 1, sizeTracker);
				this.tagMap.put(var5, var6);
			}
		}
	}
	
	/**
	 * Gets a set with the names of the keys in the tag compound.
	 */
	public Set<String> getKeySet()
	{
		return this.tagMap.keySet();
	}
	
	/**
	 * Gets the type byte for the tag.
	 */
	@Override
	public byte getId()
	{
		return (byte) 10;
	}
	
	/**
	 * Stores the given tag into the map with the given string key. This is mostly used to store tag lists.
	 */
	public void setTag(final String key, final NBTBase value)
	{
		this.tagMap.put(key, value);
	}
	
	/**
	 * Stores a new NBTTagByte with the given byte value into the map with the given string key.
	 */
	public void setByte(final String key, final byte value)
	{
		this.setTag(key, new NBTTagByte(value));
	}
	
	/**
	 * Stores a new NBTTagShort with the given short value into the map with the given string key.
	 */
	public void setShort(final String key, final short value)
	{
		this.setTag(key, new NBTTagShort(value));
	}
	
	/**
	 * Stores a new NBTTagInt with the given integer value into the map with the given string key.
	 */
	public void setInteger(final String key, final int value)
	{
		this.setTag(key, new NBTTagInt(value));
	}
	
	/**
	 * Stores a new NBTTagLong with the given long value into the map with the given string key.
	 */
	public void setLong(final String key, final long value)
	{
		this.setTag(key, new NBTTagLong(value));
	}
	
	/**
	 * Stores a new NBTTagFloat with the given float value into the map with the given string key.
	 */
	public void setFloat(final String key, final float value)
	{
		this.setTag(key, new NBTTagFloat(value));
	}
	
	/**
	 * Stores a new NBTTagDouble with the given double value into the map with the given string key.
	 */
	public void setDouble(final String key, final double value)
	{
		this.setTag(key, new NBTTagDouble(value));
	}
	
	/**
	 * Stores a new NBTTagString with the given string value into the map with the given string key.
	 */
	public void setString(final String key, final String value)
	{
		this.setTag(key, new NBTTagString(value));
	}
	
	/**
	 * Stores a new NBTTagByteArray with the given array as data into the map with the given string key.
	 */
	public void setByteArray(final String key, final byte[] value)
	{
		this.setTag(key, new NBTTagByteArray(value));
	}
	
	/**
	 * Stores a new NBTTagIntArray with the given array as data into the map with the given string key.
	 */
	public void setIntArray(final String key, final int[] value)
	{
		this.setTag(key, new NBTTagIntArray(value));
	}
	
	/**
	 * Stores the given boolean value as a NBTTagByte, storing 1 for true and 0 for false, using the given string key.
	 */
	public void setBoolean(final String key, final boolean value)
	{
		this.setByte(key, (byte) (value ? 1 : 0));
	}
	
	/**
	 * gets a generic tag with the specified name
	 */
	public NBTBase getTag(final String key)
	{
		return this.tagMap.get(key);
	}
	
	/**
	 * Get the Type-ID for the entry with the given key
	 */
	public byte getTagType(final String key)
	{
		final NBTBase var2 = this.tagMap.get(key);
		return var2 != null ? var2.getId() : 0;
	}
	
	/**
	 * Returns whether the given string has been previously stored as a key in the map.
	 */
	public boolean hasKey(final String key)
	{
		return this.tagMap.containsKey(key);
	}
	
	public boolean hasKey(final String key, final int type)
	{
		final byte var3 = this.getTagType(key);
		
		if (var3 == type)
		{
			return true;
		}
		else if (type != 99)
		{
			if (var3 > 0)
			{
				;
			}
			
			return false;
		}
		else
		{
			return var3 == 1 || var3 == 2 || var3 == 3 || var3 == 4 || var3 == 5 || var3 == 6;
		}
	}
	
	/**
	 * Retrieves a byte value using the specified key, or 0 if no such key was stored.
	 */
	public byte getByte(final String key)
	{
		try
		{
			return !this.hasKey(key, 99) ? 0 : ((NBTBase.NBTPrimitive) this.tagMap.get(key)).getByte();
		} catch (final ClassCastException var3)
		{
			return (byte) 0;
		}
	}
	
	/**
	 * Retrieves a short value using the specified key, or 0 if no such key was stored.
	 */
	public short getShort(final String key)
	{
		try
		{
			return !this.hasKey(key, 99) ? 0 : ((NBTBase.NBTPrimitive) this.tagMap.get(key)).getShort();
		} catch (final ClassCastException var3)
		{
			return (short) 0;
		}
	}
	
	/**
	 * Retrieves an integer value using the specified key, or 0 if no such key was stored.
	 */
	public int getInteger(final String key)
	{
		try
		{
			return !this.hasKey(key, 99) ? 0 : ((NBTBase.NBTPrimitive) this.tagMap.get(key)).getInt();
		} catch (final ClassCastException var3)
		{
			return 0;
		}
	}
	
	/**
	 * Retrieves a long value using the specified key, or 0 if no such key was stored.
	 */
	public long getLong(final String key)
	{
		try
		{
			return !this.hasKey(key, 99) ? 0L : ((NBTBase.NBTPrimitive) this.tagMap.get(key)).getLong();
		} catch (final ClassCastException var3)
		{
			return 0L;
		}
	}
	
	/**
	 * Retrieves a float value using the specified key, or 0 if no such key was stored.
	 */
	public float getFloat(final String key)
	{
		try
		{
			return !this.hasKey(key, 99) ? 0.0F : ((NBTBase.NBTPrimitive) this.tagMap.get(key)).getFloat();
		} catch (final ClassCastException var3)
		{
			return 0.0F;
		}
	}
	
	/**
	 * Retrieves a double value using the specified key, or 0 if no such key was stored.
	 */
	public double getDouble(final String key)
	{
		try
		{
			return !this.hasKey(key, 99) ? 0.0D : ((NBTBase.NBTPrimitive) this.tagMap.get(key)).getDouble();
		} catch (final ClassCastException var3)
		{
			return 0.0D;
		}
	}
	
	/**
	 * Retrieves a string value using the specified key, or an empty string if no such key was stored.
	 */
	public String getString(final String key)
	{
		try
		{
			return !this.hasKey(key, 8) ? "" : this.tagMap.get(key).getString();
		} catch (final ClassCastException var3)
		{
			return "";
		}
	}
	
	/**
	 * Retrieves a byte array using the specified key, or a zero-length array if no such key was stored.
	 */
	public byte[] getByteArray(final String key)
	{
		try
		{
			return !this.hasKey(key, 7) ? new byte[0] : ((NBTTagByteArray) this.tagMap.get(key)).getByteArray();
		} catch (final ClassCastException var3)
		{
			throw new ReportedException(this.createCrashReport(key, 7, var3));
		}
	}
	
	/**
	 * Retrieves an int array using the specified key, or a zero-length array if no such key was stored.
	 */
	public int[] getIntArray(final String key)
	{
		try
		{
			return !this.hasKey(key, 11) ? new int[0] : ((NBTTagIntArray) this.tagMap.get(key)).getIntArray();
		} catch (final ClassCastException var3)
		{
			throw new ReportedException(this.createCrashReport(key, 11, var3));
		}
	}
	
	/**
	 * Retrieves a NBTTagCompound subtag matching the specified key, or a new empty NBTTagCompound if no such key was stored.
	 */
	public NBTTagCompound getCompoundTag(final String key)
	{
		try
		{
			return !this.hasKey(key, 10) ? new NBTTagCompound() : (NBTTagCompound) this.tagMap.get(key);
		} catch (final ClassCastException var3)
		{
			throw new ReportedException(this.createCrashReport(key, 10, var3));
		}
	}
	
	/**
	 * Gets the NBTTagList object with the given name. Args: name, NBTBase type
	 */
	public NBTTagList getTagList(final String key, final int type)
	{
		try
		{
			if (this.getTagType(key) != 9)
			{
				return new NBTTagList();
			}
			else
			{
				final NBTTagList var3 = (NBTTagList) this.tagMap.get(key);
				return var3.tagCount() > 0 && var3.getTagType() != type ? new NBTTagList() : var3;
			}
		} catch (final ClassCastException var4)
		{
			throw new ReportedException(this.createCrashReport(key, 9, var4));
		}
	}
	
	/**
	 * Retrieves a boolean value using the specified key, or false if no such key was stored. This uses the getByte method.
	 */
	public boolean getBoolean(final String key)
	{
		return this.getByte(key) != 0;
	}
	
	/**
	 * Remove the specified tag.
	 */
	public void removeTag(final String key)
	{
		this.tagMap.remove(key);
	}
	
	@Override
	public String toString()
	{
		String var1 = "{";
		String var3;
		
		for (final Iterator<String> var2 = this.tagMap.keySet().iterator(); var2.hasNext(); var1 = var1 + var3 + ':' + this.tagMap.get(var3) + ',')
		{
			var3 = var2.next();
		}
		
		return var1 + "}";
	}
	
	/**
	 * Return whether this compound has no tags.
	 */
	@Override
	public boolean hasNoTags()
	{
		return this.tagMap.isEmpty();
	}
	
	/**
	 * Create a crash report which indicates a NBT read error.
	 */
	private CrashReport createCrashReport(final String key, final int expectedType, final ClassCastException ex)
	{
		final CrashReport var4 = CrashReport.makeCrashReport(ex, "Reading NBT data");
		final CrashReportCategory var5 = var4.makeCategoryDepth("Corrupt NBT tag", 1);
		var5.addCrashSectionCallable("Tag type found", new Callable<String>()
		{
			private static final String __OBFID = "CL_00001216";
			
			@Override
			public String call()
			{
				return NBTBase.NBT_TYPES[NBTTagCompound.this.tagMap.get(key).getId()];
			}
		});
		var5.addCrashSectionCallable("Tag type expected", new Callable<String>()
		{
			private static final String __OBFID = "CL_00001217";
			
			@Override
			public String call()
			{
				return NBTBase.NBT_TYPES[expectedType];
			}
		});
		var5.addCrashSection("Tag name", key);
		return var4;
	}
	
	/**
	 * Creates a clone of the tag.
	 */
	@Override
	public NBTBase copy()
	{
		final NBTTagCompound var1 = new NBTTagCompound();
		final Iterator<String> var2 = this.tagMap.keySet().iterator();
		
		while (var2.hasNext())
		{
			final String var3 = var2.next();
			var1.setTag(var3, this.tagMap.get(var3).copy());
		}
		
		return var1;
	}
	
	@Override
	public boolean equals(final Object p_equals_1_)
	{
		if (super.equals(p_equals_1_))
		{
			final NBTTagCompound var2 = (NBTTagCompound) p_equals_1_;
			return this.tagMap.entrySet().equals(var2.tagMap.entrySet());
		}
		else
		{
			return false;
		}
	}
	
	@Override
	public int hashCode()
	{
		return super.hashCode() ^ this.tagMap.hashCode();
	}
	
	private static void writeEntry(final String name, final NBTBase data, final DataOutput output) throws IOException
	{
		output.writeByte(data.getId());
		
		if (data.getId() != 0)
		{
			output.writeUTF(name);
			data.write(output);
		}
	}
	
	private static byte readType(final DataInput input, final NBTSizeTracker sizeTracker) throws IOException
	{
		return input.readByte();
	}
	
	private static String readKey(final DataInput input, final NBTSizeTracker sizeTracker) throws IOException
	{
		return input.readUTF();
	}
	
	static NBTBase readNBT(final byte id, final String key, final DataInput input, final int depth, final NBTSizeTracker sizeTracker)
	{
		final NBTBase var5 = NBTBase.createNewByType(id);
		
		try
		{
			var5.read(input, depth, sizeTracker);
			return var5;
		} catch (final IOException var9)
		{
			final CrashReport var7 = CrashReport.makeCrashReport(var9, "Loading NBT data");
			final CrashReportCategory var8 = var7.makeCategory("NBT Tag");
			var8.addCrashSection("Tag name", key);
			var8.addCrashSection("Tag type", Byte.valueOf(id));
			throw new ReportedException(var7);
		}
	}
	
	/**
	 * Merges this NBTTagCompound with the given compound. Any sub-compounds are merged using the same methods, other types of tags are overwritten from the given compound.
	 */
	public void merge(final NBTTagCompound other)
	{
		mergeChecked(other);
	}
	
	public boolean mergeChecked(final NBTTagCompound other)
	{
		final Iterator<String> it = other.tagMap.keySet().iterator();
		
		boolean modified = false;
		
		while (it.hasNext())
		{
			final String key = it.next();
			final NBTBase value = other.tagMap.get(key);
			
			if (value.getId() == 10)
			{
				if (this.hasKey(key, 10))
				{
					final NBTTagCompound var5 = this.getCompoundTag(key);
					modified |= var5.mergeChecked((NBTTagCompound) value);
				}
				else
				{
					this.setTag(key, new NBTTagCompound.CopyOnWrite((NBTTagCompound) value));
					modified = true;
				}
			}
			else
			{
				this.setTag(key, value.copy());
				modified = true;
			}
		}
		
		return modified;
	}
	
	public static class CopyOnWrite extends NBTTagCompound
	{
		boolean copied = false;
		
		public CopyOnWrite(final NBTTagCompound tag)
		{
			super(tag.tagMap);
		}
		
		@Override
		public void setTag(final String key, final NBTBase value)
		{
			this.shallowCopy();
			super.setTag(key, value);
		}
		
		@Override
		public void removeTag(final String key)
		{
			if (!this.copied && this.hasKey(key))
				this.shallowCopy();
			super.removeTag(key);
		}
		
		private void shallowCopy()
		{
			if (!this.copied)
			{
				this.copied = true;
				
				final Map<String, NBTBase> oldMap = this.tagMap;
				this.tagMap = new HashMap<>();
				
				this.tagMap.putAll(oldMap);
			}
		}
		
		@Override
		public NBTTagCompound getCompoundTag(final String key)
		{
			return new CopyOnWrite(super.getCompoundTag(key));
		}
		
		@Override
		public NBTBase getTag(final String key)
		{
			final NBTBase tag = super.getTag(key);
			
			if (tag == null)
				return null;
			
			if (tag.getId() == 9)
				return new NBTTagList.CopyOnWrite((NBTTagList) tag);
			else if (tag.getId() == 10)
				return new CopyOnWrite((NBTTagCompound) tag);
			else
				return tag.copy();
		}
		
		@Override
		public NBTTagList getTagList(final String key, final int type)
		{
			return new NBTTagList.CopyOnWrite(super.getTagList(key, type));
		}
	}
}
