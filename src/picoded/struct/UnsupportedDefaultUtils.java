package picoded.struct;

import java.util.List;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;

/// Class of utility functions used by UnsupportedDefaultList / UnssuportedDefaultMap
/// This is used by the polyfills, for features such as constant error message format, etc
///
/// This is not a public class
class UnsupportedDefaultUtils {
	
	///
	/// Checks if the given index, is within 0 to last index (size - 1).
	/// Throws the respective IndexOutOfBoundsException if it fails
	///
	/// @param  index position to check
	/// @param  list size to assume in check
	///
	static void checkIndexRange(int index, int size) {
		// Out of bound check
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
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
		if (index < 0 || index > size) {
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
		}
	}
}
