package picoded.struct;

import java.util.List;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;

///
/// Interface pattern, that implements most of the default list functions, 
/// This builds ontop of core functions, in which implmentors of this interface will need to support. 
///
/// These core functions are as followed
/// + get
/// + set
/// + remove
/// + size
///
/// All other functions are then built ontop of these core function, 
/// with suboptimal usage patterns. For example .set(), calls up the get(),
/// and put(), to mimick its usage. When there are probably much more efficent implmentation.
///
/// Also certain compromises were done to achieve the polyfill. The most prominent one,
/// being that iterators and sublist do not gurantee a "ConcurrentModificationException", 
/// on array change, when size does not change (such as via "set")
///
/// However more importantly is, it works =)
///
/// The idea is that this interface allows a programmer, to rapidly implement
/// a Map object from any class, with just 4 function, instead of 24++ (with iterators)
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
	
	/// [Needs to be overriden, currently throws UnsupportedOperationException]
	/// 
	/// Returns the element at the specified position in this list.
	///
	/// @param   index of the element to return
	///
	/// @returns  the element at the specified position in this list
	default E get(int key) {
		throw new UnsupportedOperationException("function not supported");
	}
	
	/// [Needs to be overriden, currently throws UnsupportedOperationException]
	/// 
	/// Inserts the specified element at the specified position in this list.
	/// Shifts the element currently at that position (if any) and any subsequent elements 
	/// to the right (adds one to their indices).
	///
	/// @param   index of the element to be inserted
	/// @param   element to be stored at the specified position
	///
	/// @returns  the element at the specified position in this list
	default void add(int index, E value) {
		throw new UnsupportedOperationException("function not supported");
	}
	
	/// [Needs to be overriden, currently throws UnsupportedOperationException]
	/// 
	/// Removes the element at the specified position in this list. 
	/// Shifts any subsequent elements to the left (subtracts one from their indices). 
	/// Returns the element that was removed from the list.
	///
	/// @param   index of the element to be removed
	///
	/// @returns  the element at the specified position in this list
	default E remove(int index) {
		throw new UnsupportedOperationException("function not supported");
	}
	
	/// [Needs to be overriden, currently throws UnsupportedOperationException]
	/// 
	/// Returns the number of elements in this list. If this list contains more than
	/// Integer.MAX_VALUE elements, erms, rewrite the polyfills. They WILL break.
	///
	/// @returns  the number of elements in this list
	default int size() {
		throw new UnsupportedOperationException("function not supported");
	}
	
	//-------------------------------------------------------------------
	//
	// Simple immediate polyfill's (few liners)
	//
	//-------------------------------------------------------------------
	
	/// Appends the specified element to the end of this list
	///
	/// @param   element to be stored 
	/// 
	/// @return  true, if added successfully (always, unless exception)
	default boolean add(E value) {
		add(size(), value);
		return true;
	}
	
	/// Replaces the element at the specified position in this list with the specified element
	///
	/// @param   index of the element to store
	/// @param   element to be stored at the specified position
	///
	/// @return  Previous element that was stored
	default E set(int index, E value) {
		UnsupportedDefaultUtils.checkIndexRange(index, size());
		E oldVal = remove(index);
		add(index, value);
		return oldVal;
	}
	
	/// Returns true if this list contains no elements.
	///
	/// @return  true, if size is not 0
	default boolean isEmpty() {
		return (size() <= 0);
	}
	
	/// Inserts all of the elements in the specified collection into this list at the 
	/// end of the list. Shifts elements similar to how the add operation works.
	///
	/// The behavior of this operation is undefined if the specified collection 
	/// is modified while the operation is in progress. 
	///
	/// @param   element collection to be stored
	///
	/// @return  true, if any insertion occurs
	default boolean addAll(Collection<? extends E> c) {
		return addAll(size(), c);
	}
	
	/// Removes all of the elements from this collection. 
	/// The collection will be empty after this method returns.
	default void clear() {
		// Iterate all items from top, and remove if
		for (int i = size() - 1; i >= 0; --i) {
			remove(i);
		}
	}
	
	/// Returns true if this collection contains the specified element. 
	///
	/// @return  true, if element is found
	default boolean contains(Object o) {
		return indexOf(o) >= 0;
	}
	
	//-------------------------------------------------------------------
	//
	// Polyfills of more complex operations
	//
	//-------------------------------------------------------------------
	
	/// Inserts all of the elements in the specified collection into this list at the 
	/// specified position. Shifts elements similar to how the add operation works.
	///
	/// The behavior of this operation is undefined if the specified collection 
	/// is modified while the operation is in progress. 
	///
	/// @param   index of the element to store
	/// @param   element collection to be stored
	///
	/// @return  true, if any insertion occurs
	default boolean addAll(int index, Collection<? extends E> c) {
		UnsupportedDefaultUtils.checkInsertRange(index, size());
		// Iterate collection, and add items
		int idx = index;
		for (E item : c) {
			add(idx, item);
			++idx;
		}
		// Returns true, if iteration has occured
		return index != idx;
	}
	
	/// Returns the index of the first occurrence of the specified element in this list, 
	/// or -1 if this list does not contain the element
	///
	/// @param   element to find
	///
	/// @return  Index of the found item, else -1
	default int indexOf(Object o) {
		// Get the size
		int len = size();
		
		// Iterate to find
		for (int i = 0; i < len; ++i) {
			E val = get(i);
			
			// Null find
			if (o == null && val == null) {
				return i;
			}
			
			// Not a null find
			if (val != null && val.equals(o)) {
				return i;
			}
		}
		
		// Failed find
		return -1;
	}
	
	/// Returns the index of the last occurrence of the specified element in this list, 
	/// or -1 if this list does not contain the element
	///
	/// @param   element to find
	///
	/// @return  Index of the found item, else -1
	default int lastIndexOf(Object o) {
		// Iterate to find
		for (int i = size() - 1; i >= 0; --i) {
			E val = get(i);
			
			// Null find
			if (o == null && val == null) {
				return i;
			}
			
			// Not a null find
			if (val != null && val.equals(o)) {
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
	
	/// Removes the first occurrence of the specified element 
	/// from this list, if it is present
	///
	/// @param  element to be removed from this list, if present
	///
	/// @return  true, if element is found and removed
	default boolean remove(Object o) {
		int idx = indexOf(o);
		if (idx >= 0) {
			remove(idx);
			return true;
		}
		return false;
	}
	
	/// Returns true if this collection contains all of the elements in the specified collection.
	///
	/// @param   element collection to scan
	///
	/// @return  true, if all items is found
	default boolean containsAll(Collection<?> c) {
		// Iterate collection
		for (Object item : c) {
			// Terminates checks the moment one lookup fails
			if (!contains(item)) {
				return false;
			}
		}
		// Assumes checks completed, return success
		return true;
	}
	
	/// Removed all items found in a collection.
	///
	/// @param   element collection to scan
	///
	/// @return  true, if any item was removed
	default boolean removeAll(Collection<?> c) {
		boolean ret = false;
		// Iterate collection
		for (Object item : c) {
			// Remove, and cache any success
			ret = remove(item) || ret;
		}
		return ret;
	}
	
	/// Retains only the elements in this collection that are contained 
	/// in the specified collection
	///
	/// @param   element collection to scan
	/// 
	/// @return  true, if any item was removed
	default boolean retainAll(Collection<?> c) {
		int oldSize = size();
		
		// Iterate entire array from the top
		for (int idx = oldSize - 1; idx >= 0; --idx) {
			// If item at index is not found, remove it
			// And move to next index
			if (!c.contains(get(idx))) {
				remove(idx);
			}
		}
		
		// Returns true, if size changed
		return (size() != oldSize);
	}
	
	//-------------------------------------------------------------------
	//
	// Complex toArray polyfills
	//
	//-------------------------------------------------------------------
	
	/// Returns an array containing all of the elements in this collection.
	///
	/// @return  Array containing all of the elements in this collection
	default Object[] toArray() {
		int size = size();
		Object[] ret = new Object[size];
		for (int i = 0; i < size; ++i) {
			ret[i] = get(i);
		}
		return ret;
	}
	
	/// Returns an array containing all of the elements in this collection; 
	/// the runtime type of the returned array is that of the specified array. 
	/// 
	/// If the collection fits in the specified array, it is returned therein. 
	/// Otherwise, a new array is allocated with the runtime type of the specified array 
	/// and the size of this collection.
	///
	/// @param   the array into to store the results, if it is big enough;
	///
	/// @return  Array containing all of the elements in this collection
	@SuppressWarnings("unchecked")
	default <T> T[] toArray(T[] a) {
		int size = size();
		
		// Write into input array, if it can fit
		if (a.length >= size) {
			// Iterate and write
			for (int i = 0; i < size; ++i) {
				a[i] = (T) get(i);
			}
			
			// Null terminator, if applicable
			if (a.length > size) {
				a[size] = null;
			}
		}
		
		// Create a new array, and returns it
		return (T[]) toArray();
	}
	
	//-------------------------------------------------------------------
	//
	// Complex Polyfills that you should be glad you are not doing
	// - For list iterators
	//
	//-------------------------------------------------------------------
	
	/// Returns a list iterator over the elements in this list (in proper sequence).
	/// 
	/// @return  list iterator over the elements in this list (in proper sequence)
	default ListIterator<E> listIterator() {
		return listIterator(0);
	}
	
	/// Returns a list iterator over the elements in this list, from the index (in proper sequence).
	/// 
	/// @return  list iterator over the elements in this list (in proper sequence)
	default ListIterator<E> listIterator(int index) {
		return new ArbitaryListIterator<E>(this, index);
	}
	
	/// Returns a iterator over the elements in this list (in proper sequence).
	/// 
	/// @return  iterator over the elements in this list (in proper sequence)
	default Iterator<E> iterator() {
		return new ArbitaryListIterator<E>(this, 0);
	}
	
	//-------------------------------------------------------------------
	//
	// Complex Polyfills that you should be glad you are not doing
	// - For list subList
	//
	//-------------------------------------------------------------------
	
	/// throws an UnsupportedOperationException
	default List<E> subList(int frmIdx, int toIdx) {
		return new ArbitaryListSubList<E>(this, frmIdx, toIdx);
	}
	
}
