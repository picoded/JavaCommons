package picoded.JStruct;

import java.util.Set;

import org.apache.commons.lang3.RandomUtils;

import picoded.security.NxtCrypt;
import picoded.struct.GenericConvertMap;

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
public interface KeyValueMap extends GenericConvertMap<String, String> {
	
	//
	// Temp mode optimization, used to indicate pure session like data,
	// that does not require persistance (or even SQL)
	//
	//--------------------------------------------------------------------------
	
	/// Gets if temp mode optimization hint is indicated
	/// Note that this only serve as a hint, as does not indicate actual setting
	///
	/// @returns boolean  temp mode value
	boolean getTempHint();
	
	/// Sets temp mode optimization indicator hint
	/// Note that this only serve as a hint, as does not indicate actual setting
	///
	/// @param  mode  the new temp mode hint
	///
	/// @returns boolean  previous value if set
	boolean setTempHint(boolean mode);
	
	//
	// Backend system setup / teardown
	//--------------------------------------------------------------------------
	
	/// Setsup the backend storage table, etc. If needed
	void systemSetup();
	
	/// Teardown and delete the backend storage table, etc. If needed
	void systemTeardown();
	
	/// Perform maintenance, mainly removing of expired data if applicable
	void maintenance();
	
	/// perform increment maintenance, meant for minor changes between requests
	default void incrementalMaintenance() {
		// 2 percent chance of trigering maintenance
		// This is to lower to overall performance cost incrementalMaintenance per request
		int num = RandomUtils.nextInt(0, 100);
		if (num <= 2) {
			maintenance();
		}
	}
	
	/// Removes all data, without tearing down setup
	///
	/// Handles re-entrant lock where applicable
	///
	@Override
	void clear();
	
	//
	// Expiration and lifespan handling (public access)
	//--------------------------------------------------------------------------
	
	/// Returns the expire time stamp value, if still valid
	///
	/// @param key as String
	///
	/// @returns long, 0 means no expirary, -1 no data / expire
	long getExpiry(String key);
	
	/// Returns the lifespan time stamp value
	///
	/// @param key as String
	///
	/// @returns long, 0 means no expirary, -1 no data / expire
	long getLifespan(String key);
	
	/// Sets the expire time stamp value, if still valid
	///
	/// @param key 
	/// @param time 
	void setExpiry(String key, long time);
	
	/// Sets the expire time stamp value, if still valid
	///
	/// @param key 
	/// @param lifespan 
	void setLifeSpan(String key, long lifespan);
	
	/// Search using the value, all the relevent key mappings
	///
	/// Handles re-entrant lock where applicable
	///
	/// @param key, note that null matches ALL
	///
	/// @returns array of keys
	Set<String> getKeys(String value);
	
	//
	// put, get, etc (public)
	//--------------------------------------------------------------------------
	
	/// Contains key operation.
	///
	/// note that boolean false can either mean no value or expired value
	///
	/// @param key as String
	/// @returns boolean true or false if the key exists
	@Override
	default boolean containsKey(Object key) {
		//		getLifespan(key.toString()) >= 0;
		return true;
	}
	
	/// Returns the value, given the key
	/// @param key param find the thae meta key
	///
	/// @returns  value of the given key
	@Override
	String get(Object key);
	
	/// Remove the value, given the key
	/// @param key param find the thae meta key
	///
	/// @returns  null
	@Override
	String remove(Object key);
	
	/// Stores (and overwrites if needed) key, value pair
	///
	/// Important note: It does not return the previously stored value
	///
	/// @param key as String
	/// @param value as String
	///
	/// @returns null
	String put(String key, String value);
	
	/// Returns all the valid keys
	///
	/// @returns  the full keyset
	@Override
	default Set<String> keySet() {
		return getKeys(null);
	}
	
	//
	// Extended map operations
	//--------------------------------------------------------------------------
	
	/// Stores (and overwrites if needed) key, value pair
	///
	/// Important note: It does not return the previously stored value
	///
	/// @param key as String
	/// @param value as String
	/// @param lifespan time to expire in seconds
	///
	/// @returns null
	String putWithLifespan(String key, String value, long lifespan);
	
	/// Stores (and overwrites if needed) key, value pair
	///
	/// Important note: It does not return the previously stored value
	///
	/// @param key as String
	/// @param value as String
	/// @param expireTime expire time stamp value
	///
	/// @returns String
	String putWithExpiry(String key, String value, long expireTime);
	
	//
	// Nonce operations suppport (public)
	//--------------------------------------------------------------------------
	
	/// Generates a random nonce hash, and saves the value to it
	///
	/// Relies on both nonce_defaultLength & nonce_defaultLifetime for default parameters
	///
	/// @param value to store as string
	///
	/// @returns String value of the random key generated
	String generateNonce(String val);
	
	/// Generates a random nonce hash, and saves the value to it
	///
	/// Relies on nonce_defaultLength for default parameters
	///
	/// @param value to store as string
	/// @param lifespan time to expire in seconds
	///
	/// @returns String value of the random key generated
	String generateNonce(String val, long lifespan);
	
	/// Generates a random nonce hash, and saves the value to it
	///
	/// Note that the random nonce value returned, is based on picoded.security.NxtCrypt.randomString.
	/// Note that this relies on true random to avoid collisions, and if it occurs. Values are over-written
	///
	/// @param keyLength random key length size
	/// @param value to store as string
	/// @param lifespan time to expire in seconds
	///
	/// @returns String value of the random key generated
	default String generateNonce(String val, long lifespan, int keyLength) {
		String res = NxtCrypt.randomString(keyLength);
		putWithLifespan(res, val, lifespan);
		return res;
	}
	
}
