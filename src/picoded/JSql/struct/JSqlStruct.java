package picoded.JSql.struct;

import picoded.JSql.JSql;
import picoded.JStruct.AccountTable;
import picoded.JStruct.AtomicLongMap;
import picoded.JStruct.JStruct;
import picoded.JStruct.KeyValueMap;
import picoded.JStruct.MetaTable;

public class JSqlStruct extends JStruct {
	
	/// The sql object implmentation
	protected JSql sqlObj = null;
	
	/// Setup with nothing
	/// TEMP: for JStack
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
	/// @returns AtomicLongMap
	protected AtomicLongMap setupAtomicLongMap(String name) {
		if (sqlObj == null) {
			throw new RuntimeException("Missing required SQL Object");
		}
		
		return new JSql_AtomicLongMap(sqlObj, name);
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
