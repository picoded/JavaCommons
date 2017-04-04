package picoded.dstack;

import picoded.struct.GenericConvertMap;

///
/// [DO NOT USE : THIS IS CURRENTLY FLAGGED OUT TO BE REFACTORED]
/// 
/// @TODO : EUGENE, consider if this should be rewritten to Map<String,AtomicLong> 
///         instead to be more standard compliant
///
/// This is intended to be an optimized incremental long data storage
/// Because in "most" cases this would be good enough to handle 
/// any transactional data that is required.
///
public interface AtomicLongMap extends GenericConvertMap<String, Long>, DStackCommon<String, Long> {
	
	// Core put / get
	//--------------------------------------------------------------------------
	
	/// Stores (and overwrites if needed) key, value pair
	///
	/// Important note: It does not return the previously stored value
	///
	/// @param key as String
	/// @param value as Long
	///
	/// @returns null
	Long put(Object key, Long value);
	
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
	
	// put, get varients
	//--------------------------------------------------------------------------
	
	/// Stores (and overwrites if needed) key, value pair
	///
	/// Important note: It does not return the previously stored value
	///
	/// @param key as String
	/// @param value as Number
	///
	/// @returns long
	default Long put(Object key, Number value) {
		return put(key, value.longValue());
	}
	
	/// Stores (and overwrites if needed) key, value pair
	///
	/// Important note: It does not return the previously stored value
	///
	/// @param key as String
	/// @param value as long
	///
	/// @returns long
	default Long put(Object key, long value) {
		return put(key, Long.valueOf(value));
	}
	
	/// Returns the value, given the key
	///
	/// @param key param find the meta key
	/// @param delta value to add
	///
	/// @returns  value of the given key after adding
	default Long addAndGet(Object key, Object delta) {
		Long res = getAndAdd(key, delta);
		if (res == null) {
			return null;
		}
		return res.longValue() + ((Long) delta).longValue();
	}
	
	/// Returns the value, given the key, and increment it
	///
	/// @param key param find the meta key
	///
	/// @returns  value of the given key before adding
	default Long getAndIncrement(Object key) {
		return getAndAdd(key, 1);
	}
	
	/// Returns the value, given the key, and decrement it
	///
	/// @param key param find the meta key
	///
	/// @returns  value of the given key before adding
	default Long getAndDecrement(Object key) {
		return getAndAdd(key, -1);
	}
	
	/// Returns the value, given the key
	///
	/// @param key param find the meta key
	/// @param delta value to add
	///
	/// @returns  value of the given key after decrement
	default Long incrementAndGet(Object key) {
		return addAndGet(key, 1);
	}
	
	/// Returns the value, given the key
	///
	/// @param key param find the meta key
	/// @param delta value to add
	///
	/// @returns  value of the given key after decrement
	default Long decrementAndGet(Object key) {
		return addAndGet(key, -1);
	}
	
}
