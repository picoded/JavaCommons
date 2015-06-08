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

/// @TODO Documentation
public class KeyValueMap {
	
	/*
	///
	/// Internal config vars
	///--------------------------------------------------------------------------
	
	/// Primary key type
	protected String pKeyColumnType = "INTEGER PRIMARY KEY AUTOINCREMENT";
	
	/// Timestamp field type
	protected String tStampColumnType = "BIGINT";
	
	/// Key name field type
	protected String keyColumnType = "VARCHAR(64)";
	
	/// Value field type
	protected String valueColumnType = "VARCHAR(MAX)";
	
	/// Is temp values only flag. This is used to indicate the stack should
	/// optimize purely to JCache if possible (JSql will be used if a Jcache isnt provided)
	protected boolean isTempValuesOnly = false;
	
	///
	/// Constructor setup
	///--------------------------------------------------------------------------
	
	/// Setup the metatable with the default table name
	public KeyValueMap(JStack inStack) {
		super( inStack );
		tableName = "kv_KeyValueMap";
	}
	
	/// Setup the metatable with the given stack
	public KeyValueMap(JStack inStack, String inTableName) {
		super( inStack, "kv_"+inTableName );
	}
	
	
	///
	/// Internal JSql table setup and teardown
	///--------------------------------------------------------------------------
	
	/// Setsup the respective JSql table
	@Override
	protected boolean JSqlSetup(JSql sql) throws JSqlException, JStackException {
		
		return true;
	}
	
	/// Removes the respective JSQL tables and view (if it exists)
	@Override
	protected boolean JSqlTeardown(JSql sql) throws JSqlException, JStackException {
		sql.execute("DROP VIEW IF EXISTS " + sqlViewName(sql, "view"));
		sql.execute("DROP TABLE IF EXISTS " + sqlViewName(sql, "vCfg"));
		sql.execute("DROP TABLE IF EXISTS " + sqlTableName(sql));
		return true;
	}
	*/
	
}