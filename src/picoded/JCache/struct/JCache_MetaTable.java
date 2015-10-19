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
public class JCache_MetaTable extends JStruct_MetaTable {
	
	///
	/// Temporary logger used to make sure incomplete implmentation is noted
	///--------------------------------------------------------------------------
	
	/// Standard java logger
	protected static Logger logger = Logger.getLogger(JCache_MetaTable.class.getName());
	
	///
	/// Constructor setup
	///--------------------------------------------------------------------------
	
	/// The inner sql object
	protected JCache jCacheObj = null;
	
	/// The tablename for the key value pair map
	protected String tablename = null;
	
	/// JCache setup 
	public JCache_MetaTable(JCache inCache, String inTablename) {
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
	protected JCacheMap<String, Map<String, Object>> _coreCacheMap = null;
	
	/// Core cache map
	protected JCacheMap<String, Map<String, Object>> coreCacheMap() {
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
	
	// MetaObject MAP operations
	//----------------------------------------------
	
	/// Gets the full keySet
	public Set<String> keySet() {
		return coreCacheMap().keySet();
	}
	
	/// Remove the node
	public MetaObject remove(Object key) {
		coreCacheMap().remove(key);
		return null;
	}
	
	//
	// Internal functions, used by MetaObject
	//--------------------------------------------------------------------------
	
	/// Gets the complete remote data map, for MetaObject.
	/// Returns null
	protected Map<String, Object> metaObjectRemoteDataMap_get(String _oid) {
		return coreCacheMap().get(_oid);
	}
	
	/// Updates the actual backend storage of MetaObject 
	/// either partially (if supported / used), or completely
	protected void metaObjectRemoteDataMap_update(String _oid, Map<String, Object> fullMap, Set<String> keys) {
		// Ensure all object refence is lost
		Map<String, Object> storeMap = new HashMap<String, Object>();
		
		// Iterate and create a store map, as fullMap 
		for (Map.Entry<String, Object> entry : fullMap.entrySet()) {
			storeMap.put(entry.getKey(), entry.getValue());
		}
		
		// Store the object
		coreCacheMap().put(_oid, storeMap);
	}
	
}
