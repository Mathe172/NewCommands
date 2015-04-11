package net.minecraft.command.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class SnapMap<K, V> implements Iterable<Entry<K, V>>
{
	private final Map<K, Pair<Integer, V>> map = new HashMap<>();
	private int snapId = 0;
	
	public int save()
	{
		return this.snapId++;
	}
	
	public void restore(final int snapId)
	{
		final Iterator<Entry<K, Pair<Integer, V>>> it = this.map.entrySet().iterator();
		
		while (it.hasNext())
			if (it.next().getValue().getLeft() > snapId)
				it.remove();
		
		this.snapId = snapId;
	}
	
	public boolean put(final K key, final V value)
	{
		if (this.map.containsKey(key))
			return false;
		
		this.map.put(key, new ImmutablePair<>(this.snapId, value));
		
		return true;
	}
	
	public V get(final K key)
	{
		final Pair<Integer, V> entry = this.map.get(key);
		
		if (entry == null)
			return null;
		
		return entry.getRight();
	}
	
	public Map<K, V> getMap()
	{
		final Map<K, V> ret = new HashMap<>(this.map.size());
		
		for (final Entry<K, Pair<Integer, V>> e : this.map.entrySet())
			ret.put(e.getKey(), e.getValue().getRight());
		
		return ret;
	}
	
	public Set<K> keySet()
	{
		return this.map.keySet();
	}
	
	@Override
	public Iterator<Entry<K, V>> iterator()
	{
		final Iterator<Entry<K, Pair<Integer, V>>> it = this.map.entrySet().iterator();
		return new Iterator<Entry<K, V>>()
		{
			
			@Override
			public boolean hasNext()
			{
				return it.hasNext();
			}
			
			@Override
			public Entry<K, V> next()
			{
				final Entry<K, Pair<Integer, V>> e = it.next();
				return new ImmutablePair<K, V>(e.getKey(), e.getValue().getRight());
			}
			
			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}
	
	public int size()
	{
		return this.map.size();
	}
}
