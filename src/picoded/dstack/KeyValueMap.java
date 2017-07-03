package picoded.dstack;

import java.util.Set;

import org.apache.commons.lang3.RandomUtils;

import picoded.security.NxtCrypt;
import picoded.struct.GenericConvertMap;

/**
* Refence interface of KeyValueMap Map data structure
*
* This is intended to be an optimized key value map data storage
* Used mainly in caching or performance critical scenerios.
*
* As such its sacrifices much utility for performance
*
* Its value type is also intentionally a String, to ensure compatibility
* with a large number of String based caching systems. Additionally,
* NULL is considered a delete value.
*
* Note that expire timestamps are measured in seconds,
* this is intentional design as anything smaller then a second
* is pointless over a network.
**/
public interface KeyValueMap extends GenericConvertMap<String, String>, CommonStructure {

	//--------------------------------------------------------------------------
	//
	// Basic put / get / remove operations
	//
	//--------------------------------------------------------------------------

	/**
	* Returns the value, given the key
	*
	* Null return can either represent no value or expired value.
	*
	* @param key param find the thae meta key
	*
	* @return  value of the given key
	**/
	@Override
	String get(Object key);

	/**
	* Stores (and overwrites if needed) key, value pair
	*
	* Important note: It does not return the previously stored value
	* Its return String type is to maintain consistency with Map interfaces
	*
	* @param key as String
	* @param value as String
	*
	* @return null
	**/
	String put(String key, String value);

	/**
	* Remove the value, given the key
	* @param key param find the thae meta key
	*
	* @return  null
	**/
	@Override
	String remove(Object key);

	/**
	* Contains key operation.
	*
	* Boolean false can either represent no value or expired value
	*
	* @param key as String
	* @return boolean true or false if the key exists
	**/
	@Override
	default boolean containsKey(Object key) {
		return getLifespan(key.toString()) >= 0;
	}

	/**
	* [warning] : avoid use in production, use a MetaTable instead.
	*
	* Use only for debugging. Returns all the valid keys.
	* Kept to ensure full map interface compatibility
	*
	* NOTE: You DO NOT want to use this, as most KeyValueMap,
	* systems are NOT designed for this operation. And will do so
	* the hard way (searching every data value).
	*
	* @return  the full keyset
	**/
	@Override
	default Set<String> keySet() {
		return keySet(null);
	}

	/**
	* [warning] : avoid use in production, use a MetaTable instead.
	*
	* NOTE: You DO NOT want to use this, as most KeyValueMap,
	* systems are NOT designed for this operation. And will do so
	* the hard way (searching every data value)
	*
	* @param value to search, note that null matches ALL. This is used by keySet()
	*
	* @return array of keys
	**/
	Set<String> keySet(String value);

	//--------------------------------------------------------------------------
	//
	// Expiration and lifespan handling
	//
	//--------------------------------------------------------------------------

	/**
	* Returns the expire time stamp value, if still valid
	*
	* @param key as String
	*
	* @return long, 0 means no expirary, -1 no data / expire
	**/
	long getExpiry(String key);
	/**
	* Returns the lifespan time stamp value
	*
	* @param key as String
	*
	* @return long, 0 means no expirary, -1 no data / expire
	**/
	long getLifespan(String key);
	/**
	* Sets the expire time stamp value, if still valid
	*
	* @param key
	* @param time
	**/
	void setExpiry(String key, long time);
	/**
	* Sets the expire time stamp value, if still valid
	*
	* @param key
	* @param lifespan
	**/
	void setLifeSpan(String key, long lifespan);

	//--------------------------------------------------------------------------
	//
	// Extended map operations
	//
	//--------------------------------------------------------------------------

	/**
	* Stores (and overwrites if needed) key, value pair
	* with lifespan value.
	*
	* Important note: It does not return the previously stored value
	*
	* @param key as String
	* @param value as String
	* @param lifespan time to expire in seconds
	*
	* @return null
	**/
	String putWithLifespan(String key, String value, long lifespan);
	/**
	* Stores (and overwrites if needed) key, value pair
	* with expirary value.
	*
	* Important note: It does not return the previously stored value
	*
	* @param key as String
	* @param value as String
	* @param expireTime expire unix time stamp value
	*
	* @return String
	**/
	String putWithExpiry(String key, String value, long expireTime);

	//--------------------------------------------------------------------------
	//
	// Nonce operations suppport (public)
	//
	//--------------------------------------------------------------------------

	/**
	* Generates a random nonce hash, and saves the value into it
	*
	* This can be reconfigured via the following config map value
	* + NonceLifespan
	* + NonceKeyLength
	*
	* When in doubt, use the default of 3600 seconds or about 1 hour.
	* And 22 characters, which is consistent with Base58 GUID
	*
	* @param value to store as string
	*
	* @return String value of the random key generated
	**/
	default String generateNonceKey(String val) {
		return generateNonceKey(val, configMap().getLong("NonceLifespan", 3600));
	}
	/**
	* Generates a random nonce hash, and saves the value to it
	*
	* This can be reconfigured via the following config map value
	* + NonceKeyLength
	*
	* When in doubt, use the default of 3600 seconds or about 1 hour.
	* And 22 characters, which is consistent with Base58 GUID
	*
	* @param value to store as string
	* @param lifespan time to expire in seconds
	*
	* @return String value of the random key generated
	**/
	default String generateNonceKey(String val, long lifespan) {
		return generateNonceKey(val, lifespan, configMap().getInt("NonceKeyLength"));
	}

	/**
	* Generates a random nonce hash, and saves the value to it
	*
	* Note that the random nonce value returned, is based on picoded.security.NxtCrypt.randomString.
	* Note that this relies on true random to avoid collisions, and if it occurs. Values are over-written
	*
	* @param keyLength random key length size
	* @param value to store as string
	* @param lifespan time to expire in seconds
	*
	* @return String value of the random key generated
	**/
	default String generateNonceKey(String val, long lifespan, int keyLength) {
		String res = NxtCrypt.randomString(keyLength);
		putWithLifespan(res, val, lifespan);
		return res;
	}

	//--------------------------------------------------------------------------
	//
	// Backend system setup / teardown / maintenance
	//
	// @see `DStackCommon` for the rest of the interface requirments
	//
	//--------------------------------------------------------------------------

	/**
	* Removes all data, without tearing down setup
	*
	* This is equivalent of "TRUNCATE TABLE {TABLENAME}"
	*
	* Note: that this is here to help resolve the interface conflict
	**/
	default void clear() {
		((GenericConvertMap<String, String>) this).clear();
	}
}
