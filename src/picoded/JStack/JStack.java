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

/// JStack provides various common data storage format, that utalizes a combination of
/// JCache, and JSql instances implementation.
///
/// The design principle is based on the prototyping experience for mmObjDB, and the original servlet-commons implementation of metaTables.
public class JStack extends JStruct implements JStackLayer {
	
	//----------------------------------------------
	// Readonly internal variables
	//----------------------------------------------
	
	/// Internal JCache layers stack used
	protected JStackLayer[] _stackLayers = null;
	
	/// Final getter for JStackLayers 
	public final JStackLayer[] stackLayers() {
		return _stackLayers;
	}
	
	/// JStruct layering internal var
	protected JStruct[] _structLayers = null;
	
	/// Final getter for JStruct layers
	public final JStruct[] structLayers() {
		if (_structLayers != null) {
			return _structLayers;
		}
		
		int len = stackLayers().length;
		JStruct[] ret = new JStruct[len];
		for (int i = 0; i < len; ++i) {
			JStackLayer layer = _stackLayers[i];
			if (layer instanceof JStruct) {
				ret[i] = (JStruct) layer;
			} else if (layer instanceof JSql) {
				ret[i] = new JSqlStruct((JSql) layer);
			} else if (layer instanceof JCache) {
				ret[i] = new JCacheStruct((JCache) layer);
			}
		}
		
		return (_structLayers = ret);
	}
	
	//----------------------------------------------
	// Constructor
	//----------------------------------------------
	
	public JStack(JStackLayer inLayer) {
		_stackLayers = new JStackLayer[] { inLayer };
	}
	
	public JStack(JStackLayer[] inStack) {
		_stackLayers = inStack;
	}
	
	//----------------------------------------------
	// Teardown 
	//----------------------------------------------
	
	public void disposeStackLayers() throws JStackException {
		try {
			for (JStackLayer oneLayer : _stackLayers) {
				oneLayer.dispose();
			}
		} catch (Exception e) {
			throw new JStackException(e);
		}
	}
	
	//----------------------------------------------
	// MetaTable, KeyValueMap handling
	//----------------------------------------------
	
	/// Actual setup implmentation to overwrite
	///
	/// @param name - name of map in backend
	///
	/// @returns KeyValueMap
	protected KeyValueMap setupKeyValueMap(String name) {
		return new JStack_KeyValueMap(this, name);
	}
	
	/// Actual setup implmentation to overwrite
	///
	/// @param name - name of map in backend
	///
	/// @returns MetaTable
	protected MetaTable setupMetaTable(String name) {
		return new JStack_MetaTable(this, name);
	}
	
	/// Actual setup implmentation to overwrite
	///
	/// @param name - name of map in backend
	///
	/// @returns AccountTable
	protected AccountTable setupAccountTable(String name) {
		return new AccountTable(this, name);
	}
	
	//----------------------------------------------
	// Static public helper functions
	//----------------------------------------------
	
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
	
	/// Generate JStack layers based on the configuration format of 
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
