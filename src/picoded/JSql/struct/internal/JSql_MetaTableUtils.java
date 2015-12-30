package picoded.JSql.struct.internal;

/// Java imports
import java.util.*;
import java.util.logging.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/// Picoded imports
import picoded.conv.*;
import picoded.enums.JSqlType;
import picoded.enums.ObjectTokens;
import picoded.JSql.*;
import picoded.JStruct.*;
import picoded.struct.*;
import picoded.struct.query.*;
import picoded.struct.query.condition.*;
import picoded.struct.query.internal.*;

///
/// Protected class, used to orgainze the various JSql based logic
/// used in MetaTable.
///
/// The larger intention is to keep the MetaTable class more maintainable and unit testable
///
public class JSql_MetaTableUtils {
	
	/// Static local logger
	protected static Logger logger = Logger.getLogger(JSql_MetaTableUtils.class.getName());
	
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
			
			if (key != null && !key.equals(((String) (kID_list.get(i))))) {
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
	public static Object[] valueToOptionSet(MetaTypeMap mtm, String key, Object value) {
		if (value instanceof Integer) {
			return new Object[] { new Integer(MetaType.INTEGER.getValue()), value, null, null }; //Typ, N,S,I,T
		} else if (value instanceof Float) {
			return new Object[] { new Integer(MetaType.FLOAT.getValue()), value, null, null }; //Typ, N,S,I,T
		} else if (value instanceof Double) {
			return new Object[] { new Integer(MetaType.DOUBLE.getValue()), value, null, null }; //Typ, N,S,I,T
		} else if (value instanceof String) {
			String shortenValue = ((String) value).toLowerCase();
			if (shortenValue.length() > 64) {
				shortenValue = shortenValue.substring(0, 64);
			}
			return new Object[] { new Integer(MetaType.STRING.getValue()), 0, shortenValue, value }; //Typ, N,S,I,T
		} else if (value instanceof byte[]) {
			return new Object[] { new Integer(MetaType.BINARY.getValue()), 0, null,
				(Base64.getEncoder().encodeToString((byte[]) value)) }; //Typ, N,S,I,T
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
		} else if (baseType == MetaType.BINARY.getValue()) {
			return (Base64.getDecoder().decode((String) (r.get("tVl").get(pos))));
		} else if (baseType == MetaType.JSON.getValue()) { // JSON
			return ConvertJSON.toObject((String) (r.get("tVl").get(pos)));
		}
		
		throw new RuntimeException("Object type not yet supported: oID = " + r.get("oID").get(pos) + ", kID = "
			+ r.get("kID").get(pos) + ", BaseType = " + baseType);
		
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
			Object v;
			
			for (String k : keyList) {
				
				// Skip reserved key, otm is allowed to be saved (to ensure blank object is saved)
				if (k.equalsIgnoreCase("_otm")) { //reserved
					continue;
				}
				
				if (k.length() > 64) {
					throw new RuntimeException("Attempted to insert a key value larger then 64 for (_oid = " + _oid + "): "
						+ k);
				}
				
				// Checks if keyList given, if so skip if not on keyList
				if (keyList != null && !keyList.contains(k)) {
					continue;
				}
				
				// Get the value to insert
				v = objMap.get(k);
				
				if (v == ObjectTokens.NULL || v == null) {
					// Skip reserved key, oid key is allowed to be removed directly
					if (k.equalsIgnoreCase("oid") || k.equalsIgnoreCase("_oid")) {
						continue;
					}
					sql.deleteQuerySet(tName, "oID=? AND kID=?", new Object[] { _oid, k }).execute();
				} else {
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
	public static Map<String, Object> JSqlObjectMapFetch( //
		MetaTypeMap mtm, JSql sql, //
		String sqlTableName, String _oid, //
		Map<String, Object> ret //
	) {
		try {
			JSqlResult r = sql.selectQuerySet(sqlTableName, "*", "oID=?", new Object[] { _oid }).query();
			return extractObjectMapFromJSqlResult(mtm, r, _oid, ret);
		} catch (JSqlException e) {
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
	public static Map<String, Object> extractObjectMapFromJSqlResult(//
		MetaTypeMap mtm, JSqlResult r, //
		String _oid, Map<String, Object> ret //
	) {
		if (r == null) {
			return ret;
		}
		
		List<Object> oID_list = r.get("oID");
		List<Object> kID_list = r.get("kID");
		List<Object> idx_list = r.get("idx");
		List<Object> val_list = r.get("tVl");
		
		if (kID_list == null) {
			return ret;
		}
		
		int lim = kID_list.size();
		for (int i = 0; i < lim; ++i) {
			if (_oid != null && !_oid.equals(oID_list.get(i))) {
				continue;
			}
			
			if (((Number) (idx_list.get(i))).intValue() != 0) {
				continue; //Now only accepts first value (not an array)
			}
			
			Object[] rowData = extractKeyValueNonArrayValueFromPos(r, i);
			
			/// Only check for ret, at this point,
			/// so returning null when no data occurs
			if (ret == null) {
				ret = new ConcurrentHashMap<String, Object>();
			}
			
			ret.put(rowData[1].toString(), rowData[2]);
		}
		
		return ret;
	}
	
	/// Lowercase suffix string
	protected static String lowerCaseSuffix = "#lc";
	
	///
	/// The complex left inner join StringBuilder used for view / query requests
	///
	/// @TODO: Protect index names from SQL injections. Since index columns may end up "configurable". This can end up badly for SAAS build
	///
	/// @params  sql connection used, this is used to detect vendor specific logic =(
	/// @params  meta table name, used to pull the actual data the view is based on
	/// @params  type mapping to build the complex view from
	/// @params  additional arguments needed to build the query
	///
	/// @returns StringBuilder for the view building statement, this can be used for creating permenant view / queries
	///
	protected static StringBuilder sqlComplexLeftJoinQueryBuilder(JSql sql, String tableName, MetaTypeMap mtm,
		List<Object> queryArgs) {
		
		//
		// Vendor specific custimization
		//-----------------------------------------
		
		String joinType = "LEFT";
		
		String lBracket = "'";
		String rBracket = "'";
		
		// Reserved to overwrite, and to do more complex quotes
		// if (sql.sqlType == JSqlType.mssql) {
		// 	lBracket = "[";
		// 	rBracket = "]";
		// }
		
		//
		// Select / From StringBuilder setup
		//
		// Also setup the distinct oID collumn
		//
		//-----------------------------------------
		
		StringBuilder select = new StringBuilder("SELECT B.oID AS ");
		select.append(lBracket + "oID" + rBracket);
		
		StringBuilder from = new StringBuilder(" FROM ");
		from.append("(SELECT DISTINCT oID");
		
		//
		// Optional object created time support
		// (Commented out)
		//
		//-----------------------------------------
		
		//select.append(", B.oTm AS ");
		//select.append(lBracket + "_otm" + rBracket);
		//from.append(", oTm");
		
		//
		// Distinct table reference
		//
		//-----------------------------------------
		
		from.append(" FROM " + tableName + ")");
		//from.append( tableName );
		from.append(" AS B");
		
		//
		// Distinct table reference
		//
		//-----------------------------------------
		
		String key;
		MetaType type;
		
		// ArrayList<Object> argList = new ArrayList<Object>();
		
		int joinCount = 0;
		for (Map.Entry<String, MetaType> e : mtm.entrySet()) {
			key = e.getKey();
			type = e.getValue();
			
			if ( //
			type == MetaType.INTEGER || //
				type == MetaType.FLOAT || //
				type == MetaType.DOUBLE //
			) { //
			
				select.append(", N" + joinCount + ".nVl AS ");
				select.append(lBracket + key + rBracket);
				
				from.append(" " + joinType + " JOIN " + tableName + " AS N" + joinCount);
				from.append(" ON B.oID = N" + joinCount + ".oID");
				from.append(" AND N" + joinCount + ".idx = 0 AND N" + joinCount + ".kID = ?");
				
				queryArgs.add(key);
				
			} else if (type == MetaType.STRING) {
				
				select.append(", S" + joinCount + ".tVl AS ");
				select.append(lBracket + key + rBracket);
				
				select.append(", S" + joinCount + ".sVl AS ");
				select.append(lBracket + key + lowerCaseSuffix + rBracket);
				
				from.append(" " + joinType + " JOIN " + tableName + " AS S" + joinCount);
				from.append(" ON B.oID = S" + joinCount + ".oID");
				
				// Key based seperation
				from.append(" AND S" + joinCount + ".idx = 0 AND S" + joinCount + ".kID = ?");
				queryArgs.add(key);
				
				// Numeric value = 0, index optimization
				from.append(" AND S" + joinCount + ".nVl = ? ");
				queryArgs.add(0.0);
				
			} else if (type == MetaType.TEXT) {
				
				select.append(", S" + joinCount + ".tVl AS ");
				select.append(lBracket + key + rBracket);
				
				from.append(" " + joinType + " JOIN " + tableName + " AS S" + joinCount);
				from.append(" ON B.oID = S" + joinCount + ".oID");
				
				// Key based seperation
				from.append(" AND S" + joinCount + ".idx = 0 AND S" + joinCount + ".kID = ?");
				queryArgs.add(key);
				
				// Numeric value = 0, index optimization
				from.append(" AND S" + joinCount + ".nVl = ? ");
				queryArgs.add(0.0);
				
			} else {
				// Unknown MetaType ignored
				logger.log(Level.WARNING, "sqlComplexLeftJoinQueryBuilder -> Unknown MetaType (" + type
					+ ") - for meta key : " + key);
			}
			
			++joinCount;
		}
		
		// The final return string builder
		StringBuilder ret = new StringBuilder();
		ret.append(select);
		ret.append(from);
		
		// Return StringBuilder
		return ret;
	}
	
	/// Does the value to MetaType conversion
	public static MetaType valueToMetaType(Object value) {
		if (value instanceof Integer) {
			return MetaType.INTEGER;
		} else if (value instanceof Float) {
			return MetaType.FLOAT;
		} else if (value instanceof Double) {
			return MetaType.DOUBLE;
		} else if (value instanceof String) {
			return MetaType.STRING;
		} else if (value instanceof byte[]) {
			return MetaType.BINARY;
		} //else {
		  //	return MetaType.JSON;
		  //}
		return null;
	}
	
	/// Sanatize the order by string, and places the field name as query arguments
	public static String sanatizeOrderByString(String rawString, List<Object> queryArgs) {
		// Return string
		StringBuilder ret = new StringBuilder();
		
		// Clear out excess whtiespace
		rawString = rawString.trim().replaceAll("\\s+", " ");
		
		// Split for multiple fields
		String[] orderByArr = rawString.split(",");
		
		if (orderByArr == null || orderByArr[0].length() <= 0) {
			throw new RuntimeException("Unexpected blank found in OrderBy query : " + rawString);
		}
		
		boolean first = true;
		for (String orderSet : orderByArr) {
			
			// Get orderset, without excess whitespace
			orderSet = orderSet.trim();
			if (orderSet.length() <= 0) {
				throw new RuntimeException("Invalid OrderBy string query: " + rawString);
			}
			
			// Default ordering is asecending
			OrderBy.OrderType ot = OrderBy.OrderType.ASC;
			
			// Check for DESC / ASC suffix
			if (orderSet.length() > 4) {
				String lowerCaseOrderSet = orderSet.toLowerCase();
				if (lowerCaseOrderSet.endsWith(" desc")) {
					ot = OrderBy.OrderType.DESC;
					orderSet = orderSet.substring(0, orderSet.length() - 5).trim();
				} else if (lowerCaseOrderSet.endsWith(" asc")) {
					ot = OrderBy.OrderType.ASC;
					orderSet = orderSet.substring(0, orderSet.length() - 4).trim();
				}
			}
			
			// Unwrap the field name (since they will be passed by params anyway)
			String field = QueryUtils.unwrapFieldName(orderSet);
			
			// Add as query args
			// No longer used, as its not supported in most SQL
			//
			// queryArgs.add(field);
			//
			
			// Push the return string, add the seperators when needed
			if (first != true) {
				ret.append(", ");
			} else {
				first = false;
			}
			
			// field -> sanatize .[]-_,
			field = RegexUtils.removeAllNonAlphaNumeric_allowCommonSeparators(field);
			
			// Push the actual order by command
			if (ot == OrderBy.OrderType.DESC) {
				ret.append("\"" + field + "\"" + " DESC ");
			} else {
				ret.append("\"" + field + "\"" + " ASC ");
			}
		}
		
		return ret.toString();
	}
	
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
	public static JSqlResult metaTableSelectQueryBuilder( //
		MetaTable metaTableObj, JSql sql, String tablename, String selectedCols, //
		String whereClause, Object[] whereValues, String orderByStr, int offset, int limit //
	) { //
	
		/// Query string, for either a newly constructed view, or cached view
		StringBuilder queryBuilder = new StringBuilder();
		List<Object> complexQueryArgs = new ArrayList<Object>();
		Object[] queryArgs = null;
		
		if (orderByStr != null) {
			orderByStr.replaceAll("_oid", "oID");
		}
		
		if (whereClause == null) {
			
			queryBuilder.append("SELECT " + selectedCols + " FROM " + tablename + "");
			
			// @TODO sanatize ORDER BY for SQL injection
			// Support ORDER BY values forming the MetaMap
			if (orderByStr != null) {
				queryBuilder.append(" ORDER BY ");
				queryBuilder.append(sanatizeOrderByString(orderByStr, complexQueryArgs));
			}
			
		} else {
			
			// Building the MetaTypeMap from where request
			MetaTypeMap queryTypeMap = new MetaTypeMap();
			
			// Validating the Where clause and using it to build the MetaTypeMap
			Query queryObj = null;
			
			if (whereClause != null && whereClause.length() >= 0) {
				
				// Conversion to query object, round 1 of sanatizing
				queryObj = Query.build(whereClause, whereValues);
				
				// Build the query type map
				Map<String, List<Object>> queryMap = queryObj.keyValuesMap();
				for (String key : queryMap.keySet()) {
					
					//if( key.endsWith(lowerCaseSuffix) ) {
					//	
					//}
					
					MetaType subType = valueToMetaType(queryMap.get(key).get(0));
					if (subType != null) {
						queryTypeMap.put(key, subType);
					}
				}
				
				// Gets the original field query map, to do subtitution
				Map<String, List<Query>> fieldQueryMap = queryObj.fieldQueryMap();
				Map<String, Object> queryArgMap = queryObj.queryArgumentsMap();
				int newQueryArgsPos = queryArgMap.size() + 1;
				
				//
				// Parses the where clause with query type map, 
				// to enforce lower case search for string based nodes
				//
				for (String key : queryTypeMap.keySet()) {
					
					// For each string based search, enforce lowercase search
					MetaType subType = queryTypeMap.get(key);
					if (subType == MetaType.STRING) {
						
						// The query list to do lower case replacement
						List<Query> toReplaceQueries = fieldQueryMap.get(key);
						
						// Skip if blank
						if (toReplaceQueries == null || toReplaceQueries.size() <= 0) {
							continue;
						}
						
						// Iterate the queries to replace them
						for (Query toReplace : toReplaceQueries) {
							
							String argName = toReplace.argumentName();
							Object argLowerCase = queryArgMap.get(argName);
							if (argLowerCase != null) {
								argLowerCase = argLowerCase.toString().toLowerCase();
							}
							
							queryArgMap.put("" + newQueryArgsPos, argLowerCase);
							Query replacement = QueryFilter.basicQueryFromTokens(queryArgMap, toReplace.fieldName()
								+ lowerCaseSuffix, toReplace.operatorSymbol(), ":" + newQueryArgsPos);
							
							// Case sensitive varient
							// replacement = new And(
							// 	replacement,
							// 	toReplace,
							// 	queryArgMap
							// );
							
							queryObj = queryObj.replaceQuery(toReplace, replacement);
							
							++newQueryArgsPos;
						}
					}
				}
			}
			
			// Building the Inner join query
			StringBuilder innerJoinQuery = sqlComplexLeftJoinQueryBuilder(sql, tablename, queryTypeMap, complexQueryArgs);
			
			//queryBuilder.append( innerJoinQuery );
			// Building the complex inner join query
			queryBuilder.append("SELECT " + selectedCols.replaceAll("DISTINCT", "") + " FROM (");
			queryBuilder.append(innerJoinQuery);
			queryBuilder.append(") AS R");
			
			// WHERE query is built from queryObj, this acts as a form of sql sanitization
			if (queryObj != null) {
				queryBuilder.append(" WHERE ");
				queryBuilder.append(queryObj.toSqlString());
				complexQueryArgs.addAll(queryObj.queryArgumentsList());
			}
			
			// @TODO sanatize ORDER BY for SQL injection
			// Support ORDER BY values forming the MetaMap
			if (orderByStr != null) {
				queryBuilder.append(" ORDER BY ");
				queryBuilder.append(sanatizeOrderByString(orderByStr, complexQueryArgs));
			}
			
			//logger.log( Level.WARNING, queryBuilder.toString() );
			//logger.log( Level.WARNING, Arrays.asList(queryArgs).toString() );
		}
		
		// Finalize query args
		queryArgs = complexQueryArgs.toArray(new Object[0]);
		
		// Limit and offset clause
		if (limit > 0) {
			queryBuilder.append(" LIMIT " + limit);
			
			if (offset > 0) {
				queryBuilder.append(" OFFSET " + offset);
			}
		}
		
		try {
			// Execute and get the result
			return sql.query(queryBuilder.toString(), queryArgs);
		} catch (JSqlException e) {
			throw new RuntimeException(e);
		}
	}
	
	/// Performs a search query, and returns the respective MetaObjects GUID keys
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
	public static String[] metaTableQueryKey( //
		//
		MetaTable metaTableObj, JSql sql, String tablename, //
		//
		String whereClause, Object[] whereValues, String orderByStr, int offset, int limit //
	) { //
		JSqlResult r = metaTableSelectQueryBuilder( //
			metaTableObj, sql, tablename, "DISTINCT oID", whereClause, whereValues, orderByStr, offset, limit);
		//logger.log( Level.WARNING, r.toString() );
		List<Object> oID_list = r.get("oID");
		// Generate the object list
		if (oID_list != null) {
			return ListValueConv.objectListToStringArray(oID_list);
		}
		// Blank list as fallback
		return new String[0];
	}
	
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
	public static MetaObject[] metaTableQuery( //
		//
		MetaTable metaTableObj, JSql sql, String tablename, //
		//
		String whereClause, Object[] whereValues, String orderByStr, int offset, int limit //
	) { //
		return metaTableObj.getArrayFromID(
			metaTableQueryKey(metaTableObj, sql, tablename, whereClause, whereValues, orderByStr, offset, limit), true);
	}
	
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
	public static long metaTableCount( //
		//
		MetaTable metaTableObj, JSql sql, String tablename, //
		//
		String whereClause, Object[] whereValues, String orderByStr, int offset, int limit //
	) { //
		JSqlResult r = metaTableSelectQueryBuilder(metaTableObj, sql, tablename, "COUNT(DISTINCT oID) AS rcount",
			whereClause, whereValues, orderByStr, offset, limit);
		
		List<Object> rcountArr = r.get("rcount");
		// Generate the object list
		if (rcountArr != null) {
			return ((Number) rcountArr.get(0)).longValue();
		}
		// Blank as fallback
		return 0;
	}
	
}
