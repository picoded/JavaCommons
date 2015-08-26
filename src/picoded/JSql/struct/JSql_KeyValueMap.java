package picoded.JSql.struct;

import java.util.*;
import java.util.logging.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import picoded.struct.*;
import picoded.JSql.*;
import picoded.conv.*;
import picoded.JStruct.*;
import picoded.JStruct.internal.*;
import picoded.security.NxtCrypt;

import org.apache.commons.lang3.RandomUtils;

/// JSql implmentation of KeyValueMap
public class JSql_KeyValueMap extends JStruct_KeyValueMap {
	
	///
	/// Temporary logger used to make sure incomplete implmentation is noted
	///--------------------------------------------------------------------------
	
	/// Standard java logger
	protected static Logger logger = Logger.getLogger(JSql_KeyValueMap.class.getName());
	
	///
	/// Constructor setup
	///--------------------------------------------------------------------------

	/// The inner sql object
	protected JSql sqlObj = null;
	
	/// The tablename for the key value pair map
	protected String sqlTableName = null;
	
	/// JSql setup 
	public JSql_KeyValueMap(JSql inJSql, String tablename) {
		super();
		sqlObj = inJSql;
		sqlTableName = tablename;
	}
	
	///
	/// Internal config vars
	///--------------------------------------------------------------------------
	
	/// Primary key type
	protected String pKeyColumnType = "BIGINT PRIMARY KEY AUTOINCREMENT";

	/// Timestamp field type
	protected String tStampColumnType = "BIGINT";

	/// Key name field type
	protected String keyColumnType = "VARCHAR(64)";

	/// Value field type
	protected String valueColumnType = "VARCHAR(MAX)";
	
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
												tStampColumnType,
												tStampColumnType,
												// Storage keys
												keyColumnType, //
												// Value storage
												valueColumnType
											} //
			).execute();

			// Unique index
			//------------------------------------------------
			sqlObj.createTableIndexQuerySet( //
												  sqlTableName, "kID", "UNIQUE", "unq" //
												  ).execute();

			// Value search index
			//------------------------------------------------
			sqlObj.createTableIndexQuerySet( //
												  sqlTableName, "kVl", null, "valMap" //
												  ).execute();
		} catch( JSqlException e ) {
			throw new RuntimeException(e);
		}
	}
	
	/// Teardown and delete the backend storage table, etc. If needed
	///
	/// @TODO properly handle this: Especially adding (and testing) the IF EXISTS clause
	public void systemTeardown() {
		try {
			sqlObj.execute("DROP TABLE IF EXISTS " + sqlTableName); //IF EXISTS
		} catch( JSqlException e ) {
			logger.log(Level.SEVERE, "systemTeardown JSqlException (@TODO properly handle this): ", e);
		}
	}
	
	/// perform increment maintenance, meant for minor changes between requests
	public void incrementalMaintenance() {
		// 5 percent chance of trigering maintenance
		// This is to lower to overall performance cost incrementalMaintenance per request
		if( RandomUtils.nextInt(0,100) <= 5 ) {
			maintenance();
		}
	}
	
	/// Perform maintenance, mainly removing of expired data if applicable
	public void maintenance() {
		try {
			long now = currentSystemTimeInSeconds();
			sqlObj.execute("DELETE FROM `" + sqlTableName + "` WHERE eTm <= ?", now);
		} catch (JSqlException e) {
			throw new RuntimeException(e);
		}
	}
	
	///
	/// Expiration and lifespan handling (to override)
	///--------------------------------------------------------------------------
	
	/// Gets the expire time from the JSqlResult
	protected long getExpiryRaw(JSqlResult r) throws JSqlException {
		// Search for the key
		Object rawTime = null;
		
		// Has value
		if( r != null && r.rowCount() > 0 ) {
			rawTime = r.get("eTm").get(0);
		} else {
			return -1; //No value (-1)
		}
		
		long ret = 0;
		if (rawTime != null) {
			if( rawTime instanceof Number ) {
				ret = ((Number)rawTime).longValue();
			} else {
				ret = (Long.parseLong(rawTime.toString()));
			}
		}
		
		if( ret <= 0 ) {
			return 0;
		} else {
			return ret;
		}
	}
	
	/// [Internal use, to be extended in future implementation]
	/// Returns the expire time stamp value, raw without validation
	///
	/// Handles re-entrant lock where applicable
	///
	/// @param key as String
	///
	/// @returns long
	protected long getExpiryRaw(String key) {
		try {
			// Search for the key
			JSqlResult r = sqlObj.selectQuerySet(sqlTableName, "eTm", "kID=?", new Object[] { key }).query();
			return getExpiryRaw(r);
		} catch( JSqlException e ) {
			throw new RuntimeException(e);
		}
	}
	
	/// [Internal use, to be extended in future implementation]
	/// Sets the expire time stamp value, raw without validation
	///
	/// Handles re-entrant lock where applicable
	///
	/// @param key as String
	///
	/// @returns long
	protected void setExpiryRaw(String key, long time) {
		try {
			sqlObj.execute("UPDATE " + sqlTableName + " SET eTm=? WHERE kID = ?", time, key);
		} catch( JSqlException e ) {
			throw new RuntimeException(e);
		}

		return;
	}
	
	///
	/// put, get, etc (to override)
	///--------------------------------------------------------------------------
	
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
		try {
			// Search for the key
			JSqlResult r = sqlObj.selectQuerySet(sqlTableName, "*", "kID=?", new Object[] { key }).query();
			long expiry = getExpiryRaw(r);
			
			if( expiry != 0 && expiry < now ) {
				return null;
			}
			
			return r.get("kVl").get(0).toString();
		} catch( JSqlException e ) {
			throw new RuntimeException(e);
		}
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
		try {
			long now = currentSystemTimeInSeconds();
			sqlObj.upsertQuerySet( //
									 sqlTableName, //
									 new String[] { "kID" }, //unique cols
									 new Object[] { key }, //unique value
									 //
									 new String[] { "cTm", "eTm", "kVl" }, //insert cols
									 new Object[] { now, expire, value } //insert values
									 ).execute();
									 
		} catch (JSqlException e) {
			throw new RuntimeException(e);
		}
		return null;
	}
	
	/// Search using the value, all the relevent key mappings
	///
	/// Handles re-entrant lock where applicable
	///
	/// @param key, note that null matches ALL
	///
	/// @returns array of keys
	public Set<String> getKeys(String value) {
		try {
			long now = currentSystemTimeInSeconds();
			JSqlResult r = null;
			if( value == null ) {
				r = sqlObj.selectQuerySet(sqlTableName, "kID", "eTm <= ? OR eTm > ?", new Object[] { 0, now }).query();
			} else {
				r = sqlObj.selectQuerySet(sqlTableName, "kID", "kVl = ? AND (eTm <= ? OR eTm > ?)", new Object[] { value, 0, now }).query();
			}
			
			if( r == null || r.get("kID") == null ) {
				return new HashSet<String>();
			}
			
			return ListValueConv.toStringSet( r.get("kID") );
		} catch(JSqlException e) {
			throw new RuntimeException(e);
		}
	}
	
	/// Remove the value, given the key
	///
	/// @param key param find the thae meta key
	///
	/// @returns  null
	public String remove(Object key) {
		try {
			String keyName = key.toString();
			sqlObj.execute("DELETE FROM `" + sqlTableName + "` WHERE kID = ?", keyName);
		} catch (JSqlException e) {
			throw new RuntimeException(e);
		}
		return null;
	}

}