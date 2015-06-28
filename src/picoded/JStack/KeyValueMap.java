package picoded.JStack;

import picoded.JSql.JSql;
import picoded.JSql.JSqlException;
import picoded.JSql.JSqlResult;
import picoded.JSql.JSqlType;

import java.util.ArrayList;

public class KeyValueMap{
	
	private JSql sqlObj;
	private final String tableName="KeyVal";
	
	// Constructor with database connector
	public KeyValueMap(JSql jSql){
		sqlObj = jSql;
	}
	
	// table set up
	//key will be column and value will be expiration time
	public void tableSetup() throws JSqlException {
		sqlObj.execute("CREATE TABLE IF NOT EXISTS `" + tableName +
						 "` (metaKey VARCHAR(60) PRIMARY KEY , val VARCHAR(4000) )");
		
	}
	
	//put values to table
	public boolean put(String key, String value) throws JSqlException {
		// As mysql doesn`t support INSERT OR REPLACE query
		if (sqlObj.sqlType == JSqlType.sqlite) {
			return sqlObj.execute("INSERT OR REPLACE INTO `" + tableName
									 + "` (metaKey,val) VALUES (?,?)", key, value);
		} else if(sqlObj.sqlType == JSqlType.oracle) {
			return sqlObj.execute("BEGIN BEGIN INSERT INTO "+tableName.toUpperCase()+" (metaKey,val) VALUES (?,?); EXCEPTION WHEN dup_val_on_index THEN UPDATE "+tableName.toUpperCase()+" SET val=? WHERE metaKey=?; END; END;", key, value, value, key );
      } else if(sqlObj.sqlType == JSqlType.mssql) {
         return sqlObj.execute("IF EXISTS (Select * from "+tableName + " where metaKey = ?) " 
                            + " UPDATE "+ tableName + "  set val = ? WHERE metaKey=? " + " ELSE INSERT INTO "+tableName+" (metaKey,val) VALUES (?,?) ", key,value,key,key,value);
      }
         else {
			return sqlObj.execute("INSERT INTO `" + tableName
									 + "` (metaKey,val) VALUES (?,?) ON DUPLICATE KEY UPDATE val=VALUES(val)", key, value);
		}
	}
	
	//get values from table based on key
	public String get(Object key) throws JSqlException {
		JSqlResult r = null;
		try {
			r = sqlObj.query("SELECT * FROM `" + tableName + "` WHERE metaKey=?;",
							  key);
		} catch (JSqlException e) {
			e.printStackTrace();
		}
		if (r.fetchAllRows() > 0) {
			return (String) r.readRowCol(0, "val");
		}
		return null;
		
	}
	
	
	/// Remove stored key, value pair
	public boolean remove(Object key) throws JSqlException {
		return sqlObj.execute("DELETE FROM `" + tableName + "` WHERE metaKey=?",
								 key);
	}
	
	/// Remove stored key, value pair
	public boolean removeValues(Object value) throws JSqlException {
		return sqlObj
		.execute("DELETE FROM `" + tableName + "` WHERE val=?", value);
	}
	
	/// returns true if database table is empty
	public boolean isEmpty() throws JSqlException {
	JSqlResult	r = sqlObj.query("SELECT * FROM `" + tableName + "`;");
		if (r.rowCount() > 0) {
			return false;
		}
		
		return true;
	}
	
	/// returns database number of key, values pair
	public int size() {
		try {
		JSqlResult	r = sqlObj.query("SELECT * FROM `" + tableName + "`;");
			return r.rowCount();
		} catch (JSqlException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	//returns unix time
	public long getUnixTime() {
		 long unixTime = System.currentTimeMillis() / 1000L;
		 return unixTime;
	}
	
	public void tearDown() throws JSqlException{
   sqlObj.executeQuery("DROP TABLE IF EXISTS `"+tableName+"`;").dispose();	
	}
	
}