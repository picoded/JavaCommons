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

/// hazelcast
import com.hazelcast.core.*;
import com.hazelcast.config.*;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/// @TODO: Convert to Map<String, Map<String, Object>>
/// @TODO: Documentation =( of class
public class MetaTable {
	
	///
	/// Constructor setup
	///--------------------------------------------------------------------------
	
	/// Internal JStackObj
	protected JStack JStackObj = null;
	
	/// Internal table name, before prefix?
	protected String tableName = "MetaTable";
	
	/// Setup the metatable with the default table name
	public MetaTable(JStack inStack) {
		JStackObj = inStack;
	}
	
	/// Setup the metatable with the given stack
	public MetaTable(JStack inStack, String inTableName) {
		JStackObj = inStack;
		
		if (inTableName == null) {
			throw new RuntimeException("Invalid table name (null): " + inTableName);
		}
		
		final String numericString = "0123456789";
		if (numericString.indexOf(inTableName.substring(0, 1)) > 0) {
			throw new RuntimeException("Invalid table name (cannot start with numbers): " + inTableName);
		}
		
		tableName = inTableName;
	}
	
	///
	/// Internal config vars
	///--------------------------------------------------------------------------
	
	/// Object ID field type
	protected String objColumnType = "VARCHAR(32)";
	
	/// Key name field type
	protected String keyColumnType = "VARCHAR(32)";
	
	/// Type collumn type
	protected String typeColumnType = "TINYINT";
	
	/// Index collumn type
	protected String indexColumnType = "TINYINT";
	
	/// String value field type
	/// @TODO: Investigate performance issues for this approach
	protected String numColumnType = "DECIMAL(22,12)";
	
	/// String value field type
	protected String strColumnType = "VARCHAR(64)";
	
	/// Full text value field type
	protected String fullTextColumnType = "TEXT";
	
	/// Timestamp field type
	protected String tStampColumnType = "BIGINT";
	
	/// Primary key type
	protected String pKeyColumnType = "INTEGER PRIMARY KEY AUTOINCREMENT";
	
	/// Indexed view prefix, this is used to handle index conflicts between "versions" if needed
	protected String viewSuffix = "";
	
	///
	/// JStack setup functions
	///--------------------------------------------------------------------------
	
	/// Does the required table setup for the various applicable stack layers
	public MetaTable stackSetup() throws JStackException {
		try {
			JStackLayer[] sl = JStackObj.stackLayers();
			for (int a = 0; a < sl.length; ++a) {
				// JSql specific setup
				if (sl[a] instanceof JSql) {
					JSqlDataTableSetup((JSql) (sl[a]));
					JSqlIndexConfigTableSetup((JSql) (sl[a]));
				} else if (sl[a] instanceof JCache) {
					
				}
			}
		} catch (JSqlException e) {
			throw new JStackException(e);
		}
		
		return this;
	}
	
	/// Removes all the various application stacklayers
	///
	/// WARNING: this deletes all the data, and is not reversable
	public MetaTable stackTeardown() {
		
		return this;
	}
	
	///
	/// Indexed columns setup
	///--------------------------------------------------------------------------
	
	/// SQL view name
	protected String sqlViewName(JSql sql, String vTyp) {
		return (sql.getTablePrefix() + tableName + "_" + vTyp + viewSuffix);
	}
	
	/// Default type for all values not defined in the index
	protected CaseInsensitiveHashMap<String, MetaType> typeMapping = new CaseInsensitiveHashMap<String, MetaType>();
	protected MetaType defaultType = new MetaType(MetaType.TYPE_MIXED);
	
	/// Returned the defined metaType for the given key
	public MetaType getType(String name) {
		if (name == null || (name = name.trim().toLowerCase()).length() <= 0) {
			throw new RuntimeException("Name parameter cannot be NULL or BLANK");
		}
		
		return typeMapping.get(name);
	}
	
	/// Sets the defined metaType for the given key
	public MetaTable putType(String name, MetaType type) {
		if (name == null || (name = name.trim().toLowerCase()).length() <= 0) {
			throw new RuntimeException("Name parameter cannot be NULL or BLANK");
		}
		
		if (name.equals("_oid") || name.equals("_otm")) {
			throw new RuntimeException("Name parameter uses reserved name " + name);
		}
		
		typeMapping.put(name, type);
		return this;
	}
	
	///
	/*
	public MetaTable setIndex( String[] inIndxList, Object[] inTypeList ) {
		indxList = inIndxList;
		//typeList = inTypeList;
		return this;
	}
	
	public MetaTable setIndex( Map<String,Object> indexMap ) {
		List<String> list = new ArrayList<String>( indexMap.keySet() );
		
		
		
		return this;
	}
	 */

	///
	/// Internal JSql index setup
	///--------------------------------------------------------------------------
	/// @TODO: Protect index names from SQL injections. Since index columns may end up "configurable". This can end up badly for SAAS build
	protected String JSqlMakeInnerJoinIndexQuery(JSql sql) {
		
		StringBuilder sb = new StringBuilder("CREATE VIEW "); //OR REPLACE
		sb.append(sqlViewName(sql, "view"));
		sb.append(" AS ");
		
		String tableName = sqlTableName(sql);
		StringBuilder select = new StringBuilder("SELECT DISTINCT B.oID AS '_oid', B.oTm AS '_otm'");
		StringBuilder from = new StringBuilder("FROM " + tableName + " AS B");
		
		String key;
		MetaType type;
		
		int joinCount = 0;
		for (Map.Entry<String, MetaType> e : typeMapping.entrySet()) {
			key = e.getKey();
			type = e.getValue();
			
			if (type.valueType >= MetaType.TYPE_INTEGER && type.valueType <= MetaType.TYPE_FLOAT) {
				
				select.append(", N" + joinCount + ".nVl AS '" + key + "'");
				
				from.append(" INNER JOIN " + tableName + " AS N" + joinCount);
				from.append(" ON B.oID = N" + joinCount + ".oID");
				from.append(" AND N" + joinCount + ".idx = 0 AND N" + joinCount + ".kID = '" + key + "'");
				
			} else if (type.valueType == MetaType.TYPE_STRING) {
				
				select.append(", S" + joinCount + ".tVl AS '" + key + "'");
				select.append(", S" + joinCount + ".sVl AS '" + key + "_lc'");
				
				from.append(" INNER JOIN " + tableName + " AS S" + joinCount);
				from.append(" ON B.oID = S" + joinCount + ".oID");
				from.append(" AND S" + joinCount + ".idx = 0 AND S" + joinCount + ".kID = '" + key + "'");
				
			} else if (type.valueType == MetaType.TYPE_TEXT) {
				
				select.append(", S" + joinCount + ".tVl AS '" + key + "'");
				
				from.append(" INNER JOIN " + tableName + " AS S" + joinCount);
				from.append(" ON B.oID = S" + joinCount + ".oID");
				from.append(" AND S" + joinCount + ".idx = 0 AND S" + joinCount + ".kID = '" + key + "'");
				
			}
			
			++joinCount;
		}
		
		sb.append(select);
		sb.append(from);
		
		return sb.toString();
	}
	
	protected void JSqlIndexConfigTableSetup(JSql sql) throws JSqlException {
		String tName = sqlViewName(sql, "vCfg");
		
		// Table constructor
		//-------------------
		sql.createTableQuerySet( //
			tName, //
			new String[] { //
			"nme", //Index column name
				"typ", //Index column type
				"con" //Index type string value
			}, //
			new String[] { //
			keyColumnType, //
				typeColumnType, //
				keyColumnType //
			} //
			).execute(); //
		
		// Checks if the view needs to be recreated
		boolean recreatesView = false;
		
		// Checks if view actually needs recreation?
		recreatesView = true;
		
		// Recreates the view if needed
		if (recreatesView) {
			String vName = sqlViewName(sql, "view");
			
			sql.execute("DROP VIEW IF EXISTS " + vName);
			sql.execute(JSqlMakeInnerJoinIndexQuery(sql));
		}
	}
	
	///
	/// Internal JSql table setup
	///--------------------------------------------------------------------------
	
	/// SQL Table name
	protected String sqlTableName(JSql sql) {
		return (sql.getTablePrefix() + tableName);
	}
	
	/// JSQL Based tabel data storage
	protected void JSqlDataTableSetup(JSql sql) throws JSqlException {
		String tName = sqlTableName(sql);
		
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
				tStampColumnType, tStampColumnType, tStampColumnType,
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
			).execute();
		
		// Unique index
		//
		// This also optimizes query by object keys
		//------------------------------------------------
		sql.createTableIndexQuerySet( //
			tName, "oID, kID, idx", "UNIQUE", "unq" //
		).execute();
		
		// Key Values search index 
		//------------------------------------------------
		sql.createTableIndexQuerySet( //
			tName, "kID, nVl, sVl", null, "valMap" //
		).execute();
		
		// Object timestamp optimized Key Value indexe
		//------------------------------------------------
		sql.createTableIndexQuerySet( //
			tName, "oTm, kID, nVl, sVl", null, "oTm_valMap" //
		).execute();
		
		// Full text index, for textual data
		//------------------------------------------------
		if (sql.sqlType != JSqlType.sqlite) {
			sql.createTableIndexQuerySet( //
				tName, "tVl", "FULLTEXT", "tVl" //
			).execute();
		} else {
			sql.createTableIndexQuerySet( //
				tName, "tVl", null, "tVl" // Sqlite uses normal index
			).execute();
		}
		
		// timestamp index, is this needed?
		//------------------------------------------------
		//sql.createTableIndexQuerySet( //
		//	tName, "uTm", null, "uTm" //
		//).execute();
		
		//sql.createTableIndexQuerySet( //
		//	tName, "cTm", null, "cTm" //
		//).execute();
	}
	
	///
	/// JSQL Specific PUT and GET meta map operations
	///--------------------------------------------------------------------------
	
	/// Fetches the result array position using the filters
	protected int fetchResultPosition(JSqlResult r, String _oid, String key, int idx) {
		List<Object> oID_list = r.get("oID");
		List<Object> kID_list = r.get("kID");
		List<Object> idx_list = r.get("idx");
		
		int lim = kID_list.size();
		for (int i = 0; i < lim; ++i) {
			
			if (_oid != null && !_oid.equals(oID_list.get(i))) {
				continue;
			}
			
			if (key != null && !key.equals(((String) (kID_list.get(i))).toLowerCase())) {
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
	protected int fetchResultPosition(JSqlResult r, String key, int idx) {
		return fetchResultPosition(r, null, key, idx);
	}
	
	/// Extract out all the unique string values from a list array
	protected String[] extractUnique(List<Object> arr) {
		HashSet<String> retSet = new HashSet<String>();
		for (Object t : arr) {
			retSet.add(((String) t).toLowerCase());
		}
		return retSet.toArray(new String[retSet.size()]);
	}
	
	/// Extract the key value
	///
	/// @TODO: Support the various numeric value
	/// @TODO: Support string / text
	/// @TODO: Support array sets
	/// @TODO: Support GUID hash
	/// @TODO: Support MetaTable
	///
	protected Object extractKeyValue(JSqlResult r, String key) throws JSqlException {
		int pos = fetchResultPosition(r, key, 0); //get the 0 pos value
		
		if (pos <= -1) {
			return null;
		}
		
		List<Object> typList = r.get("typ");
		int baseType = ((Number) (typList.get(pos))).intValue();
		
		// Int, Long, Double, Float
		if (baseType >= MetaType.TYPE_INTEGER && baseType <= 34) {
			if (baseType == MetaType.TYPE_INTEGER) {
				return new Integer(((Number) (r.get("nVl").get(pos))).intValue());
			}
		} else if (baseType == MetaType.TYPE_STRING) { // String
			return (String) (r.get("tVl").get(pos));
		} else if (baseType == 52) { // Text
			return (String) (r.get("tVl").get(pos));
		} else {
			
		}
		throw new JSqlException("Object type not yet supported: " + baseType);
		//return null;
	}
	
	/// MetaTable JSqlResult to CaseInsensitiveHashMap
	protected Map<String, Object> JSqlResultToMap(JSqlResult r) throws JSqlException {
		if (r != null && r.rowCount() <= 1) {
			return null;
		}
		
		// Get all the unique keys
		String[] keys = extractUnique(r.get("kID"));
		
		// Extract the respective key values
		Map<String, Object> retMap = new CaseInsensitiveHashMap<String, Object>();
		for (int a = 0; a < keys.length; ++a) {
			if (keys[a].equals("oid")) { //reserved
				continue;
			}
			retMap.put(keys[a], extractKeyValue(r, keys[a]));
		}
		
		return retMap;
	}
	
	/// JSQL based GET
	protected Map<String, Object> JSqlObjectGet(JSql sql, String _oid) throws JSqlException {
		String tName = sqlTableName(sql);
		
		// Fetch all the meta fields
		JSqlResult r = sql.selectQuerySet(tName, "*", "oID=?", new Object[] { _oid }).query();
		
		// Convert to map object
		Map<String, Object> retMap = JSqlResultToMap(r);
		
		// Enforce policies (if data is valid)
		if (retMap != null) {
			// Add object ID (enforce it)
			retMap.put("oID", _oid);
		}
		
		return retMap;
	}
	
	/// JSQL based Append
	protected void JSqlObjectAppend(JSql sql, String _oid, Map<String, Object> obj, Set<String> keyList,
		boolean handleQuery) throws JSqlException {
		String tName = sqlTableName(sql);
		
		boolean sqlMode = handleQuery ? sql.getAutoCommit() : false;
		if (sqlMode) {
			sql.setAutoCommit(false);
		}
		
		Object[] typSet;
		String k;
		Object v;
		
		for (Map.Entry<String, Object> entry : obj.entrySet()) {
			
			k = (entry.getKey()).toLowerCase();
			if (k.equals("oid") || k.equals("_oid") || k.equals("_otm")) { //reserved
				continue;
			}
			
			if (keyList != null && !keyList.contains(k)) {
				continue;
			}
			
			v = entry.getValue();
			typSet = valueToOptionSet(k, v);
			
			// This is currently only for NON array mode
			sql.upsertQuerySet( //
				tName, //
				new String[] { "oID", "kID", "idx" }, //
				new Object[] { _oid, k, 0 }, //
				//
				new String[] { "typ", "nVl", "sVl", "tVl" }, //
				new Object[] { typSet[0], typSet[1], typSet[2], typSet[3] }, //
				null, null, null//
				).execute();
		}
		
		sql.commit();
		if (sqlMode) {
			sql.setAutoCommit(true);
		}
	}
	
	/// Values to option set conversion
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
	protected Object[] valueToOptionSet(String key, Object value) throws JSqlException {
		if (value instanceof Integer) {
			return new Object[] { new Integer(MetaType.TYPE_INTEGER), value, null, null }; //Typ, N,S,I,T
		} else if (value instanceof String) {
			return new Object[] { new Integer(MetaType.TYPE_STRING), 0, ((String) value).toLowerCase(), value }; //Typ, N,S,I,T
		}
		
		throw new JSqlException("Object type not yet supported: " + value);
	}
	
	//
	// Query operations
	//--------------------------------------------------------------------------
	
	/// Query from JSql layer
	///
	/// @TODO: support array fetches / searches
	/// @TODO: support string lower case search index optimization
	/// @TODO: Protect index names from SQL injections. Since index columns may end up "configurable". This can end up badly for SAAS build
	protected Map<String, ArrayList<Object>> JSqlQuery(JSql sql, String selectCols, String whereClause,
		Object[] whereValues, String orderBy, long limit, long offset) throws JSqlException {
		if (selectCols == null) {
			selectCols = "*";
		} else {
			selectCols.toLowerCase();
		}
		
		if (whereClause != null) {
			whereClause = whereClause.toLowerCase();
		}
		
		return (sql
			.selectQuerySet(sqlViewName(sql, "view"), selectCols, whereClause, whereValues, orderBy, limit, offset)
			.query());
	}
	
	/// SQL Query to fetch the relevent data, values are loaded on query
	public Map<String, ArrayList<Object>> queryData(String selectCols, String whereClause, Object[] whereValues)
		throws JStackException {
		return queryData(selectCols, whereClause, whereValues, null, 0, 0);
	}
	
	/// SQL Query to fetch the relevent data, values are loaded on query
	public Map<String, ArrayList<Object>> queryData(String selectCols, String whereClause, Object[] whereValues,
		String orderBy) throws JStackException {
		return queryData(selectCols, whereClause, whereValues, orderBy, 0, 0);
	}
	
	/// SQL Query to fetch the relevent data, values are loaded on query
	public Map<String, ArrayList<Object>> queryData(String selectCols, String whereClause, Object[] whereValues,
		String orderBy, long limit, long offset) throws JStackException {
		Map<String, ArrayList<Object>> ret = null;
		try {
			JStackLayer[] sl = JStackObj.stackLayers();
			for (int a = 0; a < sl.length; ++a) {
				// JSql specific setup
				if (sl[a] instanceof JSql) {
					ret = JSqlQuery((JSql) (sl[a]), selectCols, whereClause, whereValues, orderBy, limit, offset);
					
					if (ret != null) {
						return ret;
					}
				} else if (sl[a] instanceof JCache) {
					
				}
			}
		} catch (JSqlException e) {
			throw new JStackException(e);
		}
		throw new JStackException("queryData not supported, missing JSql layer?");
	}
	
	//public MetaObject[] queryObjects
	
	//
	// Query pagenation operations (experimental keyset hybrid operations)
	//--------------------------------------------------------------------------
	
	/*
	public Map<String, Map<String, ArrayList<Object>>> queryObjectPage(String selectCols, String whereClause, Object[] whereValues, String orderBy, Map<String, ArrayList<Object>>nowPage, long limit, long offset) throws JStackException {
	
		
	}
	 */

	//
	// Internal PUT / GET object functions
	//--------------------------------------------------------------------------
	/// GET operation used for lazy loading in MetaObject
	protected Map<String, Object> lazyLoadGet(String _oid) throws JStackException {
		// This is the non-lazy load method
		Map<String, Object> ret = null;
		try {
			JStackLayer[] sl = JStackObj.stackLayers();
			for (int a = 0; a < sl.length; ++a) {
				// JSql specific setup
				if (sl[a] instanceof JSql) {
					ret = JSqlObjectGet((JSql) (sl[a]), _oid);
					
					if (ret != null) {
						return ret;
					}
				} else if (sl[a] instanceof JCache) {
					
				}
			}
		} catch (JSqlException e) {
			throw new JStackException(e);
		}
		return null;
	}
	
	/// Update the object map with the given values, List<String> key list is used to 'optimize' sql insertions
	protected void updateMap(String _oid, Map<String, Object> obj, Set<String> keyList) throws JStackException {
		try {
			JStackLayer[] sl = JStackObj.stackLayers();
			for (int a = 0; a < sl.length; ++a) {
				// JSql specific setup
				if (sl[a] instanceof JSql) {
					JSqlObjectAppend((JSql) (sl[a]), _oid, obj, keyList, true);
				} else if (sl[a] instanceof JCache) {
					
				}
			}
		} catch (JSqlException e) {
			throw new JStackException(e);
		}
	}
	
	//
	// APPEND, PUT, GET, REMOVE meta map operations
	//--------------------------------------------------------------------------
	
	/// GET, returns the stored map, note that oID is reserved for the objectID
	public MetaObject get(String _oid) throws JStackException {
		// This is the non-lazy load method
		Map<String, Object> ret = lazyLoadGet(_oid);
		return (ret == null) ? null : new MetaObject(this, _oid, ret);
	}
	
	/// Lazy GET, returns the the object map, loads it data only when its iterated
	public MetaObject lazyGet(String _oid) throws JStackException {
		// This is the lazy load method
		return new MetaObject(this, _oid, null);
	}
	
	/// Generates a new blank object, with a GUID
	public MetaObject newObject() {
		return new MetaObject(this, null, new CaseInsensitiveHashMap<String, Object>());
	}
	
	/// PUT, returns the object ID (especially when its generated), note that this
	/// adds the value in a merger style. Meaning for example, existing values not explicitely
	/// nulled or replaced are maintained
	public MetaObject append(String _oid, Map<String, Object> obj) throws JStackException {
		MetaObject r = null;
		if (obj instanceof MetaObject && ((MetaObject) obj)._oid.equals(_oid)) {
			(r = (MetaObject) obj).saveDelta();
			return r;
		}
		
		r = lazyGet(_oid);
		r.putAll(obj);
		r.saveDelta();
		
		return r;
	}
	
	/// @TODO: Delete operations
	public void remove(String _oid) {
		
	}
}