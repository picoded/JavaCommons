package picoded.JSql.struct.internal;

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
import picoded.JStack.*;
import picoded.struct.CaseInsensitiveHashMap;
import picoded.struct.UnsupportedDefaultMap;

///
/// Protected class, used to orgainze the various JSql based logic
/// used in MetaTable. 
///
/// The larger intention is to keep the MetaTable class more maintainable and unit testable
///
public class MetaTableJSql_query {
	
	///
	/// Setsup the JSql main data storage table for MetaTable. Using the colTypes configured
	/// 
	/// @param {JSql} sql            - sql connection to setup the table
	/// @param {String} tName        - table name to setup, this holds the actual meta table data
	/// @param {MetaTableJSql_columnTypes} colTypes - main MetaTable, where this function shoudl be called from
	/// 
	public static void JSqlSetup(JSql sql, String tName, MetaTableJSql_columnTypes colTypes) throws JSqlException {
		if( colTypes == null ) {
			colTypes = new MetaTableJSql_columnTypes();
		}
		
		// Table constructor
		//-------------------
		sql.createTableQuerySet( //
										tName, //
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
											colTypes.pKeyColumnType, //Primary key
											// Time stamps
											colTypes.tStampColumnType, //
											colTypes.tStampColumnType, //
											colTypes.tStampColumnType, //
											colTypes.tStampColumnType, //
											// Object keys
											colTypes.objColumnType, //
											colTypes.keyColumnType, //
											colTypes.indexColumnType, //
											// Value storage
											colTypes.typeColumnType, //
											colTypes.numColumnType, //
											colTypes.strColumnType, //
											colTypes.fullTextColumnType //
										} //
										).execute(); //
	
		// Unique index
		//
		// This also optimizes query by object keys
		//------------------------------------------------
		sql.createTableIndexQuerySet( //
											  tName, "oID, kID, idx", "UNIQUE", "unq" //
											  ).execute(); //
	
		// Key Values search index
		//------------------------------------------------
		sql.createTableIndexQuerySet( //
											  tName, "kID, nVl, sVl", null, "valMap" //
											  ).execute(); //
	
		// Object timestamp optimized Key Value indexe
		//------------------------------------------------
		sql.createTableIndexQuerySet( //
											  tName, "oTm, kID, nVl, sVl", null, "oTm_valMap" //
											  ).execute(); //
	
		// Full text index, for textual data
		// @TODO FULLTEXT index support
		//------------------------------------------------
		//if (sql.sqlType != JSqlType.sqlite) {
		//	sql.createTableIndexQuerySet( //
		//		tName, "tVl", "FULLTEXT", "tVl" //
		//	).execute();
		//} else {
		sql.createTableIndexQuerySet( //
											  tName, "tVl", null, "tVl" // Sqlite uses normal index
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
		
		//sql.createTableIndexQuerySet( //
		//	tName, "uTm", null, "uTm" //
		//).execute();
	
		//sql.createTableIndexQuerySet( //
		//	tName, "cTm", null, "cTm" //
		//).execute();
	}
	
	// /// Values to option set conversion
	// ///
	// /// @TODO: Support the various numeric value
	// /// @TODO: Support string / text
	// /// @TODO: Support array sets
	// /// @TODO: Support GUID hash
	// /// @TODO: Support MetaTable
	// /// @TODO: Check against configured type
	// /// @TODO: Convert to configured type if possible (like numeric)
	// /// @TODO: Support proper timestamp handling (not implemented)
	// ///
	// public static Object[] valueToOptionSet(MetaTypeMap mtm, String key, Object value) throws JSqlException {
	// 	if (value instanceof Integer) {
	// 		return new Object[] { new Integer(MetaType.TYPE_INTEGER), value, null, null }; //Typ, N,S,I,T
	// 	} else if (value instanceof String) {
	// 		return new Object[] { new Integer(MetaType.TYPE_STRING), 0, ((String) value).toLowerCase(), value }; //Typ, N,S,I,T
	// 	}
	// 
	// 	throw new JSqlException("Object type not yet supported: "+key+" = "+ value);
	// }
	// 
	// ///
	// /// Iterates the relevent keyList, and appends its value from the objMap, into the sql colTypes database
	// /// 
	// /// @param {JSql} sql                  - sql connection to setup the table
	// /// @param {String} tName              - table name to setup, this holds the actual meta table data
	// /// @param {MetaTable} colTypes       - main MetaTable, where this function shoudl be called from
	// /// @param {String} _oid               - object id to store the key value pairs into
	// /// @param {Map<String,Object>} objMap - map to extract values to store from
	// /// 
	// @SuppressWarnings("unchecked")
	// public static void JSqlObjectMapAppend(MetaTypeMap mt, //
	// 	JSql sql, String tName, String _oid, //
	// 	Map<String, Object> objMap, Set<String> keyList, //
	// 	boolean optimizeAutoCommit //
	// ) throws JSqlException {
	// 	
	// 	// boolean sqlMode = handleQuery ? sql.getAutoCommit() : false;
	// 	// if (sqlMode) {
	// 	// 	sql.setAutoCommit(false);
	// 	// }
	// 	
	// 	try {
	// 		Object[] typSet;
	// 		String k;
	// 		Object v;
	// 
	// 		for (Map.Entry<String, Object> entry : objMap.entrySet()) {
	// 
	// 			k = entry.getKey(); //(entry.getKey()).toLowerCase();
	// 			if ( /*k.equals("oid") || k.equals("_oid") ||*/ k.equals("_otm")) { //reserved
	// 				continue;
	// 			}
	// 
	// 			if (keyList != null && !keyList.contains(k)) {
	// 				continue;
	// 			}
	// 
	// 			v = entry.getValue();
	// 			valueToOptionSet(mtm, k, v);
	// 			
	// 			System.out.println("APPEND: "+k+" = "+v);
	// 			
	// 			// This is currently only for NON array mode
	// 			sql.upsertQuerySet( //
	// 				tName, //
	// 				new String[] { "oID", "kID", "idx" }, //
	// 				new Object[] { _oid, k, 0 }, //
	// 				//
	// 				new String[] { "typ", "nVl", "sVl", "tVl" }, //
	// 				new Object[] { typSet[0], typSet[1], typSet[2], typSet[3] }, //
	// 				null, null, null //
	// 			).execute();
	// 		}
	// 		//sql.commit();
	// 	} catch (Exception e) {
	// 		throw new JSqlException(e);
	// 	} finally {
	// 		// if (sqlMode) {
	// 		// 	sql.setAutoCommit(true);
	// 		// }
	// 	}
	// }
	
}
