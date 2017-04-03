package picoded.struct;

/// Class of utility functions used by UnsupportedDefaultList / UnssuportedDefaultMap
/// This is used by the polyfills, for features such as constant error message format, etc
///
/// This is not a public class
class UnsupportedDefaultUtils {
	
	/// Invalid constructor (throws exception)
	protected UnsupportedDefaultUtils() {
		throw new IllegalAccessError("Utility class");
	}
	
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
