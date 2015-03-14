package net.minecraft.command.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SnapList<V> implements Iterable<V>
{	
	private final List<V> list;
	private final List<Integer> snapshots = new ArrayList<>();
	
	public SnapList(List<V> list)
	{
		this.list = list;
	}
	
	public SnapList()
	{
		 this.list = new ArrayList<>();
	}
	
	public int save() {
		this.snapshots.add(this.list.size());
		return this.snapshots.size() - 1;
	}
	
	public void restore(int snapId) {
		this.list.subList(this.snapshots.get(snapId), this.list.size()).clear();
		this.snapshots.subList(snapId, this.snapshots.size()).clear();
	}
	
	public void add(V value) {
		this.list.add(value);
	}
	
	public V get(int index) {
		return this.list.get(index);
	}
	
	@Override
	public Iterator<V> iterator()
	{
		final Iterator<V> it = this.list.iterator();
		
		return new Iterator<V>() {

			@Override
			public boolean hasNext()
			{
				return it.hasNext();
			}

			@Override
			public V next()
			{
				return it.next();
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
			
		};
	}
}
