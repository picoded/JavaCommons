package picoded.JSql.struct;

import java.util.*;
import java.util.logging.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import picoded.struct.*;
import picoded.JSql.*;
import picoded.JSql.struct.internal.*;
import picoded.conv.*;
import picoded.struct.*;
import picoded.enums.*;
import picoded.JStruct.*;
import picoded.JStruct.internal.*;
import picoded.security.NxtCrypt;

import org.apache.commons.lang3.RandomUtils;

/// JSql implmentation of MetaTable
///
public class JSql_MetaTable extends JStruct_MetaTable {
	
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
	public JSql_MetaTable(JSql inJSql, String tablename) {
		super();
		sqlObj = inJSql;
		sqlTableName = tablename;
	}
	
	///
	/// Internal config vars
	///--------------------------------------------------------------------------
	
	/// Object ID field type
	protected String objColumnType = "VARCHAR(64)";
	
	/// Key name field type
	protected String keyColumnType = "VARCHAR(64)";
	
	/// Type collumn type
	protected String typeColumnType = "TINYINT";
	
	/// Index collumn type
	protected String indexColumnType = "TINYINT";
	
	/// String value field type
	/// @TODO: Investigate performance issues for this approach
	protected String numColumnType = "DECIMAL(36,12)";
	
	/// String value field type
	protected String strColumnType = "VARCHAR(64)";
	
	/// Full text value field type
	protected String fullTextColumnType = "VARCHAR(MAX)";
	
	/// Timestamp field type
	protected String tStampColumnType = "BIGINT";
	
	/// Primary key type
	protected String pKeyColumnType = "BIGINT PRIMARY KEY AUTOINCREMENT";
	
	/// Indexed view prefix, this is used to handle index conflicts between "versions" if needed
	protected String viewSuffix = "";
	
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
				// Primary key, as classic int, htis is used to lower SQL
				// fragmentation level, and index memory usage. And is not accessible.
				// Sharding and uniqueness of system is still maintained by GUID's
					"pKy", //
					// Time stamps
					"cTm", //value created time
					"uTm", //value updated time
					"oTm", //object created time
					"eTm", //value expire time (for future use)
					// Object keys
					"oID", //_oid
					"kID", //key storage
					"idx", //index collumn
					// Value storage (except text)
					"typ", //type collumn
					"nVl", //numeric value (if applicable)
					"sVl", //case insensitive string value (if applicable), or case sensitive hash
					// Text value storage
					"tVl" //Textual storage, placed last for storage optimization
				}, //
				new String[] { //
				pKeyColumnType, //Primary key
					// Time stamps
					tStampColumnType, //
					tStampColumnType, //
					tStampColumnType, //
					tStampColumnType, //
					// Object keys
					objColumnType, //
					keyColumnType, //
					indexColumnType, //
					// Value storage
					typeColumnType, //
					numColumnType, //
					strColumnType, //
					fullTextColumnType //
				} //
				).execute(); //
			
			// Unique index
			//
			// This also optimizes query by object keys
			//------------------------------------------------
			sqlObj.createTableIndexQuerySet( //
				sqlTableName, "oID, kID, idx", "UNIQUE", "unq" //
			).execute(); //
			
			// Key Values search index
			//------------------------------------------------
			sqlObj.createTableIndexQuerySet( //
				sqlTableName, "kID, nVl, sVl", null, "valMap" //
			).execute(); //
			
			// Object timestamp optimized Key Value indexe
			//------------------------------------------------
			sqlObj.createTableIndexQuerySet( //
				sqlTableName, "oTm, kID, nVl, sVl", null, "oTm_valMap" //
			).execute(); //
			
			// Full text index, for textual data
			// @TODO FULLTEXT index support
			//------------------------------------------------
			//if (sqlObj.sqlType != JSqlType.sqlite) {
			//	sqlObj.createTableIndexQuerySet( //
			//		tName, "tVl", "FULLTEXT", "tVl" //
			//	).execute();
			//} else {
			sqlObj.createTableIndexQuerySet( //
				sqlTableName, "tVl", null, "tVl" // Sqlite uses normal index
			).execute(); //
			//}
			
			//
			// timestamp index, is this needed?
			//
			// Currently commented out till a usage is found for them
			// This can be easily recommented in.
			//
			// Note that the main reason this is commented out is because
			// updated time and created time does not work fully as intended
			// as its is more of a system point of view. Rather then adminstration
			// point of view. 
			//
			// A good example is at times several fields in buisness logic is set
			// to NOT want to update the udpated time stamp of the object.
			//
			//------------------------------------------------
			
			//sqlObj.createTableIndexQuerySet( //
			//	tName, "uTm", null, "uTm" //
			//).execute();
			
			//sqlObj.createTableIndexQuerySet( //
			//	tName, "cTm", null, "cTm" //
			//).execute();
		} catch (JSqlException e) {
			throw new RuntimeException(e);
		}
	}
	
	/// Teardown and delete the backend storage table, etc. If needed
	public void systemTeardown() {
		try {
			sqlObj.execute("DROP TABLE IF EXISTS " + sqlTableName); //IF EXISTS
		} catch (JSqlException e) {
			logger.log(Level.SEVERE, "systemTeardown JSqlException (@TODO properly handle this): ", e);
		}
	}
	
	// MetaObject MAP operations
	//----------------------------------------------
	
	// /// Gets the MetaObject, if it exists
	// public MetaObject get(Object _oid) {
	// 	try {
	// 		_accessLock.readLock().lock();
	// 		
	// 		String oid = _oid.toString();
	// 		Map<String,Object> fullMap = metaObjectRemoteDataMap_get(oid);
	// 		
	// 		if( fullMap == null ) {
	// 			return null;
	// 		}
	// 		return new JStruct_MetaObject(this, oid, fullMap, true);
	// 	} finally {
	// 		_accessLock.readLock().unlock();
	// 	}
	// }
	
	/// Gets the full keySet
	public Set<String> keySet() {
		try {
			JSqlResult r = sqlObj.selectQuerySet(sqlTableName, "oID").query();
			if (r == null || r.get("oID") == null) {
				return new HashSet<String>();
			}
			
			return ListValueConv.toStringSet(r.get("oID"));
		} catch (JSqlException e) {
			throw new RuntimeException(e);
		}
	}
	
	/// Remove the node
	public MetaObject remove(Object key) {
		try {
			String keyID = key.toString();
			sqlObj.execute("DELETE FROM `" + sqlTableName + "` WHERE oID = ?", keyID);
		} catch (JSqlException e) {
			throw new RuntimeException(e);
		}
		return null;
	}
	
	// MetaObject operations
	//----------------------------------------------
	
	// /// Generates a new blank object, with a GUID
	// public MetaObject newObject() {
	// 	MetaObject ret = new JStruct_MetaObject(this, null, null, false);
	// 	return ret;
	// }
	
	// /// Gets the MetaObject, regardless of its actual existance
	// public MetaObject uncheckedGet(String _oid) {
	// 	return new JStruct_MetaObject(this, _oid, null, false);
	// }
	
	// /// PUT, returns the object ID (especially when its generated), note that this
	// /// adds the value in a merger style. Meaning for example, existing values not explicitely
	// /// nulled or replaced are maintained
	// public MetaObject append(String _oid, Map<String, Object> obj) {
	// 	
	// 	/// Appending a MetaObject is equivalent of saveDelta
	// 	MetaObject r = null;
	// 	if (obj instanceof MetaObject && ((MetaObject) obj)._oid().equals(_oid)) {
	// 		(r = (MetaObject) obj).saveDelta();
	// 		return r;
	// 	}
	// 	
	// 	/// Unchecked get, put, and save
	// 	r = uncheckedGet(_oid);
	// 	r.putAll(obj);
	// 	r.saveDelta();
	// 	
	// 	return r;
	// }
	
	//
	// Internal functions, used by MetaObject
	//--------------------------------------------------------------------------
	
	/// Gets the complete remote data map, for MetaObject.
	/// Returns null
	protected Map<String, Object> metaObjectRemoteDataMap_get(String _oid) {
		return JSql_MetaTableUtils.JSqlObjectMapFetch(typeMap(), sqlObj, sqlTableName, _oid, null);
	}
	
	/// Updates the actual backend storage of MetaObject 
	/// either partially (if supported / used), or completely
	protected void metaObjectRemoteDataMap_update(String _oid, Map<String, Object> fullMap, Set<String> keys) {
		try {
			JSql_MetaTableUtils.JSqlObjectMapAppend(typeMap(), sqlObj, sqlTableName, _oid, fullMap, keys, true);
		} catch (JSqlException e) {
			throw new RuntimeException(e);
		}
	}
	
	/// 
	/// MetaType handling, does type checking and conversion
	///--------------------------------------------------------------------------
	
	// /// Gets and return the internal MetaTypeMap
	// public MetaTypeMap typeMap() {
	// 	return _typeMap;
	// }
	
	/// 
	/// Query based optimization
	///--------------------------------------------------------------------------
	
	/// Performs a search query, and returns the respective MetaObjects
	///
	/// CURRENTLY: It is entirely dependent on the whereValues object type to perform the relevent search criteria
	/// @TODO: Performs the search pattern using the respective type map
	///
	/// @param   where query statement
	/// @param   where clause values array
	/// @param   query string to sort the order by, use null to ignore
	/// @param   offset of the result to display, use -1 to ignore
	/// @param   number of objects to return max
	///
	/// @returns  The MetaObject[] array
	public MetaObject[] query(String whereClause, Object[] whereValues, String orderByStr, int offset, int limit) {
		return JSql_MetaTableUtils.metaTableQuery(this, sqlObj, sqlTableName, whereClause, whereValues, orderByStr,
			offset, limit);
		//return super.query( whereClause, whereValues, orderByStr, offset, limit );
	}
	
	/// Performs a search query, and returns the respective MetaObjects
	///
	/// @param   where query statement
	/// @param   where clause values array
	///
	/// @returns  The total count for the query
	public long queryCount(String whereClause, Object[] whereValues) {
		return JSql_MetaTableUtils.metaTableCount(this, sqlObj, sqlTableName, whereClause, whereValues, null, -1, -1);
	}
	
}
