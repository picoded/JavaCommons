package picoded.servlet.api.module.account;

import picoded.servlet.api.*;
import picoded.servlet.api.module.*;
import picoded.servlet.api.module.dstack.*;
import picoded.dstack.module.account.*;
import picoded.core.struct.GenericConvertMap;
import static picoded.servlet.api.module.account.AccountConstantStrings.*;
import static picoded.servlet.api.module.ApiModuleConstantStrings.*;
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
		return new SystemSetupInterface[] {  };
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
		if( req.getString("_oid") == null ) {
			// Get the current user
			AccountObject currentUser = table.getRequestUser(req.getHttpServletRequest(), null);

			// If current user is null, halt and throw an error
			if( currentUser == null ) {
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
		if(ao != null){
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
	 * See: DataTableApi.list
	 **/
	protected ApiFunction info_list = (req, res) -> {
		return dataTableApi.list.apply(req, res);
	};

	/**
	 * # $prefix/info/datatables
	 * See: DataTableApi.list.datatables
	 **/
	protected ApiFunction info_list_datatables = (req, res) -> {
		return dataTableApi.datatables.apply(req, res);
	};

	/**
	 * Does the actual setup for the API
	 * Given the API Builder, and the namespace prefix
	 *
	 * @param  api ApiBuilder to add the required functions
	 * @param  prefixPath to assume
	 * @param  config configuration map
	 **/
	protected void apiSetup(ApiBuilder api, String prefixPath, GenericConvertMap<String,Object> config) {
		// Account info get, set, list
		api.put(prefixPath + "account/info/get", info_get);
		api.put(prefixPath + "account/info/set", info_set);
		api.put(prefixPath + "account/info/list", info_list);
		api.put(prefixPath + "account/info/list/datatables", info_list_datatables);
	}
}
