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

	/**
	 * Setup the account table api class
	 *
	 * @param  The input AccountTable to use
	 **/
	public AccountTableApi(AccountTable inTable) {
		table = inTable;
		isTesting = false;
		dataTableApi = new DataTableApi(inTable.accountDataTable());
	}

	public AccountTableApi(AccountTable inTable, boolean setTesting) {
		table = inTable;
		isTesting = setTesting;
		dataTableApi = new DataTableApi(inTable.accountDataTable());
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
	 * # $prefix/login
	 *
	 * Login a user
	 *
	 * ## Request Parameters
	 *
	 * +----------------+--------------------+-------------------------------------------------------------------------------+
	 * | Parameter Name | Variable Type      | Description                                                                   |
	 * +----------------+--------------------+-------------------------------------------------------------------------------+
	 * | accountID      | String (Optional)  | Either the loginID or the accountID is needed                                 |
	 * | loginName      | String (Optional)  | Either the loginID or the accountID is needed                                 |
	 * | password       | String             | The account password used for login                                           |
	 * | rememberMe     | boolean            | indicator if the session is persistent (remember me)                          |
	 * +----------------+--------------------+-------------------------------------------------------------------------------+
	 *
	 * ## JSON Object Output Parameters
	 *
	 * +----------------+--------------------+-------------------------------------------------------------------------------+
	 * | Parameter Name | Variable Type      | Description                                                                   |
	 * +----------------+--------------------+-------------------------------------------------------------------------------+
	 * | result         | boolean            | indicator if the session is logged in or not                                  |
	 * | isSuperUser    | boolean            | indicator if the user is a superuser or not                                   |
	 * | accountID      | String             | account id of the session                                                     |
	 * | loginNameList  | String[]           | array of account names representing the session                               |
	 * | rememberMe     | boolean            | indicator if the session is persistent (remember me)                          |
	 * +----------------+--------------------+-------------------------------------------------------------------------------+
	 * | error          | String (Optional)  | Errors encountered if any                                                     |
	 * +----------------+--------------------+-------------------------------------------------------------------------------+
	 **/
	protected ApiFunction login = (req, res) -> {

		// Setup default (failed) login
		res.put(ACCOUNT_ID, null);
		res.put(LOGIN_NAME_LIST, null);
		res.put(RESULT, false);
		res.put(REMEMBER_ME, false);

		// Get the login parameters
		String accountID = req.getString(ACCOUNT_ID, null);
		String loginID = req.getString(LOGINNAME, null);
		String loginPass = req.getString(PASSWORD, null);
		boolean rememberMe = req.getBoolean(REMEMBER_ME, false);

		// Request to get info of user
		if (accountID == null && loginID == null) {
			// Get current user if any, http response is given to allow any update of the cookies if needed
			AccountObject currentUser = table.getRequestUser(req.getHttpServletRequest(), res.getHttpServletResponse());
			if (currentUser == null) {
				res.put(ERROR, ERROR_NO_USER);
				return res;
			}
			res.put(RESULT, true);
			res.put(REMEMBER_ME, rememberMe);
			Map<String, Object> commonInfo = extractCommonInfoFromAccountObject(currentUser, true);
			res.putAll(commonInfo);
			return res;
		}

		// Log in Process
		// Missing parameter error checks
		if (loginPass == null) {
			res.put(ERROR, ERROR_NO_LOGIN_PASSWORD);
			return res;
		}

		// Fetch the respective account object
		AccountObject ao = null;
		if (accountID != null) {
			ao = table.get(accountID);
		} else if (loginID != null) {
			ao = table.getFromLoginName(loginID);
		}

		// Check if account has been locked out
		if (ao != null) {
			int timeAllowed = ao.getNextLoginTimeAllowed(ao._oid());
			if (timeAllowed != 0) {
				res.put(ERROR, "Unable to login, user locked out for " + timeAllowed + " seconds.");
				return res;
			}
		}

		// Continue only with an account object and does not have a lockout timing
		if (ao != null && ao.validatePassword(loginPass)) {
			// Validate and login, with password
			ao = table.loginAccount(req.getHttpServletRequest(), res.getHttpServletResponse(), ao,
				loginPass, rememberMe);
			// Reset any failed login attempts
			ao.resetLoginThrottle(loginID);
			// If ao is not null, it assumes a valid login
			res.put(RESULT, true);
			res.put(REMEMBER_ME, rememberMe);
			res.put(ACCOUNT_ID, ao._oid());
			// Extract Common Info from user account object
			Map<String, Object> commonInfo = extractCommonInfoFromAccountObject(ao, true);
			res.putAll(commonInfo);

			// loginID, as a list - as set does not gurantee sorting, a sort is done for the list for alphanumeric
			List<String> loginIDList = new ArrayList<String>(ao.getLoginNameSet());
			Collections.sort(loginIDList);

			// Return the loginIDList
			res.put(LOGIN_NAME_LIST, loginIDList);
		} else {
			// Legitimate user but wrong password
			if (ao != null) {
				ao.addDelay(ao);
			}
			res.put(ERROR, ERROR_FAIL_LOGIN);
		}

		return res;
	};

	/**
	 * # $prefix/logout
	 *
	 * The logout GET function, used to logout the current browser session
	 *
	 * ## HTTP Request Parameters
	 *
	 * +----------------+--------------------+-------------------------------------------------------------------------------+
	 * | Parameter Name | Variable Type      | Description                                                                   |
	 * +----------------+--------------------+-------------------------------------------------------------------------------+
	 * | No parameters options                                                                                               |
	 * +----------------+--------------------+-------------------------------------------------------------------------------+
	 *
	 * ## JSON Object Output Parameters
	 *
	 * +----------------+--------------------+-------------------------------------------------------------------------------+
	 * | Parameter Name | Variable Type      | Description                                                                   |
	 * +----------------+--------------------+-------------------------------------------------------------------------------+
	 * | result         | boolean            | indicator if logout is successful or not                                      |
	 * +----------------+--------------------+-------------------------------------------------------------------------------+
	 * | error          | String (Optional)  | Errors encountered if any                                                     |
	 * +----------------+--------------------+-------------------------------------------------------------------------------+
	 **/
	protected ApiFunction logout = (req, res) -> {
		res.put(RESULT, false);

		if (req.getHttpServletRequest() != null) {
			res.put(RESULT,
				table.logoutAccount(req.getHttpServletRequest(), res.getHttpServletResponse()));
		} else {
			res.put(ERROR, MISSING_REQUEST_PAGE);
		}
		return res;
	};

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
		if (res.get(ERROR) != null){
			return res;
		}
		String loginName = req.getString(LOGINNAME);
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
				newAccount.setGroupStatus(true);
				boolean defaultRoles = req.getBoolean(DEFAULT_ROLES, true);
				List<String> list = req.getList(ROLE, null);
				// Set roles
				if (defaultRoles || list == null) {
					newAccount.setMembershipRoles(table.defaultMembershipRoles());
				} else {
					newAccount.setMembershipRoles(list);
				}
				// Add the user who create the group as the admin
				AccountObject firstAdmin = table.getRequestUser(req.getHttpServletRequest(), null);
				if (firstAdmin != null) // Set the creator as the admin
					newAccount.setMember(firstAdmin, "admin");
			}

			newAccount.setPassword(password);
			newAccount.putAll(givenMetaObj);
			newAccount.saveAll();

			res.put(DATA, newAccount);
			res.put(ACCOUNT_ID, newAccount._oid());
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
	// protected ApiFunction groupRoles = (req, res) -> {
	// 	String[] paramsToCheck = new String[] { GROUP_ID };
	// 	res = check_parameters(paramsToCheck, req, res);
	// 	if (res.get(ERROR) != null)
	// 		return res;
	// 	String groupID = req.getString(GROUP_ID);
	//
	// 	AccountObject group = table.get(groupID);
	// 	if (group == null) {
	// 		res.put(ERROR, ERROR_NO_GROUP);
	// 		return res;
	// 	}
	// 	List<String> membershipRoles = group.group_membershipRoles().getList(
	// 		PROPERTIES_MEMBERSHIP_ROLE, "[]");
	// 	res.put(LIST, membershipRoles);
	// 	// Return result
	// 	return res;
	// };
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
	// protected ApiFunction getMemberRoleFromGroup = (req, res) -> {
	// 	String[] paramsToCheck = new String[] { GROUP_ID, USER_ID };
	// 	res = check_parameters(paramsToCheck, req, res);
	// 	if (res.get(ERROR) != null)
	// 		return res;
	// 	String groupID = req.getString(GROUP_ID);
	// 	String userID = req.getString(USER_ID);
	//
	// 	AccountObject group = table.get(groupID);
	// 	if (group == null) {
	// 		res.put(ERROR, ERROR_NO_GROUP);
	// 		return res;
	// 	}
	// 	AccountObject userToAdd = table.get(userID);
	// 	if (userToAdd == null) {
	// 		res.put(ERROR, ERROR_NO_USER);
	// 		return res;
	// 	}
	// 	String role = group.getMemberRole(userToAdd);
	// 	if (role == null) {
	// 		res.put(ERROR, "No role for user is found.");
	// 	} else {
	// 		res.put(SINGLE_RETURN_VALUE, role);
	// 	}
	// 	return res;
	// };
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
	// protected ApiFunction add_new_membership_role = (req, res) -> {
	// 	String[] paramsToCheck = new String[] { GROUP_ID, ROLE };
	// 	res = check_parameters(paramsToCheck, req, res);
	// 	if (res.get(ERROR) != null)
	// 		return res;
	// 	String groupID = req.getString(GROUP_ID);
	// 	String role = req.getString(ROLE);
	//
	// 	AccountObject group = table.get(groupID);
	// 	if (group == null) {
	// 		res.put(ERROR, ERROR_NO_GROUP);
	// 		return res;
	// 	}
	//
	// 	DataObject groupResult = group.addNewMembershipRole(role);
	// 	res.put(DATA, groupResult);
	// 	return res;
	// };
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
	//  * | groupname        | String                | name of the group to remove from                                           |
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
	// protected ApiFunction remove_membership_role = (req, res) -> {
	// 	String[] paramsToCheck = new String[] { GROUP_ID, ROLE };
	// 	res = check_parameters(paramsToCheck, req, res);
	// 	if (res.get(ERROR) != null)
	// 		return res;
	// 	String groupID = req.getString(GROUP_ID);
	// 	String role = req.getString(ROLE);
	// 	AccountObject group = table.get(groupID);
	// 	if (group == null) {
	// 		res.put(ERROR, ERROR_NO_GROUP);
	// 		return res;
	// 	}
	//
	// 	DataObject groupResult = group.removeMembershipRole(role);
	// 	if (groupResult == null) {
	// 		res.put(ERROR, "No such role is found.");
	// 	} else {
	// 		res.put(DATA, groupResult);
	// 	}
	//
	// 	return res;
	// };
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
	// protected ApiFunction get_member_list_info = (req, res) -> {
	// 	String groupID = req.getString(GROUP_ID);
	//
	// 	AccountObject group = (groupID != null) ? table.get(groupID) : table.getRequestUser(
	// 		req.getHttpServletRequest(), null);
	// 	if (group == null || !group.isGroup()) {
	// 		res.put(ERROR, ERROR_NO_GROUP);
	// 		return res;
	// 	}
	//
	// 	String[] headers = req.getStringArray(HEADERS, "['" + PROPERTIES_OID + "', '"
	// 		+ PROPERTIES_ROLE + "']");
	// 	AccountObject[] memberobjList = group.getMembersAccountObject();
	// 	List<List<Object>> returnList = new ArrayList<List<Object>>();
	// 	int listCounter = 0;
	// 	for (AccountObject ao : memberobjList) {
	// 		DataObject currentGrpAOMeta = group.getMember(ao);
	// 		returnList.add(new ArrayList<Object>());
	// 		for (String column : headers) {
	// 			if (column.equalsIgnoreCase(PROPERTIES_OID)) {
	// 				returnList.get(listCounter).add(ao._oid());
	// 			} else if (column.equalsIgnoreCase(PROPERTIES_NAME)) {
	// 				Set<String> names = ao.getLoginNameSet();
	// 				names.clear();
	// 				for (String name : ao.getLoginNameSet()) {
	// 					names.add(StringEscape.commonHtmlEscapeCharacters(name));
	// 				}
	// 				returnList.get(listCounter).add(ao.getLoginNameSet());
	// 			} else if (column.equalsIgnoreCase(PROPERTIES_ROLE)) {
	// 				returnList.get(listCounter).add(
	// 					StringEscape.commonHtmlEscapeCharacters(group.getMemberRole(ao)));
	// 			} else if (column.toLowerCase().startsWith("account_")) {
	// 				String headerSuffix = column.substring("account_".length());
	// 				Object propertyValue = (ao.get(headerSuffix) != null) ? ao.get(headerSuffix) : "";
	// 				returnList.get(listCounter).add(propertyValue);
	// 			} else if (column.toLowerCase().startsWith("group_")) {
	// 				String headerSuffix = column.substring("group_".length());
	// 				Object propertyValue = (group.get(headerSuffix) != null) ? group.get(headerSuffix)
	// 					: "";
	// 				returnList.get(listCounter).add(propertyValue);
	// 			} else {
	// 				Object propertyValue = (currentGrpAOMeta.get(column) != null) ? currentGrpAOMeta
	// 					.get(column) : "";
	// 				returnList.get(listCounter).add(propertyValue);
	// 			}
	// 		}
	// 		listCounter++;
	// 	}
	// 	res.put(GROUP_ID, group._oid());
	// 	res.put(DATA, returnList);
	// 	res.put(DRAW, req.getInt("draw"));
	// 	res.put(RECORDS_TOTAL, returnList.size());
	// 	res.put(RECORDS_FILTERED, returnList.size());
	// 	res.put(HEADERS, headers);
	// 	return res;
	// };
	//
	// /**
	//  * Description: This function performs add/remove members from existing groups
	//  *
	//  *
	//  *  @param actionObject - GenericConvertMap<String, Object> Contains the action and groupID to be performed by the function
	//  * @param userIDList - String[] Contains the list of user ID to be processed
	//  *
	//  *  @return returnResult - GenericConvertMap<String, Object> Returns the failedList and successList back to caller
	//  */
	// private BiFunction<GenericConvertMap<String, Object>, String[], GenericConvertHashMap<String, Object>> addOrRemove = (
	// 	actionObject, userIDList) -> {
	// 	List<String> failedList = new ArrayList<String>(), successList = new ArrayList<String>();
	// 	AccountObject group = (AccountObject) actionObject.get(GROUP), currentUser = null;
	// 	String action = actionObject.getString("action"), role = actionObject.getString(ROLE);
	// 	String actionErrorMsg = (action.equalsIgnoreCase("add")) ? "User is already in group or role is not found."
	// 		: "User is not in group.";
	// 	for (String userID : userIDList) {
	// 		currentUser = table.get(userID);
	// 		if (currentUser == null) {
	// 			failedList.add("ID: " + userID + ", Error: " + ERROR_NO_USER);
	// 			continue;
	// 		}
	// 		DataObject result = (action.equalsIgnoreCase("add")) ? group.addMember(currentUser, role)
	// 			: group.removeMember(currentUser);
	// 		if (result == null) {
	// 			failedList.add("ID: " + userID + ", Error: " + actionErrorMsg);
	// 			continue;
	// 		} else if (action.equalsIgnoreCase("remove") && result.getInt(PROPERTIES_IS_GROUP) == 0
	// 			&& result._oid() == group._oid()) {
	// 			failedList.add("ID: " + group._oid() + ", Error: " + ERROR_NOT_GROUP);
	// 			break;
	// 		}
	// 		successList.add(userID);
	// 	}
	// 	GenericConvertHashMap<String, Object> returnResult = new GenericConvertHashMap<String, Object>();
	// 	returnResult.put("failedList", failedList);
	// 	returnResult.put("successList", successList);
	// 	return returnResult;
	// };
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
	//  * | role            | String                | name of the role assigned for the user                                     |
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
	// protected ApiFunction add_remove_member = (req, res) -> {
	// 	// Checks all input are given before proceeding
	// 	String[] addList = req.getStringArray(ADD_LIST, "[]");
	// 	String[] removeList = req.getStringArray(REMOVE_LIST, "[]");
	// 	String role = req.getString(ROLE, null);
	//
	// 	String[] paramsToCheck = new String[] { GROUP_ID };
	// 	res = check_parameters(paramsToCheck, req, res);
	// 	if (res.get(ERROR) != null)
	// 		return res;
	// 	String groupID = req.getString(GROUP_ID);
	//
	// 	AccountObject group = table.get(groupID);
	// 	if (group == null) {
	// 		res.put(ERROR, ERROR_NO_GROUP);
	// 		return res;
	// 	}
	// 	GenericConvertHashMap<String, Object> actionObject = new GenericConvertHashMap<String, Object>();
	// 	actionObject.put(GROUP, group);
	// 	if (role == null && addList.length > 0) {
	// 		res.put(ERROR, ERROR_NO_ROLE);
	// 	} else if (addList.length > 0) {
	// 		actionObject.put("action", "add");
	// 		actionObject.put(ROLE, role);
	// 		GenericConvertHashMap<String, Object> result = addOrRemove.apply(actionObject, addList);
	// 		res.put(FAIL_ADD, result.getStringArray("failedList"));
	// 		res.put(SUCCESS_ADD, result.getStringArray("successList"));
	// 	}
	// 	if (removeList.length > 0) {
	// 		actionObject.put("action", "remove");
	// 		GenericConvertMap<String, Object> result = addOrRemove.apply(actionObject, removeList);
	// 		res.put(FAIL_REMOVE, result.getStringArray("failedList"));
	// 		res.put(SUCCESS_REMOVE, result.getStringArray("successList"));
	// 	}
	// 	return res;
	// };
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
	// protected ApiFunction get_single_member_meta = (req, res) -> {
	// 	String userID = req.getString(USER_ID, null);
	// 	AccountObject ao = (userID != null) ? table.get(userID) : table.getRequestUser(
	// 		req.getHttpServletRequest(), null);
	// 	if (ao == null) {
	// 		res.put(ERROR, ERROR_NO_USER);
	// 		return res;
	// 	}
	// 	String role = req.getString(ROLE);
	//
	// 	String[] paramsToCheck = new String[] { GROUP_ID };
	// 	res = check_parameters(paramsToCheck, req, res);
	// 	if (res.get(ERROR) != null)
	// 		return res;
	// 	String groupID = req.getString(GROUP_ID);
	//
	// 	AccountObject group = table.get(groupID);
	// 	if (group == null) {
	// 		res.put(ERROR, ERROR_NO_GROUP);
	// 		return res;
	// 	}
	//
	// 	DataObject groupResult = (role != null) ? group.getMember(ao, role) : group.getMember(ao);
	// 	if (groupResult == null) {
	// 		res.put(ERROR, ERROR_NOT_IN_GROUP_OR_ROLE);
	// 	} else {
	// 		res.put(DATA, groupResult);
	// 	}
	// 	return res;
	// };
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
	//  * | userID          | String  (Optional)    | ID of the user/current user to retrieve                                     |
	//  * | groupID          | String                | ID of the group to retrieve from                                           |
	//  * | updateMode      | String                | Mode of the update used, full or delta (default: delta)                     |
	//  * | meta            | {Object}              | name of the role assigned to the user                                       |
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
	// protected ApiFunction update_member_meta_info = (req, res) -> {
	// 	res.put(SUCCESS, false);
	// 	String memberIDToUpdate = req.getString(USER_ID, null);
	// 	AccountObject ao = (memberIDToUpdate != null) ? table.get(memberIDToUpdate) : table
	// 		.getRequestUser(req.getHttpServletRequest(), null);
	// 	if (ao == null) {
	// 		res.put(ERROR, ERROR_NO_USER);
	// 		return res;
	// 	}
	//
	// 	String[] paramsToCheck = new String[] { GROUP_ID, DATA };
	// 	res = check_parameters(paramsToCheck, req, res);
	// 	if (res.get(ERROR) != null)
	// 		return res;
	// 	String groupID = req.getString(GROUP_ID);
	// 	Object metaObjRaw = req.get(DATA);
	//
	// 	AccountObject group = table.get(groupID);
	// 	if (group == null) {
	// 		res.put(ERROR, ERROR_NO_GROUP);
	// 		return res;
	// 	}
	// 	DataObject currentMemberMeta = group.getMember(ao);
	// 	if (currentMemberMeta == null) {
	// 		res.put(ERROR, ERROR_NOT_IN_GROUP_OR_ROLE);
	// 		return res;
	// 	}
	// 	String updateMode = req.getString(UPDATE_MODE, "delta");
	// 	Map<String, Object> metaObj = ConvertJSON.toMap((String) metaObjRaw);
	// 	updateMode = (!updateMode.equalsIgnoreCase("full")) ? "delta" : updateMode;
	// 	currentMemberMeta.putAll(metaObj);
	// 	if (updateMode.equalsIgnoreCase("full")) {
	// 		currentMemberMeta.saveAll();
	// 	} else {
	// 		currentMemberMeta.saveDelta();
	// 	}
	// 	res.put(ACCOUNT_ID, ao._oid());
	// 	res.put(DATA, metaObj);
	// 	res.put(UPDATE_MODE, updateMode);
	// 	res.put(SUCCESS, true);
	//
	// 	return res;
	// };
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
		if (res.get(ERROR) != null){
			return res;
		}

		// Get the old, and new password
		String oldPassword = req.getString(OLD_PASSWORD);
		String newPassword = req.getString(NEW_PASSWORD);

		// If current account is super user,
		// and is not modifying himself
		if( oldPassword == null && cu.isSuperUser() && !cu._oid().equals(ao._oid()) ) {
			// Bypass old password check, and set new password
			ao.setPassword( newPassword );
			res.put(RESULT, true);
			return res;
		}

		// Ensure from here user logged in and changing password is the same
		if(ao._oid().equals(cu._oid()) == false) {
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

	// /**
	//  * # get_user_or_group_list
	//  *
	//  * Lists the users according to the search criteria
	//  *
	//  * This JSON api is compatible with the datatables.js server side API.
	//  * See: https://web.archive.org/web/20140627100023/http://datatables.net/manual/server-side
	//  *
	//  * ## HTTP Request Parameters
	//  *
	//  * +-----------------+--------------------+-------------------------------------------------------------------------------+
	//  * | Parameter Name  | Variable Type      | Description                                                                   |
	//  * +-----------------+--------------------+-------------------------------------------------------------------------------+
	//  * | draw            | int (optional)     | Draw counter echoed back, and used by the datatables.js server-side API       |
	//  * | start           | int (optional)     | Default 0: Record start listing, 0-indexed                                    |
	//  * | length          | int (optional)     | Default 50: The number of records to return                                   |
	//  * +-----------------+--------------------+-------------------------------------------------------------------------------+
	//  * | insideGroup_any | String[](optional) | Default null, else filters for only accounts inside the listed groups ID      |
	//  * +-----------------+--------------------+-------------------------------------------------------------------------------+
	//  * | headers         | String[](optional) | Default ["_oid", "names"], the collumns to return                             |
	//  * | query           | String (optional)  | Requested Query filter                                                        |
	//  * | queryArgs       | String[] (optional)| Requested Query filter arguments                                              |
	//  * +-----------------+--------------------+-------------------------------------------------------------------------------+
	//  * | groupStatus     | String (optional)  | Default "both", either "user" or "group". Used to lmit the result set         |
	//  * +-----------------+--------------------+-------------------------------------------------------------------------------+
	//  * | sanitiseOutput | boolean (optional) | Default TRUE. If false, returns UNSANITISED data, so common escape characters |
	//  * |                |                    | are returned as well.                                                         |
	//  * +----------------+--------------------+-------------------------------------------------------------------------------+s
	//  *
	//  * ## JSON Object Output Parameters
	//  *
	//  * +-----------------+--------------------+-------------------------------------------------------------------------------+
	//  * | Parameter Name  | Variable Type      | Description                                                                   |
	//  * +-----------------+--------------------+-------------------------------------------------------------------------------+
	//  * | draw            | int (optional)     | Draw counter echoed back, and used by the datatables.js server-side API       |
	//  * | recordsTotal    | int                | Total amount of records. Before any search filter (But after base filters)    |
	//  * | recordsFilterd  | int                | Total amount of records. After all search filter                              |
	//  * +-----------------+--------------------+-------------------------------------------------------------------------------+
	//  * | headers         | String[](optional) | Default ["_oid", "names"], the collumns to return                             |
	//  * +-----------------+--------------------+-------------------------------------------------------------------------------+
	//  * | data            | array              | Array of row records                                                          |
	//  * +-----------------+--------------------+-------------------------------------------------------------------------------+
	//  * | error           | String (Optional)  | Errors encounted if any                                                       |
	//  * +-----------------+--------------------+-------------------------------------------------------------------------------+
	//  **/
	// protected ApiFunction get_user_or_group_list = (req, res) -> {
	// 	int draw = req.getInt("draw");
	// 	int start = req.getInt("start", 0);
	// 	int limit = req.getInt("length");
	// 	String[] insideGroupAny = req.getStringArray("insideGroup_any");
	// 	String[] hasGroupRole_any = req.getStringArray("hasGroupRole_any");
	// 	String groupStatus = req.getString("groupStatus");
	//
	// 	String orderByStr = req.getString("orderBy", "oID");
	//
	// 	String[] headers = req.getStringArray("headers", "['" + PROPERTIES_OID + "', '"
	// 		+ PROPERTIES_NAME + "']");
	//
	// 	String query = req.getString("query");
	// 	String[] queryArgs = req.getStringArray("queryArgs");
	//
	// 	// Data tables search refinement
	// 	String[] queryColumns = req.getStringArray("queryColumns", headers);
	// 	String wildcardMode = req.getString("wildcardMode", "suffix");
	//
	// 	String searchParams = req.getString("searchValue", "").trim(); //datatables specific key
	//
	// 	if (searchParams.isEmpty()) {
	// 		searchParams = req.getString("search[value]", "").trim();
	// 	}
	// 	if (searchParams.length() >= 2 && searchParams.charAt(0) == '"'
	// 		&& searchParams.charAt(searchParams.length() - 1) == '"') {
	// 		searchParams = searchParams.substring(1, searchParams.length() - 1);
	// 	}
	//
	// 	if (!searchParams.isEmpty() && queryColumns != null && queryColumns.length > 0) {
	// 		List<String> queryArgsList = new ArrayList<String>(); //rebuild query arguments
	// 		if (queryArgs != null) {
	// 			for (String queryArg : queryArgs) {
	// 				queryArgsList.add(queryArg);
	// 			}
	// 		}
	//
	// 		if (query == null) {
	// 			query = generateQueryStringForSearchValue(searchParams, queryColumns);
	// 		} else {
	// 			query = query + " AND " + generateQueryStringForSearchValue(searchParams, queryColumns);
	// 		}
	//
	// 		generateQueryStringArgsForSearchValue_andAddToList(searchParams, queryColumns,
	// 			wildcardMode, queryArgsList);
	// 		queryArgs = queryArgsList.toArray(new String[queryArgsList.size()]);
	// 	}
	//
	// 	DataTable mtObj = table.accountDataTable();
	// 	Map<String, Object> links = new HashMap<>();
	// 	Map<String, Object> pagination = new HashMap<>();
	// 	pagination.put("total", table.size());
	// 	limit = (limit == 0) ? table.size() : limit;
	// 	pagination.put("per_page", limit);
	// 	pagination.put("current_page", start + 1);
	// 	pagination.put("last_page", Math.ceil(table.size() / limit));
	// 	links.put("pagination", pagination);
	// 	//put back into response
	// 	res.put("links", links);
	// 	res.put(DRAW, draw);
	// 	res.put(HEADERS, headers);
	// 	res.put(RECORDS_TOTAL, table.size());
	// 	if (query != null && !query.isEmpty() && queryArgs != null && queryArgs.length > 0) {
	// 		res.put(RECORDS_FILTERED, mtObj.queryCount(query, queryArgs));
	// 	} else {
	// 		res.put(RECORDS_FILTERED, mtObj.queryCount(null, null));
	// 	}
	//
	// 	boolean sanitiseOutput = req.getBoolean("sanitiseOutput", true);
	//
	// 	// List<List<Object>> data = new ArrayList<List<Object>>();
	// 	String dataStr = "";
	// 	boolean asObject = true;
	// 	try {
	// 		dataStr = list_GET_and_POST_inner(table, draw, start, limit, headers, query, queryArgs,
	// 			orderByStr, insideGroupAny, hasGroupRole_any, groupStatus, sanitiseOutput, asObject);
	// 		// System.out.println(ConvertJSON.fromObject(data)+" <<<<<<<<<<<<<<<<< data");
	// 		List<Object> data = ConvertJSON.toList(dataStr);
	// 		res.put(DATA, data);
	// 	} catch (Exception e) {
	// 		res.put("error", e.getMessage());
	// 	}
	//
	// 	return res;
	// };
	//
	// /**
	//  * # update_current_user_info
	//  *
	//  * Update the account meta of existing/current user
	//  *
	//  * ## HTTP Request Parameters
	//  *
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | Parameter Name  | Variable Type          | Description                                                               |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | accountID       | String  (Optional)    | ID of the user/current user to retrieve                                    |
	//  * | loginName       | String  (Optional)    | loginName of the user/current user to retrieve                             |
	//  * | updateMode      | String                | Mode of the update used, full or delta (default: delta)                    |
	//  * | data            | {Object}              | information to be updated                                                  |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  *
	//  * ## JSON Object Output Parameters
	//  *
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | Parameter Name  | Variable Type         | Description                                                                |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | accountID       | String                | The account ID of the user                                                 |
	//  * | result          | {Object}              | The information of the user                                                |
	//  * | update          | boolean               | false for failed change and true for success                               |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | ERROR           | String (Optional)     | Errors encountered if any                                                  |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  **/
	// protected ApiFunction update_current_user_info = (req, res) -> {
	// 	res.put(UPDATE, false);
	// 	String loginName = req.getString(LOGINNAME, "");
	// 	String accountID = req.getString(ACCOUNT_ID, "");
	// 	AccountObject ao = (!accountID.isEmpty()) ? table.get(accountID) :
	// 										 (!loginName.isEmpty()) ? table.getFromLoginName(loginName) :
	// 										 table.getRequestUser(req.getHttpServletRequest(), null);
	// 	if (ao == null) {
	// 		res.put(ERROR, ERROR_NO_USER);
	// 		return res;
	// 	}
	//
	// 	String[] paramsToCheck = new String[] { DATA };
	// 	res = check_parameters(paramsToCheck, req, res);
	// 	if (res.get(ERROR) != null){
	// 		return res;
	// 	}
	// 	Object metaObjRaw = req.getStringMap(DATA);
	// 	String updateMode = req.getString(UPDATE_MODE, "delta");
	// 	Map<String, Object> metaObj = ConvertJSON.toMap(ConvertJSON.fromObject(metaObjRaw));
	// 	updateMode = ( !updateMode.equalsIgnoreCase("full") ) ? "delta" : updateMode;
	// 	ao.putAll(metaObj);
	// 	if (updateMode.equalsIgnoreCase("full")) {
	// 		ao.saveAll();
	// 	} else {
	// 		ao.saveDelta();
	// 	}
	// 	Map<String, Object> commonInfo = extractCommonInfoFromAccountObject(ao, true);
	// 	res.put(ACCOUNT_ID, ao._oid());
	// 	res.put(RESULT, commonInfo);
	// 	res.put(UPDATE, true);
	//
	// 	return res;
	// };

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
	 * | userID          | String  (Optional)    | ID of the user/current user to retrieve                                     |
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
		AccountObject ao = (!userID.isEmpty()) ? table.get(userID) : table.getRequestUser(
			req.getHttpServletRequest(), null);
		if (ao == null) {
			res.put(ERROR, ERROR_NO_USER);
			return res;
		}
		if (userID.isEmpty()) { // logout any current session if it is the current user
			this.logout.apply(req, res);
		}
		table.remove(ao);
		res.put(RESULT, true);
		return res;
	};

	// /**
	//  * # account_info
	//  *
	//  * Retrieve the account information of existing/current user using username
	//  *
	//  * ## HTTP Request Parameters
	//  *
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | Parameter Name  | Variable Type          | Description                                                                |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | userID          | String  (Optional)    | ID of the user/current user to retrieve                                     |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  *
	//  * ## JSON Object Output Parameters
	//  *
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | Parameter Name  | Variable Type         | Description                                                                |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | accountID       | String                | The account ID of the user                                                 |
	//  * | result          | {Object}              | The information of the user                                                |
	//  * | update          | boolean               | false for no change and true for success                                   |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  * | ERROR           | String (Optional)     | Errors encountered if any                                                  |
	//  * +-----------------+-----------------------+----------------------------------------------------------------------------+
	//  **/
	// protected ApiFunction account_info = (req, res) -> {
	// 	String loginName = req.getString(LOGINNAME, "");
	// 	String accountID = req.getString(ACCOUNT_ID, "");
	// 	AccountObject ao = (!loginName.isEmpty()) ? table.getFromLoginName(loginName) :
	// 										 (!accountID.isEmpty()) ? table.get(accountID) : table
	// 										 .getRequestUser(req.getHttpServletRequest(), null);
	// 	if (ao == null) {
	// 		res.put(ERROR, ERROR_NO_USER);
	// 		return res;
	// 	}
	// 	Object metaObjRaw = req.getStringMap(DATA, null);
	// 	if (metaObjRaw == null ){
	// 		Map<String, Object> commonInfo = extractCommonInfoFromAccountObject(ao, true);
	// 		res.put(ACCOUNT_ID, ao._oid());
	// 		res.put(RESULT, commonInfo);
	// 		res.put(UPDATE, false);
	// 		return res;
	// 	} else {
	// 		return this.update_current_user_info.apply(req, res);
	// 	}
	// };
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
	// protected ApiFunction getListOfGroupIDOfMember = (req, res) -> {
	// 	String userID = req.getString(USER_ID, "");
	// 	AccountObject ao = (!userID.isEmpty()) ? table.get(userID) : table.getRequestUser(
	// 		req.getHttpServletRequest(), null);
	// 	if (ao == null) {
	// 		res.put(ERROR, ERROR_NO_USER);
	// 		return res;
	// 	}
	// 	String[] listOfGroups = ao.getGroups_id();
	// 	res.put(LIST, listOfGroups);
	// 	return res;
	// };
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
	// protected ApiFunction getListOfGroupObjectOfMember = (req, res) -> {
	// 	String userID = req.getString(USER_ID, "");
	// 	AccountObject ao = (!userID.isEmpty()) ? table.get(userID) : table.getRequestUser(
	// 		req.getHttpServletRequest(), null);
	// 	if (ao == null) {
	// 		res.put(ERROR, ERROR_NO_USER);
	// 		return res;
	// 	}
	// 	AccountObject[] listOfGroupsObj = ao.getGroups();
	// 	List<Map<String, Object>> groupList = new ArrayList<Map<String, Object>>();
	// 	int listCounter = 0;
	// 	for (AccountObject groupObj : listOfGroupsObj) {
	// 		Map<String, Object> commonInfo = extractCommonInfoFromAccountObject(groupObj, true);
	// 		groupList.add(commonInfo);
	// 		listCounter++;
	// 	}
	// 	res.put(LIST, groupList);
	// 	return res;
	// };
	//
	// /**
	//  * # lock time
	//  *
	//  * This function is used to retrieve the locked out timing for the accountName
	//  *
	//  * ## HTTP Request Parameters
	//  *
	//  * +----------------+--------------------+-------------------------------------------------------------------------------+
	//  * | Parameter Name | Variable Type      | Description                                                                   |
	//  * +----------------+--------------------+-------------------------------------------------------------------------------+
	//  * | accountName    | String             | Errors encountered if any                                                       |
	//  * +----------------+--------------------+-------------------------------------------------------------------------------+
	//  *
	//  * ## JSON Object Output Parameters
	//  *
	//  * +----------------+--------------------+-------------------------------------------------------------------------------+
	//  * | Parameter Name | Variable Type      | Description                                                                   |
	//  * +----------------+--------------------+-------------------------------------------------------------------------------+
	//  * | lockTime       | long               | If the number is whole number, it will be int                                 |
	//  * +----------------+--------------------+-------------------------------------------------------------------------------+
	//  **/
	//
	// protected ApiFunction lockTime = (req, res) -> {
	// 	String accountName = req.getString(ACCOUNT_NAME, null);
	// 	if (accountName != null) {
	// 		AccountObject ao = table.getFromLoginName(accountName);
	// 		if (ao != null) {
	// 			long attempts = ao.getAttempts(ao._oid());
	// 			res.put("lockTime", table.calculateDelay.apply(ao, attempts));
	// 		}
	// 	}
	// 	return res;
	// };

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
		if (res.get(ERROR) != null){
			return res;
		}
		// loginName to set
		String loginName = req.getString(LOGINNAME, "");
		// Check if loginName has been used
		AccountObject ao = table.getFromLoginName(loginName);
		if (ao != null){
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

	protected void apiSetup(ApiBuilder api, String prefixPath, GenericConvertMap<String,Object> config) {
		apiSetup(api, prefixPath);
	}

	/**
	 * Does the actual setup for the API
	 * Given the API Builder, and the namespace prefix
	 *
	 * @param  API builder to add the required functions
	 * @param  Path to assume
	 **/
	public void apiSetup(ApiBuilder builder, String path) {

		// Basic new account, login, and logout
		builder.put(path + API_ACCOUNT_LOGIN, login); // Tested
		builder.put(path + API_ACCOUNT_LOGOUT, logout); // Tested
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
		// builder.put(path + API_ACCOUNT_LIST, dataTableApi.list);
		//
		// //Group functionalities
		// builder.put(path + API_GROUP_GRP_ROLES, groupRoles); // Tested
		// builder.put(path + API_GROUP_GET_MEM_ROLE, getMemberRoleFromGroup); // Tested
		// builder.put(path + API_GROUP_GET_LIST_GRP_ID_MEM, getListOfGroupIDOfMember); // Tested
		// builder.put(path + API_GROUP_GET_LIST_GRP_OBJ_MEM, getListOfGroupObjectOfMember); // Tested
		// builder.put(path + API_GROUP_GET_SINGLE_MEM_META, get_single_member_meta); // Tested
		// builder.put(path + API_GROUP_UPDATE_MEM_META, update_member_meta_info); // Tested
		//
		// builder.put(path + API_GROUP_ADMIN_ADD_MEM_ROLE, add_new_membership_role); // Tested
		// builder.put(path + API_GROUP_ADMIN_REM_MEM_ROLE, remove_membership_role); // Tested
		// builder.put(path + API_GROUP_ADMIN_GET_MEM_LIST_INFO, get_member_list_info); // Tested
		// builder.put(path + API_GROUP_ADMIN_ADD_REM_MEM, add_remove_member); // Tested
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
			commonInfo.put("isSuperUser", account.isSuperUser());
			commonInfo.put("isGroup", account.isGroup());
			// Extract hostURL from user account object
			commonInfo.put(PROPERTIES_HOST_URL, account.get(PROPERTIES_HOST_URL));
			// Extract email from user account object
			commonInfo.put(PROPERTIES_EMAIL, account.get(PROPERTIES_EMAIL));
			// Extract name from user account object
			commonInfo.put(PROPERTIES_NAME, account.get(PROPERTIES_NAME));
			Map<String, List<Map<String, Object>>> groupMap = new HashMap<String, List<Map<String, Object>>>();
			AccountObject[] groups = account.getGroups();
			// Check if any groups exist for the member
			if (groups != null) {
				List<Map<String, Object>> groupList = new ArrayList<Map<String, Object>>();
				for (AccountObject group : groups) {
					Map<String, Object> newGroup = new HashMap<String, Object>();
					newGroup.put("groupID", group._oid());
					// Extracting group names and sanitising if needed
					Set<String> groupNames = group.getLoginNameSet();
					if (sanitiseOutput) {
						groupNames.clear();
						for (String groupName : group.getLoginNameSet()) {
							groupNames.add(StringEscape.commonHtmlEscapeCharacters(groupName));
						}
					}
					newGroup.put("names", groupNames);
					//extracting member roles and sanitising if needed
					String role = group.getMemberRole(account);
					if (sanitiseOutput) {
						role = StringEscape.commonHtmlEscapeCharacters(role);
					}
					newGroup.put("role", role); //sanitise role just in case
					role = (role == null) ? "" : role;
					boolean isAdmin = role.equalsIgnoreCase("admin")
						|| role.equalsIgnoreCase("superuser");
					newGroup.put("isAdmin", isAdmin);
					// Check if is group admin
					if (isAdmin) {
						commonInfo.replace("isAnyGroupAdmin", true);
					}
					groupList.add(newGroup);
				}
				commonInfo.put("groups", groupList);
			}
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
				} else {
					//do filtered query
					fullUserArray = _DataTableObj.getUsersByGroupAndRole(insideGroup_any,
						hasGroupRole_any);
				}

				if (fullUserArray == null || fullUserArray.length == 0) {
					return ConvertJSON.fromObject(ret);
				}

				//group status filtering
				if (groupStatus != null) {
					List<AccountObject> filteredUsers = new ArrayList<AccountObject>();
					for (AccountObject ao : fullUserArray) {
						if (groupStatus.equalsIgnoreCase("group")) {
							if (ao.isGroup()) {
								filteredUsers.add(ao);
							}
						} else if (groupStatus.equalsIgnoreCase("user")) {
							if (!ao.isGroup()) {
								filteredUsers.add(ao);
							}
						}
					}

					fullUserArray = filteredUsers.toArray(new AccountObject[filteredUsers.size()]);
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
			switch(paramName) {
				case ACCOUNT_ID :
					value = req.getString(paramName, "");
					if ( value.isEmpty() )
						res.put(ERROR, ERROR_NO_USER_ID);
					break;
				case LOGINNAME :
					value = req.getString(paramName, "");
					if ( value.isEmpty() )
						res.put(ERROR, ERROR_NO_LOGINNAME);
					break;
				case PASSWORD :
					value = req.getString(paramName, "");
					if ( value.isEmpty() )
						res.put(ERROR, ERROR_NO_PASSWORD);
					break;
				case GROUP_ID :
					value = req.getString(paramName, "");
					if ( value.isEmpty() )
						res.put(ERROR, ERROR_NO_GROUP_ID);
					break;
				case ROLE :
					value = req.getString(paramName, "");
					if ( value.isEmpty() )
						res.put(ERROR, ERROR_NO_ROLE);
					break;
				case GROUPNAME :
					value = req.getString(paramName, "");
					if ( value.isEmpty() )
						res.put(ERROR, ERROR_NO_GROUPNAME);
					break;
				case DATA :
					Object metaObj = req.get(paramName);
					if ( metaObj == null )
						res.put(ERROR, ERROR_NO_META);
					break;
				case OLD_PASSWORD:
					value = req.getString(paramName, "");
					if ( value.isEmpty() )
						res.put(ERROR, ERROR_NO_PASSWORD);
					break;
				case NEW_PASSWORD:
					value = req.getString(paramName, "");
					if ( value.isEmpty() )
						res.put(ERROR, ERROR_NO_NEW_PASSWORD);
					break;
				case REPEAT_PASSWORD:
					value = req.getString(paramName, "");
					if ( value.isEmpty() )
						res.put(ERROR, ERROR_NO_NEW_REPEAT_PASSWORD);
					break;
				case AUTH_KEY: // No authKey means unable to use this API endpoint
					value = req.getString(paramName, "");
					if ( value.isEmpty() || !value.equals("thisawesomestring") )
						res.put(ERROR, ERROR_NO_PRIVILEGES);
					break;
				case EMAIL:
					value = req.getString(paramName, "");
					if ( value.isEmpty() )
						res.put(ERROR, ERROR_NO_EMAIL);
					break;
				case NODE_ID:
					value = req.getString(paramName, "");
					if ( value.isEmpty() )
						res.put(ERROR, ERROR_NO_NODE_ID);
					break;
				case NAME:
					value = req.getString(paramName, "");
					if ( value.isEmpty() )
						res.put(ERROR, ERROR_NO_NAME);
					break;
			}
			if (res.get(ERROR) != null){
				break;
			}
		}
		return res;
	}
}
