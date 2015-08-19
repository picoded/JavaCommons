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

/// MetaTable, servs as the core flexible backend storage implmentation for the whole
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
	protected Map<String, Map<String,Object>> valueMap = new ConcurrentHashMap<String, Map<String,Object>>();
	
	/// Read write lock
	protected ReentrantReadWriteLock accessLock = new ReentrantReadWriteLock();
	
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
	protected boolean isTempHint = false;
	
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
		valueMap.clear();
	}
	
	/// Teardown and delete the backend storage table, etc. If needed
	public void systemTeardown() {
		valueMap.clear();
		
	}
	
	/// perform increment maintenance, meant for minor changes between requests
	public void incrementalMaintenance() {
		// For JStruct, both is same
		maintenance();
	}
	
	/// Perform maintenance, mainly removing of expired data if applicable
	public void maintenance() {
		// does nothing?
	}
	
	///
	/// Internal functions, used by MetaObject
	///--------------------------------------------------------------------------
	
	/// Ensures the returned value is not refencing the input value, cloning if needed
	protected Object detachValue(Object in) {
		return ConvertJSON.toObject( ConvertJSON.fromObject(in) );
	}
	
	/// Gets the complete remote data map, for MetaObject
	protected Map<String, Object> metaObjectRemoteDataMap_get(String _oid) {
		try {
			accessLock.readLock().lock();
			
			Map<String, Object> ret = new HashMap<String,Object>();
			Map<String, Object> storedValue = valueMap.get(_oid);
			
			if( storedValue != null ) {
				for( String key : storedValue.keySet() ) {
					ret.put( key, detachValue(storedValue.get(key)) );
				}
			}
			
			return ret;
		} finally {
			accessLock.readLock().unlock();
		}
	}
	
	/// Updates the actual backend storage of MetaObject 
	/// either partially (if supported / used), or completely
	protected void metaObjectRemoteDataMap_update(String _oid, Map<String,Object> fullMap, Set<String> keys) {
		try {
			accessLock.writeLock().lock();
			
			if( keys == null ) {
				keys = fullMap.keySet();
			}
			
			Map<String, Object> storedValue = valueMap.get(_oid);
			if( storedValue == null ) {
				storedValue = new ConcurrentHashMap<String, Object>();
			}
			
			for( String key : keys ) {
				Object val = fullMap.get(key);
				
				if( val == null || val == ObjectTokens.NULL ) {
					storedValue.remove(val);
				} else {
					storedValue.put( key, val );
				}
			}
			
			valueMap.put(_oid, storedValue);
		} finally {
			accessLock.writeLock().unlock();
		}
	}
	
}
