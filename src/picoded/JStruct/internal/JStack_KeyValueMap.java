package picoded.JStruct.internal;

import java.util.*;
import java.util.logging.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import picoded.struct.*;
import picoded.security.NxtCrypt;
import picoded.JStruct.KeyValueMap;

import picoded.struct.*;
import picoded.JStack.*;
import picoded.JCache.*;
import picoded.JSql.*;
import picoded.JCache.struct.*;
import picoded.JSql.struct.*;
import picoded.conv.*;
import picoded.JStruct.*;
import picoded.JStruct.internal.*;
import picoded.security.NxtCrypt;

import org.apache.commons.lang3.RandomUtils;

public class JStack_KeyValueMap extends JStruct_KeyValueMap {
	
	///
	/// Temporary logger used to make sure incomplete implmentation is noted
	///--------------------------------------------------------------------------
	
	/// Standard java logger
	public static Logger logger = Logger.getLogger(JStack_KeyValueMap.class.getName());
	
	///
	/// Constructor setup
	///--------------------------------------------------------------------------
	
	/// The inner sql object
	public JStack stackObj = null;
	
	/// The tablename for the key value pair map
	public String stackTablename = null;
	
	/// JStack setup
	public JStack_KeyValueMap(JStack inStack, String tablename) {
		super();
		stackObj = inStack;
		stackTablename = tablename;
	}
	
	///
	/// Internal implementation layers handling
	///--------------------------------------------------------------------------
	
	/// The cached structure implmentation layers
	public JStruct_KeyValueMap[] _implementationLayers = null;
	
	/// The cached structure implmentation layers reversed
	public JStruct_KeyValueMap[] _implementationLayers_reversed = null;
	
	///
	/// Getting the implmentation layers
	/// This is used internally to iterate the KeyValueMap layers
	///
	public JStruct_KeyValueMap[] implementationLayers() {
		if (_implementationLayers != null) {
			return _implementationLayers;
		}
		
		// Get the structure layers
		JStruct[] struct = stackObj.structLayers();
		JStruct_KeyValueMap[] ret = new JStruct_KeyValueMap[struct.length];
		
		// Fetch their respective key value map
		for (int a = 0; a < struct.length; ++a) {
			
			// Safety check
			if (struct[a] == null) {
				ret[a] = null;
				continue;
			}
			
			// Fetch the implementation
			ret[a] = (JStruct_KeyValueMap) struct[a].getKeyValueMap(stackTablename);
		}
		
		return (_implementationLayers = ret);
	}
	
	///
	/// Getting the implmentation layers reversed
	/// This is used internally to iterate the KeyValueMap layers reversed
	///
	public JStruct_KeyValueMap[] implementationLayers_reverse() {
		if (_implementationLayers_reversed != null) {
			return _implementationLayers_reversed;
		}
		
		// Get and reverse
		JStruct_KeyValueMap[] impleLayers = implementationLayers();
		int impleLength = impleLayers.length;
		JStruct_KeyValueMap[] ret = new JStruct_KeyValueMap[impleLength];
		for (int i = 0; i < impleLength; i++) {
			ret[i] = impleLayers[impleLength - 1 - i];
		}
		
		// Return the reversed PDF
		return _implementationLayers_reversed = ret;
	}
	
	///
	/// Backend system setup / teardown
	///--------------------------------------------------------------------------
	
	/// Setsup the backend storage table, etc. If needed
	public void systemSetup() {
		for (JStruct_KeyValueMap i : implementationLayers()) {
			i.systemSetup();
		}
	}
	
	/// Teardown and delete the backend storage table, etc. If needed
	public void systemTeardown() {
		for (JStruct_KeyValueMap i : implementationLayers()) {
			i.systemTeardown();
		}
	}
	
	/// Perform maintenance, mainly removing of expired data if applicable
	public void maintenance() {
		for (JStruct_KeyValueMap i : implementationLayers()) {
			i.maintenance();
		}
	}
	
	/// Perform maintenance, mainly removing of expired data if applicable
	public void incrementalMaintenance() {
		for (JStruct_KeyValueMap i : implementationLayers()) {
			i.incrementalMaintenance();
		}
	}
	
	/// Perform maintenance, mainly removing of expired data if applicable
	public void clear() {
		for (JStruct_KeyValueMap i : implementationLayers()) {
			i.clear();
		}
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
		long ret = -1;
		for (JStruct_KeyValueMap i : implementationLayers()) {
			if ((ret = i.getExpiryRaw(key)) >= 0) {
				return ret;
			}
		}
		return -1;
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
		for (JStruct_KeyValueMap i : implementationLayers_reverse()) {
			i.setExpiryRaw(key, time);
		}
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
		String val = null;
		for (JStruct_KeyValueMap i : implementationLayers()) {
			if ((val = i.getValueRaw(key, now)) != null) {
				return val;
			}
		}
		return null;
	}
	
	///
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
		for (JStruct_KeyValueMap i : implementationLayers_reverse()) {
			i.setValueRaw(key, value, expire);
		}
		return null;
	}
	
	/// Search using the value, all the relevent key mappings
	///
	/// Handles re-entrant lock where applicable
	///
	/// @param key, note that null matches ALL
	///
	/// @returns array of keys
	public Set<String> getKeys(String value) {
		Set<String> ret = null;
		for (JStruct_KeyValueMap i : implementationLayers_reverse()) {
			if ((ret = i.getKeys(value)) != null) {
				return ret;
			}
		}
		return null;
	}
	
	/// Search using the value, all the relevent key mappings
	///
	/// Handles re-entrant lock where applicable
	///
	/// @param key, note that null matches ALL
	///
	/// @returns array of keys
	public String remove(Object key) {
		for (JStruct_KeyValueMap i : implementationLayers_reverse()) {
			i.remove(key);
		}
		return null;
	}
	
}
