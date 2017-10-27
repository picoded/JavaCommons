package picoded.servlet.api.module.struct;

import picoded.servlet.api.module.*;
import picoded.servlet.api.*;
import picoded.dstack.*;

import static picoded.servlet.api.module.ApiModuleConstantStrings.*;

import picoded.core.common.*;
import picoded.core.struct.query.*;
import picoded.core.struct.*;
import picoded.core.conv.*;

import java.util.*;

/**
 * Simple API module, to proxy to an actual map object.
 * With additional configuration options.
 * 
 * NOTE: This only support GET commands for now
 * 
 * 
 */
public class MapProxyApi extends CommonApiModule {

	/////////////////////////////////////////////
	//
	// Constructor setup
	//
	/////////////////////////////////////////////

	// Internal data table object, set by constructor
	protected Map<String,Object> dataMap = null;

	/**
	 * Constructor for DataTableApi
	 *
	 * @param  DataTable  the data table object for the API to build on
	 */
	public MapProxyApi(Map<String,Object> inMap) {
		dataMap = inMap;
	}

	/**
	 * Array of internal subsystems : Currently only DataTable dstack module
	 *
	 * @return  Array of internal subsystems
	 */
	protected SystemSetupInterface[] internalSubsystemArray() {
		return new SystemSetupInterface[] { };
	}

	/////////////////////////////////////////////
	//
	// Utility functionality
	//
	/////////////////////////////////////////////

	/**
	 * Validates the given key name and check against the configured blacklist prefix
	 */
	public boolean validateKeyname(String key) {
		if(key == null || key.length() == 0) {
			return false;
		}

		String[] blacklistPrefix = config.getStringArray("blacklistPrefix", "[]");
		for(String item : blacklistPrefix) {
			if(key.startsWith(item)) {
				return false;
			}
		}
		return true;
	}

	/////////////////////////////////////////////
	//
	// API functionality
	//
	/////////////////////////////////////////////

	/**
	 * # $prefix/get
	 *
	 * Get and return a value from the map
	 *
	 * ## HTTP Request Parameters
	 *
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | Parameter Name  | Variable Type	   | Description                                                                   |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | key             | String             | key name, if found                                                            |
	 * | keyList         | String[]           | key name list, if found. This takes priority over key                         |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 *
	 * ## JSON Object Output Parameters
	 *
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | Parameter Name  | Variable Type	   | Description                                                                   |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | result          | {*}                | The internal object value if given.                                           |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | ERROR           | String (Optional)  | Errors encounted if any                                                       |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	*/
	public ApiFunction getEntry = (req, res) -> {

		//-------------------------------
		// Key List logic
		//-------------------------------

		// Get the key List
		String[] keyList = req.getStringArray("keyList");
		res.put("keyList", keyList);

		// Keylist found, uses it
		if( keyList != null ) {
			// The result data map
			Map<String,Object> resMap = new HashMap<String,Object>();
	
			// Validate key and get result map
			for(String key : keyList) {
				if(validateKeyname(key)) {
					resMap.put( key, dataMap.get(key) );
				}
			}
	
			// End and return result
			res.put(RESULT, resMap);
			return res;
		}

		//-------------------------------
		// Key String logic
		//-------------------------------

		// Get the key
		String key = req.getString("key");
		res.put("key", key);

		// Validate key, and get result
		if(validateKeyname(key)) {
			res.put(RESULT, dataMap.get(key));
		}

		// Return the res
		return res;
	};

	/////////////////////////////////////////////
	//
	// Actual API setup
	//
	/////////////////////////////////////////////

	/**
	 * Does the setup of the StringEscape filter, and AccessFilter config.
	 *
	 * This functionality can be refined via the config object
	 */
	protected void apiSetup(ApiBuilder api, String prefixPath, GenericConvertMap<String,Object> config) {
		super.apiSetup(api, prefixPath, config);
		api.endpoint(prefixPath+"/get", getEntry);
	}

}
