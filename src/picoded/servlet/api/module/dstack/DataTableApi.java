package picoded.servlet.api.module.dstack;

import static picoded.servlet.api.module.dstack.DStackApiConstantStrings.*;
import picoded.servlet.api.module.*;
import picoded.servlet.api.*;

import picoded.dstack.*;

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
	protected DataTable dataTableObj = null;

	/**
	 * Constructor for DataTableApi
	 *
	 * @param  DataTable  the data table object for the API to build on
	 */
	public DataTableApi(DataTable inTable) {
		dataTableObj = inTable;
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
	 * | _oid            | String             | object ID used to retrieve the meta object. If no oid is given, return null.  |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | stringEscape    | Boolean (optional) | Default TRUE. If false, returns UNSANITISED data, so common HTML escape       |
	 * |                 |                    | characters are returned as well.                                              |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 *
	 * ## JSON Object Output Parameters
	 *
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | Parameter Name  | Variable Type	    | Description                                                                  |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | _oid            | String             | The internal object ID used (or created)                                      |
	 * | stringEscape    | Boolean            | Indicate if string escape occured                                             |
	 * | data            | {Object}           | Meta object that represents this account                                      |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | error           | String (Optional)  | Errors encounted if any                                                       |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	*/
	public ApiFunction dataGet = (req, res) -> {
		// Get the oid, and sanatise output settings
		String oid = req.getString(OID, null);
		boolean stringEscape = req.getBoolean(STRING_ESCAPE, true);
		
		// Put back config in response
		res.put(OID, oid);
		res.put(STRING_ESCAPE, stringEscape);
		
		// Try get the object respectively
		try {
			// DataObject 
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return res;
	};
	
	/////////////////////////////////////////////
	//
	// Utility functions
	//
	/////////////////////////////////////////////
	


}