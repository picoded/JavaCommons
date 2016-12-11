package picoded.struct;

import java.util.List;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Collection;

import picoded.conv.GenericConvert;

///
/// Interface pattern, that implements most of the default list functions, 
/// This builds ontop of core functions, in which implmentors of this interface will need to support. 
///
/// These core functions are as followed
/// + get
/// + put
/// + remove
/// + keyset
///
/// All other functions are then built ontop of these core function, 
/// with suboptimal usage patterns. For example .isEmpty(), calls up the keyset(),
/// and check its length. When there are probably much more efficent implmentation.
///
/// However more importantly is, it works =)
///
/// The idea is that this interface allows a programmer, to rapidly implement
/// a Map object from any class, with just 4 function, instead of 24
///
/// ### Example Usage
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
///
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
///
public interface UnsupportedDefaultList<E> extends List<E> {
	
	//-------------------------------------------------------------------
	//
	// Critical functions that need to over-ride, to support List
	//
	//-------------------------------------------------------------------
	
	/// throws an UnsupportedOperationException
	default E get(int key) {
		throw new UnsupportedOperationException("function not supported");
	}
	
	/// throws an UnsupportedOperationException
	default E set(int index, E value) {
		throw new UnsupportedOperationException("function not supported");
	}
	
	/// throws an UnsupportedOperationException
	default E remove(int index) {
		throw new UnsupportedOperationException("function not supported");
	}
	
	/// throws an UnsupportedOperationException
	default int size() {
		throw new UnsupportedOperationException("function not supported");
	}
	
	//-------------------------------------------------------------------
	//
	// Polyfilled critical functions, you probably should still over-ride
	// For performance issue fixes
	//
	//-------------------------------------------------------------------
	
	/// throws an UnsupportedOperationException
	default void add(int index, E value) {
		// Get the old size
		int oldSize = size();
		
		// Out of bound check
		if(index < 0 || index > oldSize) {
			throw new IndexOutOfBoundsException("Index: "+index+", Size: "+oldSize);
		}
		
		// Get the previous value, while inserting the new value
		E prVal = set(index, value);
		
		// Push all the index upwards by one
		for( int i = oldSize-1; i > index; ++i ) {
			// For example, at size 5 the following occurs first
			//
			// i = 4 : (5-1)
			// set(5, get(4))
			//
			// 5, is the new index position
			// 4, is the last index
			//
			// This operation will repeat until its the item, above the added index
			set( i + 1, get(i) );
		}
		
		// After all the above items been "pushed" by 1 index, insert the old value
		set(index+1, prVal);
	}
	
	//-------------------------------------------------------------------
	//
	// Simple immediate polyfill's (one/two liners)
	//
	//-------------------------------------------------------------------
	
	/// throws an UnsupportedOperationException
	default E get(Object key) {
		return get( GenericConvert.toInt(key) );
	}
	
	/// throws an UnsupportedOperationException
	default boolean isEmpty() {
		return (size() <= 0);
	}
	
	//-------------------------------------------------------------------
	//
	// Polyfills of more complex operations
	//
	//-------------------------------------------------------------------
	
	/// throws an UnsupportedOperationException
	default int indexOf(Object o) {
		// Get the size
		int len = size();
		
		// Iterate to find
		for(int i=0; i<len; ++i) {
			E val = get(i);
			
			// Null find
			if(o == null && val == null) {
				return i;
			}
			
			// Not a null find
			if( val != null && val.equals(o) ) {
				return i;
			}
		}
		
		// Failed find
		return -1;
	}
	
	/// throws an UnsupportedOperationException
	default int lastIndexOf(Object o) {
		// Iterate to find
		for(int i=size() - 1; i>=0; --i) {
			E val = get(i);
			
			// Null find
			if(o == null && val == null) {
				return i;
			}
			
			// Not a null find
			if( val != null && val.equals(o) ) {
				return i;
			}
		}
		
		// Failed find
		return -1;
	}
	
	//-------------------------------------------------------------------
	//
	// Simple Polyfills built ontop of other Polyfills
	//
	//-------------------------------------------------------------------
	
	/// throws an UnsupportedOperationException
	default boolean add(E value) {
		add( size(), value );
		return true;
	}
	
	/// throws an UnsupportedOperationException
	default boolean remove(Object o) {
		int idx = indexOf(o);
		if( idx >= 0 ) {
			remove(idx);
		}
		return false;
	}
	
	/// throws an UnsupportedOperationException
	default void clear() {
		// Iterate all items from top, and remove if
		for(int i=size() - 1; i>=0; --i) {
			remove(i);
		}
	}
	
	/// throws an UnsupportedOperationException
	default boolean contains(Object o) {
		return indexOf(o) >= 0;
	}
	
	/// throws an UnsupportedOperationException
	default ListIterator<E> listIterator() {
		throw new UnsupportedOperationException("function not supported");
	}
	
	/// throws an UnsupportedOperationException
	default ListIterator<E> listIterator(int index) {
		throw new UnsupportedOperationException("function not supported");
	}
	
	/// throws an UnsupportedOperationException
	default Iterator<E> iterator() {
		throw new UnsupportedOperationException("function not supported");
	}
	
	/// throws an UnsupportedOperationException
	default List<E> subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException("function not supported");
	}
	
	/// throws an UnsupportedOperationException
	default boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("function not supported");
	}
	
	/// throws an UnsupportedOperationException
	default boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException("function not supported");
	}
	
	/// throws an UnsupportedOperationException
	default boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException("function not supported");
	}
	
	/// throws an UnsupportedOperationException
	default boolean addAll(int index, Collection<? extends E> c) {
		throw new UnsupportedOperationException("function not supported");
	}
	
	/// throws an UnsupportedOperationException
	default boolean containsAll(Collection<?> c) {
		throw new UnsupportedOperationException("function not supported");
	}
	
	/// throws an UnsupportedOperationException
	default Object[] toArray() {
		throw new UnsupportedOperationException("function not supported");
	}
	
	/// throws an UnsupportedOperationException
	default <T> T[] toArray(T[] a) {
		throw new UnsupportedOperationException("function not supported");
	}
	
}
