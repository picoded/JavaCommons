package picoded.dstack.jsql;

import java.util.logging.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import picoded.dstack.KeyValueMap;
import picoded.dstack.core.Core_KeyValueMap;
import picoded.security.NxtCrypt;
import picoded.struct.GenericConvertMap;
import picoded.struct.GenericConvertHashMap;
import picoded.dstack.jsql.connector.*;
import picoded.set.JSqlType;

///
/// Reference implementation of KeyValueMap data structure.
/// This is done via a minimal implementation via internal data structures.
///
/// Built ontop of the Core_KeyValueMap implementation.
///
public class JSql_KeyValueMap extends Core_KeyValueMap {
	
	///
	/// Temporary logger used to make sure incomplete implmentation is noted
	///--------------------------------------------------------------------------
	
	/// Standard java logger
	public static Logger logger = Logger.getLogger(JSql_KeyValueMap.class.getName());
	
	//--------------------------------------------------------------------------
	//
	// Constructor vars
	//
	//--------------------------------------------------------------------------
	
	/// The inner sql object
	protected JSql sqlObj = null;
	
	/// The tablename for the key value pair map
	protected String sqlTableName = null;
	
	/// [internal use] JSql setup with a SQL connection and tablename
	public JSql_KeyValueMap(JSql inJSql, String tablename) {
		super();
		sqlTableName = tablename;
		sqlObj = inJSql;
	}
	
	//--------------------------------------------------------------------------
	//
	// KeySet support implementation
	//
	//--------------------------------------------------------------------------
	
	/// Search using the value, all the relevent key mappings
	///
	/// Handles re-entrant lock where applicable
	///
	/// @param key, note that null matches ALL
	///
	/// @returns array of keys
	@Override
	public Set<String> keySet(String value) {
		return null;
	}
	
	//--------------------------------------------------------------------------
	//
	// Fundemental set/get value (core)
	//
	//--------------------------------------------------------------------------
	
	/// [Internal use, to be extended in future implementation]
	/// Returns the value, with validation
	///
	/// Handles re-entrant lock where applicable
	///
	/// @param key as String
	/// @param now timestamp
	///
	/// @returns String value
	protected String getValueRaw(String key, long now) {
		return null;
	}
	
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
	protected String setValueRaw(String key, String value, long expire) {
		return null;
	}
	
	//--------------------------------------------------------------------------
	//
	// Expiration and lifespan handling (core)
	//
	//--------------------------------------------------------------------------
	
	/// [Internal use, to be extended in future implementation]
	/// Returns the expire time stamp value, raw without validation
	///
	/// Handles re-entrant lock where applicable
	///
	/// @param key as String
	///
	/// @returns long
	protected long getExpiryRaw(String key) {
		return 0;
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
	public void setExpiryRaw(String key, long time) {
		
	}
	
	//--------------------------------------------------------------------------
	//
	// Backend system setup / teardown / maintenance (DStackCommon)
	//
	//--------------------------------------------------------------------------
	
	/// Primary key type
	protected String pKeyColumnType = "BIGINT PRIMARY KEY AUTOINCREMENT";
	
	/// Timestamp field type
	protected String tStampColumnType = "BIGINT";
	
	/// Key name field type
	protected String keyColumnType = "VARCHAR(64)";
	
	/// Value field type
	protected String valueColumnType = "VARCHAR(MAX)";
	
	/// Setsup the backend storage table, etc. If needed
	public void systemSetup() {
		// Table constructor
		//-------------------
		sqlObj.createTable( //
			sqlTableName, //
			new String[] { //
			// Primary key, as classic int, this is used to lower SQL
			// fragmentation level, and index memory usage. And is not accessible.
			// Sharding and uniqueness of system is still maintained by meta keys
				"pKy", //
				// Time stamps
				"cTm", //value created time
				"eTm", //value expire time
				// Storage keys
				"kID", //
				// Value storage
				"kVl" //
			}, //
			new String[] { //
				pKeyColumnType, //Primary key
				// Time stamps
				tStampColumnType, tStampColumnType,
				// Storage keys
				keyColumnType, //
				// Value storage
				valueColumnType //
			} //
		);
		
		// Unique index
		//------------------------------------------------
		sqlObj.createIndex( //
			sqlTableName, "kID", "UNIQUE", "unq" //
		);
		
		// Value search index
		//------------------------------------------------
		if (sqlObj.sqlType() == JSqlType.MYSQL) {
			sqlObj.createIndex( //
				sqlTableName, "kVl(255)", null, "valMap" //
			);
		} else {
			sqlObj.createIndex( //
				sqlTableName, "kVl", null, "valMap" //
			);
		}
	}
	
	/// Teardown and delete the backend storage table, etc. If needed
	///
	/// @TODO properly handle this: Especially adding (and testing) the IF EXISTS clause
	public void systemDestroy() {
		try {
			sqlObj.update("DROP TABLE IF EXISTS " + sqlTableName); //IF EXISTS
		} catch (JSqlException e) {
			logger.log(Level.SEVERE, "systemTeardown JSqlException (@TODO properly handle this): ", e);
		}
	}
	
	/// Perform maintenance, mainly removing of expired data if applicable
	public void maintenance() {
		try {
			long now = currentSystemTimeInSeconds();
			sqlObj.update("DELETE FROM `" + sqlTableName + "` WHERE eTm <= ? AND eTm > ?", now, 0);
		} catch (JSqlException e) {
			throw new RuntimeException(e);
		}
	}
	
	///
	/// Removes all data, without tearing down setup
	///
	/// Handles re-entrant lock where applicable
	///
	@Override
	public void clear() {
		try {
			sqlObj.update("DELETE FROM `" + sqlTableName + "`");
		} catch (JSqlException e) {
			throw new RuntimeException(e);
		}
	}
	
}
