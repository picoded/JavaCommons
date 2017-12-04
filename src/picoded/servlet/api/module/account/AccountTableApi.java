package picoded.servlet.api.module.account;

import java.util.*;

import picoded.servlet.api.*;
import picoded.servlet.api.module.*;
import picoded.dstack.module.account.*;
import picoded.servlet.api.module.dstack.*;
import picoded.dstack.*;
import picoded.core.conv.ConvertJSON;
import picoded.core.conv.StringEscape;
import picoded.core.conv.GenericConvert;
import java.util.function.BiFunction;
import picoded.core.struct.GenericConvertMap;
import picoded.core.struct.GenericConvertHashMap;
import static picoded.servlet.api.module.account.AccountConstantStrings.*;
import static picoded.servlet.api.module.ApiModuleConstantStrings.*;
import picoded.core.common.SystemSetupInterface;

/**
 * Account table API builder
 **/
public class AccountTableApi extends CommonApiModule {
	
	/**
	 * The AccountTable reference
	 **/
	protected AccountTable table = null;
	
	/**
	 * Static ERROR MESSAGES
	 **/
	public static final String MISSING_REQUEST_PAGE = "Unexpected Exception: Missing requestPage()";
	
	public boolean isTesting = false;
	protected DataTableApi dataTableApi = null;
	protected AccountLoginApi accountLoginApi = null;
	
	/**
	 * Setup the account table api class
	 *
	 * @param  The input AccountTable to use
	 **/
	public AccountTableApi(AccountTable inTable) {
		table = inTable;
		isTesting = false;
		dataTableApi = new DataTableApi(inTable.accountDataTable());
		accountLoginApi = new AccountLoginApi(table);
	}
	
	protected SystemSetupInterface[] internalSubsystemArray() {
		return new SystemSetupInterface[] {};
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	//   Basic login, logout, and account creation
	//
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * # $prefix/new
	 *
	 * Creates a new account in the table
	 *
	 * ## HTTP Request Parameters
	 *
	 * +-----------------+-----------------------+----------------------------------------------------------------------------+
	 * | Parameter Name  | Variable Type         | Description                                                                |
	 * +-----------------+-----------------------+----------------------------------------------------------------------------+
	 * | data            | {Object} Map<S, O>    | Meta object that represents this account                                   |
	 * | password        | String                | Password of new account                                                    |
	 * | loginName       | String                | Username of new account                                                    |
	 * | isGroup         | boolean (optional)    | whether this is a group object (defaults to false)                         |
	 * +-----------------+-----------------------+----------------------------------------------------------------------------+
	 *
	 * ## JSON Object Output Parameters
	 *
	 * +-----------------+-----------------------+----------------------------------------------------------------------------+
	 * | Parameter Name  | Variable Type         | Description                                                                |
	 * +-----------------+-----------------------+----------------------------------------------------------------------------+
	 * | result          | String                | account ID used                                                            |
	 * +-----------------+-----------------------+----------------------------------------------------------------------------+
	 * | data            | {Object}              | DataObject representing this account                                       |
	 * | loginNameList   | String[]              | array of account names representing the user                               |
	 * +-----------------+-----------------------+----------------------------------------------------------------------------+
	 * | error           | String (Optional)     | Errors encountered if any                                                  |
	 * +-----------------+-----------------------+----------------------------------------------------------------------------+
	 **/
	public ApiFunction new_account = (req, res) -> {
		// Only runs function if logged in, and valid group object
		boolean isGroup = req.getBoolean(IS_GROUP, false);
		String[] paramsToCheck = new String[] { LOGINNAME };
		res = check_parameters(paramsToCheck, req, res);
		if (res.get(ERROR) != null) {
			return res;
		}
		String loginName = req.getString(LOGINNAME);
		// either loginName or loginNameList
		String[] loginNameList = req.getStringArray(LOGINNAMELIST, new String[] {});
		if (loginNameList.length > 0) {
			loginName = loginNameList[0];
		}
		String password = req.getString(PASSWORD);
		if (!isGroup && (password == null || password.isEmpty())) {
			res.put(ERROR, ERROR_NO_PASSWORD);
			return res;
		}
		Object metaObjRaw = req.get(DATA);
		Map<String, Object> givenMetaObj = new HashMap<String, Object>();
		if (metaObjRaw instanceof String) {
			String jsonMetaString = (String) metaObjRaw;
			if (jsonMetaString != null && !jsonMetaString.isEmpty()) {
				givenMetaObj = ConvertJSON.toMap(jsonMetaString);
			}
		}
		
		AccountObject newAccount = table.newEntry(loginName);
		// Create new account
		if (newAccount != null) {
			// Check is it group
			if (isGroup) {
				res.put(ERROR, "Creating group is currently unavailable.");
				res.put(INFO, "`isGroup` param is disabled.");
				return res;
			}
			// Attach all of the login names in loginNameList to account
			for (String name : loginNameList) {
				newAccount.setLoginName(name);
			}
			newAccount.setPassword(password);
			newAccount.putAll(givenMetaObj);
			newAccount.saveAll();
			
			res.put(DATA, newAccount);
			res.put(RESULT, newAccount._oid());
		} else {
			res.put(ERROR, ERROR_LOGIN_NAME_EXISTS);
		}
		return res;
	};
	
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
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	//   Other stuff (to review)
	//
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	// /**
	//  * # groupRoles
	//  *
	//  * Retrieves the existing roles available from an existing group
	//  *
	//  * ## HTTP Request Parameters
	//  *
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | Parameter Name  | Variable Type          | Description                                                                |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | groupID          | String                | ID of the group to retrieve                                                 |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  *
	//  * ## JSON Object Output Parameters
	//  *
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | Parameter Name  | Variable Type          | Description                                                                |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | list            | List                  | List of roles                                                               |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | ERROR           | String (Optional)     | Errors encountered if any                                                  |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  **/
	protected ApiFunction groupRoles = (req, res) -> {
		res.put(INFO, "group roles endpoint is currently unavailable");
		// Return result
		return res;
	};
	//
	// /**
	//  * # getMemberRoleFromGroup
	//  *
	//  * Retrieve the role of an existing user from an existing group
	//  *
	//  * ## HTTP Request Parameters
	//  *
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | Parameter Name  | Variable Type          | Description                                                                |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | userID          | String                | ID of the user to retrieve                                                 |
	//  * | groupID          | String                | ID of the group to retrieve from                                           |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  *
	//  * ## JSON Object Output Parameters
	//  *
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | Parameter Name  | Variable Type          | Description                                                                |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | single          | String                | Role of the user                                                           |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | ERROR           | String (Optional)     | Errors encountered if any                                                  |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  **/
	protected ApiFunction getMemberRoleFromGroup = (req, res) -> {
		res.put(INFO, "get member role endpoint is currently unavailable");
		// Return result
		return res;
	};
	//
	// /**
	//  * # add_new_membership_role
	//  *
	//  * Adding a new role to an existing group/user
	//  *
	//  * ## HTTP Request Parameters
	//  *
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | Parameter Name  | Variable Type          | Description                                                                |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | role            | String                | name of the role to add                                                     |
	//  * | groupID          | String                | ID of the group to add to                                                   |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  *
	//  * ## JSON Object Output Parameters
	//  *
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | Parameter Name  | Variable Type          | Description                                                                |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | meta            | {Object}              | Object containing the list of roles of the group                           |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | ERROR           | String (Optional)     | Errors encountered if any                                                  |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  **/
	protected ApiFunction add_new_membership_role = (req, res) -> {
		res.put(INFO, "add new member role endpoint is currently unavailable");
		// Return result
		return res;
	};
	//
	// /**
	//  * # remove_membership_role
	//  *
	//  * Remove an existing role from an existing group/user
	//  *
	//  * ## HTTP Request Parameters
	//  *
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | Parameter Name  | Variable Type          | Description                                                                |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | role            | String                | name of the role to remove                                                 |
	//  * | groupID        | String                | name of the group to remove from                                           |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  *
	//  * ## JSON Object Output Parameters
	//  *
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | Parameter Name  | Variable Type          | Description                                                                |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | meta            | {Object}              | Object containing the list of roles of the group                           |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | ERROR           | String (Optional)     | Errors encountered if any                                                  |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  **/
	protected ApiFunction remove_membership_role = (req, res) -> {
		res.put(INFO, "remove member role endpoint is currently unavailable");
		// Return result
		return res;
	};
	//
	// /**
	//  * # get_member_list_info
	//  *
	//  * Retrieve the list of members' OBJECT from an existing group
	//  *
	//  * ## HTTP Request Parameters
	//  *
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | Parameter Name  | Variable Type          | Description                                                                |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | draw            | int (optional)         | Draw counter echoed back, and used by the datatables.js server-side API    |
	//  * | start           | int (optional)         | Default 0: Record start listing, 0-indexed                                 |
	//  * | length          | int (optional)         | Default max: The number of records to return                               |
	//  * | headers          | String [](optional)    | The columns headers returned                                                |
	//  * | groupID          | String (optional)      | ID of the group/current user to retrieve from                               |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  *
	//  * ## JSON Object Output Parameters
	//  *
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | Parameter Name  | Variable Type          | Description                                                                |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | data            | array                  | Rows containing the members' OBJECT of the group                           |
	//  * | draw            | int (optional)         | Draw counter echoed back, and used by the datatables.js server-side API       | not returned
	//  * | recordsTotal    | int                    | Total amount of records. Before any search filter (But after base filters)    | not returned
	//  * | recordsFilterd  | int                    | Total amount of records. After all search filter                              | not returned
	//  * | headers         | String[](optional)     | The collumns headers returned                                                 |
	//  * | groupID         | String             | group ID used in the request                                                  |
	//  * | groupID_exist   | boolean            | indicates if the account ID exists in the system                              |
	//  * | groupID_valid   | boolean            | indicates if the account ID exists and is a group                             |
	//  * | groupID_admin   | boolean            | indicates if the session has admin rights over the group                      |
	//  * | groupID_names   | String[]           | the group various names, if ID is valid                                       | sanitise
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | ERROR           | String (Optional)     | Errors encountered if any                                                  |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  **/
	protected ApiFunction get_member_list_info = (req, res) -> {
		res.put(INFO, "get member list endpoint is currently unavailable");
		// Return result
		return res;
	};
	//
	// /**
	//  * # add_remove_member
	//  *
	//  * Add/Remove an existing user (with role) to/from an existing group
	//  *
	//  * ## HTTP Request Parameters
	//  *
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | Parameter Name  | Variable Type          | Description                                                                |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | addList          | String []              | List of ID of the users to add                                             |
	//  * | removeList      | String []              | List of ID of the users to remove                                           |
	//  * | groupID          | String []              | ID of the group to add/remove to/from                                       |
	//  * | role            | String                | name of the role assigned for the users                                     |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  *
	//  * ## JSON Object Output Parameters
	//  *
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | Parameter Name  | Variable Type          | Description                                                                |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | meta            | {Object}              | Information of the newly added user                                         |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | ERROR           | String (Optional)     | Errors encountered if any                                                  |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  **/
	protected ApiFunction add_remove_member = (req, res) -> {
		res.put(INFO, "add/remove member endpoint is currently unavailable");
		// Return result
		return res;
	};
	//
	// /**
	//  * # get_single_member_meta
	//  *
	//  * Retrieve the meta information of an existing/current user from an existing group
	//  *
	//  * ## HTTP Request Parameters
	//  *
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | Parameter Name  | Variable Type          | Description                                                                |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | userID          | String                | ID of the user to retrieve                                                 |
	//  * | groupID          | String                | ID of the group to retrieve from                                           |
	//  * | role            | String (Optional)      | name of the role assigned to the user                                       |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  *
	//  * ## JSON Object Output Parameters
	//  *
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | Parameter Name  | Variable Type          | Description                                                                |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | meta            | {Object}              | Meta information of the user                                               |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | ERROR           | String (Optional)     | Errors encountered if any                                                  |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  **/
	protected ApiFunction get_single_member_meta = (req, res) -> {
		res.put(INFO, "get member meta endpoint is currently unavailable");
		// Return result
		return res;
	};
	//
	// /**
	//  * # update_member_meta_info
	//  *
	//  * Update the member meta information of an existing/current user from an existing group
	//  *
	//  * ## HTTP Request Parameters
	//  *
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | Parameter Name  | Variable Type          | Description                                                                |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | userID          | String 				    | ID of the user/current user to retrieve                                     |
	//  * | groupID          | String                | ID of the group to retrieve from                                           |
	//  * | updateMode      | String                | Mode of the update used, full or delta (default: delta)                     |
	//  * | data            | {Object}              | name of the role assigned to the user                                       |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  *
	//  * ## JSON Object Output Parameters
	//  *
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | Parameter Name  | Variable Type          | Description                                                                |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | meta            | {Object}              | Meta information of the user                                               |
	//  * | accountID        | String                | ID of the user                                                             |
	//  * | updateMode      | String                | Mode of the update used                                                     |
	//  * | success          | boolean                | false for failed update and true for success                               |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | ERROR           | String (Optional)     | Errors encountered if any                                                  |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  **/
	protected ApiFunction update_member_meta_info = (req, res) -> {
		res.put(INFO, "update member meta endpoint is currently unavailable");
		// Return result
		return res;
	};
	//
	/**
	 * # account/changePassword
	 *
	 * Resets the password of the user/current member
	 *
	 * ## HTTP Request Parameters
	 *
	 * +-----------------+-----------------------+----------------------------------------------------------------------------+
	 * | Parameter Name  | Variable Type         | Description                                                                |
	 * +-----------------+-----------------------+----------------------------------------------------------------------------+
	 * | accountID       | String  (Optional)    | ID of the user/current user to retrieve                                    |
	 * | oldPassword     | String                | Old password of the user                                                   |
	 * | newPassword     | String                | New password of the user                                                   |
	 * +-----------------+-----------------------+----------------------------------------------------------------------------+
	 *
	 * ## JSON Object Output Parameters
	 *
	 * +-----------------+-----------------------+----------------------------------------------------------------------------+
	 * | Parameter Name  | Variable Type         | Description                                                                |
	 * +-----------------+-----------------------+----------------------------------------------------------------------------+
	 * | accountID       | String                | ID of the user                                                             |
	 * | result          | boolean               | false for failed change and true for success                               |
	 * +-----------------+-----------------------+----------------------------------------------------------------------------+
	 * | ERROR           | String (Optional)     | Errors encountered if any                                                  |
	 * +-----------------+-----------------------+----------------------------------------------------------------------------+
	 **/
	protected ApiFunction changePassword = (req, res) -> {
		
		// Get accountID to update
		String accountID = req.getString(ACCOUNT_ID, "");
		
		// Get the current user ID
		AccountObject cu = table.getRequestUser(req.getHttpServletRequest(), null);
		AccountObject ao = table.get(accountID);
		
		// Validate that account object is given
		if (ao == null) {
			res.put(ERROR, ERROR_NO_USER);
			return res;
		}
		
		// Validate if the required params exists
		String[] paramsToCheck = new String[] { NEW_PASSWORD };
		res = check_parameters(paramsToCheck, req, res);
		if (res.get(ERROR) != null) {
			return res;
		}
		
		// Get the old, and new password
		String oldPassword = req.getString(OLD_PASSWORD);
		String newPassword = req.getString(NEW_PASSWORD);
		
		// @TODO:If current account is super user,
		// and is not modifying himself
		
		// Ensure from here user logged in and changing password is the same
		if (ao._oid().equals(cu._oid()) == false) {
			res.put(ERROR, "Change password, requires the respective user login permission");
			return res;
		}
		
		// Else require a proper setPassword with validation
		if (!ao.setPassword(newPassword, oldPassword)) {
			res.put(ERROR, ERROR_PASS_INCORRECT);
			return res;
		}
		
		// Return successful password reset
		res.put(RESULT, true);
		res.put(ACCOUNT_ID, ao._oid());
		
		// return result
		return res;
	};
	
	/**
	 * # delete_user_account
	 *
	 * Delete an existing/current user
	 *
	 * ## HTTP Request Parameters
	 *
	 * +-----------------+-----------------------+----------------------------------------------------------------------------+
	 * | Parameter Name  | Variable Type          | Description                                                                |
	 * +-----------------+-----------------------+----------------------------------------------------------------------------+
	 * | oid          | String  			    | ID of the user/current user to retrieve                                    |
	 * +-----------------+-----------------------+----------------------------------------------------------------------------+
	 *
	 * ## JSON Object Output Parameters
	 *
	 * +-----------------+-----------------------+----------------------------------------------------------------------------+
	 * | Parameter Name  | Variable Type          | Description                                                                |
	 * +-----------------+-----------------------+----------------------------------------------------------------------------+
	 * | result          | boolean                | false for failed change and true for success                               |
	 * +-----------------+-----------------------+----------------------------------------------------------------------------+
	 * | ERROR           | String (Optional)     | Errors encountered if any                                                  |
	 * +-----------------+-----------------------+----------------------------------------------------------------------------+
	 **/
	public ApiFunction delete_user_account = (req, res) -> {
		String userID = req.getString(ACCOUNT_ID, "");
		if (userID.isEmpty()) {
			res.put(RESULT, false);
			res.put(INFO, "No oid is supplied.");
			return res;
		}
		AccountObject ao = (!userID.isEmpty()) ? table.get(userID) : table.getRequestUser(
			req.getHttpServletRequest(), null);
		if (ao == null) {
			res.put(ERROR, ERROR_NO_USER);
			return res;
		}
		table.remove(ao);
		res.put(RESULT, true);
		res.put(OID, userID);
		return res;
	};
	
	//
	// /**
	//  * # getListOfGroupIDOfMember
	//  *
	//  * Retrieve a list of groups ID from an existing member/current user
	//  *
	//  * ## HTTP Request Parameters
	//  *
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | Parameter Name  | Variable Type          | Description                                                                |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | username        | String  (Either or)    | name of the user to retrieve from                                           |
	//  * | oid              | String                | oid of the user to retrieve from                                           |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  *
	//  * ## JSON Object Output Parameters
	//  *
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | Parameter Name  | Variable Type          | Description                                                                |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | list            | List<String>          | List containing the groups' ID of the member                               |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | ERROR           | String (Optional)     | Errors encountered if any                                                  |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  **/
	protected ApiFunction getListOfGroupIDOfMember = (req, res) -> {
		res.put(INFO, "get list of group id endpoint is currently unavailable");
		// Return result
		return res;
	};
	//
	// /**
	//  * # getListOfGroupObjectOfMember
	//  *
	//  * Retrieve a list of groups OBJECT from an existing member/current user
	//  *
	//  * ## HTTP Request Parameters
	//  *
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | Parameter Name  | Variable Type         | Description                                                                |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | userID          | String  (Either or)   | ID of the user to retrieve from                                            |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  *
	//  * ## JSON Object Output Parameters
	//  *
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | Parameter Name  | Variable Type         | Description                                                                |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | list            | List<String>          | List containing the groups' OBJECT of the member                           |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | ERROR           | String (Optional)     | Errors encountered if any                                                  |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  **/
	protected ApiFunction getListOfGroupObjectOfMember = (req, res) -> {
		res.put(INFO, "get list of group object endpoint is currently unavailable");
		// Return result
		return res;
	};
	
	/**
	 * # set_login_name
	 *
	 * Set the login name of the user
	 *
	 * ## HTTP Request Parameters
	 *
	 * +-----------------+-----------------------+----------------------------------------------------------------------------+
	 * | Parameter Name  | Variable Type         | Description                                                                |
	 * +-----------------+-----------------------+----------------------------------------------------------------------------+
	 * | loginName       | String                | The login name to change to                                                |
	 * | accountID       | String  (Optional)    | ID of the user/current user to retrieve                                    |
	 * +-----------------+-----------------------+----------------------------------------------------------------------------+
	 *
	 * ## JSON Object Output Parameters
	 *
	 * +-----------------+-----------------------+----------------------------------------------------------------------------+
	 * | Parameter Name  | Variable Type         | Description                                                                |
	 * +-----------------+-----------------------+----------------------------------------------------------------------------+
	 * | accountID       | String                | The account ID of the user                                                 |
	 * | result          | boolean               | The state of change of login name of the user                              |
	 * | loginName       | String                | The login name that was change to                                          |
	 * +-----------------+-----------------------+----------------------------------------------------------------------------+
	 * | ERROR           | String (Optional)     | Errors encountered if any                                                  |
	 * +-----------------+-----------------------+----------------------------------------------------------------------------+
	 **/
	protected ApiFunction set_login_name = (req, res) -> {
		// Either the current user or the super user should be using this endpoint
		String[] paramsToCheck = new String[] { LOGINNAME };
		res = check_parameters(paramsToCheck, req, res);
		if (res.get(ERROR) != null) {
			return res;
		}
		// loginName to set
		String loginName = req.getString(LOGINNAME, "");
		// Check if loginName has been used
		AccountObject ao = table.getFromLoginName(loginName);
		if (ao != null) {
			res.put(ERROR, ERROR_LOGIN_NAME_EXISTS);
			return res;
		}
		// accountID to change if there is
		String accountID = req.getString(ACCOUNT_ID, "");
		ao = (!accountID.isEmpty()) ? table.get(accountID) : table.getRequestUser(
			req.getHttpServletRequest(), null);
		if (ao == null) {
			res.put(ERROR, ERROR_NO_USER);
			return res;
		}
		// Set it as a unique name
		ao.setUniqueLoginName(loginName);
		// Set and send back response
		res.put(RESULT, true);
		res.put(ACCOUNT_ID, ao._oid());
		res.put(LOGINNAME, loginName);
		return res;
	};
	
	protected void apiSetup(ApiBuilder api, String prefixPath,
		GenericConvertMap<String, Object> config) {
		apiSetup(api, prefixPath);
		accountLoginApi.apiSetup(api, prefixPath, config);
	}
	
	/**
	 * Does the actual setup for the API
	 * Given the API Builder, and the namespace prefix
	 *
	 * @param  API builder to add the required functions
	 * @param  Path to assume
	 **/
	public void apiSetup(ApiBuilder builder, String path) {
		
		accountLoginApi.apiSetup(builder, path, null);
		// Basic new account, login, and logout
		// builder.put(path + API_ACCOUNT_LOGIN, login); // Tested
		// builder.put(path + API_ACCOUNT_LOGOUT, logout); // Tested
		builder.put(path + API_ACCOUNT_NEW, new_account); // Tested
		builder.put(path + API_ACCOUNT_SET_LOGIN_NAME, set_login_name);
		// Account info get, set, list
		builder.put(path + "account/info/get", info_get);
		builder.put(path + "account/info/set", info_set);
		builder.put(path + "account/info/list", info_list);
		builder.put(path + "account/info/list/datatables", info_list_datatables);
		
		// builder.put(path + API_ACCOUNT_LOCKTIME, lockTime); // Tested
		builder.put(path + "account/changePassword", changePassword); // Tested
		
		// builder.put(path + API_ACCOUNT_INFO, account_info); // Tested
		// builder.put(path + API_ACCOUNT_INFO_ID, account_info_by_ID); // Tested
		builder.put(path + "account/admin/remove", delete_user_account); // Tested
		builder.put(path + API_ACCOUNT_LIST, dataTableApi.list);
		//
		// //Group functionalities
		builder.put(path + API_GROUP_GRP_ROLES, groupRoles); // Tested
		builder.put(path + API_GROUP_GET_MEM_ROLE, getMemberRoleFromGroup); // Tested
		builder.put(path + API_GROUP_GET_LIST_GRP_ID_MEM, getListOfGroupIDOfMember); // Tested
		builder.put(path + API_GROUP_GET_LIST_GRP_OBJ_MEM, getListOfGroupObjectOfMember); // Tested
		builder.put(path + API_GROUP_GET_SINGLE_MEM_META, get_single_member_meta); // Tested
		builder.put(path + API_GROUP_UPDATE_MEM_META, update_member_meta_info); // Tested
		//
		builder.put(path + API_GROUP_ADMIN_ADD_MEM_ROLE, add_new_membership_role); // Tested
		builder.put(path + API_GROUP_ADMIN_REM_MEM_ROLE, remove_membership_role); // Tested
		builder.put(path + API_GROUP_ADMIN_GET_MEM_LIST_INFO, get_member_list_info); // Tested
		builder.put(path + API_GROUP_ADMIN_ADD_REM_MEM, add_remove_member); // Tested
	}
	
	// Private Methods
	
	private static Map<String, Object> extractCommonInfoFromAccountObject(AccountObject account,
		boolean sanitiseOutput) {
		//sanitise accountNames, groupNames,
		Map<String, Object> commonInfo = new HashMap<String, Object>();
		sanitiseOutput = true; // let it always sanitise the output
		commonInfo.put(ACCOUNT_ID, null);
		commonInfo.put("accountNames", null);
		commonInfo.put("isSuperUser", null);
		commonInfo.put("isGroup", null);
		commonInfo.put("groups", null);
		commonInfo.put("isAnyGroupAdmin", false);
		
		if (account != null) {
			commonInfo.put(ACCOUNT_ID, account._oid());
			Set<String> accNameSet = account.getLoginNameSet();
			if (accNameSet != null) {
				String[] accNames = new String[accNameSet.size()];
				accNameSet.toArray(accNames);
				if (sanitiseOutput && accNames != null) {
					for (int i = 0; i < accNames.length; ++i) {
						accNames[i] = StringEscape.commonHtmlEscapeCharacters(accNames[i]);
					}
				}
				commonInfo.put("accountNames", accNames);
			}
			// Extract hostURL from user account object
			commonInfo.put(PROPERTIES_HOST_URL, account.get(PROPERTIES_HOST_URL));
			// Extract email from user account object
			commonInfo.put(PROPERTIES_EMAIL, account.get(PROPERTIES_EMAIL));
			// Extract name from user account object
			commonInfo.put(PROPERTIES_NAME, account.get(PROPERTIES_NAME));
		} else {
			System.out
				.println("AccountLogin -> extractCommonInfoFromAccountObject -> AccountObject is null!");
		}
		return commonInfo;
	}
	
	private static String generateQueryStringForSearchValue(String inSearchString,
		String[] queryColumns) {
		if (inSearchString != null && queryColumns != null) {
			String[] searchStringSplit = inSearchString.trim().split("\\s+");
			StringBuilder querySB = new StringBuilder();
			
			for (int i = 0; i < searchStringSplit.length; ++i) {
				querySB.append("(");
				for (int queryCol = 0; queryCol < queryColumns.length; ++queryCol) {
					querySB.append(queryColumns[queryCol] + " LIKE ?");
					
					if (queryCol < queryColumns.length - 1) {
						querySB.append(" OR ");
					}
				}
				querySB.append(")");
				
				if (i < searchStringSplit.length - 1) {
					querySB.append(" AND ");
				}
			}
			return querySB.toString();
		}
		
		return "";
	}
	
	private static List<String> generateQueryStringArgsForSearchValue_andAddToList(
		String inSearchString, String[] queryColumns, String wildcardMode, List<String> ret) {
		if (inSearchString != null && queryColumns != null) {
			String[] searchStringSplit = inSearchString.trim().split("\\s+");
			StringBuilder querySB = new StringBuilder();
			
			String wildMode = wildcardMode;
			for (String searchString : searchStringSplit) {
				for (String queryColumn : queryColumns) {
					ret.add(getStringWithWildcardMode(searchString, wildMode));
				}
				wildMode = "both"; //Second string onwards is both side wildcard
			}
		}
		
		return ret;
	}
	
	private static String list_GET_and_POST_inner(AccountTable _DataTableObj, int draw, int start,
		int length, String[] headers, String query, String[] queryArgs, String orderBy,
		String[] insideGroup_any, String[] hasGroupRole_any, String groupStatus,
		boolean sanitiseOutput, boolean asObject) throws RuntimeException {
		
		List<Object> ret = new ArrayList<Object>();
		
		if (_DataTableObj == null) {
			return ConvertJSON.fromObject(ret);
		}
		
		try {
			if (headers != null && headers.length > 0) {
				DataObject[] metaObjs = null;
				AccountObject[] fullUserArray = null;
				
				if ((insideGroup_any == null || insideGroup_any.length == 0)
					&& (hasGroupRole_any == null || hasGroupRole_any.length == 0)) {
					//do normal query
					DataTable accountDataTable = _DataTableObj.accountDataTable();
					
					if (accountDataTable == null) {
						return ConvertJSON.fromObject(ret);
					}
					
					if (query == null || query.isEmpty() || queryArgs == null || queryArgs.length == 0) {
						metaObjs = accountDataTable.query(null, null, orderBy, start, length);
					} else {
						metaObjs = accountDataTable.query(query, queryArgs, orderBy, start, length);
					}
					
					List<AccountObject> retUsers = new ArrayList<AccountObject>();
					for (DataObject metaObj : metaObjs) {
						AccountObject ao = _DataTableObj.get(metaObj._oid()); //a single account
						// System.out.println(ao._oid()+" ahwejakwekawej<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
						retUsers.add(ao);
					}
					
					fullUserArray = retUsers.toArray(new AccountObject[retUsers.size()]);
				}
				
				if (fullUserArray == null || fullUserArray.length == 0) {
					return ConvertJSON.fromObject(ret);
				}
				
				for (AccountObject ao : fullUserArray) {
					List<Object> rowAsList = new ArrayList<Object>();
					Map<String, Object> rowAsObject = new HashMap<String, Object>();
					for (String header : headers) {
						
						if (header.equalsIgnoreCase("names")) {
							if (ao != null) {
								Set<String> aoNames = ao.getLoginNameSet();
								
								if (sanitiseOutput) {
									aoNames.clear();
									for (String name : ao.getLoginNameSet()) {
										aoNames.add(StringEscape.commonHtmlEscapeCharacters(name));
									}
								}
								
								if (aoNames != null) {
									List<String> aoNameList = new ArrayList<String>(aoNames);
									if (asObject)
										rowAsObject.put(header, aoNameList);
									else
										rowAsList.add(aoNameList);
								}
							}
						} else {
							Object rawVal = ao.get(header); //this used to be metaObj.get
							if (header.equalsIgnoreCase("_oid"))
								header = "id";
							if (sanitiseOutput && rawVal instanceof String) {
								String stringVal = GenericConvert.toString(rawVal);
								if (asObject)
									rowAsObject.put(header, stringVal);
								else
									rowAsList.add(stringVal);
							} else {
								if (asObject)
									rowAsObject.put(header, rawVal);
								else
									rowAsList.add(rawVal);
							}
							
						}
					}
					if (asObject)
						ret.add(rowAsObject);
					else
						ret.add(rowAsList);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("list_GET_and_POST_inner() ", e);
		}
		
		return ConvertJSON.fromObject(ret);
	}
	
	private static String getStringWithWildcardMode(String searchString, String wildcardMode) {
		if (wildcardMode.equalsIgnoreCase("prefix")) {
			return "%" + searchString;
		} else if (wildcardMode.equalsIgnoreCase("suffix")) {
			return searchString + "%";
		} else {
			return "%" + searchString + "%";
		}
	}
	
	public static ApiResponse check_parameters(String[] listToCheck, ApiRequest req, ApiResponse res) {
		for (String paramName : listToCheck) {
			String value = "";
			switch (paramName) {
			case ACCOUNT_ID:
				value = req.getString(paramName, "");
				if (value.isEmpty())
					res.put(ERROR, ERROR_NO_USER_ID);
				break;
			case LOGINNAME:
				value = req.getString(paramName, "");
				if (value.isEmpty())
					res.put(ERROR, ERROR_NO_LOGINNAME);
				break;
			case PASSWORD:
				value = req.getString(paramName, "");
				if (value.isEmpty())
					res.put(ERROR, ERROR_NO_PASSWORD);
				break;
			case GROUP_ID:
				value = req.getString(paramName, "");
				if (value.isEmpty())
					res.put(ERROR, ERROR_NO_GROUP_ID);
				break;
			case ROLE:
				value = req.getString(paramName, "");
				if (value.isEmpty())
					res.put(ERROR, ERROR_NO_ROLE);
				break;
			case GROUPNAME:
				value = req.getString(paramName, "");
				if (value.isEmpty())
					res.put(ERROR, ERROR_NO_GROUPNAME);
				break;
			case DATA:
				Object metaObj = req.get(paramName);
				if (metaObj == null)
					res.put(ERROR, ERROR_NO_META);
				break;
			case OLD_PASSWORD:
				value = req.getString(paramName, "");
				if (value.isEmpty())
					res.put(ERROR, ERROR_NO_PASSWORD);
				break;
			case NEW_PASSWORD:
				value = req.getString(paramName, "");
				if (value.isEmpty())
					res.put(ERROR, ERROR_NO_NEW_PASSWORD);
				break;
			case REPEAT_PASSWORD:
				value = req.getString(paramName, "");
				if (value.isEmpty())
					res.put(ERROR, ERROR_NO_NEW_REPEAT_PASSWORD);
				break;
			case AUTH_KEY: // No authKey means unable to use this API endpoint
				value = req.getString(paramName, "");
				if (value.isEmpty() || !value.equals("thisawesomestring"))
					res.put(ERROR, ERROR_NO_PRIVILEGES);
				break;
			case EMAIL:
				value = req.getString(paramName, "");
				if (value.isEmpty())
					res.put(ERROR, ERROR_NO_EMAIL);
				break;
			case NODE_ID:
				value = req.getString(paramName, "");
				if (value.isEmpty())
					res.put(ERROR, ERROR_NO_NODE_ID);
				break;
			case NAME:
				value = req.getString(paramName, "");
				if (value.isEmpty())
					res.put(ERROR, ERROR_NO_NAME);
				break;
			}
			if (res.get(ERROR) != null) {
				break;
			}
		}
		return res;
	}
}
