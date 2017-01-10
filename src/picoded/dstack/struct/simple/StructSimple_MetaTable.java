package picoded.dstack.struct.simple;

// Java imports
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// Picoded imports
import picoded.conv.ConvertJSON;
import picoded.set.ObjectToken;
import picoded.dstack.*;
import picoded.dstack.core.*;

///
/// Reference implementation of MetaTable data structure.
/// This is done via a minimal implementation via internal data structures.
///
/// Built ontop of the Core_MetaTable implementation.
///
public class StructSimple_MetaTable extends Core_MetaTable {
	
	///
	/// Constructor vars
	///--------------------------------------------------------------------------
	
	/// Stores the key to value map
	protected Map<String, Map<String, Object>> _valueMap = new ConcurrentHashMap<String, Map<String, Object>>();
	
	/// Read write lock
	protected ReentrantReadWriteLock _accessLock = new ReentrantReadWriteLock();
	
	///
	/// Backend system setup / teardown
	///--------------------------------------------------------------------------
	
	/// Setsup the backend storage table, etc. If needed
	@Override
	public void systemSetup() {
		// does nothing
	}
	
	/// Teardown and delete the backend storage table, etc. If needed
	@Override
	public void systemDestroy() {
		_valueMap.clear();
	}
	
	/// Removes all data
	@Override
	public void clear() {
		_valueMap.clear();
	}
	
	///
	/// Internal functions, used by MetaObject
	///--------------------------------------------------------------------------
	
	/// Gets the complete remote data map, for MetaObject.
	/// Returns null
	public Map<String, Object> metaObjectRemoteDataMap_get(String oid) {
		try {
			_accessLock.readLock().lock();
			Map<String, Object> storedValue = _valueMap.get(oid);
			if (storedValue == null) {
				return null;
			}
			Map<String, Object> ret = new HashMap<String, Object>();
			for (Entry<String, Object> entry : storedValue.entrySet()) {
				ret.put(entry.getKey(), detachValue(storedValue.get(entry.getKey())));
			}
			return ret;
		} finally {
			_accessLock.readLock().unlock();
		}
	}
	
	/// Updates the actual backend storage of MetaObject
	/// either partially (if supported / used), or completely
	public void metaObjectRemoteDataMap_update(String oid, Map<String, Object> fullMap,
		Set<String> keys) {
		try {
			_accessLock.writeLock().lock();
			
			// Get keys to store, null = all
			if (keys == null) {
				keys = fullMap.keySet();
			}
			
			// Makes a new map if needed
			Map<String, Object> storedValue = _valueMap.get(oid);
			if (storedValue == null) {
				storedValue = new ConcurrentHashMap<String, Object>();
			}
			
			// Get and store the required values
			for (String key : keys) {
				Object val = fullMap.get(key);
				if (val == null) {
					storedValue.remove(key);
				} else {
					storedValue.put(key, val);
				}
			}
			
			// Ensure the value map is stored
			_valueMap.put(oid, storedValue);
		} finally {
			_accessLock.writeLock().unlock();
		}
	}
	
	///
	/// KeySet support
	///--------------------------------------------------------------------------
	
	/// Get and returns all the GUID's, note that due to its 
	/// potential of returning a large data set, production use
	/// should be avoided.
	///
	/// @returns set of keys
	@Override
	public Set<String> keySet() {
		try {
			_accessLock.readLock().lock();
			return _valueMap.keySet();
		} finally {
			_accessLock.readLock().unlock();
		}
	}
	
}
