package picoded.JCache.struct;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import picoded.JSql.*;
import picoded.JCache.*;
import picoded.JStruct.*;
import picoded.struct.*;

public class JCacheStruct extends JStruct {
	
	/// The JCache object implmentation
	protected JCache jCacheObj = null;
	
	/// Setup with the JCache object
	public JCacheStruct(JCache cache) {
		jCacheObj = cache;
	}
	
	/// Actual setup implmentation to overwrite
	///
	/// @param name - name of map in backend
	///
	/// @TODO: Cache generated object
	///
	/// @returns KeyValueMap
	protected KeyValueMap setupKeyValueMap(String name) {
		if (jCacheObj == null) {
			throw new RuntimeException("Missing required Cache Object");
		}
		
		return new JCache_KeyValueMap(jCacheObj, name);
	}
	
	/// Actual setup implmentation to overwrite
	///
	/// @param name - name of map in backend
	///
	/// @returns MetaTable
	protected MetaTable setupMetaTable(String name) {
		if (jCacheObj == null) {
			throw new RuntimeException("Missing required Cache Object");
		}
		
		return new JCache_MetaTable(jCacheObj, name);
	}
	
	/// Actual setup implmentation to overwrite
	///
	/// @param name - name of map in backend
	///
	/// @TODO: Cache generated object
	///
	/// @returns AccountTable
	protected AccountTable setupAccountTable(String name) {
		if (jCacheObj == null) {
			throw new RuntimeException("Missing required Cache Object");
		}
		
		return new AccountTable(this, name);
	}
	
}
