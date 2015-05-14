package picoded.JStack;

/// Java imports
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.*;
import java.util.List;
import java.util.ArrayList;

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

///
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
	
	/// Default type for all values not defined in the index
	protected CaseInsensitiveHashMap<String, MetaTypes> typeMapping = new CaseInsensitiveHashMap<String, MetaTypes>();
	
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
	/// Internal JSql table setup
	///--------------------------------------------------------------------------
	protected void JSqlIndexConfigTableSetup(JSql sql) throws JSqlException {
		String tName = sql.getNamespace() + tableName + "_viewCfg" + viewSuffix;
		
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
	}
	
	/// JSQL Based tabel data storage
	protected void JSqlDataTableSetup(JSql sql) throws JSqlException {
		String tName = sql.getNamespace() + tableName;
		
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
				"oID", //objID
				"kID", //key storage
				"idx", //index collumn
				// Value storage (except text)
				"typ", //type collumn
				"nVl", //numeric value (if applicable)
				"sVl", //string value (if applicable)
				"iVl", //case insensitive string value (if applicable)
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
				strColumnType, //
				fullTextColumnType } //
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
			tName, "kID, nVl, iVl", null, "valMap" //
		).execute();
		
		// Object timestamp optimized Key Value indexe
		//------------------------------------------------
		sql.createTableIndexQuerySet( //
			tName, "oTm, kID, nVl, iVl", null, "oTm_valMap" //
		).execute();
		
		// Full text index, for textual data
		//------------------------------------------------
		if (sql.sqlType != JSqlType.sqlite) {
			sql.createTableIndexQuerySet( //
				tName, "tVl", "FULLTEXT", "tVl" //
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
	protected int fetchResultPosition(JSqlResult r, String objID, String key, int idx) {
		List<Object> oID_list = r.get("oID");
		List<Object> kID_list = r.get("kID");
		List<Object> idx_list = r.get("idx");
		
		int lim = kID_list.size();
		for (int i = 0; i < lim; ++i) {
			
			if (objID != null && !objID.equals(oID_list.get(i))) {
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
		if (baseType >= MetaTypes.TYPE_INTEGER && baseType <= 34) {
			if (baseType == MetaTypes.TYPE_INTEGER) {
				return new Integer(((Number) (r.get("nVl").get(pos))).intValue());
			}
		} else if (baseType == MetaTypes.TYPE_STRING) { // String
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
	protected Map<String, Object> JSqlObjectGet(JSql sql, String objID) throws JSqlException {
		String tName = sql.getNamespace() + tableName;
		
		// Fetch all the meta fields
		JSqlResult r = sql.selectQuerySet(tName, "*", "oID=?", new Object[] { objID }).query();
		
		// Convert to map object
		Map<String, Object> retMap = JSqlResultToMap(r);
		
		// Enforce policies (if data is valid)
		if (retMap != null) {
			// Add object ID (enforce it)
			retMap.put("oID", objID);
		}
		
		return retMap;
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
			return new Object[] { new Integer(MetaTypes.TYPE_INTEGER), value, null, null, null }; //Typ, N,S,I,T
		} else if (value instanceof String) {
			return new Object[] { new Integer(MetaTypes.TYPE_STRING), 0, value, ((String) value).toLowerCase(), value }; //Typ, N,S,I,T
		}
		
		throw new JSqlException("Object type not yet supported: " + value);
	}
	
	/// JSQL based PUT
	protected void JSqlObjectPut(JSql sql, String objID, Map<String, Object> obj) throws JSqlException {
		String tName = sql.getNamespace() + tableName;
		
		boolean sqlMode = sql.getAutoCommit();
		if (sqlMode) {
			sql.setAutoCommit(false);
		}
		
		Object[] typSet;
		String k;
		Object v;
		
		for (Map.Entry<String, Object> entry : obj.entrySet()) {
			
			k = (entry.getKey()).toLowerCase();
			if (k.equals("oid")) { //reserved
				continue;
			}
			
			v = entry.getValue();
			typSet = valueToOptionSet(k, v);
			
			// This is currently only for NON array mode
			sql.upsertQuerySet( //
				tName, //
				new String[] { "oID", "kID", "idx" }, //
				new Object[] { objID, k, 0 }, //
				//
				new String[] { "typ", "nVl", "sVl", "iVl", "tVl" }, //
				new Object[] { typSet[0], typSet[1], typSet[2], typSet[3], typSet[4] }, //
				null, null, null).execute();
		}
		
		sql.commit();
		if (sqlMode) {
			sql.setAutoCommit(true);
		}
	}
	
	///
	/// PUT and GET meta map operations
	///--------------------------------------------------------------------------
	
	/// GET, returns the stored map, note that oID is reserved for the objectID
	public Map<String, Object> get(String objID) throws JStackException {
		Map<String, Object> ret = null;
		try {
			JStackLayer[] sl = JStackObj.stackLayers();
			for (int a = 0; a < sl.length; ++a) {
				// JSql specific setup
				if (sl[a] instanceof JSql) {
					ret = JSqlObjectGet((JSql) (sl[a]), objID);
					
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
	
	/// PUT, returns the object ID (especially when its generated)
	public String put(String objID, Map<String, Object> obj) throws JStackException {
		if (objID == null || objID.length() < 22) {
			objID = GUID.base58();
		}
		
		try {
			JStackLayer[] sl = JStackObj.stackLayers();
			for (int a = 0; a < sl.length; ++a) {
				// JSql specific setup
				if (sl[a] instanceof JSql) {
					JSqlObjectPut((JSql) (sl[a]), objID, obj);
				} else if (sl[a] instanceof JCache) {
					
				}
			}
		} catch (JSqlException e) {
			throw new JStackException(e);
		}
		
		return objID;
	}
}