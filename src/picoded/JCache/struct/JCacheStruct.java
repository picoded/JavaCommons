package picoded.JCache.struct;

import picoded.JCache.JCache;
import picoded.JStruct.AccountTable;
import picoded.JStruct.JStruct;
import picoded.JStruct.KeyValueMap;
import picoded.JStruct.MetaTable;

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
