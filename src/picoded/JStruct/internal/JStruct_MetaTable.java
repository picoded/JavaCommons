package picoded.JStruct.internal;

/// Java imports
import java.util.logging.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/// Picoded imports
import picoded.conv.*;
import picoded.struct.*;
import picoded.enums.*;
import picoded.JStruct.*;

/// MetaTable, serves as the core flexible backend storage implmentation for the whole
/// JStack setup. Its role can be viewed similarly to NoSql, or AWS SimpleDB
/// where almost everything is indexed and cached.
///
/// On a performance basis, it is meant to trade off raw query performance of traditional optimized
/// SQL lookup, over flexibility in data model. This is however heavily mintigated by the inclusion
/// of a JCache layer for non-complex lookup cached reads. Which will in most cases be the main
/// read request load.
///
public class JStruct_MetaTable implements MetaTable {

	///
	/// Constructor vars
	///--------------------------------------------------------------------------

	/// Stores the key to value map
	public Map<String, Map<String, Object>> _valueMap = new ConcurrentHashMap<String, Map<String, Object>>();

	/// Read write lock
	public ReentrantReadWriteLock _accessLock = new ReentrantReadWriteLock();

	/// Internal MetaTypeMap
	public MetaTypeMap _typeMap = new MetaTypeMap();

	///
	/// Constructor setup
	///--------------------------------------------------------------------------

	/// Constructor
	public JStruct_MetaTable() {
		// does nothing =X
	}

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
	public boolean getTempHint() {
		return isTempHint;
	}

	/// Sets temp mode optimization indicator hint
	/// Note that this only serve as a hint, as does not indicate actual setting
	///
	/// @param  mode  the new temp mode hint
	///
	/// @returns boolean  previous value if set
	public boolean setTempHint(boolean mode) {
		boolean ret = isTempHint;
		isTempHint = mode;
		return ret;
	}

	///
	/// Backend system setup / teardown
	///--------------------------------------------------------------------------

	/// Setsup the backend storage table, etc. If needed
	public void systemSetup() {
		// does nothing
	}

	/// Teardown and delete the backend storage table, etc. If needed
	public void systemTeardown() {
		_valueMap.clear();
	}

	// MetaObject MAP operations
	//----------------------------------------------

	/// Gets the MetaObject, if it exists
	public MetaObject get(Object _oid) {
		if(_oid == null){
			return null;
		}
		try {
			_accessLock.readLock().lock();

			String oid = _oid.toString();
			Map<String, Object> fullMap = metaObjectRemoteDataMap_get(oid);

			if (fullMap == null) {
				return null;
			}
			return new JStruct_MetaObject(this, oid, fullMap, true);
		} finally {
			_accessLock.readLock().unlock();
		}
	}

	/// Gets the full keySet
	public Set<String> keySet() {
		return _valueMap.keySet();
	}

	/// Remove the node
	public MetaObject remove(Object key) {
		_valueMap.remove(key);
		return null;
	}

	// MetaObject MAP optimizations
	//----------------------------------------------

	/// Does an unoptimized check, using keySet
	public boolean containsKey(Object key) {
		return (get(key) != null);
	}

	///
	/// Internal functions, used by MetaObject
	///--------------------------------------------------------------------------

	/// Ensures the returned value is not refencing the input value, cloning if needed
	public Object detachValue(Object in) {

		if (in instanceof byte[]) { //bytearray support
			byte[] ori = (byte[]) in;
			byte[] cop = new byte[ori.length];
			for (int a = 0; a < ori.length; ++a) {
				cop[a] = ori[a];
			}
			return cop;
		}

		return ConvertJSON.toObject(ConvertJSON.fromObject(in));
	}

	/// Gets the complete remote data map, for MetaObject.
	/// Returns null
	public Map<String, Object> metaObjectRemoteDataMap_get(String _oid) {
		try {
			_accessLock.readLock().lock();

			Map<String, Object> ret = null;
			Map<String, Object> storedValue = _valueMap.get(_oid);

			if (storedValue != null) {
				ret = new HashMap<String, Object>();
				for (String key : storedValue.keySet()) {
					ret.put(key, detachValue(storedValue.get(key)));
				}
			}

			return ret;
		} finally {
			_accessLock.readLock().unlock();
		}
	}

	/// Updates the actual backend storage of MetaObject
	/// either partially (if supported / used), or completely
	public void metaObjectRemoteDataMap_update(String _oid, Map<String, Object> fullMap, Set<String> keys) {
		try {
			_accessLock.writeLock().lock();

			if (keys == null) {
				keys = fullMap.keySet();
			}

			Map<String, Object> storedValue = _valueMap.get(_oid);
			if (storedValue == null) {
				storedValue = new ConcurrentHashMap<String, Object>();
			}

			for (String key : keys) {
				Object val = fullMap.get(key);

				if (val == null || val == ObjectTokens.NULL) {
					storedValue.remove(key);
				} else {
					storedValue.put(key, val);
				}
			}

			_valueMap.put(_oid, storedValue);
		} finally {
			_accessLock.writeLock().unlock();
		}
	}

	///
	/// MetaType handling, does type checking and conversion
	///--------------------------------------------------------------------------

	/// Gets and return the internal MetaTypeMap
	public MetaTypeMap typeMap() {
		return _typeMap;
	}

}
