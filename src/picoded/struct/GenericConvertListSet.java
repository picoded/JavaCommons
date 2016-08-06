package picoded.struct;

import java.util.*;

///
/// A combination of ArrayList and HashSet!
/// Only add to this array if not inside the list previously
///
@SuppressWarnings("unchecked")
public class GenericConvertListSet<V> implements Set<V>, GenericConvertList<V> {
	
	///
	/// The actual internal implmentation
	///
	private final List<V> list = new ArrayList<V>();
	private final Set<V> set = new HashSet<V>();
	
	/// @see java.util.List#add(V)
	public void add(int index, Object element) {
		if (this.set.add((V) element)) {
			list.add(index, (V) element);
		}
	}
	
	/// @see java.util.Set#add(V)
	public boolean add(Object o) {
		if (this.set.add((V) o)) {
			return this.list.add((V) o);
		} else {
			return false;
		}
	}
	
	/// @see java.util.List#addAll(Collection)
	public boolean addAll(Collection<? extends V> c) {
		boolean changed = false;
		Iterator<V> i = (Iterator<V>) (c.iterator());
		while (i.hasNext()) {
			Object element = i.next();
			if (this.add((V) element)) {
				changed = true;
			}
		}
		return changed;
	}
	
	/// @see java.util.List#addAll(int, Collection)
	public boolean addAll(int index, Collection<? extends V> c) {
		boolean changed = false;
		int insertIndex = index;
		Iterator<V> i = (Iterator<V>) (c.iterator());
		while (i.hasNext()) {
			Object element = i.next();
			if (this.set.add((V) element)) {
				this.list.add(insertIndex++, (V) element);
				changed = true;
			}
		}
		return changed;
	}
	
	/// @see java.util.List#clear()
	public void clear() {
		this.set.clear();
		this.list.clear();
	}
	
	/// @see java.util.List#contains(Object)
	public boolean contains(Object o) {
		return this.set.contains(o);
	}
	
	/// @see java.util.List#containsAll(Collection)
	public boolean containsAll(Collection<?> c) {
		return this.set.containsAll(c);
	}
	
	/// @see java.util.List#get(int)
	public V get(int index) {
		return this.list.get(index);
	}
	
	/// @see java.util.List#indexOf(Object)
	public int indexOf(Object o) {
		return this.list.indexOf(o);
	}
	
	/// @see java.util.List#isEmpty()
	public boolean isEmpty() {
		return this.set.isEmpty();
	}
	
	/// @see java.util.List#iterator()
	public Iterator<V> iterator() {
		return this.list.iterator();
	}
	
	/// @see java.util.List#lastIndexOf(Object)
	public int lastIndexOf(Object o) {
		return this.list.lastIndexOf((V) o);
	}
	
	/// @see java.util.List#listIterator()
	public ListIterator<V> listIterator() {
		return this.list.listIterator();
	}
	
	/// @see java.util.List#listIterator(int)
	public ListIterator<V> listIterator(int index) {
		return this.list.listIterator(index);
	}
	
	/// @see java.util.List#remove(int)
	public V remove(int index) {
		Object element = this.list.remove(index);
		if (element != null) {
			this.set.remove((V) element);
		}
		return (V) element;
	}
	
	/// @see java.util.List#remove(Object)
	public boolean remove(Object o) {
		if (this.set.remove(o)) {
			this.list.remove(o);
			return true;
		} else {
			return false;
		}
	}
	
	/// @see java.util.List#removeAll(Collection)
	public boolean removeAll(Collection<?> c) {
		if (this.set.removeAll(c)) {
			this.list.removeAll(c);
			return true;
		} else {
			return false;
		}
	}
	
	/// @see java.util.List#retainAll(Collection)
	public boolean retainAll(Collection<?> c) {
		if (this.set.retainAll(c)) {
			this.list.retainAll(c);
			return true;
		} else {
			return false;
		}
	}
	
	/// @see java.util.List#set(int, E)
	public Object set(int index, Object element) {
		this.set.add((V) element);
		return this.list.set(index, (V) element);
	}
	
	/// @see java.util.List#size()
	public int size() {
		return this.list.size();
	}
	
	/// @see java.util.List#subList(int, int)
	public List<V> subList(int fromIndex, int toIndex) {
		return this.list.subList(fromIndex, toIndex);
	}
	
	/// @see java.util.List#toArray()
	public Object[] toArray() {
		return this.list.toArray();
	}
	
	/// @see java.util.List#toArray(T[])
	public Object[] toArray(Object[] a) {
		return this.list.toArray(a);
	}
	
	public boolean equals(Object other) {
		return other instanceof GenericConvertListSet && this.list.equals(((GenericConvertListSet) other).list);
	}
	
	public int hashCode() {
		return this.list.hashCode();
	}
	
	public Spliterator<V> spliterator() {
		return list.spliterator();
	}
	
}
