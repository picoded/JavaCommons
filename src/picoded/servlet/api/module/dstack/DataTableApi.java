package picoded.servlet.api.module.dstack;

import picoded.servlet.api.module.*;
import picoded.servlet.api.*;
import picoded.dstack.*;

import static picoded.servlet.api.module.dstack.DStackApiConstantStrings.*;
import static picoded.servlet.api.module.ApiModuleConstantStrings.*;

import picoded.core.common.*;
import picoded.core.struct.query.*;
import picoded.core.struct.*;
import picoded.core.conv.*;

import java.util.*;

/**
 * Provides a centralized
 */
public class DataTableApi extends CommonApiModule {
	
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
	
	/**
	 * Array of internal subsystems : Currently only DataTable dstack module
	 *
	 * @return  Array of internal subsystems
	 */
	protected SystemSetupInterface[] internalSubsystemArray() {
		return new SystemSetupInterface[] { dataTable };
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
		return DataTableStaticApi.newEntry(res, dataTable, req.getStringMap("data", "{}"));
	};
	
	/////////////////////////////////////////////
	//
	// Basic get, set and delete
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
	 * | Parameter Name  | Variable Type      | Description                                                                   |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | _oid            | String             | object ID used to retrieve the data object. If no oid is given, return null.  |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 *
	 * ## JSON Object Output Parameters
	 *
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | Parameter Name  | Variable Type      | Description                                                                   |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | _oid            | String             | The internal object ID used                                                   |
	 * | result          | {Object}           | Data object, if found                                                         |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | error           | String (Optional)  | Errors encounted if any                                                       |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 */
	public ApiFunction get = (req, res) -> {
		return DataTableStaticApi.get(res, dataTable, req.getString("_oid", null));
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
		return DataTableStaticApi.set(res, dataTable, req.getString("_oid", null),
			req.getStringMap("data", "{}"), req.getString("updateMode", "delta"));
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
		return DataTableStaticApi.delete(res, dataTable, req.getString("_oid", null));
	};
	
	/////////////////////////////////////////////
	//
	// KeyName look up (mainly for adminstration)
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
	 * | queryArgs       | Object[] (optional)| Requested Query filter arguments                                              |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | searchString    | String   (optional)| Search string passed                                                          |
	 * | searchFieldList | String[] (optional)| Fields used for searching, defaults to headers                                |
	 * | searchMode      | String   (optional)| Default PREFIX. Determines SQL query wildcard position, for the first word.   |
	 * |                 |                    | (Either prefix, suffix, or both), second word onwards always uses both.       |
	 * |                 |                    | For example: PREFIX mode, with search string hello, will query with "hello%". |
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
		
		// Get fieldList arguments
		String[] fieldList = req.getStringArray(FIELD_LIST, "['_oid']");
		if (fieldList.length <= 0) {
			res.put(ERROR, "fieldList, requested cannot be an empty array");
			res.halt();
		}
		// The query to use
		String query = req.getString(QUERY, "").trim();
		Object[] queryArgs = req.getObjectArray(QUERY_ARGS, EmptyArray.STRING);
		// Query format safety check
		if (!query.isEmpty()) {
			try {
				Query queryCheck = Query.build(query, queryArgs);
				query = queryCheck.toSqlString();
				queryArgs = queryCheck.queryArgumentsArray();
			} catch (Exception e) {
				res.put(ERROR, "Invalid query format : " + query);
				res.halt();
			}
		}
		
		// Search value to filter the result by
		String searchString = req.getString(SEARCH_STRING, "").trim();
		String[] searchFieldList = req.getStringArray(SEARCH_FIELDLIST, fieldList);
		String searchMode = req.getString(SEARCH_MODE, "prefix");
		
		// Fix a specific issue in DataTable, where searchString is
		// sent with beginning and ending quotes, remove it accordingly
		if (searchString.length() >= 2) {
			if ( //
			(searchString.startsWith("\"") && searchString.endsWith("\"")) || //
				(searchString.startsWith("'") && searchString.endsWith("'"))) { //
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
		//
		// Since the above does extensive null check fallbacks,
		// everything past this point can safely be assumed to be not null
		//-------------------------------------------------------------------------------
		
		// The actual joint query to use in list API, handled internally
		String jointQuery = query;
		Object[] jointQueryArgs = queryArgs;
		
		// Checks if search string is valid, and process it
		MutablePair<String, Object[]> searchQuery = generateSearchStringFromSearchPhrase(
			searchString, searchFieldList, searchMode);
		if (searchQuery != null) {
			if (query.isEmpty() || queryArgs.length == 0) {
				// If query is empty, assumes that search replaces it
				jointQuery = searchQuery.getLeft();
				jointQueryArgs = searchQuery.getRight();
			} else {
				// query isnt empty, does a "merge" with it
				jointQuery = "(" + query + ") AND (" + searchQuery.getLeft() + ")";
				jointQueryArgs = ArrayConv.addAll(queryArgs, searchQuery.getRight());
			}
		} // else fallsback to just query
		
		// Executing the query, and getting its result
		//-------------------------------------------------------------------------------
		
		// The resulting data object, and total count
		DataObject[] dataObjs = null;
		long dataCount = 0;
		
		// Converts the jointQuery to null, if blank
		if (jointQuery.isEmpty() || jointQueryArgs.length == 0) {
			jointQuery = null;
			jointQueryArgs = null;
		}
		
		// Fetching the objects and count
		dataObjs = dataTable.query(jointQuery, jointQueryArgs, orderBy, start, length);
		dataCount = dataTable.queryCount(jointQuery, jointQueryArgs);
		
		// Process the result for output
		res.put(FIELD_LIST, fieldList);
		res.put(TOTAL_COUNT, dataCount);
		res.put(RESULT, formatDataObjectList(dataObjs, fieldList, rowMode));
		
		// End and return result
		return res;
	};
	
	/////////////////////////////////////////////
	//
	// List functions utils
	// (maybe migrated to another class)
	//
	/////////////////////////////////////////////
	
	/**
	 * Generate query string from a single word, to apply across multiple collumns.
	 * Returns null if searchString failed to be processed
	 *
	 * @param  searchString String used in searching
	 * @param  queryCols    String[] query collumns to use, and search against
	 * @param  queryMode String representing the wildcard mode (PREFIX / SUFFIX / BOTH)
	 *
	 * @return MutablePair<String,List> for the query and arguments respectively
	 */
	protected static MutablePair<String, Object[]> generateSearchStringFromSearchPhrase(
		String searchString, String[] queryCols, String queryMode) {
		// No query is needed, terminate and return null
		if (searchString.isEmpty() || queryCols.length <= 0) {
			return null;
		}
		
		// The return args
		StringBuilder query = new StringBuilder();
		List<Object> queryArgs = new ArrayList<Object>();
		
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
		if (query.length() <= 2) {
			return null;
		}
		
		// Return the built query
		return new MutablePair<String, Object[]>(query.toString(), queryArgs.toArray(new Object[0]));
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
	
	/**
	 * Format the data object list result
	 */
	protected static List<Object> formatDataObjectList(DataObject[] dataObjs, String[] fieldList,
		String rowMode) {
		// Check if row mode is an array, else assume its an object
		boolean isArrayMode = rowMode.equalsIgnoreCase("array");
		// The return data
		List<Object> ret = new ArrayList<Object>();
		
		// Iterate data objects
		for (DataObject obj : dataObjs) {
			// Prepare the result in accordance to the data mode
			if (isArrayMode) {
				// Assume array mode output
				List<Object> row = new ArrayList<Object>();
				for (int i = 0; i < fieldList.length; ++i) {
					row.add(obj.get(fieldList[i]));
				}
				ret.add(row);
			} else {
				// Assume object mode output
				Map<String, Object> row = new HashMap<String, Object>();
				for (int i = 0; i < fieldList.length; ++i) {
					row.put(fieldList[i], obj.get(fieldList[i]));
				}
				ret.add(row);
			}
		}
		
		// Return the result
		return ret;
	}
	
	/////////////////////////////////////////////
	//
	// List varients (for UI integration)
	//
	/////////////////////////////////////////////
	
	/**
	 * # $prefix/list/datatables
	 *
	 * Varient of the list function : Used specifically for datatable integration.
	 *
	 * This supports the list field parameters, unless overwritten by a DataTable field (as listed below), such as rowMode='array';
	 *
	 * ## DataTables specific parameters
	 *
	 * +---------------------+--------------------+-------------------------------------------------------------------------------+
	 * | Parameter Name      | Variable Type      | Description                                                                   |
	 * +---------------------+--------------------+-------------------------------------------------------------------------------+
	 * | rowMode             | String (optional)  | Fixed as 'array', any values sent here is ignored                             |
	 * +---------------------+--------------------+-------------------------------------------------------------------------------+
	 * | draw                | int (optional)     | Draw counter echoed back (used by the datatables.js server-side API)          |
	 * +---------------------+--------------------+-------------------------------------------------------------------------------+
	 * | search[value]       | String (optional)  | Overwrites searchValue if given                                               |
	 * +---------------------+--------------------+-------------------------------------------------------------------------------+
	 * | order[0][column]    | int (optional)     | Column ID to perform table ordering, ignored if 'orderBy' parameter is set    |
	 * | order[X][orderable] | boolean (optional) | Column ID (X) status, if not false. Ordering by specified column occurs       |
	 * +---------------------+--------------------+-------------------------------------------------------------------------------+
	 *
	 * ## JSON Object Output Parameters
	 *
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | Parameter Name  | Variable Type      | Description                                                                   |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | draw            | int (optional)     | Draw counter echoed back (used by the datatables.js server-side API)          |
	 * | recordsTotal    | int (not critical) | Total amount of records. Before any search filter (But after base filters)    |
	 * | recordsFiltered | int (not critical) | Total amount of records, matching the query, and search (replaces totalCount) |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | fieldList       | String[]           | Default ["_oid"], the collumns to return                                      |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | data            | Array[Array]       | Data result of row records, each row is an array, Replaces result             |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | error           | String (Optional)  | Errors encounted if any                                                       |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 */
	public ApiFunction datatables = (req, res) -> {
		// Proxy the draw request
		res.put("draw", req.getInt("draw", 0));
		
		// Set row mode
		req.put(ROW_MODE, "array");
		
		// Get search[value]
		String searchValue = req.getString("search[value]");
		if (searchValue != null && !searchValue.isEmpty()) {
			req.put(SEARCH_STRING, searchValue);
		}
		
		// DataTables : Order by string integration
		//----------------------------------------------------------------------------
		
		// Get fieldList arguments
		String[] fieldList = req.getStringArray(FIELD_LIST, "['_oid']");
		
		// If orderBy string is not configured, use dataTable settings
		String orderByStr = req.getString("orderBy");
		if (orderByStr == null || orderByStr.isEmpty()) {
			// try and get datatables order data
			int orderByColumn = req.getInt("order[0][column]", -1);
			if (orderByColumn <= -1) {
				orderByStr = null;
			} else {
				boolean isColumnOrderable = req.getBoolean("columns[" + orderByColumn + "][orderable]",
					false);
				if (!isColumnOrderable || fieldList.length < orderByColumn) {
					orderByStr = null;
				} else {
					orderByStr = fieldList[orderByColumn];
				}
			}
			
			// orderBy DESC or ASC
			String orderByStyle = req.getString("order[0][dir]", "asc");
			orderByStr += " " + orderByStyle;
			
			// Set the orderBy string value respectively
			req.put("orderBy", orderByStr);
		}
		
		// Does the default list processing
		res = list.apply(req, res);
		
		// Change some formatting of result
		res.put("data", res.get("result", null));
		res.put("recordsFiltered", res.get("totalCount", null));
		
		// Clear original formatting
		res.put("result", null);
		res.put("totalCount", null);
		
		// Return it
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
	protected void apiSetup(ApiBuilder api, String prefixPath,
		GenericConvertMap<String, Object> config) {
		super.apiSetup(api, prefixPath, config);
		
		api.endpoint(prefixPath + "/new", newEntry);
		api.endpoint(prefixPath + "/newEntry", newEntry);
		
		api.endpoint(prefixPath + "/get", get);
		api.endpoint(prefixPath + "/set", set);
		api.endpoint(prefixPath + "/delete", delete);
		
		api.endpoint(prefixPath + "/list", list);
		api.endpoint(prefixPath + "/list/datatables", datatables);
		
		api.endpoint(prefixPath + "/keyNames", keyNames);
	}
	
}
