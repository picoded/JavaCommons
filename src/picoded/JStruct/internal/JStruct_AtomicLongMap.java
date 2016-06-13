package picoded.JStruct.internal;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import picoded.struct.*;
import picoded.security.NxtCrypt;
import picoded.JStruct.*;

/// Refence implementation of KeyValueMap data structure
///
/// This is intended to be an optimized key value map data storage
/// Used mainly in caching or performance critical scenerios.
///
/// As such its sacrifices much utility for performance
///
/// Note that expire timestamps are measured in seconds,
/// anything smaller then that is pointless over a network
///
public class JStruct_AtomicLongMap implements AtomicLongMap {

	///
	/// Constructor vars
	///--------------------------------------------------------------------------

	/// Stores the key to value map
	public ConcurrentHashMap<String, Long> valueMap = new ConcurrentHashMap<String, Long>();

	/// Read write lock
	public ReentrantReadWriteLock accessLock = new ReentrantReadWriteLock();

	///
	/// Constructor setup
	///--------------------------------------------------------------------------

	/// Constructor
	public JStruct_AtomicLongMap() {
		// does nothing =X
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
	/// @returns null
	// public default Long put(String key, Number value) {
	//
	// 	//update the valuemap
	// 	valueMap.put(key, value.longValue());
	//
	// 	// return put(key, value.longValue());
	// 	return valueMap.get(key);
	// }

	/// Stores (and overwrites if needed) key, value pair
	///
	/// Important note: It does not return the previously stored value
	///
	/// @param key as String
	/// @param value as long
	///
	/// @returns null
	// public Long put(String key, long value){
	//
	// 	return put(key, value.longValue());
	// }

	/// Returns the value, given the key
	/// @param key param find the thae meta key
	///
	/// @returns  value of the given key
	public Long get(Object key) {


		return valueMap.get(key);
	}

	/// Returns the value, given the key
	/// @param key param find the meta key
	/// @param delta value to add
	///
	/// @returns  value of the given key
	// public Long get(Object key, Object delta){
	//
	// }
}
