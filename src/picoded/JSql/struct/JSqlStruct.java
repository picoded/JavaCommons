package picoded.JSql.struct;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import picoded.JSql.*;
import picoded.JStruct.*;
import picoded.struct.*;
import picoded.security.NxtCrypt;
import picoded.JStruct.KeyValueMap;

public class JSqlStruct extends JStruct {
	
	/// The sql object implmentation
	protected JSql sqlObj = null;
	
	/// Setup with nothing
	public JSqlStruct() {
		// does nothing
	}
	
	/// Setup with sql object
	public JSqlStruct(JSql sql) {
		sqlObj = sql;
	}
	
	/// Actual setup implmentation to overwrite
	///
	/// @param name - name of map in backend
	///
	/// @returns KeyValueMap
	protected KeyValueMap setupKeyValueMap(String name) {
		if (sqlObj == null) {
			throw new RuntimeException("Missing required SQL Object");
		}
		
		return new JSql_KeyValueMap(sqlObj, name);
	}
	
	/// Actual setup implmentation to overwrite
	///
	/// @param name - name of map in backend
	///
	/// @returns MetaTable
	protected MetaTable setupMetaTable(String name) {
		if (sqlObj == null) {
			throw new RuntimeException("Missing required SQL Object");
		}
		
		return new JSql_MetaTable(sqlObj, name);
	}
	
	/// Actual setup implmentation to overwrite
	///
	/// @param name - name of map in backend
	///
	/// @returns AccountTable
	protected AccountTable setupAccountTable(String name) {
		if (sqlObj == null) {
			throw new RuntimeException("Missing required SQL Object");
		}
		
		return new AccountTable(this, name);
	}
	
}
