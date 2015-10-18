package picoded.JCache.struct;

import java.util.*;
import java.util.logging.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import picoded.struct.*;
import picoded.JCache.*;
import picoded.conv.*;
import picoded.JStruct.*;
import picoded.JStruct.internal.*;
import picoded.security.NxtCrypt;

import org.apache.commons.lang3.RandomUtils;

/// JSql implmentation of KeyValueMap
public class JCache_KeyValueMap extends JStruct_KeyValueMap {
	
	///
	/// Temporary logger used to make sure incomplete implmentation is noted
	///--------------------------------------------------------------------------
	
	/// Standard java logger
	protected static Logger logger = Logger.getLogger(JCache_KeyValueMap.class.getName());
	
	///
	/// Constructor setup
	///--------------------------------------------------------------------------
	
	/// The inner sql object
	protected JCache jCacheObj = null;
	
	/// The tablename for the key value pair map
	protected String tablename = null;
	
	/// JCache setup 
	public JCache_KeyValueMap(JCache inCache, String inTablename) {
		super();
		jCacheObj = inCache;
		tablename = inTablename;
	}
	
	/// Setsup the backend storage table, etc. If needed
	public void systemSetup() {
		coreCacheMap();
	}
	
	///
	/// Core cache map handling
	///--------------------------------------------------------------------------
	
	/// Core cache map
	protected JCacheMap<String, String> _coreCacheMap = null;
	
	/// Core cache map
	protected JCacheMap<String, String> coreCacheMap() {
		if (_coreCacheMap != null) {
			return _coreCacheMap;
		}
		try {
			return _coreCacheMap = jCacheObj.getMap(tablename);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/// Teardown and delete the backend storage table, etc. If needed
	public void systemTeardown() {
		coreCacheMap().clear();
	}
	
	/// Perform maintenance, mainly removing of expired data if applicable
	public void maintenance() {
		
	}
	
	///
	/// put, get, etc (to override)
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
	protected String getValueRaw(String key, long now) {
		return coreCacheMap().get(key);
	}
	
	/// [Internal use, to be extended in future implementation]
	/// Sets the value, with validation
	///
	/// Handles re-entrant lock where applicable
	///
	/// @param key 
	/// @param value, null means removal
	/// @param expire timestamp in seconds, 0 means NO expire
	///
	/// @returns null
	protected String setValueRaw(String key, String value, long expire) {
		return coreCacheMap().put(key, value, expire);
	}
	
	/// [Internal use, to be extended in future implementation]
	/// Returns the expire time stamp value, raw without validation
	///
	/// Handles re-entrant lock where applicable
	///
	/// @param key as String
	///
	/// @returns long
	protected long getExpiryRaw(String key) {
		return coreCacheMap().getExpiry(key);
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
	protected void setExpiryRaw(String key, long expire) {
		String value = coreCacheMap().get(key);
		if (value != null) {
			coreCacheMap().put(key, value, expire);
		}
		// ignore if there is no value
	}
	
	///
	/// @TODO : getting this to work (see JCacheMap.java : 103)
	/// Current low performance fallback used below
	///
	/// Search using the value, all the relevent key mappings
	///
	/// Handles re-entrant lock where applicable
	///
	/// @param key, note that null matches ALL
	///
	/// @returns array of keys
	// public Set<String> getKeys(String value) {
	// 	return coreCacheMap().getKeys(value);
	// }
	
	/// Search using the value, all the relevent key mappings
	///
	/// Handles re-entrant lock where applicable
	///
	/// @param key, note that null matches ALL
	///
	/// @returns array of keys
	public Set<String> getKeys(String value) {
		long now = currentSystemTimeInSeconds();
		Set<String> ret = new HashSet<String>();
		
		// The keyset to check against
		Set<String> valuekeySet = coreCacheMap().keySet();
		
		// Iterate and get
		for (String key : valuekeySet) {
			String rawValue = _coreCacheMap.get(key);
			
			if (rawValue != null) {
				if (value == null || rawValue.equals(value)) {
					ret.add(key);
				}
			}
		}
		
		return ret;
	}
	
	/// Returns all the valid keys
	///
	/// @returns  the full keyset
	public Set<String> keySet() {
		return coreCacheMap().keySet();
	}
	
	/// Remove the value, given the key
	///
	/// @param key param find the thae meta key
	///
	/// @returns  null
	public String remove(Object key) {
		return coreCacheMap().remove(key);
	}
	
}
