package picoded.servlet.api.module.account;

import java.util.*;
import java.util.function.BiFunction;

import picoded.servlet.api.*;
import picoded.servlet.api.module.*;
import picoded.dstack.module.account.*;
import picoded.servlet.api.module.dstack.*;
import picoded.dstack.*;
import picoded.core.conv.ConvertJSON;
import picoded.core.conv.StringEscape;
import picoded.core.conv.GenericConvert;
import picoded.core.struct.GenericConvertMap;
import picoded.core.struct.MutablePair;
import picoded.core.struct.query.OrderBy;
import picoded.core.struct.query.Query;
import picoded.core.struct.GenericConvertHashMap;
import static picoded.servlet.api.module.account.AccountConstantStrings.*;
import static picoded.servlet.api.module.ApiModuleConstantStrings.*;

import picoded.core.common.EmptyArray;
import picoded.core.common.SystemSetupInterface;

public class AccountInfoApi extends CommonApiModule {
	
	/**
	 * The AccountTable reference
	 **/
	protected AccountTable table = null;
	
	/**
	 * The DataTableApi reference
	 **/
	protected DataTableApi dataTableApi = null;
	
	/**
	 * Static ERROR MESSAGES
	 **/
	public static final String MISSING_REQUEST_PAGE = "Unexpected Exception: Missing requestPage()";
	
	/**
	 * Setup the account login API
	 *
	 * @param  AccountTable to use
	 **/
	public AccountInfoApi(AccountTable inTable) {
		table = inTable;
		dataTableApi = new DataTableApi(inTable.accountDataTable());
	}
	
	/**
	 * Internal subsystem array, used to chain up setup commands
	 *
	 * @TODO : Chain up accountTable internalSubsystemArray to its internal objects
	 *
	 * @return  Array containing the AccountTable used
	 */
	protected SystemSetupInterface[] internalSubsystemArray() {
		return new SystemSetupInterface[] {};
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	//   DataTable info proxy
	//
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Utility function used to set the current account as _oid, for the api request
	 * IF _oid is not set
	 */
	protected void defaultsCurrentAccountAsOID(ApiRequest req, ApiResponse res) {
		// Only works if _oid is null
		if (req.getString("_oid") == null) {
			// Get the current user
			AccountObject currentUser = table.getRequestUser(req.getHttpServletRequest(), null);
			
			// If current user is null, halt and throw an error
			if (currentUser == null) {
				res.put(ERROR, ERROR_NO_USER);
				return;
			}
			
			// Put user._oid as _oid
			req.put("_oid", currentUser._oid());
		}
	}
	
	/**
	 * # $prefix/info/get
	 *
	 * Gets and return the data object
	 *
	 * ## HTTP Request Parameters
	 *
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | Parameter Name  | Variable Type      | Description                                                                   |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | _oid            | String             | object ID used to retrieve the data object. Default to login user.            |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 *
	 * ## JSON Object Output Parameters
	 *
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | Parameter Name  | Variable Type      | Description                                                                   |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | _oid            | String             | The internal object ID used                                                   |
	 * | result          | {Object}           | Data object, if found                                                         |
	 * | loginName       | String[]           | List of login names                                                           |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | error           | String (Optional)  | Errors encounted if any                                                       |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 **/
	protected ApiFunction info_get = (req, res) -> {
		defaultsCurrentAccountAsOID(req, res);
		
		// Return a list of login names of the user if exists
		String oid = req.getString(OID, null);
		AccountObject ao = table.get(oid);
		if (ao != null) {
			res.put(LOGINNAMELIST, ao.getLoginNameSet());
		}
		
		return dataTableApi.get.apply(req, res);
	};
	
	/**
	 * # $prefix/info/set
	 *
	 * Update a data object
	 *
	 * ## HTTP Request Parameters
	 *
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | Parameter Name  | Variable Type	   | Description                                                                   |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | _oid            | String             | object ID used to retrieve the data object. Default to login user.            |
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
	 **/
	protected ApiFunction info_set = (req, res) -> {
		defaultsCurrentAccountAsOID(req, res);
		return dataTableApi.set.apply(req, res);
	};
	
	/**
	 * # $prefix/info/list
	 * Account specific varient of DataTable list, with additional filters.
	 * 
	 * See: DataTableApi.list
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
	 * | recordsFiltered | int (not critical) | Total amount of records, matching the query, and search                       |
	 * | recordsTotal    | int (not critical) | Total amount of records, matching the query, before any search filter         |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | fieldList       | String[]           | Default ["_oid"], the collumns to return                                      |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | result          | Array[Obj/Array]   | Array of row records, each row is represented as an array                     |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 * | error           | String (Optional)  | Errors encounted if any                                                       |
	 * +-----------------+--------------------+-------------------------------------------------------------------------------+
	 **/
	protected ApiFunction info_list = (req, res) -> {
		// Arguments handling
		//-------------------------------------------------------------------------------
		
		// Get fieldList arguments
		String[] fieldList = req.getStringArray("fieldList", "['_oid']");
		if (fieldList.length <= 0) {
			res.put(ERROR, "fieldList, requested cannot be an empty array");
			res.halt();
		}
		
		// The query to use
		String query = req.getString("query", "").trim();
		Object[] queryArgs = req.getObjectArray("queryArgs", EmptyArray.STRING);
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
		String searchString = req.getString("searchString", "").trim();
		String[] searchFieldList = req.getStringArray("searchFieldList", fieldList);
		String searchMode = req.getString("searchMode", "prefix");
		
		// Start, Length, and ordering limiting of data
		int start = req.getInt("start", 0);
		int length = req.getInt("length", 50);
		String orderBy = req.getString("orderBy", "oID");
		
		// The result data row mode
		String rowMode = req.getString("rowMode", "object");

		// The AccountTable specific parameters
		//-------------------------------------------------------------------------------
		String[] insideGroupAny = req.getStringArray("insideGroupAny", null);
		String[] hasGroupRoleAny = req.getStringArray("hasGroupRoleAny", null);
		
		// Processing the query and search together, return result
		//-------------------------------------------------------------------------------
		return AccountInfoStaticApi.list(res, table, fieldList, query, queryArgs, searchString,
		searchFieldList, searchMode, insideGroupAny, hasGroupRoleAny, start, length, orderBy, rowMode);
	};
	
	/**
	 * # $prefix/info/datatables
	 * See: DataTableApi.list.datatables
	 **/
	protected ApiFunction info_list_datatables = (req, res) -> {
		// Adjusting arguments for compatibility
		DataTableApi.jsDatatableArgumentsCompatibility(req,res);

		// Does the default list processing
		res = info_list.apply(req, res);
		
		// Change some formatting of result
		res.put("data", res.get("result", null));
		// Clear original formatting
		res.put("result", null);
		
		// Return it
		return res;
	};
	
	/**
	 * Does the actual setup for the API
	 * Given the API Builder, and the namespace prefix
	 *
	 * @param  api ApiBuilder to add the required functions
	 * @param  prefixPath to assume
	 * @param  config configuration map
	 **/
	protected void apiSetup(ApiBuilder api, String prefixPath,
		GenericConvertMap<String, Object> config) {
		// Account info get, set, list
		api.put(prefixPath + "account/info/get", info_get);
		api.put(prefixPath + "account/info/set", info_set);
		api.put(prefixPath + "account/info/list", info_list);
		api.put(prefixPath + "account/info/list/datatables", info_list_datatables);
	}
}
