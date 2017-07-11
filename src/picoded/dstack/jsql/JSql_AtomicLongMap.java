package picoded.dstack.jsql;

import java.util.logging.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import picoded.dstack.MetaTable;
import picoded.dstack.MetaObject;
import picoded.dstack.core.Core_AtomicLongMap;
import picoded.security.NxtCrypt;
import picoded.struct.GenericConvertMap;
import picoded.struct.GenericConvertHashMap;
import picoded.dstack.jsql.connector.*;
import picoded.set.JSqlType;
import picoded.conv.ListValueConv;
import picoded.conv.GenericConvert;

/**
* JSql implmentation of AtomicLongMap
**/
public class JSql_AtomicLongMap extends Core_AtomicLongMap {

	//--------------------------------------------------------------------------
	//
	// Constructor setup
	//
	//--------------------------------------------------------------------------

	/**
	* The inner sql object
	**/
	protected JSql sqlObj = null;

	/**
	* The tablename for the key value pair map
	**/
	protected String sqlTableName = null;

	/**
	* JSql setup
	*
	* @param   JSQL connection
	* @param   Table name to use
	**/
	public JSql_AtomicLongMap(JSql inJSql, String tablename) {
		super();
		sqlObj = inJSql;
		sqlTableName = "AL_"+tablename;
	}

	//--------------------------------------------------------------------------
	//
	// Internal config vars
	//
	//--------------------------------------------------------------------------

	/**
	* Primary key type
	**/
	protected String pKeyColumnType = "BIGINT PRIMARY KEY AUTOINCREMENT";

	/**
	* Timestamp field type
	**/
	protected String tStampColumnType = "BIGINT";

	/**
	* Key name field type
	**/
	protected String keyColumnType = "VARCHAR(64)";

	/**
	* Value field type
	**/
	protected String valueColumnType = "DECIMAL(36,12)";

	//--------------------------------------------------------------------------
	//
	// Backend system setup / teardown / maintenance (DStackCommon)
	//
	//--------------------------------------------------------------------------

	/**
	* Setsup the backend storage table, etc. If needed
	**/
	@Override
	public void systemSetup() {
		try {
			// Table constructor
			//-------------------
			sqlObj.createTable( //
				sqlTableName, //
				new String[] { //
					// Primary key, as classic int, this is used to lower SQL
					// fragmentation level, and index memory usage. And is not accessible.
					// Sharding and uniqueness of system is still maintained by meta keys
					"pKy", // primary key
					// Time stamps
					"uTm", //Updated timestamp
					// "cTm", //value created time
					// "eTm", //value expire time
					// Storage keys
					"kID", //
					// Value storage
					"kVl" //
				}, //
				new String[] { //
				pKeyColumnType, //Primary key
					// Time stamps
					tStampColumnType,
					// tStampColumnType,
					// tStampColumnType,
					// Storage keys
					keyColumnType, //
					// Value storage
					valueColumnType } //
				);

			// Unique index
			//------------------------------------------------
			sqlObj.createIndex( //
				sqlTableName, "kID", "UNIQUE", "unq" //
			);

			// Value search index
			//------------------------------------------------
			sqlObj.createIndex( //
				sqlTableName, "kVl", null, "valMap" //
			);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	* Teardown and delete the backend storage table, etc. If needed
	**/
	@Override
	public void systemDestroy() {
		sqlObj.dropTable( sqlTableName );
	}

	/**
	* Removes all data, without tearing down setup
	**/
	@Override
	public void clear() {
		sqlObj.delete( sqlTableName );
	}

	//--------------------------------------------------------------------------
	//
	// Utility functions
	//
	//--------------------------------------------------------------------------

	/**
	* Simplified upsert, used throughout this package
	*
	* @param  Key to use
	* @param  Value to store
	**/
	protected void simplifiedUpsert(Object key, Object newVal) throws JSqlException {
		long now = currentSystemTimeInSeconds();
		sqlObj.upsert( //
			sqlTableName, //
			new String[] { "kID" }, //unique cols
			new Object[] { key }, //unique value
			//
			new String[] { "uTm", "kVl" }, //insert cols
			new Object[] { now, GenericConvert.toLong(newVal,0) } //insert values
		);
	}

	//--------------------------------------------------------------------------
	//
	// Core put / get
	//
	//--------------------------------------------------------------------------

	/**
	* Stores (and overwrites if needed) key, value pair
	*
	* Important note: It does not return the previously stored value
	*
	* @param key as String
	* @param value as Long
	*
	* @return null
	**/
	@Override
	public Long put(Object key, Long value) {
		try {
			simplifiedUpsert(key, value);
		} catch (JSqlException e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	/**
	* Returns the value, given the key
	*
	* @param key param find the thae meta key
	*
	* @return  value of the given key
	**/
	@Override
	public Long get(Object key) {
		// Search for the key
		JSqlResult r = sqlObj.select(sqlTableName, "*", "kID = ?", new Object[] { key });
		if (r != null && r.rowCount() > 0) {
			return new Long(GenericConvert.toLong(r.get("kVl")[0]));
		}
		return null;
	}

	/**
	* Returns the value, given the key
	*
	* @param key param find the meta key
	* @param delta value to add
	*
	* @return  value of the given key
	**/
	@Override
	public Long getAndAdd(Object key, Object delta) {
		// Tries limits
		int limit = 100;
		int tries = 0;

		// Try 100 tries
		while (tries < limit) {

			// Get the "old" value
			JSqlResult r = sqlObj.select(sqlTableName, "*", "kID = ?", new Object[] { key });

			Long oldVal = null;
			if( r.get("kVl") != null && r.get("kVl").length > 0 && r.get("kVl")[0] != null ) {
				oldVal = GenericConvert.toLong( r.get("kVl")[0], 0 );
			} else {
				oldVal = 0l;
			}

			Long newVal = oldVal.longValue() + GenericConvert.toLong(delta,0);

			// If old value holds true, update to new value
			if (weakCompareAndSet(key.toString(), oldVal, newVal)) {
				return oldVal; //return old value on success
			}

			tries++;
		}

		throw new RuntimeException("Max tries reached : "+tries);
	}

	/**
	* Returns the number of records deleted, given the key
	*
	* Important note: If the key is not unique, all of its records will be deleted
	*
	* @param key param find the meta key
	* @return number of records deleted
	**/
	@Override
	public Long remove(Object key) {
		Integer resultDeleted = new Integer(sqlObj.delete(
			sqlTableName,
			"kID = ?",
			new Object[] { key }
		));
		return resultDeleted.longValue();
	}


	/**
	* Stores (and overwrites if needed) key, value pair
	*
	* Important note: It does not return the previously stored value
	*
	* @param key as String
	* @param value as long
	*
	* @return true if successful
	**/
	@Override
	public boolean weakCompareAndSet(String key, Long expect, Long update) {
		// Potentially a new upsert, ensure there is something to "delete" atleast
		if( expect == null || expect == 0l ) {
			// Does a blank upsert, with default values (No actual insert)
			long now = currentSystemTimeInSeconds();
			sqlObj.upsert( //
				sqlTableName, // unique key
				new String[] { "kID" }, //unique cols
				new Object[] { key }, //unique value
				// insert (ignore)
				null, null,
				// default value
				new String[] { "uTm", "kVl" }, //insert cols
				new Object[] { now, 0l }, //insert values
				// misc (ignore)
				null
			);

			// Expect is now atleast 0
			expect = 0l;
		}

		// Does the update from 0
		JSqlResult r = sqlObj.query("UPDATE " + sqlTableName + " SET kVl= ? WHERE kID = ? AND kVl = ?", update, key, expect);
		return (r.affectedRows() > 0);
	}
}
