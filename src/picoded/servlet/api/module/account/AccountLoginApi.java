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
		return new SystemSetupInterface[] {};
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	//   Utility function used
	//
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Utility function used to extract out common information from an account object.
	 *
	 * This covers the following fields
	 *
	 * + accountID
	 * + accountNameList
	 * + isSuperUser
	 * + isGroup
	 *
	 * @param  account   to extract these common information from
	 * @param  resultMap to populate, can be null
	 *
	 * @return resultMap containing the common information
	 */
	private static Map<String, Object> extractCommonInfoFromAccountObject(AccountObject account,
		Map<String, Object> resultMap) {
		// Null safety
		if (resultMap == null) {
			resultMap = null;
		}
		
		// Resets the result
		resultMap.put(ACCOUNT_ID, null);
		resultMap.put(LOGIN_NAME_LIST, null);
		resultMap.put("isSuperUser", false);
		resultMap.put("isGroup", false);
		
		// Account object null safety
		if (account != null) {
			
			// Get account ID
			resultMap.put(ACCOUNT_ID, account._oid());
			
			// Get the login name set
			Set<String> loginNameSet = account.getLoginNameSet();
			if (loginNameSet != null) {
				// loginName, as a list - as set does not gurantee sorting, a sort is done for the list for alphanumeric
				List<String> loginNameList = new ArrayList<String>(loginNameSet);
				Collections.sort(loginNameList);
				resultMap.put(LOGIN_NAME_LIST, loginNameList);
			}
			
			// Get isSuperUser, isGroup
			resultMap.put("isSuperUser", account.isSuperUser());
			resultMap.put("isGroup", account.isGroup());
		}
		
		// Return result map
		return resultMap;
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	//   Basic login, logout, and account creation
	//
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * # $prefix/login
	 *
	 * Ateempts to login the user if parameters are given.
	 * If no request parameter is given, returns the current user logged in.
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
		
		// Setup default (failed) login
		res.put(ACCOUNT_ID, null);
		res.put(LOGIN_NAME_LIST, null);
		res.put(RESULT, false);
		res.put("rememberMe", false);
		
		// Get the login parameters
		String accountID = req.getString(ACCOUNT_ID, null);
		String loginName = req.getString(LOGIN_NAME, null);
		String loginPass = req.getString("password", null);
		boolean rememberMe = req.getBoolean("rememberMe", false);
		
		//---------------------------------------------------
		// Request to get info of user
		//---------------------------------------------------
		if (accountID == null && loginName == null) {
			// Get current user if any, http response is given to allow any update of the cookies if needed
			AccountObject currentUser = table.getRequestUser(req.getHttpServletRequest(),
				res.getHttpServletResponse());
			
			// Missing current user, terminate with appropriate info
			if (currentUser == null) {
				res.put(INFO, INFO_MISSING_LOGIN);
				return res;
			}
			
			// User is found, return its result
			res.put(RESULT, true);
			res.put(REMEMBER_ME, rememberMe);
			
			// Return common information for the user
			extractCommonInfoFromAccountObject(currentUser, res);
			return res;
		}
		
		//---------------------------------------------------
		// Missing parameters for login checks
		//---------------------------------------------------
		
		// Log in Process
		// Missing parameter error checks
		if (loginPass == null) {
			res.put(ERROR, "Missing login password");
			return res;
		}
		
		// Fetch the respective account object
		AccountObject ao = null;
		if (accountID != null) {
			ao = table.get(accountID);
		} else if (loginName != null) {
			ao = table.getFromLoginName(loginName);
		} else {
			res.put(ERROR, "Missing login name");
			return res;
		}
		
		// Check if account has been locked out
		if (ao != null) {
			int timeAllowed = ao.getLockTimeLeft();
			if (timeAllowed != 0) {
				res.put(INFO, "Unable to login, account is locked for " + timeAllowed + " seconds");
				return res;
			}
		}
		
		// Attempts to log in user
		// This returns NULL if login fails
		AccountObject loginAO = table.loginAccount(req.getHttpServletRequest(),
			res.getHttpServletResponse(), ao, loginPass, rememberMe);
		
		// No such user exists or wrong password
		// as table.loginAccount returns NULL
		if (loginAO == null) {
			// if user exists
			if (ao != null) {
				ao.incrementFailedLoginAttempts();
			}
			res.put(INFO, "Invalid username or password");
			return res;
		}
		
		//
		// From here downards, it is assured that login occured succesfully
		//
		
		// Reset any failed login attempts, that may failred previously
		ao.resetLoginThrottle();
		
		// Return succesful result
		res.put(RESULT, true);
		res.put(REMEMBER_ME, rememberMe);
		res.put(ACCOUNT_ID, ao._oid());
		
		// Extract Common Info from user account object
		extractCommonInfoFromAccountObject(ao, res);
		
		// Returning the result
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
	 * Does the actual setup for the API
	 * Given the API Builder, and the namespace prefix
	 *
	 * @param  api ApiBuilder to add the required functions
	 * @param  prefixPath to assume
	 * @param  config configuration map
	 **/
	protected void apiSetup(ApiBuilder api, String prefixPath,
		GenericConvertMap<String, Object> config) {
		// Basic new account, login, and logout
		api.put(prefixPath + API_ACCOUNT_LOGIN, login); // Tested
		api.put(prefixPath + API_ACCOUNT_LOGOUT, logout); // Tested
	}
}
