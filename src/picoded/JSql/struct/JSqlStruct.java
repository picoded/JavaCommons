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
		return new JSql_KeyValueMap(sqlObj, name);
	}
	
}
