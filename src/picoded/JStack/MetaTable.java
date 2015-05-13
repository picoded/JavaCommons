package picoded.JStack;

/// Java imports
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.*;

/// Picoded imports
import picoded.JSql.*;
import picoded.JCache.*;

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
			for(int a=0; a<sl.length; ++a) {
				// JSql specific setup
				if(sl[a] instanceof JSql) {
					JSqlDataTableSetup( (JSql)(sl[a]) );
					JSqlIndexConfigTableSetup( (JSql)(sl[a]) );
				} else if( sl[a] instanceof JCache ) {
					
				}
			}
		} catch(JSqlException e) {
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
	protected HashMap<String, MetaTypes> typeMapping;
	
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
		String tName = sql.getNamespace() + tableName + "_iConfig" + viewSuffix;
		
		// Table constructor
		//-------------------
		sql.createTableQuerySet( //
										tName, //
										new String[] { //
											"nme", //Index column name
											"typ", //Index column type
											"con"  //Index type string value
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
				tStampColumnType,
				tStampColumnType,
				tStampColumnType,
				// Object keys
				objColumnType, //
				keyColumnType, //
				indexColumnType, //
				// Value storage
				typeColumnType, //
				numColumnType, //
				strColumnType, //
				strColumnType, //
				fullTextColumnType
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
			tName, "kID, nVl, iVl", null, "valMap" //
		).execute();
		
		// Object timestamp optimized Key Value indexe
		//------------------------------------------------
		sql.createTableIndexQuerySet( //
			tName, "oTm, kID, nVl, iVl", null, "oTm_valMap" //
		).execute();
		
		// Full text index, for textual data
		//------------------------------------------------
		sql.createTableIndexQuerySet( //
			tName, "tVl", "FULLTEXT", "tVl" //
		).execute();
		
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
	/// PUT and GET meta map operations
	///--------------------------------------------------------------------------
	
	/// JSQL based GET
	protected Map<String,Object> JSqlObjectGet(JSql sql, String oid) {
		
	}
	
	/// JSQL based PUT
	
	
}