package picoded.JStruct.internal;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import picoded.JStruct.KeyValueMap;
import picoded.security.NxtCrypt;

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
public class JStruct_KeyValueMap implements KeyValueMap {
	
	/// Invalid constructor (throws exception)
	public JStruct_KeyValueMap() {
	}
	
	///
	/// Constructor vars
	///--------------------------------------------------------------------------
	
	/// Stores the key to value map
	public ConcurrentHashMap<String, String> valueMap = new ConcurrentHashMap<String, String>();
	
	/// Stores the expire timestamp
	public ConcurrentHashMap<String, Long> expireMap = new ConcurrentHashMap<String, Long>();
	
	/// Read write lock
	public ReentrantReadWriteLock accessLock = new ReentrantReadWriteLock();
	
	///
	/// Constructor setup
	///--------------------------------------------------------------------------
	
	///
	/// Temp mode optimization, used to indicate pure session like data,
	/// that does not require persistance (or even SQL)
	///
	///--------------------------------------------------------------------------
	
	/// Temp value flag, defaults to false
	public boolean isTempHint = false;
	
	/// Gets if temp mode optimization hint is indicated
	/// Note that this only serve as a hint, as does not indicate actual setting
	///
	/// @returns boolean  temp mode value
	@Override
	public boolean getTempHint() {
		return isTempHint;
	}
	
	/// Sets temp mode optimization indicator hint
	/// Note that this only serve as a hint, as does not indicate actual setting
	///
	/// @param  mode  the new temp mode hint
	///
	/// @returns boolean  previous value if set
	@Override
	public boolean setTempHint(boolean mode) {
		boolean ret = isTempHint;
		isTempHint = mode;
		return ret;
	}
	
	///
	/// Backend system setup / teardown
	///--------------------------------------------------------------------------
	
	/// Setsup the backend storage table, etc. If needed
	@Override
	public void systemSetup() {
		//clear();
	}
	
	/// Teardown and delete the backend storage table, etc. If needed
	@Override
	public void systemTeardown() {
		clear();
	}
	
	/// perform increment maintenance, meant for minor changes between requests
	@Override
	public void incrementalMaintenance() {
		// For JStruct, both is same
		maintenance();
	}
	
	/// Perform maintenance, mainly removing of expired data if applicable
	@Override
	public void maintenance() {
		try {
			accessLock.writeLock().lock();
			
			// The time to check against
			long now = currentSystemTimeInSeconds();
			
			// not iterated directly due to remove()
			Set<String> expireKeySet = expireMap.keySet();
			
			// The keyset to check against
			String[] expireKeyArray = expireKeySet.toArray(new String[expireKeySet.size()]);
			
			// Iterate and evict
			for (String key : expireKeyArray) {
				Long timeObj = expireMap.get(key);
				long time = (timeObj != null) ? timeObj.longValue() : 0;
				
				// expired? kick it
				if (time < now && time > 0) {
					valueMap.remove(key);
					expireMap.remove(key);
				}
			}
		} finally {
			accessLock.writeLock().unlock();
		}
	}
	
	/// Removes all data, without tearing down setup
	///
	/// Handles re-entrant lock where applicable
	///
	@Override
	public void clear() {
		try {
			accessLock.writeLock().lock();
			valueMap.clear();
			expireMap.clear();
		} finally {
			accessLock.writeLock().unlock();
		}
	}
	
	///
	/// Utility functions used internally
	///--------------------------------------------------------------------------
	
	/// Gets the current system time in seconds
	public long currentSystemTimeInSeconds() {
		return (System.currentTimeMillis()) / 1000L;
	}
	
	///
	/// Expiration and lifespan handling (to override)
	///--------------------------------------------------------------------------
	
	/// [Internal use, to be extended in future implementation]
	/// Returns the expire time stamp value, raw without validation
	///
	/// Handles re-entrant lock where applicable
	///
	/// @param key as String
	///
	/// @returns long
	public long getExpiryRaw(String key) {
		try {
			accessLock.readLock().lock();
			
			// no value fails
			if (valueMap.get(key) == null) {
				return -1;
			}
			
			// Expire value?
			Long expireObj = expireMap.get(key);
			if (expireObj == null) {
				return 0;
			}
			return expireObj.longValue();
		} finally {
			accessLock.readLock().unlock();
		}
	}
	
	/// [Internal use, to be extended in future implementation]
	/// Sets the expire time stamp value, raw without validation
	///
	/// Handles re-entrant lock where applicable
	///
	/// @param key as String
	/// @param expire timestamp in seconds, 0 means NO expire
	///
	/// @returns long
	public void setExpiryRaw(String key, long time) {
		try {
			accessLock.writeLock().lock();
			
			// Does nothing if empty
			if (time <= 0 || valueMap.get(key) == null) {
				expireMap.remove(key);
				return;
			}
			
			// Set expire value
			expireMap.put(key, new Long(time));
		} finally {
			accessLock.writeLock().unlock();
		}
	}
	
	///
	/// Expiration and lifespan handling (public access)
	///--------------------------------------------------------------------------
	
	/// Returns the expire time stamp value, if still valid
	///
	/// @param key as String
	///
	/// @returns long, 0 means no expirary, -1 no data / expire
	@Override
	public long getExpiry(String key) {
		long expire = getExpiryRaw(key);
		if (expire <= 0) { //0 = no timestamp, -1 = no data
			return expire;
		}
		if (expire > currentSystemTimeInSeconds()) {
			return expire;
		}
		return -1; //expired
	}
	
	/// Returns the lifespan time stamp value
	///
	/// @param key as String
	///
	/// @returns long, 0 means no expirary, -1 no data / expire
	@Override
	public long getLifespan(String key) {
		long expire = getExpiryRaw(key);
		if (expire <= 0) { //0 = no timestamp, -1 = no data
			return expire;
		}
		
		long lifespan = expire - currentSystemTimeInSeconds();
		if (lifespan <= 0) {
			return -1; //expired
		}
		
		return lifespan;
	}
	
	/// Sets the expire time stamp value, if still valid
	///
	/// @param key 
	/// @param expire timestamp in seconds, 0 means NO expire
	@Override
	public void setExpiry(String key, long time) {
		setExpiryRaw(key, time);
	}
	
	/// Sets the expire time stamp value, if still valid
	///
	/// @param key 
	/// @param lifespan 
	@Override
	public void setLifeSpan(String key, long lifespan) {
		setExpiryRaw(key, lifespan + currentSystemTimeInSeconds());
	}
	
	///
	/// put, get, remove, etc (to override)
	///--------------------------------------------------------------------------
	
	/// [Internal use, to be extended in future implementation]
	/// Returns the value, with validation
	///
	/// Handles re-entrant lock where applicable
	///
	/// @param key as String
	/// @param now timestamp
	///
	/// @returns String value
	public String getValueRaw(String key, long now) {
		try {
			accessLock.readLock().lock();
			
			String val = valueMap.get(key);
			if (val == null) {
				return null;
			}
			
			// Note: 0 = no timestamp, hence valid value
			long expiry = getExpiryRaw(key);
			if (expiry != 0 && expiry < now) {
				return null;
			}
			
			return val;
		} finally {
			accessLock.readLock().unlock();
		}
	}
	
	/// [Internal use, to be extended in future implementation]
	/// Sets the value, with validation
	///
	/// Handles re-entrant lock where applicable
	///
	/// @param key 
	/// @param value, null means removal
	/// @param expire timestamp, 0 means not timestamp
	///
	/// @returns null
	public String setValueRaw(String key, String value, long expire) {
		try {
			accessLock.writeLock().lock();
			
			if (value == null) {
				valueMap.remove(key);
				expireMap.remove(key);
			} else {
				valueMap.put(key, value);
			}
			setExpiryRaw(key, expire);
			return null;
		} finally {
			accessLock.writeLock().unlock();
		}
	}
	
	/// Search using the value, all the relevent key mappings
	///
	/// Handles re-entrant lock where applicable
	///
	/// @param key, note that null matches ALL
	///
	/// @returns array of keys
	@Override
	public Set<String> getKeys(String value) {
		try {
			accessLock.readLock().lock();
			
			long now = currentSystemTimeInSeconds();
			Set<String> ret = new HashSet<String>();
			
			// The keyset to check against
			Set<String> valuekeySet = valueMap.keySet();
			
			// Iterate and get
			for (String key : valuekeySet) {
				String rawValue = getValueRaw(key, now);
				
				if (rawValue != null) {
					if (value == null || rawValue.equals(value)) {
						ret.add(key);
					}
				}
			}
			
			return ret;
		} finally {
			accessLock.readLock().unlock();
		}
	}
	
	/// Remove the value, given the key
	///
	/// @param key param find the thae meta key
	///
	/// @returns  null
	@Override
	public String remove(Object key) {
		try {
			accessLock.writeLock().lock();
			
			valueMap.remove(key);
			expireMap.remove(key);
			
			return null;
		} finally {
			accessLock.writeLock().unlock();
		}
	}
	
	///
	/// put, get, etc (public)
	///--------------------------------------------------------------------------
	
	/// Returns all the valid keys
	///
	/// @returns  the full keyset
	@Override
	public Set<String> keySet() {
		return getKeys(null);
	}
	
	/// Contains key operation.
	///
	/// note that boolean false can either mean no value or expired value
	///
	/// @param key as String
	/// @returns boolean true or false if the key exists
	@Override
	public boolean containsKey(Object key) {
		return (getLifespan(key.toString()) >= 0);
	}
	
	/// Returns the value, given the key
	/// @param key param find the thae meta key
	///
	/// returns  value of the given key
	@Override
	public String get(Object key) {
		return getValueRaw(key.toString(), currentSystemTimeInSeconds());
	}
	
	/// Stores (and overwrites if needed) key, value pair
	///
	/// Important note: It does not return the previously stored value
	///
	/// @param key as String
	/// @param value as String
	///
	/// @returns null
	@Override
	public String put(String key, String value) {
		return setValueRaw(key, value, 0);
	}
	
	/// Stores (and overwrites if needed) key, value pair
	///
	/// Important note: It does not return the previously stored value
	///
	/// @param key as String
	/// @param value as String
	/// @param lifespan time to expire in seconds
	///
	/// @returns null
	@Override
	public String putWithLifespan(String key, String value, long lifespan) {
		return setValueRaw(key, value, currentSystemTimeInSeconds() + lifespan);
	}
	
	/// Stores (and overwrites if needed) key, value pair
	///
	/// Important note: It does not return the previously stored value
	///
	/// @param key as String
	/// @param value as String
	/// @param expireTime expire time stamp value
	///
	/// @returns String
	@Override
	public String putWithExpiry(String key, String value, long expireTime) {
		return setValueRaw(key, value, expireTime);
	}
	
	///
	/// Nonce operations suppport (public)
	///--------------------------------------------------------------------------
	
	/// Default nonce lifetime (1 hour)
	public int nonce_defaultLifetime = 3600;
	
	/// Default nonce string length (22 is chosen to be consistent with base58 GUID's)
	public int nonce_defaultLength = 22;
	
	/// Generates a random nonce hash, and saves the value to it
	///
	/// Relies on both nonce_defaultLength & nonce_defaultLifetime for default parameters
	///
	/// @param value to store as string
	///
	/// @returns String value of the random key generated
	@Override
	public String generateNonce(String val) {
		return generateNonce(val, nonce_defaultLifetime, nonce_defaultLength);
	}
	
	/// Generates a random nonce hash, and saves the value to it
	///
	/// Relies on nonce_defaultLength for default parameters
	///
	/// @param value to store as string
	/// @param lifespan time to expire in seconds
	///
	/// @returns String value of the random key generated
	@Override
	public String generateNonce(String val, long lifespan) {
		return generateNonce(val, lifespan, nonce_defaultLength);
	}
	
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
	@Override
	public String generateNonce(String val, long lifespan, int keyLength) {
		String res = NxtCrypt.randomString(keyLength);
		putWithLifespan(res, val, lifespan);
		return res;
	}
	
}
