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
 * Pure static function implementation of DataTableApi,
 * Including its utility and helper functions.
 * 
 * This allows for quick referencing from other API classes.
 * 
 * NOTE: These static functions may not be designed to do argument
 * double check the individual static function for more details.
 */
public class DataTableStaticApi {
	
	/////////////////////////////////////////////
	//
	// New object functionality
	//
	/////////////////////////////////////////////
	
	/**
	 * Creates a new data object. And return its _oid as result
	 * 
	 * @param  res      result object map to populate 'result' and return
	 * @param  dTable   DataTable object to use
	 * @param  newData  new meta data to populate the object
	 * 
	 * @return result object, with 'result' param
	 * 
	 * ## Result Object Output Parameters
	 * +-----------------+--------------------+--------------------------------+
	 * | Parameter Name  | Variable Type      | Description                    |
	 * +-----------------+--------------------+--------------------------------+
	 * | result          | {Object}           | The internal object ID created |
	 * +-----------------+--------------------+--------------------------------+
	 */
	public static <T extends GenericConvertMap<String, Object>> T newEntry(T res, DataTable dTable,
		Map<String, Object> newData) {
		// Prepare the new dataObject
		DataObject obj = dTable.newEntry();
		// Save all its new value
		obj.putAll(newData);
		obj.saveAll();
		// Output the OID
		res.put("result", obj._oid());
		// End and return result
		return res;
	}
	
	/////////////////////////////////////////////
	//
	// Basic get, set and delete
	//
	/////////////////////////////////////////////
	
	/**
	 * Gets and return the data object result
	 * 
	 * @param  res      result object map to populate 'result' and return
	 * @param  dTable   DataTable object to use
	 * @param  oid      oid to get result from
	 * 
	 * @return result object, with 'result' param
	 * 
	 * ## Result Object Output Parameters
	 * +-----------------+--------------------+------------------------------+
	 * | Parameter Name  | Variable Type      | Description                  |
	 * +-----------------+--------------------+------------------------------+
	 * | _oid            | String             | The internal object ID used  |
	 * | result          | {Object}           | Data object, if found        |
	 * +-----------------+--------------------+------------------------------+
	 */
	public static <T extends GenericConvertMap<String, Object>> T get(T res, DataTable dTable,
		String oid) {
		// Put back config in response
		res.put("_oid", oid);
		res.put("result", null);
		// Try get the object respectively
		DataObject obj = dTable.get(oid);
		// Provide missin information if not found
		if (obj == null) {
			res.put("INFO", "Object not found");
		} else {
			res.put("result", obj);
		}
		// End and return the result
		return res;
	}
	
	/**
	 * Gets and return the data object result
	 * 
	 * @param  res         result object map to populate 'result' and return
	 * @param  dTable      DataTable object to use
	 * @param  oid         oid to set 
	 * @param  updateData  Data object, to apply update if found
	 * @param  updateMode  "delta" for only updating the given fields, or "full" for all
	 * 
	 * @return result object, with 'result' param
	 * 
	 * ## Result Object Output Parameters
	 * +-----------------+--------------------+--------------------------------------+
	 * | Parameter Name  | Variable Type	   | Description                          |
	 * +-----------------+--------------------+--------------------------------------+
	 * | _oid            | String             | The internal object ID used          |
	 * | result          | {Object}           | Data object, of changes, if applied  |
	 * | updateMode      | String             | Update mode used                     |
	 * +-----------------+--------------------+--------------------------------------+
	 */
	public static <T extends GenericConvertMap<String, Object>> T set(T res, DataTable dTable,
		String oid, Map<String, Object> updateData, String updateMode) {
		// Put back config in response
		res.put("_oid", oid);
		res.put("result", null);
		res.put("updateMode", updateMode);
		// Try get the object respectively
		DataObject obj = dTable.get(oid);
		// Update data only if object was found, and updated
		if (obj != null) {
			if (updateMode.equalsIgnoreCase("full")) {
				// Default mode is delta
				updateMode = "full";
				// Does a full replacement update
				obj.clear();
				obj.putAll(updateData);
				obj.saveAll();
				// Return back the full updated data (for easier debugging)
				res.put("result", obj);
			} else {
				// Default mode is delta
				updateMode = "delta";
				// Does a delta update
				obj.putAll(updateData);
				obj.saveDelta();
				// Return back the delta updated data (for easier debugging)
				res.put("result", obj);
			}
		} else {
			// No object found, update is not possible
			res.put(ERROR, "Object not found");
		}
		// Actual update mode used (after validating)
		res.put("updateMode", updateMode);
		// End and return result
		return res;
	}
	
	/**
	 * Gets and return the data object result
	 * 
	 * @param  res      result object map to populate 'result' and return
	 * @param  dTable   DataTable object to use
	 * @param  oid      oid to get result from
	 * 
	 * @return result object, with 'result' param
	 * 
	 * ## Result Object Output Parameters
	 * +-----------------+--------------------+-------------------------------------------------------------+
	 * | Parameter Name  | Variable Type      | Description                                                 |
	 * +-----------------+--------------------+-------------------------------------------------------------+
	 * | _oid            | String             | The internal object ID used                                 |
	 * | result          | {Object}           | Returns true ONLY if the element was removed from the table |
	 * +-----------------+--------------------+-------------------------------------------------------------+
	 */
	public static <T extends GenericConvertMap<String, Object>> T delete(T res, DataTable dTable,
		String oid) {
		// Put back config in response
		res.put("_oid", oid);
		res.put("result", false);
		// If oid is null / empty, terminate
		if (oid == null || oid.isEmpty()) {
			res.put("INFO", "Invalid oid parameter");
			return res;
		}
		// If oid does not exsits, termiante
		if (!dTable.containsKey(oid)) {
			res.put("INFO", "Object not found");
			return res;
		}
		// Remove, and return true
		dTable.remove(oid);
		res.put("result", true);
		// End and return the result
		return res;
	}
	
	/////////////////////////////////////////////
	//
	// List functions : utility
	// (Because this is gonna be complex)
	//
	/////////////////////////////////////////////
	
	/**
	 * Generate a string, with the SQL wildcard attached, in accordence to the given search word, and/or wildcard mode
	 * Just see its code, for more details (its straight forwards)
	 *
	 * @param  searchWord  to modify
	 * @param  searchMode  to use, either prefix/suffix/any(fallback)
	 *
	 * @return  Search word with SQL prefix/suffix/both wildcard
	 */
	public static String generateSearchWordWithWildcard(String searchWord, String searchMode) {
		if (searchMode.equalsIgnoreCase("prefix")) {
			return searchWord + "%";
		} else if (searchMode.equalsIgnoreCase("suffix")) {
			return "%" + searchWord;
		} else {
			return "%" + searchWord + "%";
		}
	}
	
	/**
	 * Generate query string from a single word, to apply across multiple collumns.
	 * Returns null if searchString failed to be processed.
	 * 
	 * For example with the following parameters
	 * 
	 * ```
	 * searchString = "hello world"
	 * searchCols   = ["colA", "colB"]
	 * searchMode   = "PREFIX"
	 * ```
	 * 
	 * Would result into the following
	 * 
	 * ```
	 * (colA LIKE 'hello%' OR colB LIKE 'hello%') AND
	 * (colA LIKE '%world%' OR colB LIKE '%world%')
	 * ```
	 * 
	 * Naturally, for very large objects with 10 over collumns, 
	 * this gets out of hand rather rapidly.
	 *
	 * @param  searchString String used in searching
	 * @param  searchCols   String[] query collumns to use, and search against
	 * @param  searchMode    String representing the wildcard mode (PREFIX / SUFFIX / ANY)
	 *
	 * @return MutablePair<String,List> for the query and arguments respectively
	 */
	public static MutablePair<String, Object[]> generateSearchStringFromSearchPhrase(
		String searchString, String[] searchCols, String searchMode) {
		// No query is needed, terminate and return null
		if (searchString.isEmpty() || searchCols.length <= 0) {
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
			for (int colIdx = 0; colIdx < searchCols.length; ++colIdx) {
				
				// Within a single word, append OR statements for each collumn
				query.append(searchCols[colIdx] + " LIKE ?");
				
				// Query arg for the search
				queryArgs.add(generateSearchWordWithWildcard(searchWord, searchMode));
				
				// Append or statemetns between collumns
				if (colIdx < searchCols.length - 1) {
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
			searchMode = "any";
		}
		
		// Invalid blank query (wrongly formatted input?)
		if (query.length() <= 2) {
			return null;
		}
		
		// Return the built query
		return new MutablePair<String, Object[]>(query.toString(), queryArgs.toArray(new Object[0]));
	}
	
	/**
	 * Merges an existing query, with a search string. For it to be easier used and processed
	 *
	 * @param  query      Requested Query filter
	 * @param  queryArgs  Requested Query filter arguments
	 * 
	 * @param  searchString  String used in searching
	 * @param  searchCols    String[] query collumns to use, and search against
	 * @param  searchMode    String representing the wildcard mode (PREFIX / SUFFIX / ANY)
	 * 
	 * @param  mergeType   "AND" / "OR" merging mode
	 *
	 * @return MutablePair<String,List> for the query and arguments respectively
	 */
	public static MutablePair<String, Object[]> collapseSearchStringAndQuery(String queryString,
		Object[] queryArgs, String searchString, String[] searchCols, String searchMode,
		String mergeMode) {
		// The return query pair
		String retQuery = null;
		Object[] retQueryArgs = null;
		
		// Process the search query parameters
		MutablePair<String, Object[]> searchQuery = generateSearchStringFromSearchPhrase(
			searchString, searchCols, searchMode);
		
		// If search query parameter is valid
		if (searchQuery != null) {
			if (queryString == null || queryString.isEmpty() || queryArgs.length == 0) {
				// If query is empty, assumes that search replaces it
				retQuery = searchQuery.getLeft();
				retQueryArgs = searchQuery.getRight();
			} else {
				// query isnt empty, does a "merge" with it
				retQuery = "(" + queryString + ") " + mergeMode + " (" + searchQuery.getLeft() + ")";
				retQueryArgs = ArrayConv.addAll(queryArgs, searchQuery.getRight());
			}
		} else {
			retQuery = queryString;
			retQueryArgs = queryArgs;
		}
		// Final actual return
		return new MutablePair<String, Object[]>(retQuery, retQueryArgs);
	}
	
	/**
	 * Format the data object list into an expected result format.
	 * 
	 * @param  dataObjs  array to convert into a result list
	 * @param  fieldList list of parameters to output
	 * @param  rowMode   result mode, either as an array of array, or array of objects; with the chosen fields
	 */
	public static List<Object> formatDataObjectList(DataObject[] dataObjs, String[] fieldList,
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
	// List functions
	//
	/////////////////////////////////////////////
	
	/**
	 * Gets and return a list of data objects.
	 * 
	 * Note that the long list of parameter options is kinda necessary evil at this point,
	 * Due to the actual versitility, and complexity of the api it supports.
	 * 
	 * Query, and search string is collapsed, and used in the simplified listing function
	 * 
	 * @param  res        result object map to populate 'result' and return
	 * @param  dTable     DataTable object to use
	 * 
	 * @param  fieldList  field list to return in the result
	 * @param  query      Requested Query filter
	 * @param  queryArgs  Requested Query filter arguments
	 * 
	 * @param  searchString     Search string to use
	 * @param  searchFieldList  List of fields to search
	 * @param  searchMode       Determines SQL query wildcard position, for the first word.
	 *                          (Either prefix, suffix, or both), second word onwards always uses both.
	 * 
	 * @param  start      Record start listing, 0-indexed
	 * @param  length     Number of records to return
	 * @param  orderBy    Result ordering to use
	 * @param  rowMode    result array row format, use either "array" or "object"
	 * 
	 * @return result object, with 'result' param
	 * 
	 * ## Result Object Output Parameters
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | Parameter Name  | Variable Type      | Description                                                                   |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | recordsFiltered | int                | Total amount of records, matching the query, and search filter                |
	 * | recordsTotal    | int (not critical) | Total amount of records, matching the query, before any search filter         |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | fieldList       | String[]           | Default ["_oid"], the collumns to return                                      |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | result          | Array[Obj/Array]   | Array of row records, each row is represented as an array                     |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | error           | String (Optional)  | Errors encounted if any                                                       |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 */
	public static <T extends GenericConvertMap<String, Object>> T list(T res, DataTable dTable,
		String[] fieldList, String query, Object[] queryArgs, String searchString,
		String[] searchFieldList, String searchMode, int start, int length, String orderBy,
		String rowMode) {
		MutablePair<String, Object[]> queryPair = collapseSearchStringAndQuery(query, queryArgs,
			searchString, searchFieldList, searchMode, "AND");
		
		// Get total result counts
		res.put("recordsTotal", dTable.queryCount(query, queryArgs));

		if( queryPair.getLeft() == null || queryPair.getLeft().equalsIgnoreCase(query) ) {
			// No additional search filter used : skip additional queryCount
			res.put("recordsFiltered", res.get("recordsTotal"));
		} else {
			// Additional search filter was used
			res.put("recordsFiltered", dTable.queryCount(queryPair.getLeft(), queryPair.getRight()));
		}
		
		// Process and list the actual results
		return list(res, dTable, fieldList, queryPair.getLeft(), queryPair.getRight(), start, length,
			orderBy, rowMode);
	}
	
	/**
	 * Gets and return a list of data objects.
	 * 
	 * Simplified version of the list, without the search parameter.
	 * Which is assumed to be merged within query itself at this point.
	 * 
	 * @param  res        result object map to populate 'result' and return
	 * @param  dTable     DataTable object to use
	 * 
	 * @param  fieldList  field list to return in the result
	 * @param  query      Requested Query filter
	 * @param  queryArgs  Requested Query filter arguments.
	 * 
	 * @param  start      Record start listing, 0-indexed
	 * @param  length     Number of records to return
	 * @param  orderBy    Result ordering to use
	 * @param  rowMode    result array row format, use either "array" or "object"
	 * 
	 * @return result object, with 'result' param
	 * 
	 * ## Result Object Output Parameters
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | Parameter Name  | Variable Type      | Description                                                                   |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | fieldList       | String[]           | Default ["_oid"], the collumns to return                                      |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | result          | Array[Obj/Array]   | Array of row records, each row is represented as an array                     |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | error           | String (Optional)  | Errors encounted if any                                                       |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 */
	public static <T extends GenericConvertMap<String, Object>> T list(T res, DataTable dTable,
		String[] fieldList, String query, Object[] queryArgs, int start, int length, String orderBy,
		String rowMode) {
		
		// Converts the query to null, if 'blank'
		if (query.isEmpty() || queryArgs.length == 0) {
			query = null;
			queryArgs = null;
		}
		
		// Fetching the objects and count
		DataObject[] dataObjs = dTable.query(query, queryArgs, orderBy, start, length);
		
		// Process the result for output
		res.put("fieldList", fieldList);
		res.put("result", formatDataObjectList(dataObjs, fieldList, rowMode));
		
		// End and return result
		return res;
	}
	
}
