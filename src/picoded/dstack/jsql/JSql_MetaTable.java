package picoded.dstack.jsql;

import java.util.logging.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import picoded.dstack.MetaTable;
import picoded.dstack.core.Core_MetaTable;
import picoded.security.NxtCrypt;
import picoded.struct.GenericConvertMap;
import picoded.struct.GenericConvertHashMap;
import picoded.dstack.jsql.connector.*;
import picoded.set.JSqlType;
import picoded.conv.ListValueConv;

/// JSql implmentation of MetaTable
///
public class JSql_MetaTable extends Core_MetaTable {
	
	//--------------------------------------------------------------------------
	//
	// Constructor setup
	//
	//--------------------------------------------------------------------------
	
	// The inner sql object
	protected JSql sqlObj = null;
	
	// The tablename for the key value pair map
	protected String sqlTableName = null;
	
	// JSql setup 
	public JSql_MetaTable(JSql inJSql, String tablename) {
		super();
		sqlObj = inJSql;
		sqlTableName = tablename;
	}
	
	//--------------------------------------------------------------------------
	//
	// Internal config vars
	//
	//--------------------------------------------------------------------------
	
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

	/// Raw datastorage type
	protected String rawDataColumnType = "BLOB";
	
	/// Indexed view prefix, this is used to handle index conflicts between "versions" if needed
	protected String viewSuffix = "";
	
	//--------------------------------------------------------------------------
	//
	// Backend system setup / teardown / maintenance (DStackCommon)
	//
	//--------------------------------------------------------------------------
	
	/// Setsup the backend storage table, etc. If needed
	@Override
	public void systemSetup() {
		
		// Table constructor
		//-------------------
		sqlObj.createTable( //
			sqlTableName, //
			new String[] { //
			// Primary key, as classic int, this is used to lower SQL
			// fragmentation level, and index memory usage. And is not accessible.
			// Sharding and uniqueness of system is still maintained by GUID's
				"pKy", //
				// Time stamps
				"cTm", //value created time
				"uTm", //value updated time
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
				"tVl", //Textual storage, placed last for row storage optimization
				"rVl" //Raw binary storage, placed last for row storage optimization
			}, //
			new String[] { //
			pKeyColumnType, //Primary key
				// Time stamps
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
				fullTextColumnType, //
				rawDataColumnType
			} //
		);
			
		// Unique index
		//
		// This also optimizes query by object keys
		//------------------------------------------------
		sqlObj.createIndex( //
			sqlTableName, "oID, kID, idx", "UNIQUE", "unq" //
		); //
			
		// Key Values search index
		//------------------------------------------------
		
		//
		// Note as this checks nVl also, 
		// its IMPORTANT that nVl = 0 is passed
		// For string based search
		//
		sqlObj.createIndex( //
			sqlTableName, "kID, nVl, sVl", null, "valMap" //
		); //
		
		// Full text index, for textual data
		// @TODO FULLTEXT index support
		//------------------------------------------------
		//if (sqlObj.sqlType != JSqlType.sqlite) {
		//	sqlObj.createIndex( //
		//		tName, "tVl", "FULLTEXT", "tVlT" //
		//	);
		//} else {
		// sqlObj.createIndex( //
		// 	sqlTableName, "tVl", null, "tVlI" // Sqlite uses normal index
		// ); //
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
		// to NOT want to update the updated time stamp of the object.
		//
		//------------------------------------------------
		
		// // By created time
		// sqlObj.createIndex( //
		// 	sqlTableName, "cTm, kID, nVl, sVl", null, "cTm_valMap" //
		// ); //

		// // By updated time
		// sqlObj.createIndex( //
		// 	sqlTableName, "uTm, kID, nVl, sVl", null, "uTm_valMap" //
		// ); //
			
		//sqlObj.createIndex( //
		//	tName, "uTm", null, "uTm" //
		//);
		
		//sqlObj.createIndex( //
		//	tName, "cTm", null, "cTm" //
		//);
	}
	
	/// Teardown and delete the backend storage table, etc. If needed
	public void systemDestroy() {
		sqlObj.dropTable(sqlTableName);
	}
	
	///
	/// Removes all data, without tearing down setup
	///
	@Override
	public void clear() {
		sqlObj.delete(sqlTableName);
	}
	
	//--------------------------------------------------------------------------
	//
	// Internal functions, used by MetaObject
	//
	//--------------------------------------------------------------------------
	
	/// [Internal use, to be extended in future implementation]
	///
	/// Removes the complete remote data map, for MetaObject.
	/// This is used to nuke an entire object
	///
	/// @param  Object ID to remove
	///
	/// @return  nothing
	protected void metaObjectRemoteDataMap_remove(String oid) {
		sqlObj.delete(
			sqlTableName,
			"oID = ?",
			new Object[] { oid }
		);
	}
	
	/// Gets the complete remote data map, for MetaObject.
	/// Returns null if not exists
	protected Map<String, Object> metaObjectRemoteDataMap_get(String _oid) {
		//return JSql_MetaTableUtils.JSqlObjectMapFetch(typeMap(), sqlObj, sqlTableName, _oid, null);
		return null;
	}
	
	/// Updates the actual backend storage of MetaObject
	/// either partially (if supported / used), or completely
	protected void metaObjectRemoteDataMap_update(String oid, Map<String, Object> fullMap,
		Set<String> keys) {
		// JSql_MetaTableUtils.JSqlObjectMapAppend(typeMap(), sqlObj, sqlTableName, _oid, fullMap,
		// 	keys, true);
	}

	//--------------------------------------------------------------------------
	//
	// KeySet support
	//
	//--------------------------------------------------------------------------

	/// Get and returns all the GUID's, note that due to its 
	/// potential of returning a large data set, production use
	/// should be avoided.
	///
	/// @returns set of keys
	@Override
	public Set<String> keySet() {
		return null;
	}

	/*
	
	
	// MetaObject MAP operations
	//----------------------------------------------
	
	/// Gets the full keySet
	@Override
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
	@Override
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
	@Override
	public MetaObject[] query(String whereClause, Object[] whereValues, String orderByStr,
		int offset, int limit) {
		return JSql_MetaTableUtils.metaTableQuery(this, sqlObj, sqlTableName, whereClause,
			whereValues, orderByStr, offset, limit);
		//return super.query( whereClause, whereValues, orderByStr, offset, limit );
	}
	
	/// Performs a search query, and returns the respective MetaObjects keys
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
	/// @returns  The String[] array
	@Override
	public String[] queryKeys(String whereClause, Object[] whereValues, String orderByStr,
		int offset, int limit) {
		return JSql_MetaTableUtils.metaTableQueryKey(this, sqlObj, sqlTableName, whereClause,
			whereValues, orderByStr, offset, limit);
		//return super.query( whereClause, whereValues, orderByStr, offset, limit );
	}
	
	/// Performs a search query, and returns the respective MetaObjects
	///
	/// @param   where query statement
	/// @param   where clause values array
	///
	/// @returns  The total count for the query
	@Override
	public long queryCount(String whereClause, Object[] whereValues) {
		return JSql_MetaTableUtils.metaTableCount(this, sqlObj, sqlTableName, whereClause,
			whereValues, null, -1, -1);
	}
	
	/// 
	/// Get key names handling
	///--------------------------------------------------------------------------
	
	/// Scans the object and get the various keynames used. 
	/// This is used mainly in adminstration interface, etc.
	///
	/// The seekDepth parameter is ignored in JSql mode, as its optimized.
	///
	/// @param  seekDepth, which detirmines the upper limit for iterating
	///         objects for the key names, use -1 to search all
	///
	/// @returns  The various key names used in the objects
	///
	@Override
	public Set<String> getKeyNames(int seekDepth) {
		try {
			JSqlResult r = sqlObj.selectQuerySet(sqlTableName, "DISTINCT kID").query();
			if (r == null || r.get("kID") == null) {
				return new HashSet<String>();
			}
			
			return ListValueConv.toStringSet(r.get("kID"));
		} catch (JSqlException e) {
			throw new RuntimeException(e);
		}
	}
	*/
}
