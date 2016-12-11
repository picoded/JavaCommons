package picoded.struct;

import java.util.List;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;

import picoded.conv.GenericConvert;

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
	// Default list utility class
	//
	//-------------------------------------------------------------------
	
	/// Class of utility functions for List implmentation 
	/// This is used by the polyfills
	static class UnsupportedDefaultListUtils {
		
		///
		/// Checks if the given index, is within 0 to last index (size - 1).
		/// Throws the respective IndexOutOfBoundsException if it fails
		///
		/// @param  index position to check
		/// @param  list size to assume in check
		///
		static void checkIndexRange(int index, int size) {
			// Out of bound check
			if(index < 0 || index >= size) {
				throw new IndexOutOfBoundsException("Index: "+index+", Size: "+size);
			}
		}
		
		///
		/// Checks if the given index, is within 0 to size. Used for insertions.
		/// Throws the respective IndexOutOfBoundsException if it fails
		///
		/// @param  index position to check
		/// @param  list size to assume in check
		///
		static void checkInsertRange(int index, int size) {
			// Out of bound check
			if(index < 0 || index > size) {
				throw new IndexOutOfBoundsException("Index: "+index+", Size: "+size);
			}
		}
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
		add( size(), value );
		return true;
	}
	
	/// Replaces the element at the specified position in this list with the specified element
	///
	/// @param   index of the element to store
	/// @param   element to be stored at the specified position
	///
	/// @return  Previous element that was stored
	default E set(int index, E value) {
		UnsupportedDefaultListUtils.checkIndexRange(index, size());
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
		return addAll( size(), c );
	}
	
	/// Removes all of the elements from this collection. 
	/// The collection will be empty after this method returns.
	default void clear() {
		// Iterate all items from top, and remove if
		for(int i=size() - 1; i>=0; --i) {
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
		UnsupportedDefaultListUtils.checkInsertRange(index, size());
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
	
	/// Returns the index of the last occurrence of the specified element in this list, 
	/// or -1 if this list does not contain the element
	///
	/// @param   element to find
	///
	/// @return  Index of the found item, else -1
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
	
	/// Removes the first occurrence of the specified element 
	/// from this list, if it is present
	///
	/// @param  element to be removed from this list, if present
	///
	/// @return  true, if element is found and removed
	default boolean remove(Object o) {
		int idx = indexOf(o);
		if( idx >= 0 ) {
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
			if(!contains(item)) {
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
		for(int idx = oldSize - 1; idx >= 0; ++idx) {
			// If item at index is not found, remove it
			// And move to next index
			if(!c.contains( get(idx) )) {
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
		for(int i=0; i<size; ++i) {
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
		if(a.length >= size) {
			// Iterate and write
			for(int i=0; i<size; ++i) {
				a[i] = (T)get(i);
			}
			
			// Null terminator, if applicable
			if(a.length > size) {
				a[size] = null;
			}
		}
		
		// Create a new array, and returns it
		return (T[])toArray();
	}
	
	//-------------------------------------------------------------------
	//
	// Complex Polyfills that you should be glad you are not doing
	// - For list iterators
	//
	//-------------------------------------------------------------------
	
	///
	/// Captures the List size. And performs ConcurrentModificationException checks.
	///
	/// This considers a size change as "change" in array, on a "change" detection
	/// the status is locked. And throws ConcurrentModificationException subsequently
	///
	static class UnsupportedDefaultListSizeCapture<E> {
		
		//
		// Internal tracking variables
		//-------------------------------------------------------------------
		
		protected List<E> base; // List used to build this 
		protected int initialSize; // Initial list size
		protected boolean detectedChange; // Change has already been detected
		
		//
		// Constructor and utils
		//-------------------------------------------------------------------
		
		/// Constructor setting up the base list, and index point
		///
		/// @param  List to use as base, for get/size operations
		/// @param  index position to iterate from
		public UnsupportedDefaultListSizeCapture(List<E> inBase) {
			base = inBase;
			initialSize = base.size();
			detectedChange = false;
		}
		
		/// Check if any change has occured since previous iteration call
		/// Throws a ConcurrentModificationException if so
		protected void checkForChange() {
			if( detectedChange ) {
				throw new ConcurrentModificationException();
			}
			
			if( base.size() != initialSize ) {
				throwChangeException();
			}
		}
		
		/// Declare a change has occured
		/// Throws a ConcurrentModificationException
		protected void throwChangeException() {
			detectedChange = true;
			throw new ConcurrentModificationException();
		}
		
		/// Reset the size capture
		protected void resetSizeState() {
			if( detectedChange ) {
				throw new ConcurrentModificationException();
			}
			initialSize = base.size();
		}
	}
	
	///
	/// Dummy Iterator implmentation, as mentioned this is far from perfect,
	/// relying on the list respective get function calls, but its good enough.
	///
	static class UnsupportedDefaultListIterator<E> extends UnsupportedDefaultListSizeCapture<E> 
		implements Iterator<E>, ListIterator<E> {
		
		//
		// Internal tracking variables
		//-------------------------------------------------------------------
		
		private int idxPt; // Index of next element
		private int lastPt = -1; // Index for remove call to use
		
		//
		// Constructor and utils
		//-------------------------------------------------------------------
		
		/// Constructor setting up the base list, and index point
		///
		/// @param  List to use as base, for get/size operations
		/// @param  index position to iterate from
		public UnsupportedDefaultListIterator(List<E> inBase, int inIdx) {
			super(inBase);
			idxPt = inIdx;
		}
		
		//
		// Iterator implmentation
		//-------------------------------------------------------------------
		
		/// Indicates if there is a next iteration
		/// 
		/// @return  true if there is another element to iterate
		public boolean hasNext() {
			return base.size() > idxPt;
		}
		
		/// Gets the next element
		/// Moves iterator position
		///
		/// @return  Respective iterator element
		@SuppressWarnings("unchecked")
		public E next() {
			checkForChange();
			
			// End of iteration reached
			if (idxPt >= initialSize) {
				throw new NoSuchElementException();
			}
			
			try {
				// Get the item, while tracking the index
				E ret = base.get(lastPt = idxPt); 
				++idxPt; // Shift index point
				return ret; // returns 
			} catch (IndexOutOfBoundsException ex) {
				throwChangeException();
			}
			return null;
		}
		
		/// Removes from the underlying collection the last element returned by this iterator
		/// This method can be called only once per call to next(). 
		/// 
		/// The behavior of an iterator is unspecified if the underlying collection 
		/// is modified while the iteration is in progress in any way other than by 
		/// calling this method.
		public void remove() {
			if (lastPt < 0) {
				throw new IllegalStateException();
			}
			checkForChange();

			try {
				base.remove(lastPt);
				idxPt = lastPt;
				lastPt = -1;
				resetSizeState();
			} catch (IndexOutOfBoundsException ex) {
				throwChangeException();
			}
		}
		
		//
		// ListIterator implmentation
		//-------------------------------------------------------------------
		
		/// returns true if the iterator can step backwards
		///
		/// @returns true if the iterator can step backwards
		public boolean hasPrevious() {
			return idxPt > 0;
		}
		
		/// Returns the index of the element that would be returned by a 
		/// subsequent call to next(). 
		/// 
		/// (Returns list size if the list iterator is at the end of the list.)
		///
		/// @return  Iterator index position
		public int nextIndex() {
			return idxPt;
		}
		
		/// Returns the index of the element that would be returned by a subsequent 
		/// call to previous(). 
		/// 
		/// (Returns -1 if the list iterator is at the beginning of the list.)
		///
		/// @return  Iterator previous index position, or -1
		public int previousIndex() {
			return idxPt - 1;
		}
		
		/// Returns the previous element in the list and moves the cursor position backwards. 
		/// This method may be called repeatedly to iterate through the list backwards, 
		/// or intermixed with calls to next() to go back and forth. 
		/// 
		/// (Note that alternating calls to next and previous will return the same element repeatedly.)
		///
		/// @return  Respective iterator element
		@SuppressWarnings("unchecked")
		public E previous() {
			checkForChange();
			int i = idxPt - 1;
			
			// End of iteration reached
			if (i < 0) {
				throw new NoSuchElementException();
			}
			
			try {
				// Get the item, while tracking the index
				E ret = base.get(lastPt = i); 
				idxPt = i; // Shift index point
				return ret; // returns 
			} catch (IndexOutOfBoundsException ex) {
				throwChangeException();
			}
			return null;
		}
		
		/// Replaces the last element returned by next() or previous() with the specified element
		///
		/// @param  The element to set
		public void set(E e) {
			if (lastPt < 0) {
				throw new IllegalStateException();
			}
			checkForChange();

			try {
				base.set(lastPt, e);
			} catch (IndexOutOfBoundsException ex) {
				throwChangeException();
			}
		}
		
		/// Inserts the specified element into the list. The element is inserted 
		/// immediately before the element that would be returned by next()
		///
		/// @param  The element to add
		public void add(E e) {
			if (lastPt < 0) {
				throw new IllegalStateException();
			}
			checkForChange();

			try {
				base.add(idxPt, e);
				idxPt = idxPt + 1;
				lastPt = -1;
				resetSizeState();
			} catch (IndexOutOfBoundsException ex) {
				throwChangeException();
			}
		}
	}
	
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
		return new UnsupportedDefaultListIterator<E>(this, index);
	}
	
	/// Returns a iterator over the elements in this list (in proper sequence).
	/// 
	/// @return  iterator over the elements in this list (in proper sequence)
	default Iterator<E> iterator() {
		return new UnsupportedDefaultListIterator<E>(this, 0);
	}
	
	//-------------------------------------------------------------------
	//
	// Complex Polyfills that you should be glad you are not doing
	// - For list subList
	//
	//-------------------------------------------------------------------
	
	///
	/// Dummy subList implmentation, as mentioned this is far from perfect,
	/// relying on the list respective function calls, but its good enough.
	///
	static class UnsupportedDefaultSubList<E> extends UnsupportedDefaultListSizeCapture<E> 
		implements UnsupportedDefaultList<E> {
			
		//
		// Internal tracking variables
		//-------------------------------------------------------------------
		
		private int offset; // Index of next element
		private int size; // Index for remove call to use
		
		//
		// Constructor and utils
		//-------------------------------------------------------------------
		
		/// Constructor setting up the base list, and index point
		///
		/// @param  List to use as base, for get/size operations
		/// @param  index position to iterate from
		public UnsupportedDefaultSubList(List<E> inBase, int frmIdx, int toIdx) {
			// State capture
			super(inBase);
			
			// Index range checks
			if (frmIdx < 0) {
				throw new IndexOutOfBoundsException("fromIndex = " + frmIdx);
			} else if (toIdx > base.size()) {
				throw new IndexOutOfBoundsException("toIndex = " + toIdx);
			} else if (frmIdx > toIdx) {
				throw new IllegalArgumentException(
					"fromIndex(" + frmIdx +
					") > toIndex(" + toIdx + ")"
				);
			}
			
			// Index captures
			offset = frmIdx;
			size = toIdx - frmIdx;
		}
		
		/// Size operation proxy
		/// See: [UnsupportedDefaultList.size]
		public int size() {
			checkForChange();
			return size;
		}
	
		/// Set operation proxy
		/// See: [UnsupportedDefaultList.set]
		public E set(int index, E element) {
			checkForChange();
			UnsupportedDefaultListUtils.checkIndexRange(index, size);
			return base.set(index+offset, element);
		}
		
		/// Get operation proxy
		/// See: [UnsupportedDefaultList.get]
		public E get(int index) {
			UnsupportedDefaultListUtils.checkIndexRange(index, size);
			checkForChange();
			return base.get(index+offset);
		}
		
		/// Add operation proxy
		/// See: [UnsupportedDefaultList.add]
		public void add(int index, E element) {
			UnsupportedDefaultListUtils.checkInsertRange(index, size);
			checkForChange();
			base.add(index+offset, element);
			resetSizeState();
			size++;
		}
		
		/// Remove operation proxy
		/// See: [UnsupportedDefaultList.remove]
		public E remove(int index) {
			UnsupportedDefaultListUtils.checkIndexRange(index, size());
			checkForChange();
			E result = base.remove(index+offset);
			resetSizeState();
			size--;
			return result;
		}
	}
	
	/// throws an UnsupportedOperationException
	default List<E> subList(int frmIdx, int toIdx) {
		return new UnsupportedDefaultSubList<E>(this, frmIdx, toIdx);
	}
	
}
