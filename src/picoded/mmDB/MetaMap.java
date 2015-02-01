package picoded.mmDB;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import picoded.jSql.*;
import java.util.logging.*;

/// MetaMap, is a simple abstraction of a objectkey, key, value storage. This extends the Map<String, Map<String, Object>> class.
///
/// All key values pairs are stored and read as strings
///
/// *******************************************************************************
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
///
/// metaTable mt = new metaTable( new jSql(), "tableName" )
///                .objColumnType("VARCHAR(60)").keyColumnType("VARCHAR(60)");
/// mt.tableSetup(); //table setup
///
/// mt.put("objid","hello","world");
/// String test = mt.get("objid","hello");
/// assertEquals( test, "world" );
///
/// // getObj (and getObjRaw), also exists to get all the key value pairs associated to the object.
/// HashMap<String,String> hObj = mt.getObj("objid");
///
/// assertEquals( hObj.gets("hello"), "world );
///
/// // This is the raw varient of getObj
/// jSqlResult rObj = mt.getObj_jSql("objid");
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
///
/// *******************************************************************************
///
public class MetaMap /*implements Map<String, Map<String,Object>>*/{

	/// Internal self used logger
	private static Logger logger = Logger.getLogger(MetaMap.class.getName());

	protected JSql JSqlObj = null;

	/*
	/// [TODO: Mid priority] -> To make protected (double check test case), and
	/// create getter function
	protected String sqlTableName;

	/// Object ID field type, This value SHOULD NOT be modified directly
	/// [TODO: Mid priority] -> To make protected (double check test case), and
	/// create getter function
	public String objColumnType = "VARCHAR(60)";

	/// Key name field type, This value SHOULD NOT be modified directly
	/// [TODO: Mid priority] -> To make protected (double check test case), and
	/// create getter function
	public String keyColumnType = "VARCHAR(60)";

	/// Value name field type, This value SHOULD NOT be modified directly
	/// [TODO: Mid priority] -> To make protected (double check test case), and
	// create getter function
	public String valColumnType = "VARCHAR(4000)";

	/// Additional field defination, This value SHOULD NOT be modified directly
	/// [TODO: Mid priority] -> To make protected (double check test case), and
	/// create getter function
	public String addColumnDefination = "";

	/// Constructor with the jSql object, and the deployed table name
	public metaTable(jSql inSql, String tableName) {
		jSqlObj = inSql;
		sqlTableName = tableName;
	}

	/// Sets the Object ID field type.
	/// These are meant to give an exception if table is already setup
	public metaTable objColumnType(String inStr) {
		objColumnType = inStr;
		return this;
	}

	/// Sets the key name field type
	/// These are meant to give an exception if table is already setup
	public metaTable keyColumnType(String inStr) {
		keyColumnType = inStr;
		return this;
	}

	/// Sets the value name field type
	/// These are meant to give an exception if table is already setup
	public metaTable valColumnType(String inStr) {
		valColumnType = inStr;
		return this;
	}

	/// Add additional column defination
	/// These are meant to give an exception if table is already setup
	/// [TODO: Mid priority] The respective test case
	public metaTable addColumnDefination(String inStr) {
		addColumnDefination = inStr;
		return this;
	}

	/// Table setup : This is to be replaced by missing table exception
	/// handling
	public metaTable tableSetup() throws jSqlException {
	   String qStr;
	   if (jSqlObj.sqlType == jSqlType.sql){ //val column will be TEXT for mysql
	      valColumnType = "TEXT(65535)";
	   }
		if (addColumnDefination != null && addColumnDefination.length() > 0) {
			qStr = "CREATE TABLE IF NOT EXISTS `" + sqlTableName + "` (obj " + objColumnType + ", metaKey "
					+ keyColumnType + ", val " + valColumnType + "," + addColumnDefination + ");";
		} else {
			qStr = "CREATE TABLE IF NOT EXISTS `" + sqlTableName + "` (obj " + objColumnType + ", metaKey "
					+ keyColumnType + ", val " + valColumnType + ");";
		}

		logger.finer(qStr);
		jSqlObj.execute(qStr);
		if (jSqlObj.sqlType == jSqlType.sqlite || jSqlObj.sqlType == jSqlType.oracle) {
			// sqlite unique index http://www.sqlite.org/lang_createindex.html
			jSqlObj.execute("CREATE UNIQUE INDEX IF NOT EXISTS `" + sqlTableName + "_unique` ON `" + sqlTableName
					+ "` (obj, metaKey)");
		} else {
			// Create the index anyway : And handles the exception on unique
			// index conflict
			try {
				jSqlObj.execute("CREATE UNIQUE INDEX `" + sqlTableName + "_unique` ON `" + sqlTableName
						+ "` (obj, metaKey)");
			} catch (jSqlException e) {
				if (!e.getCause()
						.toString()
						.equals("com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException: Duplicate key name '"
								+ sqlTableName + "_unique'")) {
					// throws if not valid
					throw e;
				}
			}
		}
		
		// Rename val column type to VARCHAR if it is different (for oracle only)
		if (jSqlObj.sqlType == jSqlType.oracle ) {
			
			jSqlResult res = jSqlObj.query("SELECT DATA_TYPE FROM USER_TAB_COLUMNS WHERE TABLE_NAME = ? AND COLUMN_NAME = ?", sqlTableName.toUpperCase(), "VAL");
			if(res.rowCount() > 0 && !res.readRowCol(0,"DATA_TYPE").equals("VARCHAR2") && valColumnType.contains("VARCHAR") ) {
				jSqlObj.execute("ALTER TABLE " + sqlTableName + " ADD val_TEMP "+(valColumnType.toUpperCase().replace("VARCHAR", "VARCHAR2")) );
				jSqlObj.execute("UPDATE " + sqlTableName + "  SET val_TEMP = DBMS_LOB.SUBSTR (val, 4000)");
				jSqlObj.execute("ALTER TABLE " + sqlTableName + " DROP COLUMN val");
				jSqlObj.execute("ALTER TABLE " + sqlTableName + " RENAME COLUMN val_TEMP TO val");
			}
		}
		
		if(valColumnType.contains("VARCHAR")) {
			if (jSqlObj.sqlType == jSqlType.sqlite || jSqlObj.sqlType == jSqlType.oracle) {
				// sqlite unique index http://www.sqlite.org/lang_createindex.html
				jSqlObj.execute("CREATE INDEX IF NOT EXISTS `" + sqlTableName + "_value` ON `" + sqlTableName
									 + "` (val)");
			} else {
				// index conflict
				try {
					jSqlObj.execute("CREATE INDEX `" + sqlTableName + "_value` ON `" + sqlTableName
										 + "` (val)");
				} catch (jSqlException e) {
					if (!e.getCause()
						 .toString()
						 .equals("com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException: Duplicate key name '"
									+ sqlTableName + "_value'")) {
						// throws if not valid
						throw e;
					}
				}
			}
		}
		
		
		//valColumnType
		return this;
	}

	/// Store objid / key / value
	public boolean put(String objid, String key, String val) throws jSqlException {
		/// As mySql doesn`t support INSERT OR REPLACE INTO... need to check
		// jSqlType
		if (jSqlObj.sqlType == jSqlType.sqlite) {
			return jSqlObj.execute("INSERT OR REPLACE INTO `" + sqlTableName + "` (obj,metaKey,val) VALUES (?,?,?)",
					objid, key, val);
		} else if (jSqlObj.sqlType == jSqlType.oracle) {
			return jSqlObj.execute(
					"BEGIN BEGIN INSERT INTO " + sqlTableName.toUpperCase()
							+ " (obj,metaKey,val) VALUES (?,?,?); EXCEPTION WHEN dup_val_on_index THEN UPDATE "
							+ sqlTableName.toUpperCase() + " SET val=? WHERE obj=? AND metaKey=?; END; END;", objid,
					key, val, val, objid, key);
		} else if(jSqlObj.sqlType == jSqlType.mssql){
	      return jSqlObj.execute("IF EXISTS (Select * from "+sqlTableName + " WHERE obj = ? AND metaKey = ?) " 
	                         + " UPDATE "+ sqlTableName + "  set val = ? WHERE obj = ? AND metaKey=? " + " ELSE INSERT INTO "+sqlTableName+" (obj,metaKey,val) VALUES (?,?,?) ",objid, key, val, objid, key, objid, key, val);
	   }
	   else {
			return jSqlObj.execute("INSERT INTO `" + sqlTableName
					+ "` (obj,metaKey,val) VALUES (?,?,?) ON DUPLICATE KEY UPDATE val=VALUES(val)", objid, key, val);
		}
	}

	/// Fetches objid / key / value
	public String get(String objid, String key) throws jSqlException {
		jSqlResult r = jSqlObj.query("SELECT * FROM " + sqlTableName + " WHERE obj=? AND metaKey=?", objid, key);

		if (r.fetchAllRows() > 0) {
			return (String) r.readRowCol(0, "val");
		}
		return null;
	}

	/// Fetches objid / key / value, using partial matching search (SQL LIKE)
	public String[] getMultiple(String objid, String key) throws jSqlException {
		jSqlResult r = jSqlObj.query("SELECT * FROM " + sqlTableName + " WHERE obj LIKE ? AND metaKey LIKE ?", objid, key);
		
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
	
	/// Fetches objid / key / value, using partial matching search (SQL LIKE)
	public HashMap<String, String> getMultipleSet(String objid, String key, String val) throws jSqlException {
		jSqlResult r = jSqlObj.query("SELECT * FROM " + sqlTableName + " WHERE obj LIKE ? AND metaKey LIKE ? AND val LIKE ?", objid, key, val);
		
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
	public boolean deleteObj(String objid) throws jSqlException {
		return jSqlObj.execute("DELETE FROM `" + sqlTableName + "` WHERE obj=?;", objid);
	}

	/// Gets all the key value pairs related to the object. And returns as as
	// HashMap< metaKey, value >
	public HashMap<String, String> getObj(String objid) throws jSqlException {
		jSqlResult r = getObj_jSql(objid);

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
	public jSqlResult getObj_jSql(String objid) throws jSqlException {
		return jSqlObj.query("SELECT * FROM `" + sqlTableName + "` WHERE obj=?;", objid);
	}

	/// Returns the jSqlResult raw varient
	/// [TODO: High priority]
	public jSqlResult getAll_jSql() throws jSqlException {
		return jSqlObj.query("SELECT * FROM `" + sqlTableName + "`");
	}

	/// Fetches everything, in objid, key, value pairs
	public HashMap<String, HashMap<String, Object>> getAll() throws jSqlException {
		jSqlResult r = getAll_jSql();

		HashMap<String, HashMap<String, Object>> res = new HashMap<String, HashMap<String, Object>>();
		HashMap<String, Object> objMap = null;
		String prvObjId = null;
		String objId = null;
		int len;

		if ((len = r.fetchAllRows()) > 0) {
			for (int i = 0; i < len; i++) {
				// Gets current object id
				objId = (String) r.readRowCol(i, "obj");

				// Gets the previous objMap, if needed
				if (prvObjId != objId) {
					objMap = res.get(objId);

					// result set dosent have the object map yet
					if (objMap == null) {
						objMap = new HashMap<String, Object>();
						res.put(objId, objMap);
					}

					prvObjId = objId;
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
	public String[] getObjKeyMap(String objid, String val) throws jSqlException {
		// throw new jSqlException("Not Yet Implemented");
		jSqlResult r = getObjKeyMap_jSql(objid, val);
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
	public jSqlResult getObjKeyMap_jSql(String objid, String val) throws jSqlException {
		return jSqlObj.query("SELECT metaKey FROM `" + sqlTableName + "` WHERE obj=? AND val like ?", objid, val);
	}

	/// Fetches a HashMap<objid, val> matched to key
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
	public long objectMetaCount(String objid) throws jSqlException {
		throw new jSqlException("Not Yet Implemented");
	}
	 */
}