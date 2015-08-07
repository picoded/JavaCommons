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
/// Protected class, used to orgainze the various JSql based logic
/// used in MetaTable. 
///
/// The larger intention is to keep the MetaTable class more maintainable
///
class MetaTable_JSql {
	
	///
	/// Setsup the JSql main data storage table for MetaTable
	/// 
	/// @param {JSql} sql            - sql connection to setup the table
	/// @param {String} tName        - table name to setup, this holds the actual meta table data
	/// @param {MetaTable} mainTable - main MetaTable, where this function shoudl be called from
	/// 
	protected static void JSqlSetup(JSql sql, String tName, MetaTable mainTable) throws JSqlException {
		
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
											"eTm", //value expire time (for future use)
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
											mainTable.pKeyColumnType, //Primary key
											// Time stamps
											mainTable.tStampColumnType, //
											mainTable.tStampColumnType, //
											mainTable.tStampColumnType, //
											mainTable.tStampColumnType, //
											// Object keys
											mainTable.objColumnType, //
											mainTable.keyColumnType, //
											mainTable.indexColumnType, //
											// Value storage
											mainTable.typeColumnType, //
											mainTable.numColumnType, //
											mainTable.strColumnType, //
											mainTable.fullTextColumnType //
										} //
										).execute(); //

		// Unique index
		//
		// This also optimizes query by object keys
		//------------------------------------------------
		sql.createTableIndexQuerySet( //
											  tName, "oID, kID, idx", "UNIQUE", "unq" //
											  ).execute(); //

		// Key Values search index
		//------------------------------------------------
		sql.createTableIndexQuerySet( //
											  tName, "kID, nVl, sVl", null, "valMap" //
											  ).execute(); //

		// Object timestamp optimized Key Value indexe
		//------------------------------------------------
		sql.createTableIndexQuerySet( //
											  tName, "oTm, kID, nVl, sVl", null, "oTm_valMap" //
											  ).execute(); //

		// Full text index, for textual data
		// @TODO FULLTEXT index support
		//------------------------------------------------
		//if (sql.sqlType != JSqlType.sqlite) {
		//	sql.createTableIndexQuerySet( //
		//		tName, "tVl", "FULLTEXT", "tVl" //
		//	).execute();
		//} else {
		sql.createTableIndexQuerySet( //
											  tName, "tVl", null, "tVl" // Sqlite uses normal index
											  ).execute(); //
		//}

		//
		// timestamp index, is this needed?
		//
		// Currently commented out till a usage is found for them
		// This can be easily recommented in.
		//
		// Note that the main reason this is commented out is because
		// updated time and created time does not work fully as intended
		// as its is more of a system point of view. Rather then adminstration
		// point of view. 
		//
		// A good example is at times several fields in buisness logic is set
		// to NOT want to update the udpated time stamp of the object.
		//
		//------------------------------------------------
		
		//sql.createTableIndexQuerySet( //
		//	tName, "uTm", null, "uTm" //
		//).execute();

		//sql.createTableIndexQuerySet( //
		//	tName, "cTm", null, "cTm" //
		//).execute();
	}
	
	///
	/// Iterates the relevent keyList, and appends its value from the objMap, into the sql mainTable database
	/// 
	/// @param {JSql} sql                  - sql connection to setup the table
	/// @param {String} tName              - table name to setup, this holds the actual meta table data
	/// @param {MetaTable} mainTable       - main MetaTable, where this function shoudl be called from
	/// @param {String} _oid               - object id to store the key value pairs into
	/// @param {Map<String,Object>} objMap - map to extract values to store from
	/// 
	protected void JSqlObjectMapAppend(JSql sql, String tName, String _oid, //
		Map<String, Object> objMap, Set<String> keyList,
		boolean optimizeAutoCommit) throws JSqlException {
		
	}
	
	
}
