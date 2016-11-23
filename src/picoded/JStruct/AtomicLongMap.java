package picoded.JStruct;

import picoded.struct.GenericConvertMap;
/*import org.apache.commons.lang3.RandomUtils;*/
/// Refence implementation of AtomicLongMap data structure
///
/// This is intended to be an optimized incremental long map data storage
///
public interface AtomicLongMap extends GenericConvertMap<String, Long> {
	
	//
	// Temp mode optimization, used to indicate pure session like data,
	// that does not require persistance (or even SQL)
	//
	//--------------------------------------------------------------------------
	
	/// Gets if temp mode optimization hint is indicated
	/// Note that this only serve as a hint, as does not indicate actual setting
	///
	/// @returns boolean  temp mode value
	default boolean getTempHint() {
		return false;
	};
	
	/// Sets temp mode optimization indicator hint
	/// Note that this only serve as a hint, as does not indicate actual setting
	///
	/// @param  mode  the new temp mode hint
	///
	/// @returns boolean  previous value if set
	default boolean setTempHint(boolean mode) {
		return false;
	};
	
	//
	// Backend system setup / teardown
	//--------------------------------------------------------------------------
	
	/// Setsup the backend storage table, etc. If needed
	default void systemSetup() {
	};
	
	/// Teardown and delete the backend storage table, etc. If needed
	default void systemTeardown() {
	};
	
	/// perform increment maintenance, meant for minor changes between requests
	default void incrementalMaintenance() {
		// 2 percent chance of trigering maintenance
		// This is to lower to overall performance cost incrementalMaintenance per request
//		if (RandomUtils.nextInt(0, 100) <= 2) {
//			maintenance();
//		}
	}
	
	/// Perform maintenance, mainly removing of expired data if applicable
	default void maintenance() {
		// does nothing?
	}
	
	//
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
	/// @param key param find the meta key
	///
	/// @returns  value of the given key
	@Override
	Long get(Object key);
	
	/// Returns the value, given the key
	/// @param key param find the meta key
	/// @param delta value to add
	///
	/// @returns  value of the given key before adding
	Long getAndAdd(Object key, Object delta);
	
	/// Returns the value, given the key
	/// @param key param find the meta key
	/// @param delta value to add
	///
	/// @returns  value of the given key before adding
	Long getAndIncrement(Object key);
	
	/// Returns the value, given the key
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
