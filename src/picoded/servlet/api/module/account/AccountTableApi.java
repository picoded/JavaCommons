package picoded.servlet.api.module.account;

import java.util.*;

import picoded.servlet.api.*;
import picoded.servlet.api.module.ApiModule;
import picoded.dstack.module.account.*;
import picoded.dstack.*;
import picoded.conv.ConvertJSON;

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
		res.put(Account_Strings.RES_RETURN, ao != null);
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
		res.put(Account_Strings.RES_ACCOUNT_ID, null);
		res.put(Account_Strings.RES_LOGIN_ID_LIST, null);
		res.put(Account_Strings.RES_IS_LOGIN, false);
		res.put(Account_Strings.RES_REMEMBER_ME, false);

		// Get the login parameters
		String accountID = req.getString(Account_Strings.REQ_ACCOUNT_ID, null);
		String loginID = req.getString(Account_Strings.REQ_USERNAME, null);
		String loginPass = req.getString(Account_Strings.REQ_PASSWORD, null);
		boolean rememberMe = req.getBoolean(Account_Strings.REQ_REMEMBER_ME, false);

		// Missing paremeter error checks
		if ( loginPass == null ) {
			res.put(Account_Strings.RES_ERROR, Account_Strings.ERROR_NO_LOGIN_PASSWORD);
			return res;
		}
		if ( accountID == null && loginID == null ) {
			res.put(Account_Strings.RES_ERROR, Account_Strings.ERROR_NO_LOGIN_ID);
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
				res.put(Account_Strings.RES_ERROR, "Unable to login, user locked out for "+timeAllowed+" seconds.");
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
			res.put(Account_Strings.RES_IS_LOGIN, true);
			res.put(Account_Strings.RES_REMEMBER_ME, rememberMe);
			res.put(Account_Strings.RES_ACCOUNT_ID, ao._oid());

			// loginID, as a list - as set does not gurantee sorting, a sort is done for the list for alphanumeric
			List<String> loginIDList = new ArrayList<String>(ao.getLoginIDSet());
			Collections.sort(loginIDList);

			// Return the loginIDList
			res.put(Account_Strings.RES_LOGIN_ID_LIST, loginIDList);
			ao = table.getRequestUser(req.getHttpServletRequest(), null);
		} else {
			// Legitimate user but wrong password
			if ( ao != null ){
				ao.addDelay(ao);
			}
			res.put(Account_Strings.RES_ERROR, Account_Strings.ERROR_FAIL_LOGIN);
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
		res.put(Account_Strings.RES_RETURN, false);

		if (req.getHttpServletRequest() != null) {
			res.put(Account_Strings.RES_RETURN, table.logoutAccount(req.getHttpServletRequest(), res.getHttpServletResponse()));
		} else {
			res.put(Account_Strings.RES_ERROR, MISSING_REQUEST_PAGE);
		}
		return res;
	};

	//-------------------------------------------------------------------------------------------------------------------------
	//
	// Work in progress (not final) start
	//
	//-------------------------------------------------------------------------------------------------------------------------
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
		boolean isGroup = req.getBoolean(Account_Strings.REQ_IS_GROUP, false);

		String userName = req.getString(Account_Strings.REQ_USERNAME);
		if (userName == null || userName.isEmpty()) {
			res.put(Account_Strings.RES_ERROR, Account_Strings.ERROR_NO_USERNAME);
			return res;
		}
		String password = req.getString(Account_Strings.REQ_PASSWORD);
		if (!isGroup && (password == null || password.isEmpty())) {
			res.put(Account_Strings.RES_ERROR, Account_Strings.ERROR_NO_PASSWORD);
			return res;
		}

		Object metaObjRaw = req.get(Account_Strings.REQ_META);
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
				boolean defaultRoles = req.getBoolean(Account_Strings.REQ_DEFAULT_ROLES, true);
				List<String> list = req.getList(Account_Strings.REQ_ROLE, null);
				if ( defaultRoles || list == null ) {
					newAccount.setMembershipRoles(table.defaultMembershipRoles());
				} else {
					newAccount.setMembershipRoles(list);
				}
			}
			givenMetaObj.put(Account_Strings.PROPERTIES_EMAIL, userName);
			newAccount.setPassword(password);
			newAccount.putAll(givenMetaObj);
			newAccount.saveAll();

			res.put(Account_Strings.RES_META, newAccount);
			res.put(Account_Strings.RES_ACCOUNT_ID, newAccount._oid());
		} else {
			AccountObject existingAccount = table.getFromLoginID(userName);
			if (existingAccount != null) {
				res.put(Account_Strings.RES_ACCOUNT_ID, existingAccount._oid());
			} else {
				res.put(Account_Strings.RES_ACCOUNT_ID, null);
			}
			res.put(Account_Strings.RES_ERROR, "Object already exists in account Table");
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
	/// | Parameter Name  | Variable Type	      | Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | groupname				| String								| name of the group to retrieve
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
		String groupName = req.getString(Account_Strings.REQ_GROUPNAME);
		if ( groupName == null ) {
			res.put(Account_Strings.RES_ERROR, Account_Strings.ERROR_NO_GROUPNAME);
			return res;
		}
		AccountObject group = table.getFromLoginID(groupName);
		if ( group == null ) {
			res.put(Account_Strings.RES_ERROR, Account_Strings.ERROR_NO_GROUP);
			return res;
		}
		List<String> membershipRoles = group.group_membershipRoles().getList(Account_Strings.PROPERTIES_ROLE, "[]");
		res.put(Account_Strings.RES_LIST, membershipRoles);
		// Return result
		return res;
	};

	/// # addMemberToGroup
	///
	/// Add an existing user to an existing group
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type					| Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | username				| String								| name of the user to add																										 |
	/// | groupname				| String								| name of the group to add to																								 |
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
	protected ApiFunction addMemberToGroup = (req, res) -> {
		// Checks all input are given before proceeding
		Map<String, String> result = validateGroupParameters(req);
		if ( result.get(Account_Strings.RES_ERROR) != null ) {
			res.put(Account_Strings.RES_ERROR, result.get(Account_Strings.RES_ERROR));
			return res;
		}
		String role = req.getString(Account_Strings.REQ_ROLE);
		if ( role == null ) {
			res.put(Account_Strings.RES_ERROR, Account_Strings.ERROR_NO_ROLE);
			return res;
		}
		AccountObject group = table.getFromLoginID(result.get(Account_Strings.REQ_GROUPNAME));
		if ( group == null ) {
			res.put(Account_Strings.RES_ERROR, Account_Strings.ERROR_NO_GROUP);
			return res;
		}
		AccountObject userToAdd = table.getFromLoginID(result.get(Account_Strings.REQ_USERNAME));
		if ( userToAdd == null ) {
			res.put(Account_Strings.RES_ERROR, Account_Strings.ERROR_NO_USER);
			return res;
		}
		MetaObject groupResult = group.addMember(userToAdd, role);
		if ( groupResult == null ) {
			res.put(Account_Strings.RES_ERROR, "User is already in group or role is not found.");
		}else{
			res.put(Account_Strings.RES_META, groupResult);
		}
		return res;
	};

	/// # removeMemberFromGroup
	///
	/// Add an existing user to an existing group
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type					| Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | username				| String								| name of the user to remove																								 |
	/// | groupname				| String								| name of the group to add to																								 |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type					| Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | meta						| {Object}              | Information of the remove user																						 |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | ERROR           | String (Optional)     | Errors encountered if any                                                  |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	protected ApiFunction removeMemberFromGroup = (req, res) -> {
		Map<String, String> result = validateGroupParameters(req);
		if ( result.get(Account_Strings.RES_ERROR) != null ) {
			res.put(Account_Strings.RES_ERROR, result.get(Account_Strings.RES_ERROR));
			return res;
		}
		AccountObject group = table.getFromLoginID(result.get(Account_Strings.REQ_GROUPNAME));
		if ( group == null ) {
			res.put(Account_Strings.RES_ERROR, Account_Strings.ERROR_NO_GROUP);
			return res;
		}
		AccountObject userToRemove = table.getFromLoginID(result.get(Account_Strings.REQ_USERNAME));
		if ( userToRemove == null ) {
			res.put(Account_Strings.RES_ERROR, Account_Strings.ERROR_NO_USER);
			return res;
		}
		MetaObject groupResult = group.removeMember(userToRemove);
	  if ( groupResult == null ) {
	    res.put(Account_Strings.RES_ERROR, "User is not in group.");
	  } else if ( groupResult.getInt(Account_Strings.PROPERTIES_IS_GROUP) == 0 && groupResult._oid() == group._oid()){
	    res.put(Account_Strings.RES_ERROR, "This is not a group.");
	  } else {
	    res.put(Account_Strings.RES_META, groupResult);
	  }
		return res;
	};

	/// # getMemberMetaFromGroup
	///
	/// Retrieve the meta information of an existing user from an existing group
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type					| Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | username				| String								| name of the user to retrieve																							 |
	/// | groupname				| String								| name of the group to retrieve from																				 |
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
	protected ApiFunction getMemberMetaFromGroup = (req, res) -> {
		// Checks all input are given before proceeding
		Map<String, String> result = validateGroupParameters(req);
		if ( result.get(Account_Strings.RES_ERROR) != null ) {
			res.put(Account_Strings.RES_ERROR, result.get(Account_Strings.RES_ERROR));
			return res;
		}
		String role = req.getString(Account_Strings.REQ_ROLE);
		AccountObject group = table.getFromLoginID(result.get(Account_Strings.REQ_GROUPNAME));
		if ( group == null ) {
			res.put(Account_Strings.RES_ERROR, Account_Strings.ERROR_NO_GROUP);
			return res;
		}
		AccountObject userToAdd = table.getFromLoginID(result.get(Account_Strings.REQ_USERNAME));
		if ( userToAdd == null ) {
			res.put(Account_Strings.RES_ERROR, Account_Strings.ERROR_NO_USER);
			return res;
		}
		MetaObject groupResult = (role != null) ? group.getMember(userToAdd, role) : group.getMember(userToAdd);
		if ( groupResult == null ) {
			res.put(Account_Strings.RES_ERROR, "User is not in group or not in specified role.");
		}else{
			res.put(Account_Strings.RES_META, groupResult);
		}
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
	/// | username				| String								| name of the user to retrieve																							 |
	/// | groupname				| String								| name of the group to retrieve from																				 |
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
		// Checks all input are given before proceeding
		Map<String, String> result = validateGroupParameters(req);
		if ( result.get(Account_Strings.RES_ERROR) != null ) {
			res.put(Account_Strings.RES_ERROR, result.get(Account_Strings.RES_ERROR));
			return res;
		}
		AccountObject group = table.getFromLoginID(result.get(Account_Strings.REQ_GROUPNAME));
		if ( group == null ) {
			res.put(Account_Strings.RES_ERROR, Account_Strings.ERROR_NO_GROUP);
			return res;
		}
		AccountObject userToAdd = table.getFromLoginID(result.get(Account_Strings.REQ_USERNAME));
		if ( userToAdd == null ) {
			res.put(Account_Strings.RES_ERROR, Account_Strings.ERROR_NO_USER);
			return res;
		}
		String role = group.getMemberRole(userToAdd);
		if ( role == null ) {
			res.put(Account_Strings.RES_ERROR, "No role for user is found.");
		} else {
			res.put(Account_Strings.RES_SINGLE_RETURN_VALUE, role);
		}
		return res;
	};

	/// # addNewMembershipRole
	///
	/// Retrieve the role of an existing user from an existing group
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type					| Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | role						| String								| name of the role to add																										 |
	/// | groupname				| String								| name of the group to add to																								 |
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
	protected ApiFunction addNewMembershipRole = (req, res) -> {
		String groupName = req.getString(Account_Strings.REQ_GROUPNAME);
		if ( groupName == null ) {
			res.put(Account_Strings.RES_ERROR, Account_Strings.ERROR_NO_GROUPNAME);
			return res;
		}

		String role = req.getString(Account_Strings.REQ_ROLE);
		if ( role == null ) {
			res.put(Account_Strings.RES_ERROR, Account_Strings.ERROR_NO_ROLE);
			return res;
		}

		AccountObject group = table.getFromLoginID(groupName);
		if ( group == null ) {
			res.put(Account_Strings.RES_ERROR, Account_Strings.ERROR_NO_GROUP);
			return res;
		}

		MetaObject groupResult = group.addNewMembershipRole(role);
		res.put(Account_Strings.RES_META, groupResult);
		return res;
	};


	/// # removeMembershipRoleFromGroup
	///
	/// Retrieve the role of an existing user from an existing group
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
	protected ApiFunction removeMembershipRoleFromGroup = (req, res) -> {
		String groupName = req.getString(Account_Strings.REQ_GROUPNAME);
		if ( groupName == null ) {
			res.put(Account_Strings.RES_ERROR, Account_Strings.ERROR_NO_GROUPNAME);
			return res;
		}

		String role = req.getString(Account_Strings.REQ_ROLE);
		if ( role == null ) {
			res.put(Account_Strings.RES_ERROR, Account_Strings.ERROR_NO_ROLE);
			return res;
		}

		AccountObject group = table.getFromLoginID(groupName);
		if ( group == null ) {
			res.put(Account_Strings.RES_ERROR, Account_Strings.ERROR_NO_GROUP);
			return res;
		}

		MetaObject groupResult = group.removeMembershipRole(role);
		if ( groupResult == null ) {
			res.put(Account_Strings.RES_ERROR, "No such role is found.");
		}else{
			res.put(Account_Strings.RES_META, groupResult);
		}

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
		String accountName = req.getString(Account_Strings.REQ_ACCOUNT_NAME, null);
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
		builder.put(path+"isLogin", isLogin); // Tested
		builder.put(path+"login", login); // Tested
		builder.put(path+"lockTime", lockTime); // Tested
		builder.put(path+"logout", logout); // Tested
		builder.put(path+"new", new_account); // Tested

		//Group functionalities
		builder.put(path+"groupRoles", groupRoles); // Tested
		builder.put(path+"addMember", addMemberToGroup); // Tested
		builder.put(path+"removeMember", removeMemberFromGroup); // Tested
		builder.put(path+"getMemberMeta", getMemberMetaFromGroup); // Tested
		builder.put(path+"getMemberRole", getMemberRoleFromGroup); // Tested

		builder.put(path+"addMembershipRole", addNewMembershipRole); // Tested
		builder.put(path+"removeMembershipRole", removeMembershipRoleFromGroup); // Tested
	}


	/// Private Methods
	private Map<String, String> validateGroupParameters(ApiRequest req) {
		Map<String, String> res = new HashMap<String, String>();
		String userNameToAdd = req.getString(Account_Strings.REQ_USERNAME);
		if ( userNameToAdd == null ) {
			res.put(Account_Strings.RES_ERROR, Account_Strings.ERROR_NO_USERNAME);
			return res;
		}
		String groupName = req.getString(Account_Strings.REQ_GROUPNAME);
		if ( groupName == null ) {
			res.put(Account_Strings.RES_ERROR, Account_Strings.ERROR_NO_GROUPNAME);
			return res;
		}
		res.put(Account_Strings.REQ_USERNAME, userNameToAdd);
		res.put(Account_Strings.REQ_GROUPNAME, groupName);
		return res;
	}
}
