package picoded.core.struct;

import java.util.List;

/**
 * Arbitary List - SubList viewer.
 *
 * However instead of "true" ConcurrentModificationException protection.
 * This functions by monitoring the "size()" value, between the core functionality.
 *
 * This provides the Array.subList view for a list. For Arbitary list implementation.
 *
 * @TODO : To provide more List functionality to be supported by the main list directly.
 *         Instead of through the lower performant 4 core functions.
 **/
class ArbitaryListSubList<E> extends ArbitraryListAccessorWithConcurrentModificationException<E>
	implements UnsupportedDefaultList<E> {
	
	//
	// Internal tracking variables
	//-------------------------------------------------------------------
	
	private int offset; // Index of next element
	private int size; // Index for remove call to use
	
	//
	// Constructor and utils
	//-------------------------------------------------------------------
	
	/**
	 * Constructor setting up the base list, and index point.
	 *
	 * @param  List to use as base, for get/size operations
	 * @param  index position to iterate from
	 **/
	public ArbitaryListSubList(List<E> inBase, int frmIdx, int toIdx) {
		// State capture
		super(inBase);
		
		// Index range checks
		if (frmIdx < 0) {
			throw new IndexOutOfBoundsException("fromIndex = " + frmIdx);
		} else if (toIdx > base.size()) {
			throw new IndexOutOfBoundsException("toIndex = " + toIdx);
		} else if (frmIdx > toIdx) {
			throw new IllegalArgumentException("fromIndex(" + frmIdx + ") > toIndex(" + toIdx + ")");
		}
		
		// Index captures
		offset = frmIdx;
		size = toIdx - frmIdx;
	}
	
	/**
	 * Size operation proxy
	 * See: [UnsupportedDefaultList.size]
	 **/
	public int size() {
		checkForChange();
		return size;
	}
	
	/**
	 * Set operation proxy
	 * See: [UnsupportedDefaultList.set]
	 **/
	public E set(int index, E element) {
		checkForChange();
		UnsupportedDefaultUtils.checkIndexRange(index, size);
		return base.set(index + offset, element);
	}
	
	/**
	 * Get operation proxy
	 * See: [UnsupportedDefaultList.get]
	 **/
	public E get(int index) {
		UnsupportedDefaultUtils.checkIndexRange(index, size);
		checkForChange();
		return base.get(index + offset);
	}
	
	/**
	 * Add operation proxy
	 * See: [UnsupportedDefaultList.add]
	 **/
	public void add(int index, E element) {
		UnsupportedDefaultUtils.checkInsertRange(index, size);
		checkForChange();
		base.add(index + offset, element);
		resetSizeState();
		size++;
	}
	
	/**
	 * Remove operation proxy
	 * See: [UnsupportedDefaultList.remove]
	 **/
	public E remove(int index) {
		UnsupportedDefaultUtils.checkIndexRange(index, size());
		checkForChange();
		E result = base.remove(index + offset);
		resetSizeState();
		size--;
		return result;
	}
}
