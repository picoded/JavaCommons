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
import picoded.struct.UnsupportedDefaultMap;
import picoded.conv.ListValueConv;

/// hazelcast
import com.hazelcast.core.*;
import com.hazelcast.config.*;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/// MetaTable, servs as the core flexible backend storage implmentation for the whole
/// JStack setup. Its role can be viewed similarly to NoSql, or AWS SimpleDB
/// where almost everything is indexed and cached. 
/// 
/// On a performance basis, it is meant to trade off raw query performance of traditional optimized 
/// SQL lookup, over flexibility in data model. This is however heavily mintigated by the inclusion 
/// of a JCache layer for non-complex lookup cached reads. Which will in most cases be the main
/// read request load.
/// 
public class MetaTable extends JStackData implements UnsupportedDefaultMap<String, MetaObject> {

	///
	/// Constructor setup
	///--------------------------------------------------------------------------

	/// Setup the metatable with the default table name
	public MetaTable(JStack inStack) {
		super( inStack );
		tableName = "MetaTable";
	}

	/// Setup the metatable with the given stack
	public MetaTable(JStack inStack, String inTableName) {
		super( inStack, inTableName );
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
	/// Indexed columns internal vars
	///--------------------------------------------------------------------------

	/// SQL view name
	protected String sqlViewName(JSql sql, String vTyp) {
		return (sql.getTablePrefix() + tableName + "_" + vTyp + viewSuffix);
	}

	/// Default type for all values not defined in the index
	protected MetaTypeMap typeMapping = new MetaTypeMap( this );

	///
	/// Indexed columns actual setup
	///--------------------------------------------------------------------------

	/// Returned the defined metaType for the given key
	public MetaType getType(String name) { return typeMapping.get(name); }

	/// Sets the defined metaType for the given key
	public MetaTable putType(String name, MetaType type) { typeMapping.put(name, type); return this; }
	public MetaTable putType(String name, Object type) { typeMapping.putObject(name, type); return this; }
	
	/// Set all type metting
	public MetaTable setMapping(Map<String, Object> nameToTypeMap) { typeMapping.putObjectMap(nameToTypeMap); return this; }

	/// Clears the type mapping settings
	public void clearTypeMapping() { typeMapping.clear();	}

	/// Returns the list of typemappign keys
	public Set<String> listTypeMappingKeys() { return(typeMapping.keySet()); }
	
	/// Returns the list of typemapping values
	public Collection<MetaType> listTypeMappingValues() { return typeMapping.values(); }

	///
	/// Internal JSql table setup and teardown
	///--------------------------------------------------------------------------

	/// Setsup the respective JSql table
	@Override
	protected boolean JSqlSetup(JSql sql) throws JSqlException, JStackException {
		String tName = sqlTableName(sql);
		
		// Setup the main MetaTable
		//------------------------------------------------
		MetaTable_JSql.JSqlSetup(sql, tName, this);

		// Index view config setup
		//------------------------------------------------
		JSqlIndexConfigTableSetup(sql);

		return true;
	}

	/// Removes the respective JSQL tables and view (if it exists)
	@Override
	protected boolean JSqlTeardown(JSql sql) throws JSqlException, JStackException {
		// Drop the view
		try {
			sql.execute("DROP VIEW " + sqlViewName(sql, "view"));
		} catch(JSqlException e) {
			// This is silenced, as JSql does not support "DROP VIEW IF NOT EXISTS"
			// @TODO: drop view if not exists to JSql, this is under issue: 
			// http://gitlab.picoded-dev.com/picoded/javacommons/issues/69
		}
		sql.execute("DROP TABLE IF EXISTS " + sqlViewName(sql, "vCfg"));
		sql.execute("DROP TABLE IF EXISTS " + sqlTableName(sql));
		return true;
	}

	///
	/// Internal JSql index setup
	///--------------------------------------------------------------------------

	/// Setsup the index view configuration table,
	/// @TODO : To check against configuration table, and makes the changes ONLY when needed
	protected void JSqlIndexConfigTableSetup(JSql sql) throws JSqlException {
		typeMapping.JSqlIndexViewFullBuild( //
			sqlViewName(sql, "vCfg"), //
			sqlViewName(sql, "view"), //
			sqlTableName(sql), //
			sql // 
		); //
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
		} else if (baseType == MetaType.TYPE_TEXT) { // Text
			return (String) (r.get("tVl").get(pos));
		} else {

		}
		throw new JSqlException("Object type not yet supported: "+key+" = "+ baseType);
		//return null;
	}

	/// MetaTable JSqlResult to CaseInsensitiveHashMap
	protected Map<String, Object> JSqlResultToMap(JSqlResult r) throws JSqlException {
		if (r != null && r.rowCount() <= 0) {
			return null;
		}

		// Get all the unique keys
		String[] keys = extractUnique(r.get("kID"));

		// Extract the respective key values
		Map<String, Object> retMap = new CaseInsensitiveHashMap<String, Object>();
		for (int a = 0; a < keys.length; ++a) {
			//if (keys[a].equals("_oid")) { //reserved
			//	continue;
			//}
			retMap.put(keys[a], extractKeyValue(r, keys[a]));
		}

		return retMap;
	}

	/// JSQL based GET
	@SuppressWarnings("unchecked")
	protected Map<String, Object> JSqlObjectGet(JSql sql, String _oid) throws JSqlException {
		try {
			Object r = JStackIterate( new JStackReader() {
				/// Reads only the JSQL layer
				public Object readJSqlLayer(JSql sql, Object ret) throws JSqlException, JStackException {
					if( ret != null ) {
						return ret;
					}
	
					String tName = sqlTableName(sql);
	
					// Fetch all the meta fields
					JSqlResult r = sql.selectQuerySet( tName, "*", "oID=?", new Object[] { _oid } ).query();
					
					// Convert to map object
					Map<String, Object> retMap = JSqlResultToMap(r);
					
					// Enforce policies (if data is valid)
					if( retMap != null ) {
						// Add object ID (enforce it)
						retMap.put("_oid", _oid);
					}
	
					return retMap;
				}
			} );
			
			return ((r != null)? (Map<String, Object>)r : null);
		} catch (JStackException e) {
			throw new RuntimeException(e);
		}

	}

	/// JSQL based Append
	protected void JSqlObjectAppend(JSql sql, String _oid, Map<String, Object> obj, Set<String> keyList,
		boolean handleQuery) throws JSqlException {

		// boolean sqlMode = handleQuery ? sql.getAutoCommit() : false;
		// if (sqlMode) {
		// 	sql.setAutoCommit(false);
		// }

		try {

			Object ret = JStackReverseIterate( new JStackReader() {
				/// Reads only the JSQL layer
				public Object readJSqlLayer(JSql sql, Object ret) throws JSqlException, JStackException {
					String tName = sqlTableName(sql);

					Object[] typSet;
					String k;
					Object v;
		
					for (Map.Entry<String, Object> entry : obj.entrySet()) {
		
						k = (entry.getKey()).toLowerCase();
						if ( /*k.equals("oid") || k.equals("_oid") ||*/ k.equals("_otm")) { //reserved
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
					return ret;
				}
			} );
			
			//sql.commit();
		} catch (JStackException e) {
			throw new RuntimeException(e);
		} finally {
			// if (sqlMode) {
			// 	sql.setAutoCommit(true);
			// }
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

		throw new JSqlException("Object type not yet supported: "+key+" = "+ value);
	}

	//
	// Query operations
	//--------------------------------------------------------------------------

	/// Query from JSql layer
	///
	/// @TODO: support array fetches / searches
	/// @TODO: support string lower case search index optimization
	/// @TODO: Protect index names from SQL injections. Since index columns may end up "configurable". This can end up badly for SAAS build
	protected Map<String, List<Object>> JSqlQuery(JSql sql, String selectCols, String whereClause,
		Object[] whereValues, String orderBy, long limit, long offset) throws JSqlException {
		if (selectCols == null) {
			selectCols = "_oid";
		} else {
			selectCols.toLowerCase();

			if (selectCols.indexOf("_oid") == -1) {
				selectCols = "_oid, " + selectCols;
			}
		}

		if (whereClause != null) {
			whereClause = whereClause.toLowerCase();
		}
		
		return (sql.selectQuerySet(sqlViewName(sql, "view"), selectCols, whereClause, whereValues, orderBy, limit, offset).query());  

/*
		final String where = whereClause;
		final String cols = selectCols;

		try {
			Object ret = JStackIterate( new JStackReader() {
				/// Reads only the JSQL layer
				public Object readJSqlLayer(JSql sql, Object ret) throws JSqlException, JStackException {
					if( ret != null ) {
						return ret;
					}
	
					String tName = sqlTableName(sql);
	
					JSqlResult r = sql
						.selectQuerySet(sqlViewName(sql, "view"), cols, where, whereValues, orderBy, limit, offset)
						.query();
						
					// Has value
					if( r.rowCount() > 0 ) {
						return r;
					}
	
					return ret;
				}
			} );
	
			return (ret != null)? (Map<String, List<Object>>) ret : null;
		} catch (JStackException e) {
			throw new RuntimeException(e);
		}*/
	}

	/// SQL Query to fetch the relevent data, values are loaded on query
	protected Map<String, List<Object>> queryData(String selectCols, String whereClause, Object[] whereValues,
		String orderBy, long limit, long offset) throws JStackException {
		Map<String, List<Object>> ret = null;
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

	/// Does a conversion from the JSqlResult map, to the
	protected MetaObject[] JSqlResultToMetaObjectArray(Map<String, List<Object>> jRes) throws JStackException {
		if( jRes == null ) {
			return new MetaObject[0];
		}

		List<Object> oID_list = jRes.get("_oid");
		if (oID_list == null || oID_list.size() <= 0) {
			return new MetaObject[0];
		}

		int len = oID_list.size();

		MetaObject[] ret = new MetaObject[len];

		CaseInsensitiveHashMap<String, Object> queryCache = new CaseInsensitiveHashMap<String, Object>();
		MetaObject mObj;

		for (int a = 0; a < len; ++a) {
			mObj = lazyGet((String) (oID_list.get(a)));
			queryCache = new CaseInsensitiveHashMap<String, Object>();

			for (Map.Entry<String, List<Object>> entry : jRes.entrySet()) {
				queryCache.put(entry.getKey(), entry.getValue().get(a));
			}

			mObj.queryDataMap = queryCache;
			ret[a] = mObj;
		}

		return ret;
	}

	/// Performs a search query, and returns the respective relevent objects
	public MetaObject[] queryObjects(String whereClause, Object[] whereValues, String orderBy, long limit, long offset,
		String optimalSelect) throws JStackException {

		Map<String, List<Object>> qData = queryData(optimalSelect, whereClause, whereValues, orderBy, limit, offset);
		return JSqlResultToMetaObjectArray(qData);
	}

	/// Performs a search query, and returns the respective relevent objects
	public MetaObject[] queryObjects(String whereClause, Object[] whereValues, String orderBy, long limit, long offset)
		throws JStackException {
		return queryObjects(whereClause, whereValues, orderBy, limit, offset, null);
	}

	/// Performs a search query, and returns the respective relevent objects
	public MetaObject[] queryObjects(String whereClause, Object[] whereValues, String orderBy) throws JStackException {
		return queryObjects(whereClause, whereValues, orderBy, 0, 0, null);
	}

	/// Performs a search query, and returns the respective relevent objects
	public MetaObject[] queryObjects(String whereClause, Object[] whereValues) throws JStackException {
		return queryObjects(whereClause, whereValues, null, 0, 0, null);
	}

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

	/// Checks if the MetaObject exists
	public boolean containsKey(Object oid) {
		try {
			String _oid = oid.toString();
			return (lazyLoadGet(_oid) != null);
		} catch(JStackException e) {
			throw new RuntimeException(e);
		}
	}

	/// GET, returns the stored map, note that oID is reserved for the objectID
	///
	/// Note: get("new") is syntax sugar for newObject();
	public MetaObject get(Object oid) {
		try {
			String _oid = oid.toString();
			
			if( _oid.toLowerCase().equals("new") ) {
				return newObject();
			}
			
			Map<String, Object> ret = lazyLoadGet(_oid);
			return (ret == null) ? null : new MetaObject(this, _oid, ret);
		} catch(JStackException e) {
			throw new RuntimeException(e);
		}
	}

	/// Lazy GET, returns the the object map, loads it data only when its iterated
	public MetaObject lazyGet(String _oid) throws JStackException {
		// This is the lazy load method
		return new MetaObject(this, _oid, null);
	}

	/// Generates a new blank object, with a GUID
	public MetaObject newObject() {
		try {
			MetaObject ret = new MetaObject(this, null, new HashMap<String, Object>());
			ret.saveAll(); //ensures the blank object is now in DB
			return ret;
		} catch(JStackException e) {
			throw new RuntimeException(e);
		}
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
		throw new RuntimeException("Not supported yet");
	}
	
	//
	// Key based operation search. 
	// 
	// Note that this does not rely on the complex
	// query generator, as such has limited functionality.
	//--------------------------------------------------------------------------
	
	/// Gets an array of MetaObject, from the list of string GUID arrays
	public MetaObject[] lazyGetArray(String[] _oidList) throws JStackException {
		MetaObject[] mList = new MetaObject[_oidList.length];
		for(int a=0; a<_oidList.length; ++a) {
			mList[a] = lazyGet( _oidList[a] );
		}
		return mList;
	}
	
	/// Gets the list of meta objects GUID, with any value (even null) 
	/// from the provided key name
	protected String[] getFromKeyNames_id(String key) throws JStackException {
		List<String> sList = new ArrayList<String>();
		
		JStackIterate( new JStackReader() {
			/// Reads only the JSQL layer
			public Object readJSqlLayer(JSql sql, Object ret) throws JSqlException, JStackException {
				JSqlResult r = sql.selectQuerySet( sqlTableName(sql), "oID", "kID = ?", new Object[] { key } ).query();
				List<Object> oList = r.get("oID");
				if( oList != null ) {
					sList.addAll( ListValueConv.objectToString(oList) );
				}
				return ret;
			}
		}, sList );
	
		return sList.toArray(new String[sList.size()]);
	}
	
	/// Gets an array of MetaObject, with any value (even null) 
	/// from the provided key name
	public MetaObject[] getFromKeyNames(String key) throws JStackException {
		return lazyGetArray( getFromKeyNames_id(key) );
	}
	
}
