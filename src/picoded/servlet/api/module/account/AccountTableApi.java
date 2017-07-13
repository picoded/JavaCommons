package picoded.servlet.api.module.account;

import java.util.*;

import picoded.servlet.api.*;
import picoded.servlet.api.module.ApiModule;
import picoded.dstack.module.account.*;
import picoded.dstack.*;
import picoded.conv.ConvertJSON;
import picoded.conv.RegexUtil;
import picoded.conv.GenericConvert;
import java.util.function.BiFunction;
import picoded.struct.GenericConvertMap;
import picoded.struct.GenericConvertHashMap;

import static picoded.servlet.api.module.account.Account_Strings.*;

///
/// Account table API builder
///
public class AccountTableApi implements ApiModule {

	/// The AccountTable reference
	protected AccountTable table = null;

	/// Static ERROR MESSAGES
	public static final String MISSING_REQUEST_PAGE = "Unexpected Exception: Missing requestPage()";


	/// Setup the account table api class
	///
	/// @param  The input AccountTable to use
	public AccountTableApi(AccountTable inTable) {
		table = inTable;
	}

	///
	/// # $prefix/isLogin
	///
	/// Does a simple true / false isLogin check
	/// This can be used for simple checks
	///
	/// ## Request Parameters
	///
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name | Variable Type	   | Description                                                                   |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | -              | -                  | -                                                                             |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name | Variable Type      | Description                                                                   |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | return         | boolean            | TRUE if a login session is valid, else FALSE                                  |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | error          | String (Optional)  | Errors encountered if any                                                     |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	///
	protected ApiFunction isLogin = (req, res) -> {
		// Get the account object (if any)
		AccountObject ao = table.getRequestUser(req.getHttpServletRequest(), null);
		// Return if a valid login object was found
		res.put(RES_RETURN, ao != null);
		// Return result
		return res;
	};

	///
	/// # $prefix/login
	///
	/// Login a user
	///
	/// ## Request Parameters
	///
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name | Variable Type	   | Description                                                                   |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | accountID      | String (Optional)  | Either the loginID or the accountID is needed                                 |
	/// | loginID        | String (Optional)  | Either the loginID or the accountID is needed                                 |
	/// | loginPass      | String             | The account password used for login                                           |
	/// | rememberMe     | boolean            | indicator if the session is persistent (remember me)                          |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | sanitiseOutput | boolean (optional) | Default TRUE. If false, returns UNSANITISED data, so common escape characters |
	/// |                |                    | are returned as well.                                                         |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name | Variable Type      | Description                                                                   |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | isLogin        | boolean            | indicator if the session is logged in or not                                  |
	/// | accountID      | String             | account id of the session                                                     |
	/// | loginIDList    | String[]           | array of account names representing the session                               |
	/// | rememberMe     | boolean            | indicator if the session is persistent (remember me)                          |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | error          | String (Optional)  | Errors encountered if any                                                     |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	///
	protected ApiFunction login = (req, res) -> {

		// Setup default (failed) login
		res.put(RES_ACCOUNT_ID, null);
		res.put(RES_LOGIN_ID_LIST, null);
		res.put(RES_IS_LOGIN, false);
		res.put(RES_REMEMBER_ME, false);

		// Get the login parameters
		String accountID = req.getString(REQ_ACCOUNT_ID, null);
		String loginID = req.getString(REQ_USERNAME, null);
		String loginPass = req.getString(REQ_PASSWORD, null);
		boolean rememberMe = req.getBoolean(REQ_REMEMBER_ME, false);

		// Missing paremeter error checks
		if ( loginPass == null ) {
			res.put(RES_ERROR, ERROR_NO_LOGIN_PASSWORD);
			return res;
		}
		if ( accountID == null && loginID == null ) {
			res.put(RES_ERROR, ERROR_NO_LOGIN_ID);
			return res;
		}

		// Fetch the respective account object
		AccountObject ao = null;
		if ( accountID != null ) {
			ao = table.get(accountID);
		} else if ( loginID != null ) {
			ao = table.getFromLoginID(loginID);
		}

		// Check if account has been locked out
		if ( ao != null ){
			int timeAllowed = ao.getNextLoginTimeAllowed(ao._oid());
			if (timeAllowed != 0){
				res.put(RES_ERROR, "Unable to login, user locked out for "+timeAllowed+" seconds.");
				return res;
			}
		}

		// Continue only with an account object and does not have a lockout timing
		if ( ao != null && ao.validatePassword(loginPass) ) {
			// Validate and login, with password
			ao = table.loginAccount( req.getHttpServletRequest(), res.getHttpServletResponse(), ao, loginPass, rememberMe);
			// Reset any failed login attempts
			ao.resetLoginThrottle(loginID);
			// If ao is not null, it assumes a valid login
			res.put(RES_IS_LOGIN, true);
			res.put(RES_REMEMBER_ME, rememberMe);
			res.put(RES_ACCOUNT_ID, ao._oid());

			// loginID, as a list - as set does not gurantee sorting, a sort is done for the list for alphanumeric
			List<String> loginIDList = new ArrayList<String>(ao.getLoginIDSet());
			Collections.sort(loginIDList);

			// Return the loginIDList
			res.put(RES_LOGIN_ID_LIST, loginIDList);
			ao = table.getRequestUser(req.getHttpServletRequest(), null);
		} else {
			// Legitimate user but wrong password
			if ( ao != null ){
				ao.addDelay(ao);
			}
			res.put(RES_ERROR, ERROR_FAIL_LOGIN);
		}

		return res;
	};

	///
	/// # logout (GET)
	///
	/// The logout GET function, used to logout the current browser session
	///
	/// ## HTTP Request Parameters
	///
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name | Variable Type      | Description                                                                   |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | No parameters options                                                                                               |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name | Variable Type      | Description                                                                   |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | return         | boolean            | indicator if logout is successful or not                                      |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | error          | String (Optional)  | Errors encountered if any                                                     |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	///
	protected ApiFunction logout = (req, res) -> {
		res.put(RES_RETURN, false);

		if (req.getHttpServletRequest() != null) {
			res.put(RES_RETURN, table.logoutAccount(req.getHttpServletRequest(), res.getHttpServletResponse()));
		} else {
			res.put(RES_ERROR, MISSING_REQUEST_PAGE);
		}
		return res;
	};

	///
	/// # new [POST]
	///
	/// Creates a new account in the table
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	      | Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | meta            | {Object} Map<S, O>    | Meta object that represents this account                                   |
	/// | password        | String      	      | Password of new account                                                    |
	/// | username        | String                | Username of new account                                                    |
	/// | isGroup         | boolean (optional)    | whether this is a group object (defaults to false)                         |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	      | Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | accountID       | String                | account ID used                                                            |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | metaObject      | {Object}              | MetaObject representing this account                                       |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | error           | String (Optional)     | Errors encountered if any                                                  |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	public ApiFunction new_account = (req, res) -> {
		// Only runs function if logged in, and valid group object
		boolean isGroup = req.getBoolean(REQ_IS_GROUP, false);
		String[] paramsToCheck = new String[]{REQ_USERNAME};
		res = check_parameters(paramsToCheck, req, res);
		if ( res.get(RES_ERROR) != null )
			return res;
		String userName = req.getString(REQ_USERNAME);
		String password = req.getString(REQ_PASSWORD);
		if ( !isGroup && (password == null || password.isEmpty()) ){
			res.put(RES_ERROR, ERROR_NO_PASSWORD);
			return res;
		}
		Object metaObjRaw = req.get(REQ_META);
		Map<String, Object> givenMetaObj = new HashMap<String, Object>();
		if (metaObjRaw instanceof String) {
			String jsonMetaString = (String) metaObjRaw;
			if (jsonMetaString != null && !jsonMetaString.isEmpty()) {
				givenMetaObj = ConvertJSON.toMap(jsonMetaString);
			}
		}

		AccountObject newAccount = table.newObject(userName);
		if ( newAccount != null ) {
			if ( isGroup ) {
				newAccount.setGroupStatus(true);
				boolean defaultRoles = req.getBoolean(REQ_DEFAULT_ROLES, true);
				List<String> list = req.getList(REQ_ROLE, null);
				if ( defaultRoles || list == null ) {
					newAccount.setMembershipRoles(table.defaultMembershipRoles());
				} else {
					newAccount.setMembershipRoles(list);
				}
				AccountObject firstAdmin = table.getRequestUser(req.getHttpServletRequest(), null);
				if ( firstAdmin != null ) // Set the creator as the admin
					newAccount.setMember(firstAdmin, "admin");
			}
			givenMetaObj.put(PROPERTIES_EMAIL, userName);
			newAccount.setPassword(password);
			newAccount.putAll(givenMetaObj);
			newAccount.saveAll();

			res.put(RES_META, newAccount);
			res.put(RES_ACCOUNT_ID, newAccount._oid());
		} else {
			AccountObject existingAccount = table.getFromLoginID(userName);
			if (existingAccount != null) {
				res.put(RES_ACCOUNT_ID, existingAccount._oid());
			} else {
				res.put(RES_ACCOUNT_ID, null);
			}
			res.put(RES_ERROR, "Object already exists in account Table");
		}
		return res;
	};

	/// # groupRoles
	///
	/// Retrieves the existing roles available from an existing group
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	    	  | Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | groupID					| String								| ID of the group to retrieve																								 |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type					| Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | list						| List		              | List of roles																															 |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | ERROR           | String (Optional)     | Errors encountered if any                                                  |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	protected ApiFunction groupRoles = (req, res) -> {
		String[] paramsToCheck = new String[]{REQ_GROUP_ID};
		res = check_parameters(paramsToCheck, req, res);
		System.out.println(res.get(RES_ERROR)+" alwkejkawlkl");
		if ( res.get(RES_ERROR) != null )
			return res;
		String groupID = req.getString(REQ_GROUP_ID);

		AccountObject group = table.get(groupID);
		if ( group == null ) {
			res.put(RES_ERROR, ERROR_NO_GROUP);
			return res;
		}
		List<String> membershipRoles = group.group_membershipRoles().getList(PROPERTIES_MEMBERSHIP_ROLE, "[]");
		res.put(RES_LIST, membershipRoles);
		// Return result
		return res;
	};

	/// # getMemberRoleFromGroup
	///
	/// Retrieve the role of an existing user from an existing group
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type					| Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | userID					| String								| ID of the user to retrieve																								 |
	/// | groupID					| String								| ID of the group to retrieve from																					 |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type					| Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | single					| String	              | Role of the user																													 |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | ERROR           | String (Optional)     | Errors encountered if any                                                  |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	protected ApiFunction getMemberRoleFromGroup = (req, res) -> {
		String[] paramsToCheck = new String[]{REQ_GROUP_ID, REQ_USER_ID};
		res = check_parameters(paramsToCheck, req, res);
		if ( res.get(RES_ERROR) != null )
			return res;
		String groupID = req.getString(REQ_GROUP_ID);
		String userID = req.getString(REQ_USER_ID);

		AccountObject group = table.get(groupID);
		if ( group == null ) {
			res.put(RES_ERROR, ERROR_NO_GROUP);
			return res;
		}
		AccountObject userToAdd = table.get(userID);
		if ( userToAdd == null ) {
			res.put(RES_ERROR, ERROR_NO_USER);
			return res;
		}
		String role = group.getMemberRole(userToAdd);
		if ( role == null ) {
			res.put(RES_ERROR, "No role for user is found.");
		} else {
			res.put(RES_SINGLE_RETURN_VALUE, role);
		}
		return res;
	};

	/// # add_new_membership_role
	///
	/// Adding a new role to an existing group/user
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type					| Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | role						| String								| name of the role to add																										 |
	/// | groupID					| String								| ID of the group to add to																									 |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type					| Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | meta						| {Object}	            | Object containing the list of roles of the group													 |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | ERROR           | String (Optional)     | Errors encountered if any                                                  |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	protected ApiFunction add_new_membership_role = (req, res) -> {
		String[] paramsToCheck = new String[]{REQ_GROUP_ID, REQ_ROLE};
		res = check_parameters(paramsToCheck, req, res);
		if ( res.get(RES_ERROR) != null )
			return res;
		String groupID = req.getString(REQ_GROUP_ID);
		String role = req.getString(REQ_ROLE);

		AccountObject group = table.get(groupID);
		if ( group == null ) {
			res.put(RES_ERROR, ERROR_NO_GROUP);
			return res;
		}

		MetaObject groupResult = group.addNewMembershipRole(role);
		res.put(RES_META, groupResult);
		return res;
	};

	/// # remove_membership_role
	///
	/// Remove an existing role from an existing group/user
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type					| Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | role						| String								| name of the role to remove																								 |
	/// | groupname				| String								| name of the group to remove from																					 |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type					| Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | meta						| {Object}	            | Object containing the list of roles of the group													 |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | ERROR           | String (Optional)     | Errors encountered if any                                                  |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	protected ApiFunction remove_membership_role = (req, res) -> {
		String[] paramsToCheck = new String[]{REQ_GROUP_ID, REQ_ROLE};
		res = check_parameters(paramsToCheck, req, res);
		if ( res.get(RES_ERROR) != null )
			return res;
		String groupID = req.getString(REQ_GROUP_ID);
		String role = req.getString(REQ_ROLE);

		AccountObject group = table.get(groupID);
		if ( group == null ) {
			res.put(RES_ERROR, ERROR_NO_GROUP);
			return res;
		}

		MetaObject groupResult = group.removeMembershipRole(role);
		if ( groupResult == null ) {
			res.put(RES_ERROR, "No such role is found.");
		}else{
			res.put(RES_META, groupResult);
		}

		return res;
	};

	/// # get_member_list_info
	///
	/// Retrieve the list of members' OBJECT from an existing group
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type					| Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | draw            | int (optional)     		| Draw counter echoed back, and used by the datatables.js server-side API    |
	/// | start           | int (optional)   		  | Default 0: Record start listing, 0-indexed                                 |
	/// | length          | int (optional)   		  | Default max: The number of records to return                               |
	/// | headers					| String [](optional)		| The columns headers returned 																							 |
	/// | groupID					| String (optional)			| ID of the group/current user to retrieve from															 |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type					| Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | data						| array				          | Rows containing the members' OBJECT per row of the group									 |
	/// | draw            | int (optional)     		| Draw counter echoed back, and used by the datatables.js server-side API       | not returned
	/// | recordsTotal    | int                		| Total amount of records. Before any search filter (But after base filters)    | not returned
	/// | recordsFilterd  | int                		| Total amount of records. After all search filter                              | not returned
	/// | headers         | String[](optional) 		| The collumns headers returned                                                 |
	/// | groupID         | String             | group ID used in the request                                                  |
	/// | groupID_exist   | boolean            | indicates if the account ID exists in the system                              |
	/// | groupID_valid   | boolean            | indicates if the account ID exists and is a group                             |
	/// | groupID_admin   | boolean            | indicates if the session has admin rights over the group                      |
	/// | groupID_names   | String[]           | the group various names, if ID is valid                                       | sanitise
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | ERROR           | String (Optional)     | Errors encountered if any                                                  |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	protected ApiFunction get_member_list_info = (req, res) -> {
		String groupID = req.getString(REQ_GROUP_ID);

		AccountObject group = ( groupID != null ) ? table.get(groupID) : table.getRequestUser(req.getHttpServletRequest(), null);
		if ( group == null || !group.isGroup()) {
			res.put(RES_ERROR, ERROR_NO_GROUP);
			return res;
		}

		String[] headers = req.getStringArray(REQ_HEADERS, "['" + PROPERTIES_OID + "', '" + PROPERTIES_ROLE +"']");
		AccountObject[] memberobjList = group.getMembersAccountObject();
		List<List<Object>> returnList = new ArrayList<List<Object>>();
		int listCounter = 0;
		for ( AccountObject ao : memberobjList ) {
			MetaObject currentGrpAOMeta = group.getMember(ao);
			returnList.add(new ArrayList<Object>());
			for ( String column : headers ) {
				if ( column.equalsIgnoreCase(PROPERTIES_OID) ) {
					returnList.get(listCounter).add(ao._oid());
				} else if ( column.equalsIgnoreCase(PROPERTIES_NAMES) ) {
					Set<String> names = ao.getLoginIDSet();
					names.clear();
					for (String name : ao.getLoginIDSet()) {
						names.add(RegexUtil.sanitiseCommonEscapeCharactersIntoAscii(name));
					}
					returnList.get(listCounter).add(ao.getLoginIDSet());
				} else if ( column.equalsIgnoreCase(PROPERTIES_ROLE) ) {
					returnList.get(listCounter).add(RegexUtil.sanitiseCommonEscapeCharactersIntoAscii(group.getMemberRole(ao)));
				} else if ( column.toLowerCase().startsWith("account_") ) {
					String headerSuffix = column.substring("account_".length());
					Object propertyValue = ( ao.get(headerSuffix) != null ) ? ao.get(headerSuffix) : "";
					returnList.get(listCounter).add(propertyValue);
				} else if ( column.toLowerCase().startsWith("group_") ) {
					String headerSuffix = column.substring("group_".length());
					Object propertyValue = ( group.get(headerSuffix) != null ) ? group.get(headerSuffix) : "";
					returnList.get(listCounter).add(propertyValue);
				} else {
					Object propertyValue = ( currentGrpAOMeta.get(column) != null ) ? currentGrpAOMeta.get(column) : "";
					returnList.get(listCounter).add(propertyValue);
				}
			}
			listCounter++;
		}
		res.put(RES_GROUP_ID, group._oid());
		res.put(RES_DATA, returnList);
		res.put(RES_DRAW, req.getInt("draw"));
		res.put(RES_RECORDS_TOTAL, returnList.size());
		res.put(RES_RECORDS_FILTERED, returnList.size());
		res.put(RES_HEADERS, headers);
		return res;
	};

	/**
	* Description: This function performs add/remove members from existing groups
	*
	*
	*	@param actionObject - GenericConvertMap<String, Object> Contains the action and groupID to be performed by the function
	* @param userIDList - String[] Contains the list of user ID to be processed
	*
	*	@return returnResult - GenericConvertMap<String, Object> Returns the failedList and successList back to caller
	*/
	private BiFunction<GenericConvertMap<String, Object>, String[], GenericConvertHashMap<String, Object>> addOrRemove = (actionObject, userIDList) -> {
		List<String> failedList = new ArrayList<String>(), successList = new ArrayList<String>();
		AccountObject group = (AccountObject) actionObject.get(GROUP),  currentUser = null;
		String action = actionObject.getString("action"), role = actionObject.getString(REQ_ROLE);
		String actionErrorMsg = (action.equalsIgnoreCase("add")) ? "User is already in group or role is not found.": "User is not in group.";
		for ( String userID : userIDList ) {
			currentUser = table.get(userID);
			if ( currentUser == null ) {
				failedList.add("ID: " + userID + ", Error: " + ERROR_NO_USER);
				continue;
			}
			MetaObject result = (action.equalsIgnoreCase("add")) ? group.addMember(currentUser, role) : group.removeMember(currentUser);
			if ( result == null ) {
				failedList.add("ID: " + userID + ", Error: " + actionErrorMsg);
				continue;
			} else if ( action.equalsIgnoreCase("remove") && result.getInt(PROPERTIES_IS_GROUP) == 0 && result._oid() == group._oid()) {
				failedList.add("ID: " + group._oid() + ", Error: " + ERROR_NOT_GROUP);
				break;
			}
			successList.add(userID);
		}
		GenericConvertHashMap<String, Object> returnResult = new GenericConvertHashMap<String, Object>();
		returnResult.put("failedList", failedList);
		returnResult.put("successList", successList);
		return returnResult;
	};

	/// # add_remove_member
	///
	/// Add/Remove an existing user (with role) to/from an existing group
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type					| Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | addList					| String []							| List of ID of the users to add																						 |
	/// | removeList			| String []							| List of ID of the users to remove																					 |
	/// | groupID					| String []							| ID of the group to add/remove to/from																			 |
	/// | role						| String								| name of the role assigned for the user																		 |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type					| Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | meta						| {Object}              | Information of the newly added user																				 |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | ERROR           | String (Optional)     | Errors encountered if any                                                  |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	protected ApiFunction add_remove_member = (req, res) -> {
		// Checks all input are given before proceeding
		String [] addList = req.getStringArray(REQ_ADD_LIST, "[]");
		String [] removeList = req.getStringArray(REQ_REMOVE_LIST, "[]");
		String role = req.getString(REQ_ROLE, null);

		String[] paramsToCheck = new String[]{REQ_GROUP_ID};
		res = check_parameters(paramsToCheck, req, res);
		if ( res.get(RES_ERROR) != null )
			return res;
		String groupID = req.getString(REQ_GROUP_ID);

		AccountObject group = table.get(groupID);
		if ( group == null ) {
			res.put(RES_ERROR, ERROR_NO_GROUP);
			return res;
		}
		GenericConvertHashMap<String, Object> actionObject = new GenericConvertHashMap<String, Object>();
		actionObject.put(GROUP, group);
		if ( role == null && addList.length > 0 ) {
			res.put(RES_ERROR, ERROR_NO_ROLE);
		} else if ( addList.length > 0 ) {
				actionObject.put("action", "add");
				actionObject.put(REQ_ROLE, role);
				GenericConvertHashMap<String, Object> result = addOrRemove.apply(actionObject, addList);
				res.put(RES_FAIL_ADD, result.getStringArray("failedList"));
				res.put(RES_SUCCESS_ADD, result.getStringArray("successList"));
		}
		if ( removeList.length > 0 ) {
			actionObject.put("action", "remove");
			GenericConvertMap<String, Object> result = addOrRemove.apply(actionObject, removeList);
			res.put(RES_FAIL_REMOVE, result.getStringArray("failedList"));
			res.put(RES_SUCCESS_REMOVE, result.getStringArray("successList"));
		}
		return res;
	};


	/// # get_single_member_meta
	///
	/// Retrieve the meta information of an existing/current user from an existing group
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type					| Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | userID					| String								| ID of the user to retrieve																								 |
	/// | groupID					| String								| ID of the group to retrieve from																					 |
	/// | role						| String (Optional)			| name of the role assigned to the user																			 |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type					| Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | meta						| {Object}              | Meta information of the user																							 |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | ERROR           | String (Optional)     | Errors encountered if any                                                  |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	protected ApiFunction get_single_member_meta = (req, res) -> {
		String userID = req.getString(REQ_USER_ID, null);
		AccountObject ao = ( userID != null ) ? table.get(userID) : table.getRequestUser(req.getHttpServletRequest(), null);
		if( ao == null ) {
			res.put(RES_ERROR, ERROR_NO_USER);
			return res;
		}
		String role = req.getString(REQ_ROLE);

		String[] paramsToCheck = new String[]{REQ_GROUP_ID};
		res = check_parameters(paramsToCheck, req, res);
		if ( res.get(RES_ERROR) != null )
			return res;
		String groupID = req.getString(REQ_GROUP_ID);

		AccountObject group = table.get(groupID);
		if ( group == null ) {
			res.put(RES_ERROR, ERROR_NO_GROUP);
			return res;
		}

		MetaObject groupResult = (role != null) ? group.getMember(ao, role) : group.getMember(ao);
		if ( groupResult == null ) {
			res.put(RES_ERROR, ERROR_NOT_IN_GROUP_OR_ROLE);
		}else{
			res.put(RES_META, groupResult);
		}
		return res;
	};

	/// # update_member_meta_info
	///
	/// Update the member meta information of an existing/current user from an existing group
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type					| Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | userID					| String  (Optional)		| ID of the user/current user to retrieve																		 |
	/// | groupID					| String								| ID of the group to retrieve from																					 |
	/// | updateMode			| String	              | Mode of the update used, full or delta (default: delta)										 |
	/// | meta						| {Object}							| name of the role assigned to the user																			 |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type					| Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | meta						| {Object}              | Meta information of the user																							 |
	/// | accountID				| String	              | ID of the user																														 |
	/// | updateMode			| String	              | Mode of the update used																										 |
	/// | success					| boolean	              | false for failed update and true for success															 |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | ERROR           | String (Optional)     | Errors encountered if any                                                  |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	protected ApiFunction update_member_meta_info = (req, res) -> {
		res.put(RES_SUCCESS, false);
		String memberIDToUpdate = req.getString(REQ_USER_ID, null);
		AccountObject ao = (memberIDToUpdate != null ) ? table.get(memberIDToUpdate) : table.getRequestUser(req.getHttpServletRequest(), null);
		if ( ao == null ) {
			res.put(RES_ERROR, ERROR_NO_USER);
			return res;
		}

		String[] paramsToCheck = new String[]{REQ_GROUP_ID, REQ_META};
		res = check_parameters(paramsToCheck, req, res);
		if ( res.get(RES_ERROR) != null )
			return res;
		String groupID = req.getString(REQ_GROUP_ID);
		Object metaObjRaw = req.get(REQ_META);

		AccountObject group = table.get(groupID);
		if ( group == null ) {
			res.put(RES_ERROR, ERROR_NO_GROUP);
			return res;
		}
		MetaObject currentMemberMeta = group.getMember(ao);
		if ( currentMemberMeta == null ) {
			res.put(RES_ERROR, ERROR_NOT_IN_GROUP_OR_ROLE);
			return res;
		}
		String updateMode = req.getString(REQ_UPDATE_MODE, "delta");
		Map<String, Object> metaObj = ConvertJSON.toMap((String) metaObjRaw);
		updateMode = ( !updateMode.equalsIgnoreCase("full") ) ? "delta" : updateMode;
		currentMemberMeta.putAll(metaObj);
		if ( updateMode.equalsIgnoreCase("full") ) {
			currentMemberMeta.saveAll();
		} else {
			currentMemberMeta.saveDelta();
		}
		res.put(RES_ACCOUNT_ID, ao._oid());
		res.put(RES_META, metaObj);
		res.put(RES_UPDATE_MODE, updateMode);
		res.put(RES_SUCCESS, true);

		return res;
	};

	/// # do_password_reset
	///
	/// Resets the password of the user/current member
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type					| Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | userID					| String  (Optional)		| ID of the user/current user to retrieve																		 |
	/// | oldPassword			| String								| Old password of the user																									 |
	/// | newPassword			| String								| New password of the user																									 |
	/// | repeatPassword	| String								| Repeat new password of the user																						 |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type					| Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | accountID				| String	              | ID of the user																														 |
	/// | success					| boolean	              | false for failed change and true for success															 |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | ERROR           | String (Optional)     | Errors encountered if any                                                  |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	protected ApiFunction do_password_reset = (req, res) -> {
		res.put(RES_SUCCESS, false);
		String userID = req.getString(REQ_USER_ID, "");
		AccountObject ao = ( !userID.isEmpty() ) ? table.get(userID) : table.getRequestUser(req.getHttpServletRequest(), null);
		if ( ao == null ) {
			res.put(RES_ERROR, ERROR_NO_USER);
			return res;
		}

		String[] paramsToCheck = new String[]{REQ_OLD_PASSWORD, REQ_NEW_PASSWORD, REQ_REPEAT_PASSWORD};
		res = check_parameters(paramsToCheck, req, res);
		if ( res.get(RES_ERROR) != null )
			return res;
		String oldPassword = req.getString(REQ_OLD_PASSWORD);
		String newPassword = req.getString(REQ_NEW_PASSWORD);
		String repeatPassword = req.getString(REQ_REPEAT_PASSWORD);

		if ( !newPassword.equals(repeatPassword) ) {
			res.put(RES_ERROR, ERROR_PASS_NOT_EQUAL);
			return res;
		}
		if ( !ao.setPassword(newPassword, oldPassword) ) {
			res.put(RES_ERROR, ERROR_PASS_INCORRECT);
			return res;
		}
		res.put(RES_SUCCESS, true);
		res.put(RES_ACCOUNT_ID, ao._oid());

		return res;
	};

	///
	/// # get_user_or_group_list
	///
	/// Lists the users according to the search criteria
	///
	/// This JSON api is compatible with the datatables.js server side API.
	/// See: https://web.archive.org/web/20140627100023/http://datatables.net/manual/server-side
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	    | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | draw            | int (optional)     | Draw counter echoed back, and used by the datatables.js server-side API       |
	/// | start           | int (optional)     | Default 0: Record start listing, 0-indexed                                    |
	/// | length          | int (optional)     | Default 50: The number of records to return                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | insideGroup_any | String[](optional) | Default null, else filters for only accounts inside the listed groups ID      |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | headers         | String[](optional) | Default ["_oid", "names"], the collumns to return                             |
	/// | query           | String (optional)  | Requested Query filter                                                        |
	/// | queryArgs       | String[] (optional)| Requested Query filter arguments                                              |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | groupStatus     | String (optional)  | Default "both", either "user" or "group". Used to lmit the result set         |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | sanitiseOutput | boolean (optional) | Default TRUE. If false, returns UNSANITISED data, so common escape characters |
	/// |                |                    | are returned as well.                                                         |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+s
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	    | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | draw            | int (optional)     | Draw counter echoed back, and used by the datatables.js server-side API       |
	/// | recordsTotal    | int                | Total amount of records. Before any search filter (But after base filters)    |
	/// | recordsFilterd  | int                | Total amount of records. After all search filter                              |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | headers         | String[](optional) | Default ["_oid", "names"], the collumns to return                             |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | data            | array              | Array of row records                                                          |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | error           | String (Optional)  | Errors encounted if any                                                       |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	protected ApiFunction get_user_or_group_list = (req, res) -> {
		int draw = req.getInt("draw");
		int start = req.getInt("start");
		int limit = req.getInt("length");
		String[] insideGroupAny = req.getStringArray("insideGroup_any");
		String[] hasGroupRole_any = req.getStringArray("hasGroupRole_any");
		String groupStatus = req.getString("groupStatus");

		String orderByStr = req.getString("orderBy", "oID");

		String[] headers = req.getStringArray("headers", "['" + PROPERTIES_OID + "', '" + PROPERTIES_NAMES + "']");

		String query = req.getString("query");
		String[] queryArgs = req.getStringArray("queryArgs");

		// Data tables search refinement
		String[] queryColumns = req.getStringArray("queryColumns", headers);
		String wildcardMode = req.getString("wildcardMode", "suffix");

		String searchParams = req.getString("searchValue", "").trim(); //datatables specific key

		if (searchParams.isEmpty()) {
			searchParams = req.getString("search[value]", "").trim();
		}
		if (searchParams.length() >= 2 && searchParams.charAt(0) == '"'
			&& searchParams.charAt(searchParams.length() - 1) == '"') {
			searchParams = searchParams.substring(1, searchParams.length() - 1);
		}

		if (!searchParams.isEmpty() && queryColumns != null && queryColumns.length > 0) {
			List<String> queryArgsList = new ArrayList<String>(); //rebuild query arguments
			if (queryArgs != null) {
				for (String queryArg : queryArgs) {
					queryArgsList.add(queryArg);
				}
			}

			if(query == null){
				query = generateQueryStringForSearchValue(searchParams, queryColumns);
			}else{
				query = query + " AND " + generateQueryStringForSearchValue(searchParams, queryColumns);
			}

			generateQueryStringArgsForSearchValue_andAddToList(searchParams, queryColumns, wildcardMode, queryArgsList);
			queryArgs = queryArgsList.toArray(new String[queryArgsList.size()]);
		}

		MetaTable mtObj = table.accountMetaTable();

		//put back into response
		res.put(RES_DRAW, draw);
		res.put(RES_HEADERS, headers);
		res.put(RES_RECORDS_TOTAL, table.size());
		if (query != null && !query.isEmpty() && queryArgs != null && queryArgs.length > 0) {
			res.put(RES_RECORDS_FILTERED, mtObj.queryCount(query, queryArgs));
		} else {
			res.put(RES_RECORDS_FILTERED, mtObj.queryCount(null, null));
		}

		boolean sanitiseOutput = req.getBoolean("sanitiseOutput", true);

		List<List<Object>> data = new ArrayList<List<Object>>();
		try {
			data = list_GET_and_POST_inner(table, draw, start, limit, headers, query, queryArgs,
				orderByStr, insideGroupAny, hasGroupRole_any, groupStatus, sanitiseOutput);
				// System.out.println(ConvertJSON.fromObject(data)+" <<<<<<<<<<<<<<<<< data");
			res.put(RES_DATA, data);
		} catch (Exception e) {
			res.put("error", e.getMessage());
		}

		return res;
	};

	/// # update_current_user_info
	///
	/// Update the account meta of existing/current user
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type					| Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | userID					| String  (Optional)		| ID of the user/current user to retrieve																		 |
	/// | updateMode			| String	              | Mode of the update used, full or delta (default: delta)										 |
	/// | meta						| {Object}							| name of the role assigned to the user																			 |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type					| Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | accountID				| String	              | ID of the user																														 |
	/// | success					| boolean	              | false for failed change and true for success															 |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | ERROR           | String (Optional)     | Errors encountered if any                                                  |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	protected ApiFunction update_current_user_info = (req, res) -> {
		res.put(RES_SUCCESS, false);
		String userID = req.getString(REQ_USER_ID, null);
		AccountObject ao = (userID != null ) ? table.get(userID) : table.getRequestUser(req.getHttpServletRequest(), null);
		if ( ao == null ) {
			res.put(RES_ERROR, ERROR_NO_USER);
			return res;
		}

		String[] paramsToCheck = new String[]{REQ_META};
		res = check_parameters(paramsToCheck, req, res);
		if ( res.get(RES_ERROR) != null )
			return res;
		Object metaObjRaw = req.get(REQ_META);

		String updateMode = req.getString(REQ_UPDATE_MODE, "delta");
		Map<String, Object> metaObj = ConvertJSON.toMap((String) metaObjRaw);
		updateMode = ( !updateMode.equalsIgnoreCase("full") ) ? "delta" : updateMode;
		ao.putAll(metaObj);
		if ( updateMode.equalsIgnoreCase("full") ) {
			ao.saveAll();
		} else {
			ao.saveDelta();
		}
		res.put(RES_ACCOUNT_ID, ao._oid());
		res.put(RES_META, metaObj);
		res.put(RES_UPDATE_MODE, updateMode);
		res.put(RES_SUCCESS, true);

		return res;
	};

	/// # delete_user_account
	///
	/// Delete an existing/current user
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type					| Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | userID					| String  (Optional)		| ID of the user/current user to retrieve																		 |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type					| Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | success					| boolean	              | false for failed change and true for success															 |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | ERROR           | String (Optional)     | Errors encountered if any                                                  |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	protected ApiFunction delete_user_account = (req, res) -> {
		String userID = req.getString(REQ_USER_ID, "");
		AccountObject ao = ( !userID.isEmpty() ) ? table.get(userID) : table.getRequestUser(req.getHttpServletRequest(), null);
		if ( ao == null ) {
			res.put(RES_ERROR, ERROR_NO_USER);
			return res;
		}
		if ( userID.isEmpty() ) { // logout any current session if it is the current user
			this.logout.apply(req, res);
		}
		table.remove(ao);
		res.put(RES_SUCCESS, true);
		return res;
	};

	/// # account_info_by_ID
	///
	/// Retrieve the account information of existing/current user using ID
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type					| Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | userID					| String  (Optional)		| ID of the user/current user to retrieve																		 |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type					| Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | accountID       | String             | account ID used                                                               |
	/// | accountNames    | String[]           | array of account names representing the account                               | sanitise
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | isSuperUser     | boolean            | indicates if the account is considered a superUser                            |
	/// | isAnyGroupAdmin | boolean            | indicates if the account is considered a superUser                            |
	/// | isGroup         | boolean            | indicates if the account is considered a group                                |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | groups          | object(Map)        | Groups object as a Map<String, List<Map<String, Object>>>                     | sanitise
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | ERROR           | String (Optional)     | Errors encountered if any                                                  |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	protected ApiFunction account_info_by_ID = (req, res) -> {
		String userID = req.getString(REQ_USER_ID, "");
		AccountObject ao = ( !userID.isEmpty() ) ? table.get(userID) : table.getRequestUser(req.getHttpServletRequest(), null);
		if ( ao == null ) {
			res.put(RES_ERROR, ERROR_NO_USER);
			return res;
		}
		Map<String, Object> commonInfo = extractCommonInfoFromAccountObject(ao, true);
		res.putAll(commonInfo);
		return res;
	};

	/// # account_info_by_Name
	///
	/// Retrieve the account information of existing/current user using username
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type					| Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | userID					| String  (Optional)		| ID of the user/current user to retrieve																		 |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type					| Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | accountID       | String             | account ID used                                                               |
	/// | accountNames    | String[]           | array of account names representing the account                               | sanitise
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | isSuperUser     | boolean            | indicates if the account is considered a superUser                            |
	/// | isAnyGroupAdmin | boolean            | indicates if the account is considered a superUser                            |
	/// | isGroup         | boolean            | indicates if the account is considered a group                                |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | groups          | object(Map)        | Groups object as a Map<String, List<Map<String, Object>>>                     | sanitise
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | ERROR           | String (Optional)     | Errors encountered if any                                                  |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	protected ApiFunction account_info_by_Name = (req, res) -> {
		String userName = req.getString(REQ_USERNAME, "");
		AccountObject ao = ( !userName.isEmpty() ) ? table.getFromLoginID(userName) : table.getRequestUser(req.getHttpServletRequest(), null);
		if ( ao == null ) {
			res.put(RES_ERROR, ERROR_NO_USER);
			return res;
		}
		Map<String, Object> commonInfo = extractCommonInfoFromAccountObject(ao, true);
		res.putAll(commonInfo);
		return res;
	};


	/// # getListOfGroupIDOfMember
	///
	/// Retrieve a list of groups ID from an existing member/current user
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type					| Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | username				| String	(Either or)		| name of the user to retrieve from																					 |
	/// | oid							| String								| oid of the user to retrieve from																					 |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type					| Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | list						| List<String>          | List containing the groups' ID of the member															 |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | ERROR           | String (Optional)     | Errors encountered if any                                                  |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	protected ApiFunction getListOfGroupIDOfMember = (req, res) -> {
		String userID = req.getString(REQ_USER_ID, "");
		AccountObject ao = ( !userID.isEmpty() ) ? table.get(userID) : table.getRequestUser(req.getHttpServletRequest(), null);
		if ( ao == null ) {
			res.put(RES_ERROR, ERROR_NO_USER);
			return res;
		}
		String[] listOfGroups = ao.getGroups_id();
		res.put(RES_LIST, listOfGroups);
		return res;
	};

	/// # getListOfGroupObjectOfMember
	///
	/// Retrieve a list of groups OBJECT from an existing member/current user
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type					| Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | userID					| String	(Either or)		| ID of the user to retrieve from																						 |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type					| Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | list						| List<String>          | List containing the groups' OBJECT of the member													 |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | ERROR           | String (Optional)     | Errors encountered if any                                                  |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	protected ApiFunction getListOfGroupObjectOfMember = (req, res) -> {
		String userID = req.getString(REQ_USER_ID, "");
		AccountObject ao = ( !userID.isEmpty() ) ? table.get(userID) : table.getRequestUser(req.getHttpServletRequest(), null);
		if ( ao == null ) {
			res.put(RES_ERROR, ERROR_NO_USER);
			return res;
		}
		AccountObject[] listOfGroupsObj = ao.getGroups();
		List<Map<String, Object>> groupList = new ArrayList<Map<String,Object>>();
		int listCounter = 0;
		for ( AccountObject groupObj : listOfGroupsObj ) {
			Map<String, Object> commonInfo = extractCommonInfoFromAccountObject(groupObj, true);
			groupList.add(commonInfo);
			listCounter++;
		}
		res.put(RES_LIST, groupList);
		return res;
	};

	///
	/// # lock time
	///
	/// This function is used to retrieve the locked out timing for the accountName
	///
	/// ## HTTP Request Parameters
	///
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name | Variable Type      | Description                                                                   |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | accountName    | String             | Errors encountered if any                                                       |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name | Variable Type      | Description                                                                   |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | lockTime       | long               | If the number is whole number, it will be int                                 |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	///

	protected ApiFunction lockTime = (req, res) -> {
		String accountName = req.getString(REQ_ACCOUNT_NAME, null);
		if (accountName != null){
			AccountObject ao = table.getFromLoginID(accountName);
			if (ao != null){
				long attempts = ao.getAttempts(ao._oid());
				res.put("lockTime", table.calculateDelay.apply(ao, attempts));
			}
		}
		return res;
	};

	/// Does the actual setup for the API
	/// Given the API Builder, and the namespace prefix
	///
	/// @param  API builder to add the required functions
	/// @param  Path to assume
	public void setupApiBuilder(ApiBuilder builder, String path) {
		builder.put(path+API_ACCOUNT_IS_LOGIN, isLogin); // Tested
		builder.put(path+API_ACCOUNT_LOGIN, login); // Tested
		builder.put(path+API_ACCOUNT_LOCKTIME, lockTime); // Tested
		builder.put(path+API_ACCOUNT_LOGOUT, logout); // Tested
		builder.put(path+API_ACCOUNT_NEW, new_account); // Tested
		builder.put(path+API_ACCOUNT_PASS_RESET, do_password_reset); // Tested
		builder.put(path+API_ACCOUNT_INFO_NAME, account_info_by_Name); // Tested
		builder.put(path+API_ACCOUNT_INFO_ID, account_info_by_ID); // Tested
		builder.put(path+API_ACCOUNT_ADMIN_REMOVE, delete_user_account); // Tested
		builder.put(path+API_ACCOUNT_ADMIN_GET_U_G_LIST, get_user_or_group_list); // Tested
		builder.put(path+API_ACCOUNT_UPDATE_U_INFO, update_current_user_info);

		//Group functionalities
		builder.put(path+API_GROUP_GRP_ROLES, groupRoles); // Tested
		builder.put(path+API_GROUP_GET_MEM_ROLE, getMemberRoleFromGroup); // Tested
		builder.put(path+API_GROUP_GET_LIST_GRP_ID_MEM, getListOfGroupIDOfMember); // Tested
		builder.put(path+API_GROUP_GET_LIST_GRP_OBJ_MEM, getListOfGroupObjectOfMember); // Tested
		builder.put(path+API_GROUP_GET_SINGLE_MEM_META, get_single_member_meta); // Tested
		builder.put(path+API_GROUP_UPDATE_MEM_META, update_member_meta_info); // Tested

		builder.put(path+API_GROUP_ADMIN_ADD_MEM_ROLE, add_new_membership_role); // Tested
		builder.put(path+API_GROUP_ADMIN_REM_MEM_ROLE, remove_membership_role); // Tested
		builder.put(path+API_GROUP_ADMIN_GET_MEM_LIST_INFO, get_member_list_info); // Tested
		builder.put(path+API_GROUP_ADMIN_ADD_REM_MEM, add_remove_member); // Tested
		// builder.put(path+"getListOfMemberObjectOfGroup", getListOfMemberObjectOfGroup); // Tested
	}


	/// Private Methods

	private static Map<String, Object> extractCommonInfoFromAccountObject(AccountObject account, boolean sanitiseOutput) {
	//sanitise accountNames, groupNames,
		Map<String, Object> commonInfo = new HashMap<String, Object>();
		sanitiseOutput = true; // let it always sanitise the output
		commonInfo.put(RES_ACCOUNT_ID, null);
		commonInfo.put("accountNames", null);
		commonInfo.put("isSuperUser", null);
		commonInfo.put("isGroup", null);
		commonInfo.put("groups", null);
		commonInfo.put("isAnyGroupAdmin", false);

		if (account != null) {
			commonInfo.put(RES_ACCOUNT_ID, account._oid());
			Set<String> accNameSet = account.getLoginIDSet();
			if (accNameSet != null) {
				String[] accNames = new String[accNameSet.size()];
				accNameSet.toArray(accNames);
				if (sanitiseOutput && accNames != null) {
					for (int i = 0; i < accNames.length; ++i) {
						accNames[i] = RegexUtil.sanitiseCommonEscapeCharactersIntoAscii(accNames[i]);
					}
				}
				commonInfo.put("accountNames", accNames);
			}
			commonInfo.put("isSuperUser", account.isSuperUser());
			commonInfo.put("isGroup", account.isGroup());
			Map<String, List<Map<String, Object>>> groupMap = new HashMap<String, List<Map<String, Object>>>();
			AccountObject[] groups = account.getGroups();
			if (groups != null) {
				List<Map<String, Object>> groupList = new ArrayList<Map<String, Object>>();
				for (AccountObject group : groups) {
					Map<String, Object> newGroup = new HashMap<String, Object>();
					newGroup.put("groupID", group._oid());
					//extracting group names and sanitising if needed
					Set<String> groupNames = group.getLoginIDSet();
					if (sanitiseOutput) {
						groupNames.clear();
						for (String groupName : group.getLoginIDSet()) {
							groupNames.add(RegexUtil.sanitiseCommonEscapeCharactersIntoAscii(groupName));
						}
					}
					newGroup.put("names", groupNames);
					//extracting member roles and sanitising if needed
					String role = group.getMemberRole(account);
					if (sanitiseOutput) {
						role = RegexUtil.sanitiseCommonEscapeCharactersIntoAscii(role);
					}
					newGroup.put("role", role); //sanitise role just in case
					role = ( role == null ) ? "" : role;
					boolean isAdmin = role.equalsIgnoreCase("admin") || role.equalsIgnoreCase("superuser");
					newGroup.put("isAdmin", isAdmin);
					if (isAdmin) {
						commonInfo.replace("isAnyGroupAdmin", true);
					}
					groupList.add(newGroup);
				}
				commonInfo.put("groups", groupList);
			}
		}else{
			System.out.println("AccountLogin -> extractCommonInfoFromAccountObject -> AccountObject is null!");
		}
		return commonInfo;
	}

	private static String generateQueryStringForSearchValue(String inSearchString, String[] queryColumns) {
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
	private static List<String> generateQueryStringArgsForSearchValue_andAddToList(String inSearchString,
		String[] queryColumns, String wildcardMode, List<String> ret) {
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

	private static List<List<Object>> list_GET_and_POST_inner(AccountTable _metaTableObj, int draw, int start,
	int length, String[] headers, String query, String[] queryArgs, String orderBy, String[] insideGroup_any,
	String[] hasGroupRole_any, String groupStatus, boolean sanitiseOutput) throws RuntimeException {

		List<List<Object>> ret = new ArrayList<List<Object>>();

		if (_metaTableObj == null) {
			return ret;
		}

		try {
			if (headers != null && headers.length > 0) {
				MetaObject[] metaObjs = null;
				AccountObject[] fullUserArray = null;

				if ((insideGroup_any == null || insideGroup_any.length == 0)
					&& (hasGroupRole_any == null || hasGroupRole_any.length == 0)) {
					//do normal query
					MetaTable accountMetaTable = _metaTableObj.accountMetaTable();

					if (accountMetaTable == null) {
						return ret;
					}

					if (query == null || query.isEmpty() || queryArgs == null || queryArgs.length == 0) {
						metaObjs = accountMetaTable.query(null, null, orderBy, start, length);
					} else {
						metaObjs = accountMetaTable.query(query, queryArgs, orderBy, start, length);
					}

					List<AccountObject> retUsers = new ArrayList<AccountObject>();
					for (MetaObject metaObj : metaObjs) {
						AccountObject ao = _metaTableObj.get(metaObj._oid()); //a single account
						// System.out.println(ao._oid()+" ahwejakwekawej<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
						retUsers.add(ao);
					}

					fullUserArray = retUsers.toArray(new AccountObject[retUsers.size()]);
				} else {
					//do filtered query
					fullUserArray = _metaTableObj.getUsersByGroupAndRole(insideGroup_any, hasGroupRole_any);
				}

				if (fullUserArray == null || fullUserArray.length == 0) {
					return ret;
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
					List<Object> row = new ArrayList<Object>();
					for (String header : headers) {
						if (header.equalsIgnoreCase("names")) {
							if (ao != null) {
								Set<String> aoNames = ao.getLoginIDSet();

								if (sanitiseOutput) {
									aoNames.clear();
									for (String name : ao.getLoginIDSet()) {
										aoNames.add(RegexUtil.sanitiseCommonEscapeCharactersIntoAscii(name));
									}
								}

								if (aoNames != null) {
									List<String> aoNameList = new ArrayList<String>(aoNames);
									row.add(aoNameList);
								}
							}
						} else {
							Object rawVal = ao.get(header); //this used to be metaObj.get

							if (sanitiseOutput && rawVal instanceof String) {
								String stringVal = GenericConvert.toString(rawVal);
								row.add(stringVal);
							} else {
								row.add(rawVal);
							}

						}
					}
					ret.add(row);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("list_GET_and_POST_inner() ", e);
		}

		return ret;
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

	private ApiResponse check_parameters (String[] listToCheck, ApiRequest req, ApiResponse res) {
	  for (String paramName : listToCheck ) {
	    String value = "";
	    switch(paramName) {
	      case REQ_ACCOUNT_ID :
	        value = req.getString(paramName, "");
	        if ( value.isEmpty() )
	          res.put(RES_ERROR, ERROR_NO_USER_ID);
	        break;
	      case REQ_USERNAME :
	        value = req.getString(paramName, "");
	        if ( value.isEmpty() )
	          res.put(RES_ERROR, ERROR_NO_USERNAME);
	        break;
	      case REQ_PASSWORD :
	        value = req.getString(paramName, "");
	        if ( value.isEmpty() )
	          res.put(RES_ERROR, ERROR_NO_PASSWORD);
	        break;
	      case REQ_GROUP_ID :
	        value = req.getString(paramName, "");
	        if ( value.isEmpty() )
	          res.put(RES_ERROR, ERROR_NO_GROUP_ID);
	        break;
	      case REQ_USER_ID :
	        value = req.getString(paramName, "");
	        if ( value.isEmpty() )
	          res.put(RES_ERROR, ERROR_NO_USER_ID);
	        break;
	      case REQ_ROLE :
	        value = req.getString(paramName, "");
	        if ( value.isEmpty() )
	          res.put(RES_ERROR, ERROR_NO_ROLE);
	        break;
	      case REQ_GROUPNAME :
	        value = req.getString(paramName, "");
	        if ( value.isEmpty() )
	          res.put(RES_ERROR, ERROR_NO_GROUPNAME);
	        break;
	      case REQ_META :
	        Object metaObj = req.get(paramName);
	        if ( metaObj == null )
	          res.put(RES_ERROR, ERROR_NO_META);
	        break;
	      case REQ_OLD_PASSWORD:
	        value = req.getString(paramName, "");
	        if ( value.isEmpty() )
	          res.put(RES_ERROR, ERROR_NO_PASSWORD);
	        break;
	      case REQ_NEW_PASSWORD:
	        value = req.getString(paramName, "");
	        if ( value.isEmpty() )
	          res.put(RES_ERROR, ERROR_NO_NEW_PASSWORD);
	        break;
	      case REQ_REPEAT_PASSWORD:
	        value = req.getString(paramName, "");
	        if ( value.isEmpty() )
	          res.put(RES_ERROR, ERROR_NO_NEW_REPEAT_PASSWORD);
	        break;
	    }
			if ( res.get(RES_ERROR) != null )
				break;
	  }
	  return res;
	}
}
