package picoded.mmDB;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import picoded.jSql.*;
import java.util.logging.*;

/// MetaMap, is a simple abstraction of a objectkey, key, value storage. This extends the Map<String, MultiMap<String, Object>> class.
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
public class MetaMap /*implements Map<String, Map<String,Object>>*/{
	
	//---------------------//
	// Constructor         //
	//---------------------//
	
	/// Constructor with the jSql object, and the deployed table name
	public MetaMap(JSql inSql, String tableName) {
		JSqlObj = inSql;
		sqlTableName = tableName;
	}
	
	//---------------------//
	// Protected variables //
	//---------------------//
	
	/// vDci multiplier used to convert the decimal places, to an int value
	protected static int vDciMultiplier = 100000000;
	
	/// Internal self used logger
	private static Logger logger = Logger.getLogger(MetaMap.class.getName());
	
	/// The JSQL object that acts as the persistent object store
	protected JSql JSqlObj = null;
	
	/// Internal SQL table name
	protected String sqlTableName;
	
	/// Object byte space default as 60
	protected int objColumnLength = 60;
	
	/// Key byte space default as 60
	protected int keyColumnLength = 60;
	
	/// Value byte space default as 4000
	protected int valColumnLength = 4000;
	
	/// Various table collumn names, and classification (used in upsert)?
	protected String[] allColumnNames = new String[] { //
	"oKey", "mKey", "vIdx", //
	   "vStr", "vInt", "vDci", "vTyp", //
	   "uTim", "cTim", "eTim" //
	};
	protected String[] uniqueColumnNames = new String[] { "oKey", "mKey", "vIdx" };
	protected String[] valueColumnNames = new String[] { "vStr", "vInt", "vDci", "vTyp" };
	protected String[] timeStampColumnNames = new String[] { "uTim", "cTim", "eTim" };
	protected String[] nonUniqueColumnNames = new String[] { "vStr", "vInt", "vDci", "vTyp", "uTim", "cTim", "eTim" };
	
	//--------------------------------------//
	// Protected variables getter functions //
	//--------------------------------------//
	
	/// Returns the initialized tableName
	public String tableName() {
		return sqlTableName;
	}
	
	/// Returns the initialized JSqlObject
	public JSql JSqlObject() {
		return JSqlObj;
	}
	
	//-----------------------------------//
	// JSql database storage table setup //
	//-----------------------------------//
	
	/// Helper function, that logs the query, then execute it
	private void logAndExecute(String qStr) throws JSqlException {
		logger.finer(qStr);
		JSqlObj.execute(qStr);
	}
	
	/// Helper function, creates a non unique index for hte given collumn type
	private void createJSqlIndex(String collumnName) throws JSqlException {
		logAndExecute("CREATE INDEX IF NOT EXISTS `" + sqlTableName + "_" + collumnName + "` ON `" + sqlTableName + "` ("
		   + collumnName + ")");
	}
	
	/// Performs the required tableSetup for the JSQL database, after defining the various column lengths
	public MetaMap tableSetup(int inObjColumnLength, int inKeyColumnLength, int inValColumnLength) throws JSqlException {
		objColumnLength = inObjColumnLength;
		keyColumnLength = inKeyColumnLength;
		valColumnLength = inValColumnLength;
		return tableSetup();
	}
	
	/// Performs the required tableSetup for the JSQL database
	public MetaMap tableSetup() throws JSqlException {
		logAndExecute("CREATE TABLE IF NOT EXISTS `" + sqlTableName + "` (" + // Table Name
		   // --------------------------------------------------------------------------
		   "oKey VARCHAR(" + objColumnLength + "), " + // Object namespace
		   "mKey VARCHAR(" + keyColumnLength + "), " + // Obj.Key namespace
		   // --------------------------------------------------------------------------
		   "vIdx INT, " + // The value index (multi map support)
		   "vStr VARCHAR(" + valColumnLength + "), " + // String or numeric expression value if applicable
		   "vInt BIGINT, " + // Int value if applicable
		   "vDci INT, " + // Decimal point value if applicable
		   "vTyp INT, " + // The value type (0:null, 1:String, 2:Int/Long, 3:Double, 4:Float)
		   // --------------------------------------------------------------------------
		   "uTim BIGINT, " + // Last updated time stamp
		   "cTim BIGINT, " + // Created time stamp
		   "eTim BIGINT " + // Expire time stamp
		   ")");
		
		// Create the various indexs used for the table
		// --------------------------------------------------------------------------
		logAndExecute("CREATE UNIQUE INDEX IF NOT EXISTS `" + sqlTableName + "_unique` ON `" + sqlTableName
		   + "` (oKey, mKey, vIdx)");
		
		createJSqlIndex("oKey");
		createJSqlIndex("mKey");
		createJSqlIndex("vIdx");
		
		if (JSqlObj.sqlType == JSqlType.mysql) { //Value string index, is FULLTEXT in mysql (as normal index does not work)
			logAndExecute("CREATE FULLTEXT INDEX IF NOT EXISTS `" + sqlTableName + "_vStr` ON `" + sqlTableName
			   + "` (vStr)");
		} else {
			createJSqlIndex("vStr");
		}
		
		createJSqlIndex("vInt");
		createJSqlIndex("vDci");
		createJSqlIndex("vTyp");
		
		createJSqlIndex("uTim");
		createJSqlIndex("cTim");
		createJSqlIndex("eTim");
		
		return this;
	}
	
	/// Drops the database table with all relevant data
	public MetaMap tableDrop() throws JSqlException {
		logAndExecute("DROP TABLE IF EXISTS `" + sqlTableName + "`");
		return this;
	}
	
	public Object getKeyValue(String oKey, String mKey) throws JSqlException {
		JSqlResult r = JSqlObj.query("SELECT * FROM " + sqlTableName + " WHERE oKey LIKE ? AND mKey LIKE ? LIMIT 1",
		   oKey, mKey);
		
		return null;
	}
	
	// Internal usage putKeyValue
	private boolean putKeyValueWith(String oKey, String mKey, String vStr, long vInt, int vDci, int vIdx, int vTyp,
	   long expireUnixTime) throws JSqlException {
		
		if (expireUnixTime <= 0) {
			expireUnixTime = 0;
		}
		
		long nowTime = (System.currentTimeMillis() / 1000L);
		
		if (JSqlObj.sqlType == JSqlType.sqlite) {
			
			/*
			Object[] querySet = JSqlObj.prepareUpsertQuerySet( //
				sqlTableName, //
			   uniqueColumnNames, null, //
			   nonUniqueColumnNames, null, null, //
			   null //
			   );
			*/
			//JSqlObj.execute( JSqlObj.prepareUpsertQuery(  ), prepareUpsertArguments() );
			
			/*
			 JSqlObj.execute(
								  "INSERT OR REPLACE INTO `" + sqlTableName + "` (oKey,mKey,vIdx, vStr,vInt,vDci,vTyp) VALUES (?,?,?,?,?,?,?)",
								  oKey, mKey, vIdx,
								  vStr, vInt, vDci, vTyp
								);
			 */
		}
		/// JSqlType checks for the various insert OR replace varients
		///
		/*
		if (JSqlObj.sqlType == JSqlType.sqlite) {
			
			//return jSqlObj.execute("INSERT OR REPLACE INTO `" + sqlTableName + "` (oKey,mKey,val) VALUES (?,?,?)", oKey, key, val);
		} else if (jSqlObj.sqlType == jSqlType.oracle) {
			return jSqlObj.execute(
										  "BEGIN BEGIN INSERT INTO " + sqlTableName.toUpperCase()
										  + " (obj,metaKey,val) VALUES (?,?,?); EXCEPTION WHEN dup_val_on_index THEN UPDATE "
										  + sqlTableName.toUpperCase() + " SET val=? WHERE obj=? AND metaKey=?; END; END;", oKey,
										  key, val, val, oKey, key);
		} else if(jSqlObj.sqlType == jSqlType.mssql){
			return jSqlObj.execute("IF EXISTS (Select * from "+sqlTableName + " WHERE obj = ? AND metaKey = ?) "
										  + " UPDATE "+ sqlTableName + "  set val = ? WHERE obj = ? AND metaKey=? " + " ELSE INSERT INTO "+sqlTableName+" (obj,metaKey,val) VALUES (?,?,?) ",oKey, key, val, oKey, key, oKey, key, val);
		} else {
			return jSqlObj.execute("INSERT INTO `" + sqlTableName
										  + "` (obj,metaKey,val) VALUES (?,?,?) ON DUPLICATE KEY UPDATE val=VALUES(val)", oKey, key, val);
		}
		 */
		return true;
	}
	
	// Dynamic putKeyValue call, based on value object type
	public boolean putKeyValue(String oKey, String mKey, Object val) throws JSqlException {
		return putKeyValue(oKey, mKey, val, 0);
	}
	
	// Dynamic putKeyValue call, based on value object type. With expire unix timestamp
	public boolean putKeyValue(String oKey, String mKey, Object val, long expireUnixTime) throws JSqlException {
		// Evaluating the various put values
		if (val == null) {
			return putKeyValueWith(oKey, mKey, null, 0, 0, 0, 0, expireUnixTime);
		} else if (String.class.isInstance(val)) {
			return putKeyValueWith(oKey, mKey, (val.toString()), 0, 0, 0, 1, expireUnixTime);
		} else if (Integer.class.isInstance(val) || Long.class.isInstance(val)) {
			return putKeyValueWith(oKey, mKey, null, ((Number) val).longValue(), 0, 0, 2, expireUnixTime);
			
		} else if (Double.class.isInstance(val)) {
			
			double dVal = ((Double) val).doubleValue();
			long vInt = (long) Math.floor(dVal);
			int vDci = (int) ((dVal - (double) vInt) * (double) vDciMultiplier);
			return putKeyValueWith(oKey, mKey, null, vInt, vDci, 0, 3, expireUnixTime);
			
		} else if (Float.class.isInstance(val)) {
			
			float dVal = ((Float) val).floatValue();
			long vInt = (long) Math.floor(dVal);
			int vDci = (int) ((dVal - (float) vInt) * (float) vDciMultiplier);
			return putKeyValueWith(oKey, mKey, null, vInt, vDci, 0, 4, expireUnixTime);
			
		} else {
			String valClassName = val.getClass().getName();
			throw new JSqlException("Unknown value object type : " + (valClassName));
		}
		//return false;
	}
	
	/*

	/// Store oKey / key / value
	public boolean put(String oKey, String key, String val) throws jSqlException {
		/// As mySql doesn`t support INSERT OR REPLACE INTO... need to check
		// jSqlType
		if (jSqlObj.sqlType == jSqlType.sqlite) {
			return jSqlObj.execute("INSERT OR REPLACE INTO `" + sqlTableName + "` (obj,metaKey,val) VALUES (?,?,?)",
					oKey, key, val);
		} else if (jSqlObj.sqlType == jSqlType.oracle) {
			return jSqlObj.execute(
					"BEGIN BEGIN INSERT INTO " + sqlTableName.toUpperCase()
							+ " (obj,metaKey,val) VALUES (?,?,?); EXCEPTION WHEN dup_val_on_index THEN UPDATE "
							+ sqlTableName.toUpperCase() + " SET val=? WHERE obj=? AND metaKey=?; END; END;", oKey,
					key, val, val, oKey, key);
		} else if(jSqlObj.sqlType == jSqlType.mssql){
	      return jSqlObj.execute("IF EXISTS (Select * from "+sqlTableName + " WHERE obj = ? AND metaKey = ?) " 
	                         + " UPDATE "+ sqlTableName + "  set val = ? WHERE obj = ? AND metaKey=? " + " ELSE INSERT INTO "+sqlTableName+" (obj,metaKey,val) VALUES (?,?,?) ",oKey, key, val, oKey, key, oKey, key, val);
	   }
	   else {
			return jSqlObj.execute("INSERT INTO `" + sqlTableName
					+ "` (obj,metaKey,val) VALUES (?,?,?) ON DUPLICATE KEY UPDATE val=VALUES(val)", oKey, key, val);
		}
	}

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