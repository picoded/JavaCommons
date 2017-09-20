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
 * Account Login API builder
 **/
public class AccountLoginApi extends CommonApiModule {

	/**
	 * The AccountTable reference
	 **/
	protected AccountTable table = null;

	/**
	 * Static ERROR MESSAGES
	 **/
	public static final String MISSING_REQUEST_PAGE = "Unexpected Exception: Missing requestPage()";

	/**
	 * Setup the account login API
	 * 
	 * @param  AccountTable to use
	 **/
	public AccountLoginApi(AccountTable inTable) {
		table = inTable;
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
	 * | accountID      | String (Optional)  | Either the loginName or the accountID is needed                               |
	 * | loginName      | String (Optional)  | Either the loginName or the accountID is needed                               |
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

		// // Setup default (failed) login
		// res.put(ACCOUNT_ID, null);
		// res.put(LOGIN_NAME_LIST, null);
		// res.put(RESULT, false);
		// res.put("rememberMe", false);

		// // Get the login parameters
		// String accountID = req.getString(ACCOUNT_ID, null);
		// String loginName = req.getString(LOGIN_NAME, null);
		// String loginPass = req.getString("password", null);
		// boolean rememberMe = req.getBoolean("rememberMe", false);

		// // Request to get info of user
		// if (accountID == null && loginID == null) {
		// 	// Get current user if any, http response is given to allow any update of the cookies if needed
		// 	AccountObject currentUser = table.getRequestUser(req.getHttpServletRequest(), res.getHttpServletResponse());
		// 	if (currentUser == null) {
		// 		res.put(ERROR, ERROR_NO_USER);
		// 		return res;
		// 	}
		// 	res.put(RESULT, true);
		// 	res.put(REMEMBER_ME, rememberMe);
		// 	Map<String, Object> commonInfo = extractCommonInfoFromAccountObject(currentUser, true);
		// 	res.putAll(commonInfo);
		// 	return res;
		// }

		// // Log in Process
		// // Missing parameter error checks
		// if (loginPass == null) {
		// 	res.put(ERROR, ERROR_NO_LOGIN_PASSWORD);
		// 	return res;
		// }

		// // Fetch the respective account object
		// AccountObject ao = null;
		// if (accountID != null) {
		// 	ao = table.get(accountID);
		// } else if (loginID != null) {
		// 	ao = table.getFromLoginName(loginID);
		// }

		// // Check if account has been locked out
		// if (ao != null) {
		// 	int timeAllowed = ao.getNextLoginTimeAllowed(ao._oid());
		// 	if (timeAllowed != 0) {
		// 		res.put(ERROR, "Unable to login, user locked out for " + timeAllowed + " seconds.");
		// 		return res;
		// 	}
		// }

		// // Continue only with an account object and does not have a lockout timing
		// if (ao != null && ao.validatePassword(loginPass)) {
		// 	// Validate and login, with password
		// 	ao = table.loginAccount(req.getHttpServletRequest(), res.getHttpServletResponse(), ao,
		// 		loginPass, rememberMe);
		// 	// Reset any failed login attempts
		// 	ao.resetLoginThrottle(loginID);
		// 	// If ao is not null, it assumes a valid login
		// 	res.put(RESULT, true);
		// 	res.put(REMEMBER_ME, rememberMe);
		// 	res.put(ACCOUNT_ID, ao._oid());
		// 	// Extract Common Info from user account object
		// 	Map<String, Object> commonInfo = extractCommonInfoFromAccountObject(ao, true);
		// 	res.putAll(commonInfo);

		// 	// loginID, as a list - as set does not gurantee sorting, a sort is done for the list for alphanumeric
		// 	List<String> loginIDList = new ArrayList<String>(ao.getLoginNameSet());
		// 	Collections.sort(loginIDList);

		// 	// Return the loginIDList
		// 	res.put(LOGIN_NAME_LIST, loginIDList);
		// } else {
		// 	// Legitimate user but wrong password
		// 	if (ao != null) {
		// 		ao.addDelay(ao);
		// 	}
		// 	res.put(ERROR, ERROR_FAIL_LOGIN);
		// }

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
		// res.put(RESULT, false);

		// if (req.getHttpServletRequest() != null) {
		// 	res.put(RESULT,
		// 		table.logoutAccount(req.getHttpServletRequest(), res.getHttpServletResponse()));
		// } else {
		// 	res.put(ERROR, MISSING_REQUEST_PAGE);
		// }
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
	protected void apiSetup(ApiBuilder api, String prefixPath, GenericConvertMap<String,Object> config) {

		// Basic new account, login, and logout
		api.put(prefixPath + API_ACCOUNT_LOGIN, login); // Tested
		api.put(prefixPath + API_ACCOUNT_LOGOUT, logout); // Tested

	}
}