package picoded.mmDB.dataStore;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import picoded.jSql.*;
import java.util.logging.*;
import org.apache.commons.lang3.StringUtils;

/// MetaMap, is a simple abstraction of a objectkey, key, value storage in JSql.
/// This handles the internal managment of its SQL table, and extracting of its data.
///
/// *******************************************************************************
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
///
/// metaTable mt = new metaTable( new jSql(), "tableName" )
///                .objColumnType("VARCHAR(60)").keyColumnType("VARCHAR(60)");
/// mt.tableSetup(); //table setup
///
/// mt.put("oKey","hello","world");
/// String test = mt.get("oKey","hello");
/// assertEquals( test, "world" );
///
/// // getObj (and getObjRaw), also exists to get all the key value pairs associated to the object.
/// HashMap<String,String> hObj = mt.getObj("oKey");
///
/// assertEquals( hObj.gets("hello"), "world );
///
/// // This is the raw varient of getObj
/// jSqlResult rObj = mt.getObj_jSql("oKey");
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
///
/// *******************************************************************************
///
public class MetaMap_JSql /*implements Map<String, Map<String,Object>>*/{
	
	//---------------------//
	// Constructor         //
	//---------------------//
	
	/// Constructor with the jSql object, and the deployed table name
	public MetaMap_JSql(JSql inSql, String tableName) {
		JSqlObj = inSql;
		sqlTableName = tableName;
	}
	
	//--------------------------------------------------//
	// Protected variables with public getter functions //
	//--------------------------------------------------//
	
	/// The JSQL object that acts as the persistent object store
	protected JSql JSqlObj = null;
	
	/// Internal SQL table name
	protected String sqlTableName;
	
	/// Returns the initialized tableName
	public String tableName() {
		return sqlTableName;
	}
	
	/// Returns the initialized JSqlObject
	public JSql JSqlObject() {
		return JSqlObj;
	}
	
	//---------------------//
	// Protected variables //
	//---------------------//
	
	/// Internal self used logger
	private static Logger logger = Logger.getLogger(MetaMap_JSql.class.getName());
	
	/// vDci multiplier used to convert the decimal places, to an int value
	protected static int vDciMultiplier = 100000000;
	
	/// Object byte space default as 60
	protected int objColumnLength = 60;
	
	/// Key byte space default as 60
	protected int keyColumnLength = 60;
	
	/// Value byte space default as 4000
	protected int valColumnLength = 4000;
	
	/// Various table collumn names, and classification (used in upsert)?
	protected static String[] allColumnNames = new String[] { //
	"oKey", "mKey", "vIdx", //Object Key, Meta Key, Value Index
		"vStr", "vInt", "vDci", "vTyp", //Value String, INT, Decimal, Type
		"uTim", "cTim", "eTim" //Updated, Created, Expire timestamp
	};
	
	protected static String[] uniqueColumnNames = new String[] { "oKey", "mKey", "vIdx" };
	protected static String[] valueColumnNames = new String[] { "vTyp", "vStr", "vInt", "vDci" };
	protected static String[] timeStampColumnNames = new String[] { "uTim", "cTim", "eTim" };
	
	//-----------------------------------//
	// JSql database storage table setup //
	//-----------------------------------//
	
	/// Performs the required tableSetup for the JSQL database
	public MetaMap_JSql tableSetup() throws JSqlException {
		JSqlObj.execute("CREATE TABLE IF NOT EXISTS `" + sqlTableName + "` (" + // Table Name
			// --------------------------------------------------------------------------
			"oKey VARCHAR(" + objColumnLength + "), " + // Object namespace
			"mKey VARCHAR(" + keyColumnLength + "), " + // Obj.Key namespace
			// --------------------------------------------------------------------------
			"vIdx INT, " + // The value index (multi map support)
			"vTyp INT, " + // The value type (0:null, 1:String, 2:Int/Long, 3:Double, 4:Float)
			// --------------------------------------------------------------------------
			"vStr VARCHAR(" + valColumnLength + "), " + // String or numeric expression value if applicable
			"vInt BIGINT, " + // Int value if applicable
			"vDci INT, " + // Decimal point value if applicable
			// --------------------------------------------------------------------------
			"uTim BIGINT, " + // Last updated time stamp
			"cTim BIGINT, " + // Created time stamp
			"eTim BIGINT " + // Expire time stamp
			")" //
		);
		
		// Create the unique for the table
		// --------------------------------------------------------------------------
		JSqlObj.createTableIndexQuerySet(sqlTableName, StringUtils.join(uniqueColumnNames, ","), "UNIQUE", "unique")
			.execute();
		
		// Create the various indexs used for the table
		// --------------------------------------------------------------------------
		JSqlObj.createTableIndexQuerySet(sqlTableName, "oKey").execute();
		JSqlObj.createTableIndexQuerySet(sqlTableName, "mKey").execute();
		JSqlObj.createTableIndexQuerySet(sqlTableName, "vIdx").execute();
		
		//Value string index, is FULLTEXT in mysql (as normal index does not work)
		if (JSqlObj.sqlType == JSqlType.mysql) {
			JSqlObj.createTableIndexQuerySet(sqlTableName, "vStr", "FULLTEXT").execute();
		} else {
			JSqlObj.createTableIndexQuerySet(sqlTableName, "vStr").execute();
		}
		
		JSqlObj.createTableIndexQuerySet(sqlTableName, "vInt").execute();
		JSqlObj.createTableIndexQuerySet(sqlTableName, "vDci").execute();
		JSqlObj.createTableIndexQuerySet(sqlTableName, "vTyp").execute();
		
		JSqlObj.createTableIndexQuerySet(sqlTableName, "uTim").execute();
		JSqlObj.createTableIndexQuerySet(sqlTableName, "cTim").execute();
		JSqlObj.createTableIndexQuerySet(sqlTableName, "eTim").execute();
		
		return this;
	}
	
	/// Performs the required tableSetup for the JSQL database, after defining the various column lengths
	public MetaMap_JSql tableSetup(int inObjColumnLength, int inKeyColumnLength, int inValColumnLength)
		throws JSqlException {
		objColumnLength = inObjColumnLength;
		keyColumnLength = inKeyColumnLength;
		valColumnLength = inValColumnLength;
		return tableSetup();
	}
	
	/// Drops the database table with all relevant data
	public MetaMap_JSql tableDrop() throws JSqlException {
		JSqlObj.execute("DROP TABLE IF EXISTS `" + sqlTableName + "`");
		return this;
	}
	
	//-----------------------------------//
	// Meta data put commands            //
	//-----------------------------------//
	
	/// Internal usage putKeyValue, note that this does not handle any of the storage logic
	private boolean putKeyValueRaw( //
		String oKey, String mKey, int vIdx, // unique identifier
		int vTyp, // type identifier
		String vStr, long vInt, int vDci, // value storage
		long expireUnixTime // expire timestamp, if applicable
	) throws JSqlException {
		
		long nowTime = (System.currentTimeMillis() / 1000L);
		
		//-1 expire time, means ignore, use misc field
		if (expireUnixTime < 0) {
			// JSql based upsert
			return JSqlObj.upsertQuerySet( //prepare querySet
				//
				// Table Name
				//----------------------------------------
				sqlTableName,
				//
				// Unique Values
				//----------------------------------------
				uniqueColumnNames, // { "oKey", "mKey", "vIdx" }
				new Object[] { oKey, mKey, vIdx },
				//
				// Insert Values
				//----------------------------------------
				new String[] { "vTyp", "vStr", "vInt", "vDci", "uTim" }, //
				new Object[] { vTyp, vStr, vInt, vDci, nowTime },
				//
				// Default Values (created timestamp)
				//----------------------------------------
				new String[] { "cTim" }, new Object[] { nowTime },
				//
				// Misc value to preserve, such as expire timestamp
				//----------------------------------------
				new String[] { "eTim" } //
				).execute();
		} else {
			
			// JSql based upsert
			return JSqlObj.upsertQuerySet( //prepare querySet
				//
				// Table Name
				//----------------------------------------
				sqlTableName,
				//
				// Unique Values
				//----------------------------------------
				uniqueColumnNames, // { "oKey", "mKey", "vIdx" }
				new Object[] { oKey, mKey, vIdx },
				//
				// Insert Values
				//----------------------------------------
				new String[] { "vTyp", "vStr", "vInt", "vDci", "uTim", "eTim" }, //
				new Object[] { vTyp, vStr, vInt, vDci, nowTime, expireUnixTime },
				//
				// Default Values (created timestamp)
				//----------------------------------------
				new String[] { "cTim" }, new Object[] { nowTime },
				//
				// Misc value to preserve, such as expire timestamp
				//----------------------------------------
				null //
				).execute();
		}
	}
	
	// Dynamic putKeyValue call, based on value object type. With expire unix timestamp
	public boolean putKeyValue(String oKey, String mKey, int vIdx, Object val, long expireUnixTime) throws JSqlException {
		
		// return putKeyValueRaw(oKey, mKey, vIdx, vTyp, vStr, vInt, vDci, expireUnixTime)
		
		// Evaluating the various put values
		if (val == null) {
			return putKeyValueRaw(oKey, mKey, vIdx, 0, null, //
				0, 0, expireUnixTime);
		} else if (String.class.isInstance(val)) {
			return putKeyValueRaw(oKey, mKey, vIdx, 1, (val.toString()), //
				0, 0, expireUnixTime);
		} else if (Integer.class.isInstance(val) || Long.class.isInstance(val)) {
			return putKeyValueRaw(oKey, mKey, vIdx, 2, null, //
				((Number) val).longValue(), 0, expireUnixTime);
		} else if (Double.class.isInstance(val)) {
			double dVal = ((Double) val).doubleValue();
			long vInt = (long) Math.floor(dVal);
			int vDci = (int) ((dVal - (double) vInt) * (double) vDciMultiplier);
			
			return putKeyValueRaw(oKey, mKey, vIdx, 3, null, //
				vInt, vDci, expireUnixTime);
		} else if (Float.class.isInstance(val)) {
			float dVal = ((Float) val).floatValue();
			long vInt = (long) Math.floor(dVal);
			int vDci = (int) ((dVal - (float) vInt) * (float) vDciMultiplier);
			
			return putKeyValueRaw(oKey, mKey, vIdx, 4, null, //
				vInt, vDci, expireUnixTime);
		} else {
			String valClassName = val.getClass().getName();
			throw new JSqlException("Unknown value object type : " + (valClassName));
		}
		
		//return false;
	}
	
	// Dynamic putKeyValue call, based on value object type
	public boolean putKeyValue(String oKey, String mKey, int vIdx, Object val) throws JSqlException {
		return putKeyValue(oKey, mKey, vIdx, val, -1);
	}
	
	//-----------------------------------//
	// Meta data get commands            //
	//-----------------------------------//
	
	// The internal get call
	private JSqlResult getKeyValueRaw(String oKey, String mKey, int vIdx, int limit) throws JSqlException {
		return JSqlObj.selectQuerySet(
		//prepare select querySet
			sqlTableName, //
			null, // SELECT *
			"oKey=? AND mKey=? AND vIdx=?", // Where keys
			new Object[] { oKey, mKey, vIdx }, // where values
			"vIdx ASC", //order by
			limit, vIdx // select indexes
			).query();
	}
	
	public Object getValueFromResult(JSqlResult jRes, int vPt) {
		Object vTypObj = jRes.readRowCol(vPt, "vTyp");
		Object vStrObj = jRes.readRowCol(vPt, "vStr");
		Object vIntObj = jRes.readRowCol(vPt, "vInt");
		Object vDciObj = jRes.readRowCol(vPt, "vDci");
		
		int vTyp = ((Number) vTypObj).intValue();
		
		return null;
	}
	
	public Object getKeyValue(String oKey, String mKey, int vIdx) throws JSqlException {
		//return getKeyValueRaw(oKey, mKey, vIdx, 1);
		return null;
	}
	
	/*
	 
	 
	
	
	

	/// Fetches oKey / key / value
	public String get(String oKey, String key) throws jSqlException {
		jSqlResult r = jSqlObj.query("SELECT * FROM " + sqlTableName + " WHERE obj=? AND metaKey=?", oKey, key);

		if (r.fetchAllRows() > 0) {
			return (String) r.readRowCol(0, "val");
		}
		return null;
	}

	/// Fetches oKey / key / value, using partial matching search (SQL LIKE)
	public String[] getMultiple(String oKey, String key) throws jSqlException {
		jSqlResult r = jSqlObj.query("SELECT * FROM " + sqlTableName + " WHERE obj LIKE ? AND metaKey LIKE ?", oKey, key);
		
		String[] res = null;
		int len;
		if ((len = r.fetchAllRows()) > 0) {
			res = new String[len];
			for (int pt = 0; pt < len; pt++) {
				res[pt] = (String) r.readRowCol(pt, "val");
			}
		}
		return res;
	}
	
	/// Fetches oKey / key / value, using partial matching search (SQL LIKE)
	public HashMap<String, String> getMultipleSet(String oKey, String key, String val) throws jSqlException {
		jSqlResult r = jSqlObj.query("SELECT * FROM " + sqlTableName + " WHERE obj LIKE ? AND metaKey LIKE ? AND val LIKE ?", oKey, key, val);
		
		HashMap<String, String> res = new HashMap<String, String>();
		int pt = r.rowCount();
		if (r.fetchAllRows() > 0) {
			for (int i = 0; i < pt; i++) {
				res.put((String) r.readRowCol(i, "metaKey"), (String) r.readRowCol(i, "val"));
			}
			return res;
		}
		return null;
	}
	
	/// Deletes all records related to an object
	public boolean deleteObj(String oKey) throws jSqlException {
		return jSqlObj.execute("DELETE FROM `" + sqlTableName + "` WHERE obj=?;", oKey);
	}

	/// Gets all the key value pairs related to the object. And returns as as
	// HashMap< metaKey, value >
	public HashMap<String, String> getObj(String oKey) throws jSqlException {
		jSqlResult r = getObj_jSql(oKey);

		HashMap<String, String> res = new HashMap<String, String>();
		int pt = r.rowCount();
		if (r.fetchAllRows() > 0) {
			for (int i = 0; i < pt; i++) {
				res.put((String) r.readRowCol(i, "metaKey"), (String) r.readRowCol(i, "val"));
			}
			return res;
		}
		return null;
	}

	/// Returns the jSqlResult raw varient
	public jSqlResult getObj_jSql(String oKey) throws jSqlException {
		return jSqlObj.query("SELECT * FROM `" + sqlTableName + "` WHERE obj=?;", oKey);
	}

	/// Returns the jSqlResult raw varient
	/// [TODO: High priority]
	public jSqlResult getAll_jSql() throws jSqlException {
		return jSqlObj.query("SELECT * FROM `" + sqlTableName + "`");
	}

	/// Fetches everything, in oKey, key, value pairs
	public HashMap<String, HashMap<String, Object>> getAll() throws jSqlException {
		jSqlResult r = getAll_jSql();

		HashMap<String, HashMap<String, Object>> res = new HashMap<String, HashMap<String, Object>>();
		HashMap<String, Object> objMap = null;
		String prvoKey = null;
		String oKey = null;
		int len;

		if ((len = r.fetchAllRows()) > 0) {
			for (int i = 0; i < len; i++) {
				// Gets current object id
				oKey = (String) r.readRowCol(i, "obj");

				// Gets the previous objMap, if needed
				if (prvoKey != oKey) {
					objMap = res.get(oKey);

					// result set dosent have the object map yet
					if (objMap == null) {
						objMap = new HashMap<String, Object>();
						res.put(oKey, objMap);
					}

					prvoKey = oKey;
				} // else assumes there isnt a need to change objMap

				objMap.put((String) r.readRowCol(i, "metaKey"), (String) r.readRowCol(i, "val"));
			}
		}
		return res;
	}

	/// Fetches an array of objectid given key & value, meached to the val
	/// actually unstable : THIS IS NOT ORACLE COMPETIBLE
	@Deprecated
	public String[] getKeyValMap(String key, String val) throws jSqlException {
		jSqlResult r = getKeyValMap_jSql(key, val);

		String[] res = null;
		int len;
		if ((len = r.fetchAllRows()) > 0) {
			res = new String[len];
			for (int pt = 0; pt < len; pt++) {
				res[pt] = (String) r.readRowCol(pt, "obj");
			}
		}
		return res;
	}

	/// Returns the jSqlResult raw varient
	/// actually unstable : THIS IS NOT ORACLE COMPETIBLE
	@Deprecated
	public jSqlResult getKeyValMap_jSql(String key, String val) throws jSqlException {
		return jSqlObj.query("SELECT obj FROM `" + sqlTableName + "` WHERE metaKey=? AND val like ?", key, val);
	}

	// Fetches a HashMap<Objectid, key> matching to a val
	// [TODO: Low priority]
	public HashMap<String, String> getKeyMap(String val) throws jSqlException {
		// throw new jSqlException("Not Yet Implemented");
		jSqlResult r = getKeyMap_jSql(val);
		HashMap<String, String> res = null;
		int pt = r.rowCount();
		if (r.fetchAllRows() > 0) {
			for (int i = 0; i < pt; i++) {
				res = new HashMap<String, String>();
				Set<Map.Entry<String, Object>> mapEntrySet = r.readRow(i).entrySet();
				for (Map.Entry<String, Object> mapEntry : mapEntrySet) {
					res.put(mapEntry.getKey(), (String) mapEntry.getValue());
				}
			}
		}
		return res;
	}

	/// Returns the jSqlResult raw varient
	/// [TODO: Mid priority]
	public jSqlResult getKeyMap_jSql(String val) throws jSqlException {
		return jSqlObj.query("SELECT obj, metaKey FROM `" + sqlTableName + "` WHERE val like ?", val);
	}

	/// Fetches an array of keys, matching the object id and value
	/// [TODO: Mid priority]
	public String[] getObjKeyMap(String oKey, String val) throws jSqlException {
		// throw new jSqlException("Not Yet Implemented");
		jSqlResult r = getObjKeyMap_jSql(oKey, val);
		String[] res = null;
		int pt = r.rowCount();
		int len;
		if ((len = r.fetchAllRows()) > 0) {
			res = new String[len];
			for (pt = 0; pt < len; pt++) {
				res[pt] = (String) r.readRowCol(pt, "metaKey");
			}
		}
		return res;
	}

	/// Returns the jSqlResult raw varient
	/// [TODO: Mid priority]
	public jSqlResult getObjKeyMap_jSql(String oKey, String val) throws jSqlException {
		return jSqlObj.query("SELECT metaKey FROM `" + sqlTableName + "` WHERE obj=? AND val like ?", oKey, val);
	}

	/// Fetches a HashMap<oKey, val> matched to key
	/// [TODO: Mid priority]
	public HashMap<String, Object> getValMap(String key) throws jSqlException {
		// throw new jSqlException("Not Yet Implemented");
		try {
			HashMap<String, Object> res = null;
			jSqlResult r = getValMap_jSql(key);
			int pt = r.rowCount();
			if (r.fetchAllRows() > 0) {
				for (int i = 0; i < pt; i++) {
					// res = new HashMap<String, Object>();
					res = r.readRow(i);
				}
			}
			return res;
		} catch (jSqlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/// Returns the jSqlResult raw varient
	/// [TODO: Mid priority]
	public jSqlResult getValMap_jSql(String key) throws jSqlException {
		return jSqlObj.query("SELECT obj, val FROM `" + sqlTableName + "` WHERE metaKey like ?", key);
	}
	 
	/// Empty the table of all data
	/// [TODO: Low priority]
	public void clear() throws jSqlException {
		throw new jSqlException("Not Yet Implemented");
	}

	/// returns true if database table is empty
	/// [TODO: Low priority]
	public boolean isEmpty() throws jSqlException {
		throw new jSqlException("Not Yet Implemented");
	}

	/// returns database number of objects, key, values sets
	/// [TODO: Low priority]
	public long size() throws jSqlException {
		throw new jSqlException("Not Yet Implemented");
	}

	/// returns the total number of objects
	/// [TODO: Low priority]
	public long objectCount() throws jSqlException {
		throw new jSqlException("Not Yet Implemented");
	}

	/// returns the meta count in an object
	/// [TODO: Low priority]
	public long objectMetaCount(String oKey) throws jSqlException {
		throw new jSqlException("Not Yet Implemented");
	}
	 */
}