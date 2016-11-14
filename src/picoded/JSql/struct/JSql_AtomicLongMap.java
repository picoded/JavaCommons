package picoded.JSql.struct;

import java.util.logging.Level;
import java.util.logging.Logger;

import picoded.JSql.JSql;
import picoded.JSql.JSqlException;
import picoded.JSql.JSqlResult;
import picoded.JStruct.internal.JStruct_AtomicLongMap;
import picoded.enums.JSqlType;

/// JSql implmentation of KeyValueMap
public class JSql_AtomicLongMap extends JStruct_AtomicLongMap {
	
	///
	/// Temporary logger used to make sure incomplete implmentation is noted
	///--------------------------------------------------------------------------
	
	/// Standard java logger
	public static Logger logger = Logger.getLogger(JSql_AtomicLongMap.class.getName());
	
	///
	/// Constructor setup
	///--------------------------------------------------------------------------
	
	/// The inner sql object
	public JSql sqlObj = null;
	
	/// The tablename for the key value pair map
	public String sqlTableName = null;
	
	/// JSql setup
	public JSql_AtomicLongMap(JSql inJSql, String tablename) {
		super();
		sqlObj = inJSql;
		sqlTableName = tablename;
	}
	
	///
	/// Internal config vars
	///--------------------------------------------------------------------------
	
	/// Primary key type
	public String pKeyColumnType = "BIGINT PRIMARY KEY AUTOINCREMENT";
	
	/// Timestamp field type
	public String tStampColumnType = "BIGINT";
	
	/// Key name field type
	public String keyColumnType = "VARCHAR(64)";
	
	/// Value field type
	public String valueColumnType = "VARCHAR(MAX)";
	
	///
	/// Backend system setup / teardown
	///--------------------------------------------------------------------------
	
	/// Setsup the backend storage table, etc. If needed
	public void systemSetup() {
		try {
			// Table constructor
			//-------------------
			sqlObj.createTableQuerySet( //
				sqlTableName, //
				new String[] { //
				// Primary key, as classic int, this is used to lower SQL
				// fragmentation level, and index memory usage. And is not accessible.
				// Sharding and uniqueness of system is still maintained by meta keys
					"pKy", // primary key
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
					valueColumnType } //
				).execute();
			
			// Unique index
			//------------------------------------------------
			sqlObj.createTableIndexQuerySet( //
				sqlTableName, "kID", "UNIQUE", "unq" //
			).execute();
			
			// Value search index
			//------------------------------------------------
			if (sqlObj.sqlType == JSqlType.MYSQL) {
				sqlObj.createTableIndexQuerySet( //
					sqlTableName, "kVl(255)", null, "valMap" //
				).execute();
			} else {
				sqlObj.createTableIndexQuerySet( //
					sqlTableName, "kVl", null, "valMap" //
				).execute();
			}
		} catch (JSqlException e) {
			throw new RuntimeException(e);
		}
	}
	
	/// Teardown and delete the backend storage table, etc. If needed
	///
	/// @TODO properly handle this: Especially adding (and testing) the IF EXISTS clause
	public void systemTeardown() {
		try {
			sqlObj.execute("DROP TABLE IF EXISTS " + sqlTableName); //IF EXISTS
		} catch (JSqlException e) {
			logger.log(Level.SEVERE, "systemTeardown JSqlException (@TODO properly handle this): ", e);
		}
	}
	
	/// Perform maintenance, mainly removing of expired data if applicable
	public void maintenance() {
		try {
			long now = currentSystemTimeInSeconds();
			sqlObj.execute("DELETE FROM `" + sqlTableName + "` WHERE eTm <= ? AND eTm > ?", now, 0);
		} catch (JSqlException e) {
			throw new RuntimeException(e);
		}
	}
	
	//
	// put, get, etc (public)
	//--------------------------------------------------------------------------
	
	/// Stores (and overwrites if needed) key, value pair
	///
	/// Important note: It does not return the previously stored value
	///
	/// @param key as String
	/// @param value as Number
	///
	/// @returns null
	public Long put(String key, Number value) {
		
		try {
			long now = currentSystemTimeInSeconds();
			sqlObj.upsertQuerySet( //
				sqlTableName, //
				new String[] { "kID" }, //unique cols
				new Object[] { key }, //unique value
				//
				new String[] { "cTm", "kVl" }, //insert cols
				new Object[] { now, value } //insert values
				).execute();
			
		} catch (JSqlException e) {
			throw new RuntimeException(e);
		}
		return null;
	}
	
	/// Stores (and overwrites if needed) key, value pair
	///
	/// Important note: It does not return the previously stored value
	///
	/// @param key as String
	/// @param value as long
	///
	/// @returns null
	public Long put(String key, long value) {
		
		try {
			long now = currentSystemTimeInSeconds();
			sqlObj.upsertQuerySet( //
				sqlTableName, //
				new String[] { "kID" }, //unique cols
				new Object[] { key }, //unique value
				//
				new String[] { "cTm", "kVl" }, //insert cols
				new Object[] { now, value } //insert values
				).execute();
			
		} catch (JSqlException e) {
			throw new RuntimeException(e);
		}
		return null;
	}
	
	/// Stores (and overwrites if needed) key, value pair
	///
	/// Important note: It does not return the previously stored value
	///
	/// @param key as String
	/// @param value as Long
	///
	/// @returns null
	public Long put(String key, Long value) {
		
		try {
			long now = currentSystemTimeInSeconds();
			sqlObj.upsertQuerySet( //
				sqlTableName, //
				new String[] { "kID" }, //unique cols
				new Object[] { key }, //unique value
				//
				new String[] { "cTm", "kVl" }, //insert cols
				new Object[] { now, value } //insert values
				).execute();
			
		} catch (JSqlException e) {
			throw new RuntimeException(e);
		}
		return null;
	}
	
	/// Returns the value, given the key
	/// @param key param find the thae meta key
	///
	/// @returns  value of the given key
	public Long get(Object key) {
		
		try {
			// Search for the key
			JSqlResult r = sqlObj.selectQuerySet(sqlTableName, "*", "kID=?", new Object[] { key })
				.query();
			if (r != null && r.rowCount() > 0) {
				return Long.parseLong(r.get("kVl").get(0).toString());
			}
			return null;
		} catch (JSqlException e) {
			throw new RuntimeException(e);
		}
	}
	
	/// Returns the value, given the key
	/// @param key param find the meta key
	/// @param delta value to add
	///
	/// @returns  value of the given key
	public Long getAndAdd(Object key, Object delta) {
		
		try {
			int limit = 100;
			int tries = 0;
			Long oldVal = null;
			while (tries < limit) {
				
				JSqlResult r = sqlObj.selectQuerySet(sqlTableName, "*", "kID=?", new Object[] { key })
					.query();
				
				oldVal = Long.parseLong(r.get("kVl").get(0).toString());
				Long newVal = oldVal + (Long) delta;
				
				if (weakCompareAndSet(key.toString(), oldVal, newVal)) {
					return oldVal;
				}
				
				tries++;
			}
			
			throw new RuntimeException("Max tries reached.");
			// return oldVal;
			// Search for the key
			// JSqlResult r = sqlObj.selectQuerySet(sqlTableName, "*", "kID=?", new Object[] { key }).query();
			// long expiry = getExpiryRaw(r);
			//
			// if (expiry != 0 && expiry < now) {
			// 	return null;
			// }
			
			// Long oldVal = Long.parseLong(r.get("kVl").get(0).toString());
			// Long newVal = oldVal + (Long)delta;
			//
			// long now = currentSystemTimeInSeconds();
			// sqlObj.upsertQuerySet( //
			// 	sqlTableName, //
			// 	new String[] { "kID" }, //unique cols
			// 	new Object[] { key }, //unique value
			// 	//
			// 	new String[] { "cTm", "kVl" }, //insert cols
			// 	new Object[] { now, newVal } //insert values
			// 	).execute();
			//
			// return oldVal;
		} catch (JSqlException e) {
			throw new RuntimeException(e);
		}
	}
	
	/// Returns the value, given the key
	/// @param key param find the meta key
	/// @param delta value to add
	///
	/// @returns  value of the given key
	public Long getAndIncrement(Object key) {
		
		try {
			// Search for the key
			JSqlResult r = sqlObj.selectQuerySet(sqlTableName, "*", "kID=?", new Object[] { key })
				.query();
			// long expiry = getExpiryRaw(r);
			//
			// if (expiry != 0 && expiry < now) {
			// 	return null;
			// }
			
			Long oldVal = Long.parseLong(r.get("kVl").get(0).toString());
			Long newVal = oldVal + 1;
			
			long now = currentSystemTimeInSeconds();
			sqlObj.upsertQuerySet( //
				sqlTableName, //
				new String[] { "kID" }, //unique cols
				new Object[] { key }, //unique value
				//
				new String[] { "cTm", "kVl" }, //insert cols
				new Object[] { now, newVal } //insert values
				).execute();
			
			return oldVal;
		} catch (JSqlException e) {
			throw new RuntimeException(e);
		}
	}
	
	/// Returns the value, given the key
	/// @param key param find the meta key
	/// @param delta value to add
	///
	/// @returns  value of the given key after adding
	public Long incrementAndGet(Object key) {
		try {
			// Search for the key
			JSqlResult r = sqlObj.selectQuerySet(sqlTableName, "*", "kID=?", new Object[] { key })
				.query();
			// long expiry = getExpiryRaw(r);
			//
			// if (expiry != 0 && expiry < now) {
			// 	return null;
			// }
			
			Long oldVal = Long.parseLong(r.get("kVl").get(0).toString());
			Long newVal = oldVal + 1;
			
			long now = currentSystemTimeInSeconds();
			sqlObj.upsertQuerySet( //
				sqlTableName, //
				new String[] { "kID" }, //unique cols
				new Object[] { key }, //unique value
				//
				new String[] { "cTm", "kVl" }, //insert cols
				new Object[] { now, newVal } //insert values
				).execute();
			
			r = sqlObj.selectQuerySet(sqlTableName, "*", "kID=?", new Object[] { key }).query();
			
			return Long.parseLong(r.get("kVl").get(0).toString());
		} catch (JSqlException e) {
			throw new RuntimeException(e);
		}
	}
	
	/// Stores (and overwrites if needed) key, value pair
	///
	/// Important note: It does not return the previously stored value
	///
	/// @param key as String
	/// @param value as long
	///
	/// @returns true if successful
	public boolean weakCompareAndSet(String key, Long expect, Long update) {
		
		try {
			// Search for the key
			JSqlResult r = sqlObj.selectQuerySet(sqlTableName, "*", "kID=? AND kVl=?",
				new Object[] { key, expect }).query();
			
			Long oldVal = Long.parseLong(r.get("kVl").get(0).toString());
			
			//if no values inside
			if (oldVal == null || r.rowCount() == 0) {
				if (oldVal.equals(expect)) {
					return sqlObj.execute("INSERT INTO " + sqlTableName + "(kID, kVl) VALUES (?,?)",
						key, update);
				} else {
					return false;
				}
			} else {
				if (oldVal.equals(expect)) {
					return sqlObj.execute("UPDATE " + sqlTableName
						+ " SET kVl= ? WHERE kID = ? AND kVl = ?", update, key, expect);
				} else {
					return false;
				}
			}
		} catch (JSqlException e) {
			throw new RuntimeException(e);
		}
	}
	
}