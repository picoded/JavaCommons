package picoded.servlet.api.module.dstack;

import picoded.servlet.api.module.*;
import picoded.servlet.api.*;
import picoded.dstack.*;

import static picoded.servlet.api.module.dstack.DStackApiConstantStrings.*;
import static picoded.servlet.api.module.ApiModuleConstantStrings.*;

import picoded.core.common.EmptyArray;
import picoded.core.struct.*;

import java.util.*;

/**
 * Provides a centralized
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
	// New object functionality
	//
	/////////////////////////////////////////////
	
	/** 
	 * # $prefix/new
	 * # $prefix/newEntry
	 *
	 * Creates a new data object. Alias for newEntry exists for code libraries that disallow the new keyword in parameters.
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
	 * | result          | String             | The internal object ID created                                                |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | error           | String (Optional)  | Errors encounted if any                                                       |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	*/
	public ApiFunction newEntry = (req, res) -> {
		// Get the oid, and sanatise output settings
		Map<String,Object> newData = req.getStringMap(DATA, "{}");
		
		// Try get the object respectively
		DataObject obj = dataTable.newEntry();
		
		// Update data only if object was found, and updated
		obj.putAll(newData);
		obj.saveAll();

		// Output the update mode (for easier debugging)
		res.put(RESULT, obj._oid());
		
		// End and return result
		return res;
	};
	
	/////////////////////////////////////////////
	//
	// Basic get and set
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
	 * | result          | {Object}           | Data object, if found                                                         |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | error           | String (Optional)  | Errors encounted if any                                                       |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	*/
	public ApiFunction get = (req, res) -> {
		// Get the oid, and sanatise output settings
		String oid = req.getString(OID, null);
		
		// Put back config in response
		res.put(OID, oid);
		res.put(RESULT, null);

		// Try get the object respectively
		DataObject obj = dataTable.get(oid);
		res.put(RESULT, obj);

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
	 * | result          | {Object}           | Data object, of changes, if applied                                           |
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
		res.put(RESULT, null);
		res.put(UPDATE_MODE, updateMode);

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
				res.put(RESULT, obj);
			} else {
				// Default mode is delta
				updateMode = "delta";
				obj.putAll(updateData);
				obj.saveDelta();

				// Return back the delta updated data (for easier debugging)
				res.put(RESULT, obj);
			}
		} else {
			res.put(ERROR, "Invalid '_oid' given");
		}

		// Output the update mode (for easier debugging)
		res.put(UPDATE_MODE, updateMode);
		
		// End and return result
		return res;
	};
	
	/** 
	 * # $prefix/delete
	 *
	 * Deletes a data object
	 *
	 * ## HTTP Request Parameters
	 *
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | Parameter Name  | Variable Type	   | Description                                                                   |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | _oid            | String             | object ID used to delete                                                      |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 *
	 * ## JSON Object Output Parameters
	 *
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | Parameter Name  | Variable Type	   | Description                                                                   |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | _oid            | String             | The internal object ID used                                                   |
	 * | result          | Boolean            | Returns true ONLY if the element was removed from the table                   |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | error           | String (Optional)  | Errors encounted if any                                                       |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	*/
	public ApiFunction delete = (req, res) -> {
		// Get the oid, and sanatise output settings
		String oid = req.getString(OID, null);

		// Put back config in response
		res.put(OID, oid);
		res.put(RESULT, false);

		// If oid is null / empty, terminate
		if( oid == null || oid.isEmpty() ) {
			return res;
		}

		// If oid does not exsits, termiante
		if( !dataTable.containsKey(oid) ) {
			return res;
		}

		// Remove, and return true
		dataTable.remove(oid);
		res.put(RESULT, true);

		// End and return result
		return res;
	};
	
	/////////////////////////////////////////////
	//
	// KeyName look up (for adminstration) 
	//
	/////////////////////////////////////////////
	
	/** 
	 * # $prefix/keyNames
	 *
	 * Deletes a data object
	 *
	 * ## HTTP Request Parameters
	 *
	 * +------+
	 * | None |
	 * +------+
	 *
	 * ## JSON Object Output Parameters
	 *
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | Parameter Name  | Variable Type	   | Description                                                                   |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | result          | String[]           | Array of string keys found in this datatable                                  |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | error           | String (Optional)  | Errors encounted if any                                                       |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	*/
	public ApiFunction keyNames = (req, res) -> {
		// Get and return keynames
		Set<String> resSet = dataTable.getKeyNames();
		res.put(RESULT, new ArrayList<String>(resSet));

		// End and return result
		return res;
	};
	
	/////////////////////////////////////////////
	//
	// List functions
	//
	/////////////////////////////////////////////
	
	/** 
	 * # $prefix/list
	 *
	 * List various data for the frontend
	 *
	 * Lists the DataObjects according to the search criteria
	 *
	 * ## HTTP Request Parameters
	 *
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | Parameter Name  | Variable Type	   | Description                                                                   |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | fieldList       | String[] (optional)| Default ["_oid"], the fields to return                                        |
	 * | query           | String   (optional)| Requested Query filter, default matches all                                   |
	 * | queryArgs       | String[] (optional)| Requested Query filter arguments                                              |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | searchString    | String   (optional)| Search string passed                                                          |
	 * | searchFieldList | String[] (optional)| Fields used for searching, defaults to headers                                |
	 * | searchMode      | String   (optional)| Default PREFIX. Determines SQL query wildcard position, for the first word.   |
	 * |                 |                    | (Either prefix, suffix, or both), second word onwards always uses both.       |
	 * |                 |                    | This is used mainly to tune the generated SQL performance, against use case.  |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | start           | int (optional)     | Default 0: Record start listing, 0-indexed                                    |
	 * | length          | int (optional)     | Default 50: The number of records to return                                   |
	 * | orderBy         | String (optional)  | Default : order by _oid                                                       |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | rowMode         | String (optional)  | Default "object", the result array row format, use either "array" or "object" |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 *
	 * ## JSON Object Output Parameters
	 *
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | Parameter Name  | Variable Type      | Description                                                                   |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | totalCount      | int                | Total amount of records, matching the query, and search filter                |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | fieldList       | String[]           | Default ["_oid"], the collumns to return                                      |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | result          | Array[Obj/Array]   | Array of row records, each row is represented as an array                     |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | error           | String (Optional)  | Errors encounted if any                                                       |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 */
	public ApiFunction list = (req, res) -> {

		// Arguments handling
		//-------------------------------------------------------------------------------

		// Get header arguments
		String[] fieldList = req.getStringArray(FIELD_LIST, "['_oid']");
		
		// The query to use
		String query = req.getString(QUERY, "").trim();
		String[] queryArgs = req.getStringArray(QUERY_ARGS, EmptyArray.STRING);
		
		// Search value to filter the result by
		String searchString = req.getString(SEARCH_STRING, "").trim();
		String[] searchFieldList = req.getStringArray(SEARCH_FIELDLIST, fieldList);
		String searchMode = req.getString(SEARCH_MODE, "suffix");

		// Fix a specific issue in DataTable, where searchString is
		// sent with beginning and ending quotes, remove it accordingly
		if( searchString.length() >= 2 ) {
			if( // 
				(searchString.startsWith("\"") && searchString.endsWith("\""))  || //
				(searchString.startsWith("'") && searchString.endsWith("'")) 
			) { //
				searchString = searchString.substring(1, searchString.length() - 1);
			}
		}

		// Start, Length, and ordering limiting of data
		int start = req.getInt(START, 0);
		int length = req.getInt(LENGTH, 50);
		String orderBy = req.getString(ORDER_BY, "oID");

		// The result data row mode
		String rowMode = req.getString(ROW_MODE, "object");

		// Processing the query and search together
		//-------------------------------------------------------------------------------

		// The actual joint query to use in list API, handled internally
		String jointQuery = query;
		String[] jointQueryArgs = queryArgs;

		// End and return result
		return res;
	};

	/**
	 * Generate query string from a single word, to apply across multiple collumns.
	 *
	 * @param  searchString String used in searching
	 * @param  queryCols    String[] query collumns to use, and search against
	 * @param  queryMode String representing the wildcard mode (PREFIX / SUFFIX / BOTH)
	 * 
	 * @return MutablePair<String,List> for the query and arguments respectively
	 */
	protected static MutablePair<String,List<Object>> generateSearchStringFromSearchPhrase(String searchString, String[] queryCols, String queryMode) {
		StringBuilder query = new StringBuilder();
		List<Object> queryArgs = new ArrayList<Object>();

		// No query is needed, terminate and return null
		if( searchString == null || queryCols == null || searchString.length() <= 0 || queryCols.length <= 0 ) {
			return null;
		}

		// Split the search string where whitespaces occur
		String[] searchStringSplit = searchString.trim().split("\\s+");
		
		// Iterate the search string
		for (int i = 0; i < searchStringSplit.length; ++i) {
			String searchWord = searchStringSplit[i];

			// Prepare the query block for one search word
			query.append("(");

			// Iterate the collumns to query
			for (int colIdx = 0; colIdx < queryCols.length; ++colIdx) {

				// Within a single word, append OR statements for each collumn
				query.append(queryCols[colIdx] + " LIKE ?");

				// Query arg for the search
				queryArgs.add(generateSearchWordWithWildcard(searchWord, queryMode));

				// Append or statemetns between collumns
				if (colIdx < queryCols.length - 1) {
					query.append(" OR ");
				}
			}

			// Close the query block for the search word
			query.append(")");

			// Append the AND statement between word blocks
			if (i < searchStringSplit.length - 1) {
				query.append(" AND ");
			}
			
			// Second string onwards is an "any" prefix and suffix wildcard
			queryMode = "any"; 
		}

		// Invalid blank query (wrongly formatted input?)
		if( query.length() <= 2 ) {
			return null;
		}

		// Return the built query
		return new MutablePair<String,List<Object>>(query.toString(), queryArgs);
	}
	
	/**
	 * Generate a string, with the SQL wildcard attached, in accordence to the given search word, and/or wildcard mode
	 *
	 * @param  searchWord  to modify
	 * @param  queryMode  to use, either prefix/suffix/exact/any(fallback)
	 *
	 * @return  Modified string to pass as argument in larger query
	 */
	protected static String generateSearchWordWithWildcard(String searchWord, String queryMode) {
		// if (queryMode.equalsIgnoreCase("exact")) {
		// 	return searchWord;
		// }  
		if (queryMode.equalsIgnoreCase("prefix")) {
			return searchWord + "%";
		} else if (queryMode.equalsIgnoreCase("suffix")) {
			return "%" + searchWord;
		} else {
			return "%" + searchWord + "%";
		}
	}

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

		api.endpoint(prefixPath+"/new", newEntry);
		api.endpoint(prefixPath+"/newEntry", newEntry);

		api.endpoint(prefixPath+"/get", get);
		api.endpoint(prefixPath+"/set", set);
		api.endpoint(prefixPath+"/delete", delete);

		api.endpoint(prefixPath+"/keyNames", keyNames);
	}

}