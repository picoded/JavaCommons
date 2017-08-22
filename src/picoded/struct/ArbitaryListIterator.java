package picoded.core.struct;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
* Arbitary List Iterator implmentation.
*
* However instead of "true" ConcurrentModificationException protection.
* This functions by monitoring the "size()" value, between the core functionality.
*
* This can be used either as Iterator, or ListIterator
**/
public class ArbitaryListIterator<E> extends
	ArbitraryListAccessorWithConcurrentModificationException<E> implements Iterator<E>,
	ListIterator<E> {

	//
	// Internal tracking variables
	//-------------------------------------------------------------------

	private int idxPt; // Index of next element
	private int lastPt = -1; // Index for remove call to use

	//
	// Constructor and utils
	//-------------------------------------------------------------------

	/**
	* Constructor setting up the base list, and index point.
	*
	* @param  List to use as base, for get/size operations
	* @param  index position to iterate from
	**/
	public ArbitaryListIterator(List<E> inBase, int inIdx) {
		super(inBase);
		idxPt = inIdx;
	}

	//
	// Iterator implmentation
	//-------------------------------------------------------------------

	/**
	* Indicates if there is a next iteration.
	*
	* @return  true if there is another element to iterate
	**/
	public boolean hasNext() {
		return base.size() > idxPt;
	}

	/**
	* Gets the next element.
	* Moves iterator position.
	*
	* @return  Respective iterator element
	**/
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

	/**
	* Removes from the underlying collection the last element returned by this iterator.
	* This method can be called only once per call to next().
	*
	* The behavior of an iterator is unspecified if the underlying collection
	* is modified while the iteration is in progress in any way other than by
	* calling this method.
	**/
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

	/**
	* Returns true if the iterator can step backwards.
	*
	* @return true if the iterator can step backwards
	**/
	public boolean hasPrevious() {
		return idxPt > 0;
	}

	/**
	* Returns the index of the element that would be returned by a
	* subsequent call to next().
	*
	* (Returns list size if the list iterator is at the end of the list.)
	*
	* @return  Iterator index position
	**/
	public int nextIndex() {
		return idxPt;
	}

	/**
	* Returns the index of the element that would be returned by a subsequent
	* call to previous().
	*
	* (Returns -1 if the list iterator is at the beginning of the list.)
	*
	* @return  Iterator previous index position, or -1
	**/
	public int previousIndex() {
		return idxPt - 1;
	}

	/**
	* Returns the previous element in the list and moves the cursor position backwards.
	* This method may be called repeatedly to iterate through the list backwards,
	* or intermixed with calls to next() to go back and forth.
	*
	* (Note that alternating calls to next and previous will return the same element repeatedly.)
	*
	* @return  Respective iterator element
	**/
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

	/**
	* Replaces the last element returned by next() or previous() with the specified element
	*
	* @param  The element to set
	**/
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

	/**
	* Inserts the specified element into the list. The element is inserted
	* immediately before the element that would be returned by next()
	*
	* @param  The element to add
	**/
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
