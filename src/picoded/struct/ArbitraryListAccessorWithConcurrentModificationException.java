package picoded.struct;

import java.util.List;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;

///
/// In most cases, you probably should use AbstractListIterator, or AbstractListSublist instead.
///
/// Instead of "true" ConcurrentModificationException protection. This functions by monitoring 
/// the list "size()" value, to detect ConcurrentModificationException.
///
/// Captures the List size. And performs ConcurrentModificationException checks
/// via the checkForChange functions. Note this is not meant for direct use.
/// This considers a size change as "change" in array, on a "change" detection
/// the status is locked. And throws ConcurrentModificationException subsequently.
///
class ArbitraryListAccessorWithConcurrentModificationException<E> {
	
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
	public ArbitraryListAccessorWithConcurrentModificationException(List<E> inBase) {
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
