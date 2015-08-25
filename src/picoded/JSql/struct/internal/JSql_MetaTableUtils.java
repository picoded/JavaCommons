package picoded.JSql.struct.internal;

/// Java imports
import java.util.*;
import java.util.logging.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/// Picoded imports
import picoded.conv.*;
import picoded.JSql.*;
import picoded.JStruct.*;
import picoded.struct.*;

///
/// Protected class, used to orgainze the various JSql based logic
/// used in MetaTable. 
///
/// The larger intention is to keep the MetaTable class more maintainable and unit testable
///
public class JSql_MetaTableUtils {
	
	//
	// JSqlResult search
	//--------------------------------------------------------------------------

	/// Fetches the result array position using the filters
	/// 
	/// This is done, as each row in the SQL query only represents
	/// an object, key, value, value index pair.
	///
	/// This is done to search out the row position for the result matching the criteria
	///
	/// @params  JSqlResult to search up the row
	/// @params  _oid to check against, ignored if null
	/// @params  the key to check against, ignored if null
	/// @params  the idx to check against, ignored if -10 or below
	///
	/// @returns   row of the JSqlResult, -1 if failed to find
	///
	protected static int fetchResultPosition(JSqlResult r, String _oid, String key, int idx) {
		List<Object> oID_list = r.get("oID");
		List<Object> kID_list = r.get("kID");
		List<Object> idx_list = r.get("idx");
		
		int lim = kID_list.size(); 
		for (int i = 0; i < lim; ++i) {
			
			if (_oid != null && !_oid.equals(oID_list.get(i))) {
				continue;
			}
			
			if (key != null && !key.equals( ((String) (kID_list.get(i))) ) ) {
				continue;
			}
			
			if (idx > -9 && idx != ((Number) (idx_list.get(i))).intValue()) {
				continue;
			}
			
			return i;
		}

		return -1;
	}
	
	/// Fetches the result array position using the filters
	/// 
	/// This is done, as each row in the SQL query only represents
	/// an object, key, value, value index pair.
	///
	/// This is done to search out the row position for the result matching the criteria
	///
	/// @params  JSqlResult to search up the row
	/// @params  the key to check against, ignored if null
	/// @params  the idx to check against, ignored if -10 or below
	///
	/// @returns   row of the JSqlResult, -1 if failed to find
	///
	protected static int fetchResultPosition(JSqlResult r, String key, int idx) {
		return fetchResultPosition(r, null, key, idx);
	}

	//
	// MetaType and SQL handling
	//
	//------------------------------------------------------------------------------------------
	
	/// Values to option set conversion used by JSql
	///
	/// @TODO: Support the various numeric value
	/// @TODO: Support string / text
	/// @TODO: Support array sets
	/// @TODO: Support GUID hash
	/// @TODO: Support MetaTable
	/// @TODO: Check against configured type
	/// @TODO: Convert to configured type if possible (like numeric)
	/// @TODO: Support proper timestamp handling (not implemented)
	///
	public static Object[] valueToOptionSet(MetaTypeMap mtm, String key, Object value)  {
		if (value instanceof Integer) {
			return new Object[] { new Integer(MetaType.INTEGER.getValue()), value, null, null }; //Typ, N,S,I,T
		} else if (value instanceof Float) {
			return new Object[] { new Integer(MetaType.FLOAT.getValue()), value, null, null }; //Typ, N,S,I,T
		} else if (value instanceof Double) {
			return new Object[] { new Integer(MetaType.DOUBLE.getValue()), value, null, null }; //Typ, N,S,I,T
		} else if (value instanceof String) {
			return new Object[] { new Integer(MetaType.STRING.getValue()), 0, ((String) value).toLowerCase(), value }; //Typ, N,S,I,T
		} else {
			String jsonString = ConvertJSON.fromObject(value);
			return new Object[] { new Integer(MetaType.JSON.getValue()), 0, null, jsonString };
		}
	
		//throw new RuntimeException("Object type not yet supported: "+key+" = "+ value);
	}
	
	/// Extract the value from a position (ignore array?)
	///
	/// @TODO: Support the various numeric value
	/// @TODO: Support string / text
	/// @TODO: Support GUID hash
	/// @TODO: Support MetaTable
	///
	/// @see extractKeyValue
	protected static Object extractNonArrayValueFromPos(JSqlResult r, int pos) {
	
		List<Object> typList = r.get("typ");
		int baseType = ((Number) (typList.get(pos))).intValue();
	
		// Int, Long, Double, Float
		if (baseType == MetaType.INTEGER.getValue()) {
			return new Integer(((Number) (r.get("nVl").get(pos))).intValue());
		} else if (baseType == MetaType.FLOAT.getValue()) {
			return new Float(((Number) (r.get("nVl").get(pos))).floatValue());
		} else if (baseType == MetaType.DOUBLE.getValue()) {
			return new Double(((Number) (r.get("nVl").get(pos))).doubleValue());
		} else if (baseType == MetaType.STRING.getValue()) { // String
			return (String) (r.get("tVl").get(pos));
		} else if (baseType == MetaType.TEXT.getValue()) { // Text
			return (String) (r.get("tVl").get(pos));
		} else if (baseType == MetaType.JSON.getValue()) { // Text
			return ConvertJSON.toObject( (String)(r.get("tVl").get(pos)) );
		} 
		
		throw new RuntimeException("Object type not yet supported: oID = "+r.get("oID").get(pos)+
		                           ", kID = "+r.get("kID").get(pos)+", BaseType = "+ baseType);
		
		//throw new RuntimeException("Object type not yet supported: Pos = "+pos+", BaseType = "+ baseType);
	}
	
	/// Same as extractNonArrayValueFromPos, however returns oid, and row key names as well
	protected static Object[] extractKeyValueNonArrayValueFromPos(JSqlResult r, int pos) {
		Object value = extractNonArrayValueFromPos(r, pos);
		return new Object[] { r.get("oID").get(pos), r.get("kID").get(pos), value };
	}
	
	/// Extract the key value
	///
	/// @TODO: Support array sets
	///
	protected static Object extractKeyValue(JSqlResult r, String key) throws JSqlException {
		int pos = fetchResultPosition(r, key, 0); //get the 0 pos value
	
		if (pos <= -1) {
			return null;
		}
		
		return extractNonArrayValueFromPos(r, pos);
	}
	
	//
	// Append and get
	//
	//------------------------------------------------------------------------------------------
	
	///
	/// Iterates the relevent keyList, and appends its value from the objMap, into the sql colTypes database
	/// 
	/// @param {MetaTypeMap} mtm           - main MetaTypeMap, to check collumn configuration
	/// @param {JSql} sql                  - sql connection to setup the table
	/// @param {String} tName              - table name to setup, this holds the actual meta table data
	/// @param {String} _oid               - object id to store the key value pairs into
	/// @param {Map<String,Object>} objMap - map to extract values to store from
	/// @param {Set<String>} keyList       - keylist to limit append load
	/// @param {boolean} optimizeAppend    - Used to indicate if append batch should be optimized (not used)
	/// 
	@SuppressWarnings("unchecked")
	public static void JSqlObjectMapAppend( //
		MetaTypeMap mtm, //
		JSql sql, String tName, String _oid, //
		Map<String, Object> objMap, Set<String> keyList, //
		boolean optimizeAppend //
	) throws JSqlException {
		
		// boolean sqlMode = handleQuery ? sql.getAutoCommit() : false;
		// if (sqlMode) {
		// 	sql.setAutoCommit(false);
		// }
		
		try {
			Object[] typSet;
			String k;
			Object v;
	
			for (Map.Entry<String, Object> entry : objMap.entrySet()) {
				k = entry.getKey(); 
				
				// Skip reserved key, oid ke is allowed to be saved (to ensure blank object is saved)
				if ( /*k.equalsIgnoreCase("oid") || k.equalsIgnoreCase("_oid") || */ k.equalsIgnoreCase("_otm")) { //reserved
					continue;
				}
				
				// Checks if keyList given, if so skip if not on keyList
				if (keyList != null && !keyList.contains(k)) {
					continue;
				}
				
				// Get the value to insert
				v = entry.getValue();
				
				// Converts it into a type set, and store it
				typSet = valueToOptionSet(mtm, k, v);
				
				// This is currently only for NON array mode
				sql.upsertQuerySet( //
					tName, //
					new String[] { "oID", "kID", "idx" }, //
					new Object[] { _oid, k, 0 }, //
					//
					new String[] { "typ", "nVl", "sVl", "tVl" }, //
					new Object[] { typSet[0], typSet[1], typSet[2], typSet[3] }, //
					null, null, null //
				).execute();
			}
			//sql.commit();
		} catch (Exception e) {
			throw new JSqlException(e);
		} finally {
			// if (sqlMode) {
			// 	sql.setAutoCommit(true);
			// }
		}
	}
	
	///
	/// Extracts and build the map stored under an _oid
	/// 
	/// @param {MetaTypeMap} mtm           - main MetaTypeMap, to check collumn configuration
	/// @param {JSql} sql                  - sql connection to setup the table
	/// @param {String} sqlTableName       - table name to setup, this holds the actual meta table data
	/// @param {String} _oid               - object id to store the key value pairs into
	/// @param {Map<String,Object>} ret    - map to populate, and return, created if null if there is data
	/// 
	public static Map<String,Object> JSqlObjectMapFetch( //
		MetaTypeMap mtm, JSql sql, //
		String sqlTableName, String _oid, //
		Map<String,Object> ret //
	) {
		try {
			JSqlResult r = sql.selectQuerySet(sqlTableName, "*", "oID=?", new Object[] { _oid }).query();
			return extractObjectMapFromJSqlResult(mtm, r, _oid, ret);
		} catch( JSqlException e ) {
			throw new RuntimeException(e);
		}
	}
	
	///
	/// Extracts and build the map stored under an _oid, from the JSqlResult
	/// 
	/// @param {MetaTypeMap} mtm           - main MetaTypeMap, to check collumn configuration
	/// @param {JSqlResult} r              - sql result
	/// @param {String} _oid               - object id to store the key value pairs into
	/// @param {Map<String,Object>} ret    - map to populate, and return, created if null if there is data
	/// 
	public static Map<String,Object> extractObjectMapFromJSqlResult(//
		MetaTypeMap mtm, JSqlResult r, //
		String _oid, Map<String,Object> ret //
	) {
		if(r == null) {
			return ret;
		}
		
		List<Object> oID_list = r.get("oID");
		List<Object> kID_list = r.get("kID");
		List<Object> idx_list = r.get("idx");
		List<Object> val_list = r.get("tVl");
		
		if(kID_list == null) {
			return ret;
		}
		
		int lim = kID_list.size(); 
		for (int i = 0; i < lim; ++i) {
			if (_oid != null && !_oid.equals(oID_list.get(i))) {
				continue;
			}
			
			if( ((Number) (idx_list.get(i))).intValue() != 0 ) {
				continue; //Now only accepts first value (not an array)
			}
			
			Object[] rowData = extractKeyValueNonArrayValueFromPos(r, i);
			
			/// Only check for ret, at this point, 
			/// so returning null when no data occurs
			if( ret == null ) {
				ret = new ConcurrentHashMap<String,Object>();
			}
			
			ret.put( rowData[1].toString(), rowData[2] );
		}
		
		return ret;
	}
}
