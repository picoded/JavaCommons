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
		res.put("return", ao != null);
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
		res.put("accountID", null);
		res.put("loginIDList", null);
		res.put("isLogin", false);
		res.put("rememberMe", false);

		// Get the login parameters
		String accountID = req.getString("accountID", null);
		String loginID = req.getString("loginID", null);
		String loginPass = req.getString("loginPass", null);
		boolean rememberMe = req.getBoolean("rememberMe", false);

		// Missing paremeter error checks
		if( loginPass == null ) {
			res.put("ERROR", "Missing login password");
			return res;
		}
		if( accountID == null && loginID == null ) {
			res.put("ERROR", "Missing login ID");
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
				res.put("ERROR", "Unable to login, user locked out for "+timeAllowed+" seconds.");
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
			res.put("isLogin", true);
			res.put("rememberMe", rememberMe);
			res.put("accountID", ao._oid());

			// loginID, as a list - as set does not gurantee sorting, a sort is done for the list for alphanumeric
			List<String> loginIDList = new ArrayList<String>(ao.getLoginIDSet());
			Collections.sort(loginIDList);

			// Return the loginIDList
			res.put("loginIDList", loginIDList);
			ao = table.getRequestUser(req.getHttpServletRequest(), null);
		} else {
			// Legitimate user but wrong password
			if( ao != null ){
				ao.addDelay(ao);
			}
			res.put("ERROR", "Failed login (wrong password or invalid user?)");
		}

		return res;
	};

	protected ApiFunction membershipRoles = (req,res) -> {
		List<String> membershipRoles = table.membershipRoles();
		res.put("list", ConvertJSON.fromList(membershipRoles));
		// Return result
		return res;
	};

	protected ApiFunction lockTime = (req, res) -> {
		String accountName = req.getString("accountName", null);
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
		builder.put(path+"isLogin", isLogin);
		builder.put(path+"login", login);
		builder.put(path+"lockTime", lockTime);

		//Group functionalities
		builder.put(path+"membershipRoles", membershipRoles);
	}
}
