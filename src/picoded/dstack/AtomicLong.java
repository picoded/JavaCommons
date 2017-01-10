package picoded.dstack;

import picoded.struct.GenericConvertMap;

///
/// This is intended to be an optimized incremental long data storage
/// Because in "most" cases this would be good enough to handle any transactional data that is required.
///
public interface AtomicLong extends GenericConvertMap<String, Long>, DataStructureSetup<String, Long> {
	
	// put, get, etc (public)
	//--------------------------------------------------------------------------
	
	/// Stores (and overwrites if needed) key, value pair
	///
	/// Important note: It does not return the previously stored value
	///
	/// @param key as String
	/// @param value as Number
	///
	/// @returns long
	Long put(String key, Number value);
	
	/// Stores (and overwrites if needed) key, value pair
	///
	/// Important note: It does not return the previously stored value
	///
	/// @param key as String
	/// @param value as long
	///
	/// @returns long
	Long put(String key, long value);
	
	/// Stores (and overwrites if needed) key, value pair
	///
	/// Important note: It does not return the previously stored value
	///
	/// @param key as String
	/// @param value as Long
	///
	/// @returns null
	Long put(String key, Long value);
	
	/// Returns the value, given the key
	///
	/// @param key param find the meta key
	///
	/// @returns  value of the given key
	Long get(Object key);
	
	/// Returns the value, given the key
	///
	/// @param key param find the meta key
	/// @param delta value to add
	///
	/// @returns  value of the given key before adding
	Long getAndAdd(Object key, Object delta);
	
	/// Returns the value, given the key
	///
	/// @param key param find the meta key
	/// @param delta value to add
	///
	/// @returns  value of the given key before adding
	Long getAndIncrement(Object key);
	
	/// Returns the value, given the key
	///
	/// @param key param find the meta key
	/// @param delta value to add
	///
	/// @returns  value of the given key after adding
	Long incrementAndGet(Object key);
	
	/// Stores (and overwrites if needed) key, value pair
	///
	/// Important note: It does not return the previously stored value
	///
	/// @param key as String
	/// @param expect as Long
	/// @param update as Long
	///
	/// @returns true if successful
	boolean weakCompareAndSet(String key, Long expect, Long update);
	
}
