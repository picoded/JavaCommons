package picoded.RESTBuilder.templates;

import java.util.*;

import picoded.RESTBuilder.*;
import picoded.JStack.*;
import picoded.JStruct.*;
import picoded.servlet.*;

import picoded.enums.HttpRequestType;

/// Account login template API
public class AccountLogin extends BasePage {
	
	/////////////////////////////////////////////
	//
	// Servlet constructor and setup
	//
	/////////////////////////////////////////////
	
	/// Default api set prefix
	protected static String _apiSetPrefix_prefix = "account.";
	
	/// Internal prefix set for the API
	protected String _apiSetPrefix = _apiSetPrefix_prefix;
	
	/// The prefix for the api
	public String apiSetPrefix() {
		return _apiSetPrefix;
	}
		
	/// The prefix for the api
	public void setApiSetPrefix(String api) {
		_apiSetPrefix = api;
	}
	
	/// Flags as JSON request
	public boolean isJsonRequest() {
		return true;
	}
	
	/// Does the default setup
	public void doSetup() throws Exception {
		super.doSetup();
		setupRESTBuilder( this.restBuilder(), this.accountAuthTable(), _apiSetPrefix );
	}
	
	/// Process the request, not the authentication layer
	public boolean doJSON(Map<String,Object> outputData, Map<String,Object> templateData) throws Exception {
		return restBuilder().servletCall( _apiSetPrefix, this, outputData );
	}
	
	/////////////////////////////////////////////
	//
	// Login, Logout, and password API
	//
	/////////////////////////////////////////////
	
	///
	/// # login (GET)
	///
	/// The login GET function, this returns the current accountID and accountNAME
	///
	/// ## HTTP Request Parameters
	///
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name | Variable Type	   | Description                                                                   |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | No parameters options                                                                                               |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// 
	/// ## JSON Object Output Parameters
	///
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name | Variable Type	   | Description                                                                   |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | isLogin        | boolean            | indicator if the session is logged in or not                                  |
	/// | accountID      | String             | account ID of the session                                                     |
	/// | accountNAME    | String[]           | array of account names representing the session                               |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | error          | String (Optional)  | Errors encounted if any                                                       |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public static RESTFunction login_GET = (req, res) -> {
		res.put("accountID", null);
		res.put("accountNAME", null);
		res.put("isLogin", false);
		
		if(req.requestPage() != null) {
			BasePage bp = (BasePage)(req.requestPage());
			AccountTable at = bp.accountAuthTable();
			
			AccountObject ao = at.getRequestUser( bp.getHttpServletRequest() );
			if( ao != null ) {
				res.put("accountID", ao._oid());
				
				String[] names = ao.getNames().toArray(new String[0]);
				res.put("accountNAME", Arrays.asList( (names == null)? new String[] {} : names) );
				
				res.put("isLogin", true);
			}
		} else {
			res.put("error", "Missing requestPage()");
		}
		return res;
	};
	
	///
	/// # login (POST)
	///
	/// The login POST function, used to login a new session
	///
	/// ## HTTP Request Parameters
	///
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name | Variable Type	   | Description                                                                   |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | accountNAME    | String (Optional)  | Either the accountName or the accountID is needed                             |
	/// | accountID      | String (Optional)  | Either the accountName or the accountID is needed                             |
	/// | accountPASS    | String             | The account password used for login                                           |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// 
	/// ## JSON Object Output Parameters
	///
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name | Variable Type	   | Description                                                                   |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | isLogin        | boolean            | indicator if the session is logged in or not                                  |
	/// | accountID      | String             | account ID of the session                                                     |
	/// | accountNames   | String[]           | array of account names representing the session                               |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | error          | String (Optional)  | Errors encounted if any                                                       |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public static RESTFunction login_POST = (req, res) -> {
		res.put("accountID", null);
		res.put("accountNames", null);
		res.put("isLogin", false);
		
		if( req.requestPage() != null ) {
			BasePage bp = (BasePage)(req.requestPage());
			AccountObject ao = null;
			AccountTable at = bp.accountAuthTable();
			
			String tStr = req.getString("accountNAME");
			if( tStr != null ) {
				ao = at.getFromName(tStr);
			}
			
			if( ao == null && (tStr = req.getString("accountID")) != null ) {
				ao = at.getFromID(tStr);
			}
			
			// AccountName or AccountID is valid
			if( ao != null ) {
				ao = at.loginAccount( bp.getHttpServletRequest(), bp.getHttpServletResponse(), ao, req.getString("accountPASS", ""), false );
				
				// Login is valid
				if( ao != null ) {
					res.put("accountID", ao._oid());
					String[] names = ao.getNames().toArray(new String[0]);
					res.put("accountNames", Arrays.asList( (names == null)? new String[] {} : names) );
					res.put("isLogin", true);
				}
			}
		} else {
			res.put("error", "Missing requestPage()");
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
	/// | Parameter Name | Variable Type	   | Description                                                                   |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | No parameters options                                                                                               |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// 
	/// ## JSON Object Output Parameters
	///
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name | Variable Type	   | Description                                                                   |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | logout         | boolean            | indicator if logout is successful or not                                      |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | error          | String (Optional)  | Errors encounted if any                                                       |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public static RESTFunction logout_GET = (req, res) -> {
		res.put("logout", "false");
		
		if(req.requestPage() != null) {
			BasePage bp = (BasePage)(req.requestPage());
			AccountTable at = bp.accountAuthTable();
			
			at.logoutAccount( bp.getHttpServletRequest(), bp.getHttpServletResponse() );
			res.put("logout", "true");
		} else {
			res.put("error", "Missing requestPage()");
		}
		return res;
	};
	
	///
	/// # password (POST) [Requires login to relevent user, or superuser]
	///
	/// The password update POST function.
	///
	/// Note that this requires either the current session to be the same account, or a superUser
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	    | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | accountID       | String             | accountID to change the password for                                          |
	/// | newPassword     | String             | The account password to change to                                             |
	/// | oldPassword     | String             | The original password (optional for superuser, required for own user)         |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// 
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	    | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | passwordChanged | boolean            | indicator if the password was changed                                         |
	/// | accountID       | String             | account ID of the session                                                     |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | error           | String (Optional)  | Errors encounted if any                                                       |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public static RESTFunction password_POST = (req, res) -> {
		res.put("accountID", null);
		res.put("passwordChanged", false);
		
		if(req.requestPage() != null) {
			BasePage bp = (BasePage)(req.requestPage());
			AccountTable at = bp.accountAuthTable();
			
			// Allow change password only if it is current user
			AccountObject ao = at.getRequestUser( bp.getHttpServletRequest() );
			if( ao != null ) {
				String accID = req.getString("accountID");
				if( ao._oid().equals(accID) ) {
					if( ao.setPassword( req.getString("newPassword"), req.getString("oldPassword") ) ) {
						res.put("accountID", accID);
						res.put("passwordChanged", true);
					} else {
						res.put("accountID", accID);
						res.put("error", "Original password is wrong");
					}
				} else if( ao.isSuperUser() ) {
					AccountObject subUser = at.getFromID(accID);
					
					if( subUser != null ) {
						subUser.setPassword( req.getString("newPassword") );
						
						res.put("accountID", accID);
						res.put("passwordChanged", true);
					} else {
						res.put("error", "User does not exists: "+accID);
					}
					
				} else {
					res.put("error", "User does not have permission to edit accountID: "+accID);
				}
			}
		} else {
			res.put("error", "Missing requestPage()");
		}
		return res;
	};
	
	/////////////////////////////////////////////
	//
	// Basic info API
	//
	/////////////////////////////////////////////
	
	///
	/// # info/name/${accountName} (GET) [Requires login]
	///
	/// Gets and return the accountID for the given accountName
	///
	/// Note: if ${accountName} is blank, it assumes the current user
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	    | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | No parameters options                                                                                                |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// 
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	    | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | accountID       | String             | account ID used                                                               |
	/// | accountNames    | String[]           | array of account names representing the account                               |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | isSuperUser     | boolean            | indicates if the account is considered a superUser                            |
	/// | isGroup         | boolean            | indicates if the account is considered a group                                |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | groupIDs        | String[]           | array of account ID groups the user is in                                     |
	/// | groupNames      | String[][]         | array of account Names groups the user is in                                  |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public static RESTFunction infoByName_GET = (req, res) -> {
		return res;
	}
	
	///
	/// # info/id/${accountID} (GET) [Requires login]
	///
	/// Gets and return the accountID for the given accountName
	/// 
	/// Note: if ${accountID} is blank, it assumes the current user
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	    | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | No parameters options                                                                                                |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// 
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	    | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | accountID       | String             | account ID used                                                               |
	/// | accountNames    | String[]           | array of account names representing the account                               |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | isSuperUser     | boolean            | indicates if the account is considered a superUser                            |
	/// | isGroup         | boolean            | indicates if the account is considered a group                                |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | groupIDs        | String[]           | array of account ID groups the user is in                                     |
	/// | groupNames      | String[][]         | array of account Names groups the user is in                                  |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public static RESTFunction infoByID_GET = (req, res) -> {
		return res;
	}
	
	///
	/// # list (POST / GET) [Requires login]
	///
	/// Lists the users according to the search criteria 
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
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | groupStatus     | String (optional)  | Default "both", either "user" or "group". Used to lmit the result set         |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
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
	public static RESTFunction list_GET_and_POST = (req, res) -> {
		return res;
	}
	
	///
	/// # meta/${accountID} (GET) [Requires login]
	///
	/// Gets and return the current user info
	/// 
	/// Note: if ${accountID} is blank, it assumes the current user
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	    | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | No parameters options                                                                                                |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// 
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	    | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | accountExists   | boolean            | indicates if the account ID exists in the system                              |
	/// | accountID       | String             | account ID used                                                               |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | meta            | {Object}           | Meta object that represents this account                                      |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | error           | String (Optional)  | Errors encounted if any                                                       |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public static RESTFunction meta_GET = (req, res) -> {
		return res;
	}
	
	///
	/// # meta/${accountID} (POST) [Requires login]
	///
	/// Updates the accountID meta info, requires either the current user or SuperUser
	/// 
	/// Note: if ${accountID} is blank, it assumes the current user
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	    | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | meta            | {Object}           | Meta object that represents this account                                      |
	/// | updateMode      | String (Optional)  | (Default) "delta" for only updating the given fields, or "full" for all       |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// 
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	    | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | accountExists   | boolean            | indicates if the account ID exists in the system                              |
	/// | accountID       | String             | account ID used                                                               |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | meta            | {Object}           | Meta object that represents this account                                      |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | error           | String (Optional)  | Errors encounted if any                                                       |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public static RESTFunction meta_POST = (req, res) -> {
		return res;
	}
	
	
	
	//-------------------------------------------------------------------------------------------------------------------------
	//
	// Work in progress (not final) start
	//
	//-------------------------------------------------------------------------------------------------------------------------
	
	
	///
	/// # info/${accountID} (GET)
	///
	/// Gets and return the current user info
	/// 
	/// Note: if ${accountID} is blank, it assumes the current user
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	    | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | No parameters options                                                                                                |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// 
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	    | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | accountExists   | boolean            | indicates if the account ID exists in the system                              |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | accountID       | String             | account ID of the session                                                     |
	/// | accountNames    | String[]           | array of account names representing the session                               |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | meta            | {Object}           | Meta object that represents this account                                      |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | isSuperUser     | boolean            | indicates if the account is considered a superUser                            |
	/// | isGroup         | boolean            | indicates if the account is considered a group                                |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | groupIDs        | String[]           | array of account ID groups the user is in                                     |
	/// | groupNames      | String[][]         | array of account Names groups the user is in                                  |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | memberIDs       | String[]           | array of account ID groups that are memebrs of this account                   |
	/// | memberNames     | String[][]         | array of account Names groups that are memebrs of this account                |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | error           | String (Optional)  | Errors encounted if any                                                       |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public static RESTFunction info_GET = (req, res) -> {
		return res;
	}
	
	//-------------------------------------------------------------------------------------------------------------------------
	//
	// Work in progress (not final) end
	//
	//-------------------------------------------------------------------------------------------------------------------------
	
	///
	/// Takes the restbuilder and the account table object and implements its respective default API
	///
	public static RESTBuilder setupRESTBuilder(RESTBuilder rb, AccountTable at, String setPrefix ) {
		
		rb.getNamespace( setPrefix + "login" ).put( HttpRequestType.GET, login_GET );
		rb.getNamespace( setPrefix + "login" ).put( HttpRequestType.POST, login_POST );
		rb.getNamespace( setPrefix + "logout" ).put( HttpRequestType.GET, logout_GET );
		rb.getNamespace( setPrefix + "password" ).put( HttpRequestType.POST, password_POST );
		
		return rb;
	}
	
	public static RESTBuilder setupRESTBuilder(RESTBuilder rb, AccountTable at) {
		return setupRESTBuilder(rb, at, _apiSetPrefix_prefix);
	}
	
}
