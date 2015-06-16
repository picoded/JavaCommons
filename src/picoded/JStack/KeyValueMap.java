package picoded.JStack;

/// Java imports
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

/// Picoded imports
import picoded.conv.GUID;
import picoded.JSql.*;
import picoded.JCache.*;
import picoded.struct.CaseInsensitiveHashMap;
import picoded.struct.GenericConvertMap;

/// hazelcast
import com.hazelcast.core.*;
import com.hazelcast.config.*;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/// @TODO Documentation
public class KeyValueMap extends JStackData implements GenericConvertMap<String,String> {
	
	///
	/// Constructor setup
	///--------------------------------------------------------------------------
	
	/// Setup the metatable with the default table name
	public KeyValueMap(JStack inStack) {
		super( inStack );
		tableName = "KeyValueMap";
	}
	
	/// Setup the metatable with the given stack
	public KeyValueMap(JStack inStack, String inTableName) {
		super( inStack, inTableName );
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
	
	/// Is temp values only flag. This is used to indicate the stack should
	/// optimize purely to JCache if possible (JSql will be used if a Jcache isnt provided)
	protected boolean isTempValuesOnly = false;
	
	
	///
	/// Internal JSql table setup and teardown
	///--------------------------------------------------------------------------
	
	/// Setsup the respective JSql table
	@Override
	protected boolean JSqlSetup(JSql sql) throws JSqlException, JStackException {
		String tName = sqlTableName(sql);
		
		// Table constructor
		//-------------------
		sql.createTableQuerySet( //
										tName, //
										new String[] { //
											// Primary key, as classic int, htis is used to lower SQL
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
		sql.createTableIndexQuerySet( //
											  tName, "kID", "UNIQUE", "unq" //
											  ).execute();
		
		return true;
	}
	
	/// Removes the respective JSQL tables and view (if it exists)
	@Override
	protected boolean JSqlTeardown(JSql sql) throws JSqlException, JStackException {
		//sql.execute("DROP TABLE IF EXISTS " + sqlTableName(sql));
		return true;
	}
	
	///
	/// Utility functions used internally
	///--------------------------------------------------------------------------
	
	/// Gets the current system time in seconds
	protected long currentSystemTime_seconds() {
		return (System.currentTimeMillis())/1000L;
	}
	
	///
	/// Standard map operations
	///--------------------------------------------------------------------------
	
	/// Contains key operation.
	///
	/// note that boolean false can either mean no value or expired value
	///
	/// @param key as String
	/// @returns boolean true or false if the key exists
	public boolean containsKey(Object key) {
		if( key == null ) {
			throw new IllegalArgumentException("containKey cannot have null as parmeter");
		}
		
		try {
			long now = currentSystemTime_seconds();
			
			String kID = key.toString();
			Object r = JStackIterate( new JStackReader() {
				/// Reads only the JSQL layer
				public Object readJSqlLayer(JSql sql, Object ret) throws JSqlException, JStackException {
					if( ret != null ) {
						return ret;
					}
					
					String tName = sqlTableName(sql);
					
					// Search for the key
					JSqlResult r = sql.selectQuerySet( //
															 tName, //
															 "eTm", //
															 "kID=?", //
															 new Object[] { kID } //
															 ).query();
					// Has value
					if( r.rowCount() > 0 && r.containsKey("eTm") ) {
						Number t = (Number)(r.get("eTm").get(0));
						long l = t.longValue();
						
						// not expired yet?: its valid
						if( l == 0 || l <= now ) {
							return kID;
						}
					}
					
					return ret;
				}
			} );
			
			return (r != null);
		} catch (JStackException e) {
			throw new RuntimeException(e);
		}
	}
	
	/// Stores (and overwrites if needed) key, value pair
	///
	/// Important note: It does not return the previously stored value
	///
	/// @param key as String
	/// @param value as String
	///
	/// @returns null
	public String put(String key, String value) {
		return put(key, value, 0);
	}
	
	/// Stores (and overwrites if needed) key, value pair
	///
	/// Important note: It does not return the previously stored value
	///
	/// @param key as String
	/// @param value as String
	/// @param expireTime expire time stamp valu
	///
	/// @returns null
	public String put(String key, String value, long expireTime) {
		try {
			long now = currentSystemTime_seconds();
			
			Object ret = JStackIterate( new JStackReader() {
				/// Reads only the JSQL layer
				public Object readJSqlLayer(JSql sql, Object ret) throws JSqlException, JStackException {
					String tName = sqlTableName(sql);
					
					sql.upsertQuerySet( //
											 tName, //
											 new String[] { "kID" }, //unique cols
											 new Object[] { key }, //unique value
											 //
											 new String[] { "cTm", "eTm", "kVl" }, //insert cols
											 new Object[] { now, expireTime, value } //insert values
											 ).execute();
					
					return null;
				}
			} );
		} catch (JStackException e) {
			throw new RuntimeException(e);
		}
		
		return null;
	}
	
	/// Returns the value, given the key
	/// @param key param find the thae meta key
	///
	/// returns  value of the given key
	public String get(Object key) {
		try {
			long now = currentSystemTime_seconds();
			String kID = key.toString();
			
			Object ret = JStackIterate( new JStackReader() {
				/// Reads only the JSQL layer
				public Object readJSqlLayer(JSql sql, Object ret) throws JSqlException, JStackException {
					if( ret != null ) {
						return ret;
					}
					
					String tName = sqlTableName(sql);
					
					// Search for the key
					JSqlResult r = sql.selectQuerySet( //
																 tName, //
																 "*", //
																 "kID=?", //
																 new Object[] { kID } //
																 ).query();
					// Has value
					if( r.rowCount() > 0 && r.containsKey("eTm") ) {
						Number t = (Number)(r.get("eTm").get(0));
						long l = t.longValue();
						
						// not expired yet?: its valid
						if( l == 0 || l <= now ) {
							return r.get("kVl").get(0);
						}
					}
					
					return ret;
				}
			} );
			
			return ret.toString();
		} catch (JStackException e) {
			throw new RuntimeException(e);
		}
		//return null;
	}
	
	/// @TODO get expirary time (unix time)
	public long getExpireTime(Object key) {
		return 0L;
	}
	
	/// @TODO get expirary time left (seconds)
	public long getExpireSeconds(Object key) {
		return 0L;
	}
	
	/// @TODO set expirary time (unix time)
	public long setExpireTime(Object key, long time) {
		return 0L;
	}
	
	/// @TODO set expirary time (seconds)
	public long setExpireSeconds(Object key, long time) {
		return 0L;
	}
	
	/// Remove stored key, value pair
	/// @param key  whose value record to be deleted.
	///
	/// @returns null
	public String remove(Object key) {
		
		try {
			Object ret = JStackIterate( new JStackReader() {
				/// Reads only the JSQL layer
				public Object readJSqlLayer(JSql sql, Object ret) throws JSqlException, JStackException {
					String tName = sqlTableName(sql);
					
					sql.execute("DELETE FROM `" + tName + "` WHERE kID=?", key);
					return null;
				}
			} );
		} catch (JStackException e) {
			throw new RuntimeException(e);
		}
		
		return null;
	}
	
	/// Clears all the storage layer, without removing the
	public void clear() {
		try {
			Object ret = JStackIterate( new JStackReader() {
				/// Reads only the JSQL layer
				public Object readJSqlLayer(JSql sql, Object ret) throws JSqlException, JStackException {
					String tName = sqlTableName(sql);
					
					sql.execute("DELETE * FROM `" + tName + "`");
					return null;
				}
			} );
		} catch (JStackException e) {
			throw new RuntimeException(e);
		}
	}
	
	// Map interface compliance (that isnt actually supported)
	//-------------------------------------------------------------------------
	
	//*/
	
}