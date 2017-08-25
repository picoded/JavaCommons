package picoded.servlet.api.module.dstack;

import static picoded.servlet.api.module.dstack.DStackApiConstantStrings.*;
import picoded.servlet.api.module.*;
import picoded.servlet.api.*;

import picoded.dstack.*;
import picoded.core.struct.*;

/**
 * Does the constructor setup of DataTable
 */
public abstract class DataTableApi extends CommonApiModule {

	/////////////////////////////////////////////
	//
	// Constructor setup
	//
	/////////////////////////////////////////////
	
	// Internal data table object, set by constructor
	protected DataTable dataTable = null;

	/**
	 * Constructor for DataTableApi
	 *
	 * @param  DataTable  the data table object for the API to build on
	 */
	public DataTableApi(DataTable inTable) {
		dataTable = inTable;
	}

	/////////////////////////////////////////////
	//
	// Getter functions
	//
	/////////////////////////////////////////////
	
	/** 
	 * # $prefix/get
	 *
	 * Gets and return the data object
	 *
	 * ## HTTP Request Parameters
	 *
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | Parameter Name  | Variable Type	    | Description                                                                  |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | _oid            | String             | object ID used to retrieve the data object. If no oid is given, return null.  |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 *
	 * ## JSON Object Output Parameters
	 *
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | Parameter Name  | Variable Type	    | Description                                                                  |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | _oid            | String             | The internal object ID used                                                   |
	 * | data            | {Object}           | Data object, if found                                                         |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | error           | String (Optional)  | Errors encounted if any                                                       |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	*/
	public ApiFunction get = (req, res) -> {
		// Get the oid, and sanatise output settings
		String oid = req.getString(OID, null);
		
		// Put back config in response
		res.put(OID, oid);
		res.put(DATA, null);

		// Try get the object respectively
		DataObject obj = dataTable.get(oid);
		res.put(DATA, obj);

		// End and return result
		return res;
	};
	
	/** 
	 * # $prefix/set
	 *
	 * Update a data object
	 *
	 * ## HTTP Request Parameters
	 *
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | Parameter Name  | Variable Type	   | Description                                                                   |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | _oid            | String             | object ID used to retrieve the meta object. If no oid is given, return null.  |
	 * | data            | {Object}           | Data object, to apply update if found                                         |
	 * | updateMode      | String (Optional)  | (Default) "delta" for only updating the given fields, or "full" for all       |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 *
	 * ## JSON Object Output Parameters
	 *
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | Parameter Name  | Variable Type	   | Description                                                                   |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | _oid            | String             | The internal object ID used                                                   |
	 * | data            | {Object}           | Data object, of changes, if applied                                           |
	 * | updateMode      | String             | Update mode used                                                              |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | error           | String (Optional)  | Errors encounted if any                                                       |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	*/
	public ApiFunction set = (req, res) -> {
		// Get the oid, and sanatise output settings
		String oid = req.getString(OID, null);
		String updateMode = req.getString(UPDATE_MODE, "delta");
		Map<String,Object> updateData = req.getStringMap(DATA, "{}");
		
		// Put back config in response
		res.put(OID, oid);
		res.put(DATA, null);

		// Try get the object respectively
		DataObject obj = dataTable.get(oid);
		
		// Update data only if object was found, and updated
		if( obj != null ) {
			if( updateMode.equalsIgnoreCase("full") ) {
				// Does a full replacement update
				obj.clear();
				obj.putAll(updateData);
				obj.saveAll();

				// Return back the full updated data (for easier debugging)
				res.put(DATA, obj);
			} else {
				// Default mode is delta
				updateMode = "delta";
				obj.putAll(updateData);
				obj.saveDelta();

				// Return back the delta updated data (for easier debugging)
				res.put(DATA, obj);
			}
		}

		// Output the update mode (for easier debugging)
		res.put(UPDATE_MODE, updateMode);
		
		// End and return result
		return res;
	};
	
	/** 
	 * # $prefix/new
	 *
	 * Update a data object
	 *
	 * ## HTTP Request Parameters
	 *
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | Parameter Name  | Variable Type	   | Description                                                                   |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | data            | {Object}           | Data object, if found                                                         |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 *
	 * ## JSON Object Output Parameters
	 *
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | Parameter Name  | Variable Type	   | Description                                                                   |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | _oid            | String             | The internal object ID created                                                |
	 * | data            | {Object}           | Data object, of changes, if applied                                           |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | error           | String (Optional)  | Errors encounted if any                                                       |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	*/
	public ApiFunction newObject = (req, res) -> {
		// Get the oid, and sanatise output settings
		Map<String,Object> newData = req.getStringMap(DATA, "{}");
		
		// Try get the object respectively
		DataObject obj = dataTable.new();
		
		// Update data only if object was found, and updated
		obj.putAll(newData);
		obj.saveAll();

		// Output the update mode (for easier debugging)
		res.put(OID, obj._oid());
		
		// End and return result
		return res;
	};
	
	/////////////////////////////////////////////
	//
	// Utility functions
	//
	/////////////////////////////////////////////
	


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
	protected void apiBuilderSetup(ApiBuilder api, String prefixPath, GenericConvertMap<String,Object> config) {
		super.apiBuilderSetup(api, prefixPath, config);

		api.endpoint(prefixPath+"/get", get);
	}

}