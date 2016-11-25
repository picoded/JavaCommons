package picoded.JStruct.internal;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import picoded.JStack.JStack;
import picoded.JStruct.JStruct;

public class JStack_KeyValueMap extends JStruct_KeyValueMap {
	
	///
	/// Temporary logger used to make sure incomplete implmentation is noted
	///--------------------------------------------------------------------------
	
	/// Standard java logger
	public static final Logger logger = Logger.getLogger(JStack_KeyValueMap.class.getName());
	
	///
	/// Constructor setup
	///--------------------------------------------------------------------------
	
	/// The inner sql object
	protected static volatile JStack stackObj = null;
	
	/// The tablename for the key value pair map
	protected static volatile String stackTablename = null;
	
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
	protected JStruct_KeyValueMap[] implementationLayer = null;
	
	/// The cached structure implmentation layers reversed
	protected JStruct_KeyValueMap[] implementationLayersReversed = null;
	
	///
	/// Getting the implmentation layers
	/// This is used internally to iterate the KeyValueMap layers
	///
	public JStruct_KeyValueMap[] implementationLayers() {
		if (implementationLayer != null) {
			return implementationLayer;
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
		
		return implementationLayer = ret;
	}
	
	///
	/// Getting the implmentation layers reversed
	/// This is used internally to iterate the KeyValueMap layers reversed
	///
	public JStruct_KeyValueMap[] implementationLayers_reverse() {
		if (implementationLayersReversed != null) {
			return implementationLayersReversed;
		}
		
		// Get and reverse
		JStruct_KeyValueMap[] impleLayers = implementationLayers();
		int impleLength = impleLayers.length;
		JStruct_KeyValueMap[] ret = new JStruct_KeyValueMap[impleLength];
		for (int i = 0; i < impleLength; i++) {
			ret[i] = impleLayers[impleLength - 1 - i];
		}
		
		// Return the reversed PDF
		return implementationLayersReversed = ret;
	}
	
	///
	/// Backend system setup / teardown
	///--------------------------------------------------------------------------
	
	/// Setsup the backend storage table, etc. If needed
	@Override
	public void systemSetup() {
		for (JStruct_KeyValueMap i : implementationLayers()) {
			i.systemSetup();
		}
	}
	
	/// Teardown and delete the backend storage table, etc. If needed
	@Override
	public void systemTeardown() {
		for (JStruct_KeyValueMap i : implementationLayers()) {
			i.systemTeardown();
		}
	}
	
	/// Perform maintenance, mainly removing of expired data if applicable
	@Override
	public void maintenance() {
		for (JStruct_KeyValueMap i : implementationLayers()) {
			try {
				i.maintenance();
			} catch (Exception e) {
				logger.log(Level.WARNING, e.getMessage());
			}
		}
	}
	
	/// Perform maintenance, mainly removing of expired data if applicable
	@Override
	public void incrementalMaintenance() {
		for (JStruct_KeyValueMap i : implementationLayers()) {
			try {
				i.incrementalMaintenance();
			} catch (Exception e) {
				logger.log(Level.WARNING, e.getMessage());
			}
		}
	}
	
	/// Perform maintenance, mainly removing of expired data if applicable
	@Override
	public void clear() {
		for (JStruct_KeyValueMap i : implementationLayers()) {
			try {
				i.clear();
			} catch (Exception e) {
				logger.log(Level.WARNING, e.getMessage());
			}
			
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
	@Override
	public long getExpiryRaw(String key) {
		long ret = -1;
		for (JStruct_KeyValueMap i : implementationLayers()) {
			try {
				ret = i.getExpiryRaw(key);
			} catch (Exception e) {
				logger.log(Level.WARNING, e.getMessage());
			}
		}
		return ret;
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
	@Override
	public void setExpiryRaw(String key, long time) {
		for (JStruct_KeyValueMap i : implementationLayers_reverse()) {
			try {
				i.setExpiryRaw(key, time);
			} catch (Exception e) {
				logger.log(Level.WARNING, e.getMessage());
			}
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
	@Override
	public String getValueRaw(String key, long now) {
		String val = null;
		for (JStruct_KeyValueMap i : implementationLayers()) {
			try {
				val = i.getValueRaw(key, now);
			} catch (Exception e) {
				logger.log(Level.WARNING, e.getMessage());
			}
			
		}
		return val;
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
	@Override
	public String setValueRaw(String key, String value, long expire) {
		for (JStruct_KeyValueMap i : implementationLayers_reverse()) {
			try {
				i.setValueRaw(key, value, expire);
			} catch (Exception e) {
				logger.log(Level.WARNING, e.getMessage());
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
	@Override
	public Set<String> getKeys(String value) {
		Set<String> ret = null;
		for (JStruct_KeyValueMap i : implementationLayers_reverse()) {
			try {
				ret = i.getKeys(value);
			} catch (Exception e) {
				logger.log(Level.WARNING, e.getMessage());
			}
			
		}
		return ret;
	}
	
	/// Search using the value, all the relevent key mappings
	///
	/// Handles re-entrant lock where applicable
	///
	/// @param key, note that null matches ALL
	///
	/// @returns array of keys
	@Override
	public String remove(Object key) {
		for (JStruct_KeyValueMap i : implementationLayers_reverse()) {
			try {
				i.remove(key);
			} catch (Exception e) {
				logger.log(Level.WARNING, e.getMessage());
			}
		}
		return null;
	}
	
}
