package picoded.JStruct;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import picoded.struct.*;
import picoded.security.NxtCrypt;

import org.apache.commons.lang3.RandomUtils;

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
	public default boolean getTempHint() { return false; };

	/// Sets temp mode optimization indicator hint
	/// Note that this only serve as a hint, as does not indicate actual setting
	///
	/// @param  mode  the new temp mode hint
	///
	/// @returns boolean  previous value if set
	public default boolean setTempHint(boolean mode) { return false; };

	//
	// Backend system setup / teardown
	//--------------------------------------------------------------------------

	/// Setsup the backend storage table, etc. If needed
	public default void systemSetup() { };

	/// Teardown and delete the backend storage table, etc. If needed
	public default void systemTeardown() { };

	/// perform increment maintenance, meant for minor changes between requests
	public default void incrementalMaintenance() {
		// 2 percent chance of trigering maintenance
		// This is to lower to overall performance cost incrementalMaintenance per request
		if (RandomUtils.nextInt(0, 100) <= 2) {
			maintenance();
		}
	}

	/// Perform maintenance, mainly removing of expired data if applicable
	public default void maintenance() {
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
	/// @param value as String
	///
	/// @returns null
	public default Long put(String key, Number value) {
		return put(key, value.longValue());
	}

	public Long put(String key, long value);

	/// Returns the value, given the key
	/// @param key param find the thae meta key
	///
	/// @returns  value of the given key
	public Long get(Object key);

}
