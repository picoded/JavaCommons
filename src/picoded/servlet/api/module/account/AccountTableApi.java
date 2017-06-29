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
	/// | error          | String (Optional)  | Errors encounted if any                                                       |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	///
	protected ApiFunction isLogin = (req,res) -> {
		// Get the account object (if any)
		AccountObject ao = table.getRequestUser(req.getHttpServletRequest(), null);
		// Return if a valid login object was found
		res.put(Naming_Strings.RES_RETURN, ao != null);
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
	/// | error          | String (Optional)  | Errors encounted if any                                                       |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	///
	protected ApiFunction login = (req,res) -> {

		// Setup default (failed) login
		res.put(Naming_Strings.RES_ACCOUNT_ID, null);
		res.put(Naming_Strings.RES_LOGIN_ID_LIST, null);
		res.put(Naming_Strings.RES_IS_LOGIN, false);
		res.put(Naming_Strings.RES_REMEMBER_ME, false);

		// Get the login parameters
		String accountID = req.getString(Naming_Strings.REQ_ACCOUNT_ID, null);
		String loginID = req.getString(Naming_Strings.REQ_USERNAME, null);
		String loginPass = req.getString(Naming_Strings.REQ_PASSWORD, null);
		boolean rememberMe = req.getBoolean(Naming_Strings.REQ_REMEMBER_ME, false);

		// Missing paremeter error checks
		if( loginPass == null ) {
			res.put(Naming_Strings.RES_ERROR, "Missing login password");
			return res;
		}
		if( accountID == null && loginID == null ) {
			res.put(Naming_Strings.RES_ERROR, "Missing login ID");
			return res;
		}

		// Fetch the respective account object
		AccountObject ao = null;
		if( accountID != null ) {
			ao = table.get(accountID);
		} else if( loginID != null ) {
			ao = table.getFromLoginID(loginID);
		}

		// Check if account has been locked out
		if( ao != null ){
			int timeAllowed = ao.getNextLoginTimeAllowed(ao._oid());
			if(timeAllowed != 0){
				res.put(Naming_Strings.RES_ERROR, "Unable to login, user locked out for "+timeAllowed+" seconds.");
				return res;
			}
		}

		// Continue only with an account object and does not have a lockout timing
		if( ao != null && ao.validatePassword(loginPass) ) {
			// Validate and login, with password
			ao = table.loginAccount( req.getHttpServletRequest(), res.getHttpServletResponse(), ao, loginPass, rememberMe);
			// Reset any failed login attempts
			ao.resetLoginThrottle(loginID);
			// If ao is not null, it assumes a valid login
			res.put(Naming_Strings.RES_IS_LOGIN, true);
			res.put(Naming_Strings.RES_REMEMBER_ME, rememberMe);
			res.put(Naming_Strings.RES_ACCOUNT_ID, ao._oid());

			// loginID, as a list - as set does not gurantee sorting, a sort is done for the list for alphanumeric
			List<String> loginIDList = new ArrayList<String>(ao.getLoginIDSet());
			Collections.sort(loginIDList);

			// Return the loginIDList
			res.put(Naming_Strings.RES_LOGIN_ID_LIST, loginIDList);
			ao = table.getRequestUser(req.getHttpServletRequest(), null);
		} else {
			// Legitimate user but wrong password
			if( ao != null ){
				ao.addDelay(ao);
			}
			res.put(Naming_Strings.RES_ERROR, "Failed login (wrong password or invalid user?)");
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
	/// | error          | String (Optional)  | Errors encounted if any                                                       |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	///
	protected ApiFunction logout = (req, res) -> {
		res.put(Naming_Strings.RES_RETURN, false);

		if (req.getHttpServletRequest() != null) {
			res.put(Naming_Strings.RES_RETURN, table.logoutAccount(req.getHttpServletRequest(), res.getHttpServletResponse()));
		} else {
			res.put(Naming_Strings.RES_ERROR, MISSING_REQUEST_PAGE);
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
	/// | error           | String (Optional)     | Errors encounted if any                                                    |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	public ApiFunction new_account = (req, res) -> {
		// Only runs function if logged in, and valid group object
		boolean isGroup = req.getBoolean(Naming_Strings.REQ_IS_GROUP, false);

		String userName = req.getString(Naming_Strings.REQ_USERNAME);
		if (userName == null || userName.isEmpty()) {
			res.put(Naming_Strings.RES_ERROR, "No username was supplied");
			return res;
		}
		String password = req.getString(Naming_Strings.REQ_PASSWORD);
		if (!isGroup && (password == null || password.isEmpty())) {
			res.put(Naming_Strings.RES_ERROR, "No password was supplied");
			return res;
		}

		Object metaObjRaw = req.get(Naming_Strings.REQ_META);
		Map<String, Object> givenMetaObj = new HashMap<String, Object>();
		if (metaObjRaw instanceof String) {
			String jsonMetaString = (String) metaObjRaw;
			if (jsonMetaString != null && !jsonMetaString.isEmpty()) {
				givenMetaObj = ConvertJSON.toMap(jsonMetaString);
			}
		}

		AccountObject newAccount = table.newObject(userName);
		if (newAccount != null) {
			if (isGroup) {
				newAccount.setGroupStatus(true);
			}
			givenMetaObj.put(Naming_Strings.RES_EMAIL, userName);
			newAccount.setPassword(password);
			newAccount.putAll(givenMetaObj);
			newAccount.saveAll();
			res.put(Naming_Strings.RES_META, newAccount);
			res.put(Naming_Strings.RES_ACCOUNT_ID, newAccount._oid());
		} else {
			AccountObject existingAccount = table.getFromLoginID(userName);
			if (existingAccount != null) {
				res.put(Naming_Strings.RES_ACCOUNT_ID, existingAccount._oid());
			} else {
				res.put(Naming_Strings.RES_ACCOUNT_ID, null);
			}
			res.put(Naming_Strings.RES_ERROR, "Object already exists in account Table");
		}
		return res;
	};


	protected ApiFunction membershipRoles = (req,res) -> {
		List<String> membershipRoles = table.membershipRoles();
		res.put(Naming_Strings.RES_LIST, ConvertJSON.fromList(membershipRoles));
		// Return result
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
	/// | accountName    | String             | Errors encounted if any                                                       |
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
		String accountName = req.getString(Naming_Strings.REQ_ACCOUNT_NAME, null);
		if(accountName != null){
			AccountObject ao = table.getFromLoginID(accountName);
			if(ao != null){
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
		builder.put(path+Naming_Strings.RES_IS_LOGIN, isLogin);
		builder.put(path+"login", login);
		builder.put(path+"lockTime", lockTime);
		builder.put(path+"logout", logout);
		builder.put(path+"new", new_account);

		//Group functionalities
		builder.put(path+"membershipRoles", membershipRoles);
		// builder.put(path+"hasMe");
	}
}
