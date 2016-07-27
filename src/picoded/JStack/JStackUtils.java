package picoded.JStack;

import java.util.*;

// Picoded imports
import picoded.JSql.*;
import picoded.JCache.*;
import picoded.JStruct.*;
import picoded.JSql.struct.*;
import picoded.JCache.struct.*;
import picoded.JStack.struct.*;
import picoded.conv.*;
import picoded.struct.*;
import picoded.RESTBuilder.*;
import picoded.RESTBuilder.templates.*;

/// JStack utility to build up complex JStack structures from the JStack config file.
public class JStackUtils {
	
	public final static String[] structTypes = { "AccountTable", "KeyValueMap", "AtomicLongMap", "MetaTable" };   
	
	//--------------------------------------------------
	//
	// JStack MetaTable setups
	//
	//--------------------------------------------------
	
	///
	/// Build the RESTBuilder to a single JStruct type object 
	///
	protected static void setupRESTBuilderType(RESTBuilder rbObj, JStruct struct, String type, Map<String,Object> nameMap) {
		if(nameMap == null) {
			return;
		}
		for (String name : nameMap.keySet()) {
			if(type.equalsIgnoreCase("AccountTable")) {
				AccountLogin.setupRESTBuilder(rbObj, struct.getAccountTable(name), name+".");
			} else if(type.equalsIgnoreCase("KeyValueMap")) {
				(new KeyValueMapApiBuilder(struct.getKeyValueMap(name))).setupRESTBuilder(rbObj, name+".");
			} else if(type.equalsIgnoreCase("AtomicLongMap")) {
				(new AtomicLongMapApiBuilder(struct.getAtomicLongMap(name))).setupRESTBuilder(rbObj, name+".");
			} else if(type.equalsIgnoreCase("MetaTable")) {
				(new MetaTableApiBuilder(struct.getMetaTable(name))).setupRESTBuilder(rbObj, name+".");
			} else {
				throw new RuntimeException("Unknown struct type : "+type);
			}
		}
	}
	
	///
	/// Preload various table structures according to config. This would be the "sys.JStack.struct"
	///
	/// @param   The RESTBuilder object to build the api on
	/// @param   The jstruct to build on
	/// @param   The structure map containing { type : { name : configObj } }
	///
	public static void setupRESTBuilderStruct(RESTBuilder rbObj, JStruct struct, Map<String,Object> structMap) {
		if( structMap == null ) {
			return;
		}
		
		GenericConvertMap<String,Object> configMap = GenericConvertMap.build(structMap);
		for (String s: structTypes) {
			setupRESTBuilderType(rbObj, struct, s, configMap.getStringMap(s));
		}
	}
	
	//--------------------------------------------------
	//
	// JStack structure setups
	//
	//--------------------------------------------------
	
	///
	/// Preload a single JStruct type object 
	///
	protected static void preloadJStructType(JStruct struct, String type, Map<String,Object> nameMap) {
		if(nameMap == null) {
			return;
		}
		for (String name : nameMap.keySet()) {
			struct.preloadJStructType(type, name);
		}
	}
	
	///
	/// Preload various table structures according to config. This would be the "sys.JStack.struct"
	///
	/// @param   The jstruct to build on
	/// @param   The structure map containing { type : { name : configObj } }
	///
	public static void preloadJStruct(JStruct struct, Map<String,Object> structMap) {
		if( structMap == null ) {
			return;
		}
		
		GenericConvertMap<String,Object> configMap = GenericConvertMap.build(structMap);
		String[] structTypes = { "AccountTable", "KeyValueMap", "AtomicLongMap", "MetaTable" };   
		for (String s: structTypes) {
			preloadJStructType(struct, s, configMap.getStringMap(s));
		}
	}
	
	//--------------------------------------------------
	//
	// JStack layer handling
	//
	//--------------------------------------------------
	
	///
	/// Generate a single JStack layer using a config map representing it
	///
	public static JStackLayer stackConfigToJStackLayer(Map<String,Object> inConfig, String fullWebInfPath) {
		if( inConfig == null ) {
			return null;
		}
		
		GenericConvertMap<String,Object> configMap = GenericConvertMap.build(inConfig);
		String type = configMap.getString("type");
		JStackLayer ret = null;
		
		// Using the type, apply the relevent build logic
		if( type.equalsIgnoreCase("jsql") ) {
			
			// Gets the config vars
			String engine = configMap.getString("engine", "");
			String path = configMap.getString("path", "");
			String username = configMap.getString("username", "");
			String password = configMap.getString("password", "");
			String database = configMap.getString("database", "");
			
			// SQLite implmentation
			if (engine.equalsIgnoreCase("sqlite")) {
				
				//-------- Sample config --------//
				// // [Sqlite] database implmentation
				// // This is obviously not meant for production, but for testing,
				// // Path here refers to file path.
				// // Note that the database, username, password parameters are meaningless for sqlite
				// "engine" : "sqlite",
				// "path" : "./WEB-INF/storage/db.sqlite",

				if (path.length() <= 0) {
					throw new RuntimeException("Unsupported " + "path: " + path);
				}
				
				if( fullWebInfPath == null ) {
					fullWebInfPath = "./WEB-INF/";
				}
				
				// Replaces WEB-INF path
				path = path.replace("./WEB-INF/", fullWebInfPath);
				path = path.replace("${WEB-INF}", fullWebInfPath);
				
				// Generates the sqlite connection with the path
				return JSql.sqlite(path);
			} else if (engine.equalsIgnoreCase("mssql")) {
				
				//-------- Sample config --------//
				// // [MS-SQL] database implmentation
				// // Note that uselobs=false should be used, this is known to resolve
				// // Certain data competebility problems
				// "engine" : "mssql",
				// "path" : "54.169.34.78:1433;uselobs=false;",

				return JSql.mssql(path, database, username, password);
			} else if (engine.equalsIgnoreCase("mysql")) {
				
				//-------- Sample config --------//
				// // [MY-SQL] database implmentation
				// "engine" : "mysql",
				// "path" : "54.169.34.78:3306",
				// 
				// "database" : "JAVACOMMONS",
				// "username" : "JAVACOMMONS",
				// "password" : "JAVACOMMONS"
				
				return JSql.mysql(path, database, username, password);
			} else if (engine.equalsIgnoreCase("oracle")) {
				
				//-------- Sample config --------//
				// // [Oracle] database implmentation
				// // Note that as the database chosen is normally part of the pathing
				// // The database attribute is ignored.
				// "engine" : "oracle",
				// "path" : "JAVACOMMONS@//54.169.34.78:1521/xe",
				
				return JSql.oracle(path, username, password);
			} else {
				throw new RuntimeException("Unexpected JStack.stack JSql engine: " + engine);
			}
		} else if( type.equalsIgnoreCase("jcache") ) {
			// @TODO : JCache support
		} else if( type.equalsIgnoreCase("jstruct") ) {
			return new JStruct();
		} else {
			throw new RuntimeException("Unexpected JStack.stack type : "+type);
		}
		
		return ret;
	}
	
	///
	/// Generate JStack layers based on the configuration format of. This would be the "sys.JStack.stack" array
	///
	public static JStackLayer[] stackConfigLayersToJStackLayers(List<Object> inConfig, String fullWebInfPath) {
		if( inConfig == null ) {
			return null;
		}
		
		List<JStackLayer> ret = new ArrayList<JStackLayer>();
		for( Object config : inConfig ) {
			JStackLayer subLayer = stackConfigToJStackLayer( GenericConvert.toStringMap(config, null), fullWebInfPath );
			if( subLayer != null ) {
				ret.add( subLayer );
			}
		}
		
		if( ret.size() > 0 ) {
			return ret.toArray(new JStackLayer[0]);
		}
		return null;
	}
}
