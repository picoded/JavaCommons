package picoded.RESTBuilder.templates;

import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;

import com.amazonaws.services.ec2.model.AccountAttributeName;

import picoded.RESTBuilder.*;
import picoded.JStack.*;
import picoded.JStruct.*;
import picoded.servlet.*;
import picoded.conv.ConvertJSON;
import picoded.enums.HttpRequestType;

/// Account login template API
public class AccountLogin extends BasePage {
	
	/////////////////////////////////////////////
	//
	// Common error messages
	//
	/////////////////////////////////////////////
	
	public static final String MISSING_REQUEST_PAGE = "Unexpected Exception: Missing requestPage()";
	public static final String MISSING_LOGIN_SESSION = "Authentication Error: Missing login session";
	public static final String MISSING_PERMISSION = "Permission Error: Missing permission for request (generic)";
	public static final String MISSING_PERMISSION_GROUP = "Permission Error: Missing group admin rights required for request";
	
	/////////////////////////////////////////////
	//
	// Utility functions
	//
	/////////////////////////////////////////////
	
	///
	/// Standardised extension of RESTFunction, for login module, as a functional interface
	/// This is used in conjunction with prepareAuthenticatedREST, and is called only if user is logged in
	///
	/// The interface is called with the following parameters, this is isolated to allow more complex Authentication
	/// overwrites across all interfaces (except login) in the future
	///
	/// @params  {RESTRequest}          RESTREquest used
	/// @params  {Map<String,Object>}   Returned map expected
	/// @params  {BasePage}             BasePage object implmentation
	/// @params  {AccountTable}         Account table from basePage
	/// @params  {AccountObject}        Currently logged in user
	/// @params  {AccountObject}        Additional account object, based on utility function
	/// @params  {AccountObject}        Additional account object, based on utility function
	///
	/// @returns {Map<String,Object>}
	///
	@FunctionalInterface
	public interface loginREST {
		public abstract Map<String, Object> apply( //
			RESTRequest req, Map<String, Object> res, //
			BasePage basePageObj, AccountTable accountTableObj, //
			AccountObject currentUser, //
			AccountObject accObject_a, //
			AccountObject accObject_b);
	}
	
	private static MetaTableApiBuilder mtApi = null;
	
	///
	/// Standardised utility function use to authenticate the login request, and extend on for the respective function
	///
	/// @params  {RESTRequest}          RESTREquest used
	/// @params  {Map<String,Object>}   Returned map expected
	/// @params  {loginREST}            The function to call after authenticating
	///
	/// @returns {Map<String,Object>}
	///
	public static Map<String, Object> prepareAuthenticatedREST(RESTRequest req, Map<String, Object> res, loginREST call) {
		
		// Loads and check for request page
		//----------------------------------------
		if (req.requestPage() == null) {
			res.put("error", MISSING_REQUEST_PAGE);
			return res;
		}
		
		BasePage basePageObj = (BasePage) (req.requestPage());
		AccountTable accountTableObj = basePageObj.accountAuthTable();
		AccountObject currentUser = accountTableObj.getRequestUser(basePageObj.getHttpServletRequest());
		
		mtApi = new MetaTableApiBuilder(accountTableObj.accountMetaTable());
		
		// Checked for valid login
		//----------------------------------------
		if (currentUser == null) {
			res.put("error", MISSING_LOGIN_SESSION);
			return res;
		}
		
		// Run actual logic
		//----------------------------------------
		return call.apply(req, res, basePageObj, accountTableObj, currentUser, null, null);
	}
	
	///
	/// Standardised utility function use to authenticate the login request, and fetch the first wildcard argument as a group object (if possible)
	/// Also checks for adminstration rights, and can be set to only call the loginREST function if it has group admin rights
	///
	/// @params  {RESTRequest}          RESTREquest used
	/// @params  {Map<String,Object>}   Returned map expected
	/// @params  {boolean}              Apply the calling function ONLY if its has group adminstration rights (if true)
	/// @params  {loginREST}            The function to call after authenticating
	///
	/// @returns {Map<String,Object>}
	///
	public static Map<String, Object> fetchGroupObject_fromFirstWildcard_orCurrentUser(RESTRequest req,
		Map<String, Object> res, boolean callOnlyForGroupAdmin, loginREST call) {
		
		// Only runs function if logged in
		return prepareAuthenticatedREST(req, res, (reqObj, resMap, basePageObj, accountTableObj, currentUser, accObj_a,
			accObj_b) -> {
			
			// Default no data
			//----------------------------------------
			resMap.put("groupID", null);
			resMap.put("groupID_exist", false);
			resMap.put("groupID_valid", false);
			resMap.put("groupID_admin", false);
			resMap.put("groupID_names", null);
			
			// Group object
			AccountObject groupObj = null;
			
			// Attemptes to load the wildcard group object
			String[] wildcard = reqObj.wildCardNamespace();
			if (wildcard != null && wildcard.length >= 1) {
				resMap.put("groupID", wildcard[0]);
				groupObj = accountTableObj.getFromID(wildcard[0]);
			} else {
				groupObj = currentUser; // Defaults group object to current user
				resMap.put("groupID", currentUser._oid());
			}
			
			// Terminates if no group object (null)
			//----------------------------------------
			if (groupObj == null) {
				res.put("error", "GroupObj is null in fetchGroupObject_fromFirstWildcard_orCurrentUser");
				return resMap;
			}
			
			// Check group object
			//----------------------------------------
			resMap.put("groupID_exist", true);
			resMap.put("groupID_valid", groupObj.isGroup());
			
			// Terminates if group object is not a group
			//----------------------------------------
			if (groupObj.isGroup() == false) {
				res.put("error", "GroupObj is not group in fetchGroupObject_fromFirstWildcard_orCurrentUser");
				return resMap;
			}
			
			// Add in the group names
			resMap.put("groupID_names", new ArrayList<String>(groupObj.getNames()));
			boolean hasGroupadmin = false;
			
			// Check the group admin role
			if (currentUser.isSuperUser() || groupObj.getMemberRole(currentUser).equalsIgnoreCase("admin")) {
				hasGroupadmin = true;
				resMap.put("groupID_admin", true);
			}
			
			if (callOnlyForGroupAdmin && !hasGroupadmin) {
				resMap.put("error", MISSING_PERMISSION_GROUP);
				return resMap;
			}
			
			// Applies the call, only after fetching and validating the group object
			return call.apply(reqObj, resMap, basePageObj, accountTableObj, currentUser, groupObj, null);
		});
		
	}
	
	/////////////////////////////////////////////
	//
	// Login, Logout, and password API
	//
	/////////////////////////////////////////////
	
	///
	/// # login (GET)
	///
	/// The login GET function, this returns the current accountID and accountName
	///
	/// ## HTTP Request Parameters
	///
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name | Variable Type	  | Description                                                                   |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | No parameters options                                                                                               |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name | Variable Type      | Description                                                                   |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | isLogin        | boolean            | indicator if the session is logged in or not                                  |
	/// | accountID      | String             | account ID of the session                                                     |
	/// | accountNames   | String[]           | array of account names representing the session                               |
	/// | isSuperUser    | boolean            | indicator if user is superuser                                                |
	/// | isAdmin        | boolean            | indicator if user is admin in ANY of the groups user is in                    |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	/// | error          | String (Optional)  | Errors encounted if any                                                       |
	/// +----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public static RESTFunction login_GET = (req, res) -> {
		res.put("accountID", null);
		res.put("accountName", null);
		res.put("isLogin", false);
		
		if (req.requestPage() != null) {
			BasePage bp = (BasePage) (req.requestPage());
			AccountTable at = bp.accountAuthTable();
			
			AccountObject ao = at.getRequestUser(bp.getHttpServletRequest());
			if (ao != null) {
				res.put("accountID", ao._oid());
				
				String[] names = ao.getNames().toArray(new String[0]);
				res.put("accountNames", Arrays.asList((names == null) ? new String[] {} : names));
				
				res.put("isLogin", true);
			}
		} else {
			res.put("error", MISSING_REQUEST_PAGE);
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
	/// | accountName    | String (Optional)  | Either the accountName or the accountID is needed                             |
	/// | accountID      | String (Optional)  | Either the accountName or the accountID is needed                             |
	/// | accountPass    | String             | The account password used for login                                           |
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
		
		if (req.requestPage() != null) {
			BasePage bp = (BasePage) (req.requestPage());
			AccountObject ao = null;
			AccountTable at = bp.accountAuthTable();
			
			String tStr = req.getString("accountName");
			if (tStr != null) {
				ao = at.getFromName(tStr);
			}
			
			if (ao == null && (tStr = req.getString("accountID")) != null) {
				ao = at.getFromID(tStr);
			}
			
			// AccountName or AccountID is valid
			if (ao != null) {
				ao = at.loginAccount(bp.getHttpServletRequest(), bp.getHttpServletResponse(), ao,
					req.getString("accountPass", ""), false);
				
				// Login is valid
				if (ao != null) {
					res.put("accountID", ao._oid());
					String[] names = ao.getNames().toArray(new String[0]);
					res.put("accountNames", Arrays.asList((names == null) ? new String[] {} : names));
					res.put("isLogin", true);
				}
			}
		} else {
			res.put("error", MISSING_REQUEST_PAGE);
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
		
		if (req.requestPage() != null) {
			BasePage bp = (BasePage) (req.requestPage());
			AccountTable at = bp.accountAuthTable();
			
			at.logoutAccount(bp.getHttpServletRequest(), bp.getHttpServletResponse());
			res.put("logout", "true");
		} else {
			res.put("error", MISSING_REQUEST_PAGE);
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
	/// | success         | boolean            | indicator if the password was changed                                         |
	/// | accountID       | String             | account ID of the session                                                     |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | error           | String (Optional)  | Errors encounted if any                                                       |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public static RESTFunction password_POST = (req, res) -> {
		res.put("accountID", null);
		res.put("success", false);
		
		if (req.requestPage() != null) {
			BasePage bp = (BasePage) (req.requestPage());
			AccountTable at = bp.accountAuthTable();
			
			// Allow change password only if it is current user
			AccountObject ao = at.getRequestUser(bp.getHttpServletRequest());
			if (ao != null) {
				String accID = req.getString("accountID");
				if (ao._oid().equals(accID)) {
					if (ao.setPassword(req.getString("newPassword"), req.getString("oldPassword"))) {
						res.put("accountID", accID);
						res.put("success", true);
					} else {
						res.put("accountID", accID);
						res.put("error", "Original password is wrong");
					}
				} else if (ao.isSuperUser()) {
					AccountObject subUser = at.getFromID(accID);
					
					if (subUser != null) {
						res.put("accountID", accID);
						res.put("success", (subUser.setPassword(req.getString("newPassword")) == true));
					} else {
						res.put("error", "User does not exists: " + accID);
					}
					
				} else {
					res.put("error", "User does not have permission to edit accountID: " + accID);
				}
			}
		} else {
			res.put("error", MISSING_REQUEST_PAGE);
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
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | accountName     | String(optional)   | Account name of info to get. If blank, assume current user.                   |
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
	/// | groupRoles      | String[]           | array of account groups roles the user is in                                  |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public static RESTFunction infoByName_GET = (req, res) -> {
		if (req.requestPage() != null) {
			BasePage bp = (BasePage) (req.requestPage());
			AccountTable at = bp.accountAuthTable(); //at contains all the user data
			AccountObject ao = at.getRequestUser(bp.getHttpServletRequest()); //to check if logged in
			AccountObject account = null;
			
			if (ao != null) {
				String name = "";
				if (req.containsKey("accountName")) {
					name = req.getString("accountName");
				}
				;
				if (!name.isEmpty()) {
					account = at.getFromName(name);
				}
			} else {
				res.put("error", "Account object requested is null");
				
			}
			
			Map<String, Object> commonInfo = extractCommonInfoFromAccountObject(account);
			res.putAll(commonInfo);
		}
		return res;
	};
	
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
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | accountID       | String(optional)   | Account id of info to get. If blank, assume current user.                     |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	    | Description                                                                  |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | accountID       | String             | account ID used                                                               |
	/// | accountNames    | String[]           | array of account names representing the account                               |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | isSuperUser     | boolean            | indicates if the account is considered a superUser                            |
	/// | isAnyGroupAdmin | boolean            | indicates if the account is considered a superUser                            |
	/// | isGroup         | boolean            | indicates if the account is considered a group                                |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | groups          | object(Map)        | Groups object as a Map<String, List<Map<String, Object>>>                     |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public static RESTFunction infoByID_GET = (req, res) -> {
		if (req.requestPage() != null) {
			BasePage bp = (BasePage) (req.requestPage());
			AccountTable at = bp.accountAuthTable(); //at contains all the user data
			AccountObject ao = at.getRequestUser(bp.getHttpServletRequest()); //to check if logged in
			AccountObject account = null;
			
			if (ao != null) {
				String id = "";
				if (req.containsKey("accountID")) {
					id = req.getString("accountID");
				}
				;
				
				if (!id.isEmpty()) {
					account = at.getFromID(id);
				}
			} else {
				res.put("error", "Account object requested is null");
			}
			
			Map<String, Object> commonInfo = extractCommonInfoFromAccountObject(account);
			res.putAll(commonInfo);
		}
		
		return res;
	};
	
	private static Map<String, Object> extractCommonInfoFromAccountObject(AccountObject account) {
		Map<String, Object> commonInfo = new HashMap<String, Object>();
		
		commonInfo.put("accountID", null);
		commonInfo.put("accountNames", null);
		commonInfo.put("isSuperUser", null);
		commonInfo.put("isGroup", null);
		commonInfo.put("groups", null);
		commonInfo.put("isAnyGroupAdmin", false);
		
		if (account != null) {
			commonInfo.put("accountID", account._oid());
			
			Set<String> accNameSet = account.getNames();
			if (accNameSet != null) {
				String[] accNames = new String[accNameSet.size()];
				accNameSet.toArray(accNames);
				commonInfo.put("accountNames", accNames);
			} else {
				commonInfo.put("accountNames", null);
			}
			
			commonInfo.put("isSuperUser", account.isSuperUser());
			commonInfo.put("isGroup", account.isGroup());
			
			Map<String, List<Map<String, Object>>> groupMap = new HashMap<String, List<Map<String, Object>>>();
			AccountObject[] groups = account.getGroups();
			if (groups != null) {
				List<Map<String, Object>> groupList = new ArrayList<Map<String, Object>>();
				for (AccountObject group : groups) {
					Map<String, Object> newGroup = new HashMap<String, Object>();
					newGroup.put("accountID", group._oid());
					newGroup.put("names", group.getNames());
					String role = group.getMemberRole(account);
					newGroup.put("role", role);
					
					if (role == null) {
						role = "";
					}
					
					boolean isAdmin = role.equalsIgnoreCase("admin") || role.equalsIgnoreCase("superuser");
					newGroup.put("isAdmin", isAdmin);
					
					if (isAdmin) {
						commonInfo.replace("isAnyGroupAdmin", true);
					}
					
					groupList.add(newGroup);
				}
				groupMap.put("groups", groupList);
			}
			commonInfo.put("groups", groupMap);
		}
		
		return commonInfo;
	}
	
	/////////////////////////////////////////////
	//
	// List searching API
	//
	/////////////////////////////////////////////
	
	///
	/// # list (POST / GET) [Requires login]
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
		return prepareAuthenticatedREST(
			req,
			res,
			(reqObj, resMap, basePageObj, accountTableObj, currentUser, groupObj, accObj_b) -> {
				//cannot fall through
				int draw = req.getInt("draw");
				int start = req.getInt("start");
				int limit = req.getInt("length");
				
				String[] insideGroupAny = req.getStringArray("insideGroup_any");
				String groupStatus = req.getString("groupStatus");
				
				String orderByStr = req.getString("orderBy");
				if (orderByStr == null || orderByStr.isEmpty()) {
					orderByStr = "oID";
				}
				
				String[] headers = req.getStringArray("headers");
				
				if (headers == null || headers.length < 1) {
					headers = new String[] { "_oid", "names" };
				}
				
				String query = req.getString("query");
				String[] queryArgs = req.getStringArray("queryArgs");
				
				MetaTable mtObj = accountTableObj.accountMetaTable();
				
				//put back into response
				res.put("draw", draw);
				res.put("headers", headers);
				
				res.put("recordsTotal", accountTableObj.size());
				if (query != null && !query.isEmpty() && queryArgs != null && queryArgs.length > 0) {
					res.put("recordsFiltered", mtObj.queryCount(query, queryArgs));
				}
				
				List<List<Object>> data = null;
				try {
					data = list_GET_and_POST_inner(accountTableObj, draw, start, limit, headers, query, queryArgs,
						orderByStr, insideGroupAny, groupStatus);
					res.put("data", data);
				} catch (Exception e) {
					res.put("error", e.getMessage());
				}
				
				return res;
			});
	};
	
	private static List<List<Object>> list_GET_and_POST_inner(AccountTable _metaTableObj, int draw, int start,
		int length, String[] headers, String query, String[] queryArgs, String orderBy, String[] insideGroupAny,
		String groupStatus) throws RuntimeException {
		List<List<Object>> ret = new ArrayList<List<Object>>();
		
		if (_metaTableObj == null) {
			return ret;
		}
		
		try {
			if (headers != null && headers.length > 0) {
				MetaObject[] metaObjs = null;
				
				MetaTable accountMetaTable = _metaTableObj.accountMetaTable();
				
				if (accountMetaTable == null) {
					return ret;
				}
				
				if (query == null || query.isEmpty() || queryArgs == null || queryArgs.length == 0) {
					metaObjs = accountMetaTable.query(null, null, orderBy, start, length);
				} else {
					metaObjs = accountMetaTable.query(query, queryArgs, orderBy, start, length);
				}
				
				for (MetaObject metaObj : metaObjs) {
					List<Object> row = new ArrayList<Object>();
					
					AccountObject ao = _metaTableObj.getFromID(metaObj._oid());
					if (groupStatus != null) {
						if (groupStatus.equalsIgnoreCase("group")) {
							if (!ao.isGroup()) {
								continue;
							}
						} else if (groupStatus.equalsIgnoreCase("user")) {
							if (ao.isGroup()) {
								continue;
							}
						}
					}
					
					for (String header : headers) {
						boolean checkInsideGroupAny = (insideGroupAny != null && insideGroupAny.length > 0);
						boolean partOfGroup = false;
						if (checkInsideGroupAny) {
							String[] aoGroupIDs = ao.getGroups_id();
							if (aoGroupIDs != null && aoGroupIDs.length > 0) {
								for (String groupID : aoGroupIDs) {
									if (partOfGroup) {
										break;
									}
									
									if (ArrayUtils.contains(insideGroupAny, groupID)) {
										partOfGroup = true;
									}
								}
							}
						}
						
						if (checkInsideGroupAny && !partOfGroup) {
							continue;
						}
						
						if (header.equalsIgnoreCase("names")) {
							if (ao != null) {
								Set<String> aoNames = ao.getNames();
								if (aoNames != null) {
									List<String> aoNameList = new ArrayList<String>(aoNames);
									row.add(aoNameList);
								}
							}
						} else {
							row.add(metaObj.get(header));
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
	
	/////////////////////////////////////////////
	//
	// Meta details operations
	//
	/////////////////////////////////////////////
	
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
	/// | accountID_valid | boolean            | indicates if the account ID exists in the system                              |
	/// | accountID       | String             | account ID used                                                               |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | meta            | {Object}           | Meta object that represents this account                                      |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | error           | String (Optional)  | Errors encounted if any                                                       |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public static RESTFunction meta_GET = (req, res) -> {
		return prepareAuthenticatedREST(req, res, (reqObj, resMap, basePageObj, accountTableObj, currentUser, groupObj,
			accObj_b) -> {
			Map<String, Object> metaMap = mtApi.meta_GET.apply(req, res);
			
			AccountObject account = null;
			if (currentUser != null) {
				String id = "";
				if (req.containsKey("accountID")) {
					id = req.getString("accountID");
				}
				;
				
				if (!id.isEmpty()) {
					account = accountTableObj.getFromID(id);
				}
			} else {
				res.put("error", "Account object requested is null");
			}
			
			Map<String, Object> commonInfoMap = extractCommonInfoFromAccountObject(account);
			res.putAll(commonInfoMap);
			
			return res;
		});
	};
	
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
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | meta            | {Object}           | Meta object that represents this account                                      |
	/// | updateMode      | String (Optional)  | (Default) "delta" for only updating the given fields, or "full" for all       |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | accountID_valid | boolean            | indicates if the account ID exists in the system                              |
	/// | accountID       | String             | account ID used                                                               |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | updateMode      | String             | (Default) "delta" for only updating the given fields, or "full" for all       |
	/// | updateMeta      | {Object}           | The updated changes done                                                      |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | error           | String (Optional)  | Errors encounted if any                                                       |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public static RESTFunction meta_POST = (req, res) -> {
		// return mtApi.meta_POST.apply(req, res);
		return prepareAuthenticatedREST(req, res, (reqObj, resMap, basePageObj, accountTableObj, currentUser, groupObj,
			accObj_b) -> {
			return mtApi.meta_POST.apply(req, res);
		});
	};
	
	///
	/// # meta/${accountID} (DELETE) [Requires login]
	///
	/// Deletes the current oid object from the table
	///
	/// Note: if ${accountID} is blank, it assumes the current user
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | _oid            | String             | The internal object ID to delete                                              |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | accountID_valid | boolean            | indicates if the account ID exists in the system                              |
	/// | accountID       | String             | account ID used                                                               |
	/// | _oid            | String             | Returns oid of metaObject to delete                                           |
	/// | deleted         | boolean            | Returns true ONLY if the element was removed from the table                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | error           | String (Optional)  | Errors encounted if any                                                       |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public static RESTFunction meta_DELETE = (req, res) -> {
		// return mtApi.meta_POST.apply(req, res);
		return prepareAuthenticatedREST(req, res, (reqObj, resMap, basePageObj, accountTableObj, currentUser, groupObj,
			accObj_b) -> {
			return mtApi.meta_DELETE.apply(req, res);
		});
	};
	
	/////////////////////////////////////////////
	//
	// Group / Members management
	//
	/////////////////////////////////////////////
	
	///
	/// # members/list/${groupID} (GET) [Requires login]
	///
	/// Gets the info of group members of the respective group
	///
	/// Note: if ${groupID} is blank, it assumes the current user
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | draw            | int (optional)     | Draw counter echoed back, and used by the datatables.js server-side API       |
	/// | start           | int (optional)     | Default 0: Record start listing, 0-indexed                                    |
	/// | length          | int (optional)     | Default max: The number of records to return                                  |
	/// | groupID         | String (optional)  | group ID used in the request instead of inside URI                            | not implemented
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | headers         | String[](optional) | Default ["_oid", "name", "role"], the collumns to return                      |
	/// | If a header element contains an "account_" prefix, take the data from the curren
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | groupID         | String             | group ID used in the request                                                  |
	/// | groupID_exist   | boolean            | indicates if the account ID exists in the system                              |
	/// | groupID_valid   | boolean            | indicates if the account ID exists and is a group                             |
	/// | groupID_admin   | boolean            | indicates if the session has admin rights over the group                      |
	/// | groupID_names   | String[]           | the group various names, if ID is valid                                       |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | draw            | int (optional)     | Draw counter echoed back, and used by the datatables.js server-side API       | not returned
	/// | recordsTotal    | int                | Total amount of records. Before any search filter (But after base filters)    | not returned
	/// | recordsFilterd  | int                | Total amount of records. After all search filter                              | not returned
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | headers         | String[](optional) | The collumns headers returned                                                 |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | data            | array              | Array of row records                                                          |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | error           | String (Optional)  | Errors encounted if any                                                       |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public static RESTFunction members_list_GET = (req, res) -> {
		// Only runs function if logged in, and valid group object
		return fetchGroupObject_fromFirstWildcard_orCurrentUser(req, res, false, (reqObj, resMap, basePageObj,
			accountTableObj, currentUser, groupObj, accObj_b) -> {
			res.put("data", null);
			res.put("draw", null);
			res.put("headers", null);
			res.put("error", null);
			
			if (groupObj == null) {
				return resMap;
			}
			
			try {
				List<List<Object>> retList = new ArrayList<List<Object>>();
				
				String[] headers = reqObj.getStringArray("headers");
				
				if (headers == null || headers.length < 1) {
					headers = new String[] { "_oid", "name", "role" };
				}
				
				AccountObject[] members = groupObj.getMembersAccountObject();
				
				int listCounter = 0;
				for (AccountObject accObj : members) {
					MetaObject groupMemberMeta = groupObj.getMember(accObj);
					retList.add(new ArrayList<Object>());
					for (String header : headers) {
						if (header.equalsIgnoreCase("_oid")) {
							retList.get(listCounter).add(accObj._oid());
						} else if (header.equalsIgnoreCase("name")) {
							retList.get(listCounter).add(accObj.getNames());
						} else if (header.equalsIgnoreCase("role")) {
							String role = groupObj.getMemberRole(accObj);
							retList.get(listCounter).add(role);
						} else if (header.toLowerCase().startsWith("account_")) {
							String headerSuffix = header.substring("account_".length());
							Object rawObj = accObj.get(headerSuffix);
							if (rawObj != null) {
								retList.get(listCounter).add(rawObj);
							} else {
								retList.get(listCounter).add("");
							}
						} else if (header.toLowerCase().startsWith("group_")) {
							String headerSuffix = header.substring("group_".length());
							Object rawObj = groupObj.get(headerSuffix);
							if (rawObj != null) {
								retList.get(listCounter).add(rawObj);
							} else {
								retList.get(listCounter).add("");
							}
						} else {
							Object rawObj = groupMemberMeta.get(header);
							if (rawObj != null) {
								retList.get(listCounter).add(rawObj);
							} else {
								retList.get(listCounter).add("");
							}
						}
					}
					
					++listCounter;
					res.put("data", retList);
					res.put("draw", req.getInt("draw"));
					res.put("headers", headers);
				}
				
			} catch (Exception e) {
				res.put("error", e.getMessage());
			}
			
			return resMap;
		});
	};
	
	///
	/// # members/list/${groupID} (POST) [Requires login, and group admin rights]
	///
	/// Add/remove members to the group with their respective role. Requires the current user to be,
	/// admin of group, or super user.
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	      | Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | setMembers      | { Map } (optional)    | { memberID : role } : Array of member ID/roles to set                      |
	/// | delMembers      | String[]   (optional) | [ memberID, ... ] : Array of member ID's to delete                         |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	      | Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | groupID         | String                | group ID used in the request                                               |
	/// | groupID_exist   | boolean               | indicates if the account ID exists in the system                           |
	/// | groupID_valid   | boolean               | indicates if the account ID exists and is a group                          |
	/// | groupID_admin   | boolean               | indicates if the session has admin rights                                  |
	/// | groupID_names   | String[]              | the group various names, if ID is valid                                    |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | setMembers      | { Map } (optional)    | { memberID : role } : Array of member ID/roles set                         |
	/// | delMembers      | String[]   (optional) | [ memberID, ... ] : Array of member ID's deleted                           |
	/// | success         | boolean               | indicator if logout is successful or not                                   |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | error           | String (Optional)     | Errors encounted if any                                                    |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	@SuppressWarnings("unchecked")
	public static RESTFunction members_list_POST = (req, res) -> {
		// Only runs function if logged in, and valid group object, with admin rights
		return fetchGroupObject_fromFirstWildcard_orCurrentUser(req, res, true, (reqObj, resMap, basePageObj,
			accountTableObj, currentUser, groupObj, accObj_b) -> {
			
			res.put("setMembers", null);
			res.put("delMembers", null);
			res.put("error", null);
			
			if (groupObj == null) {
				res.put("error", "GroupObj is null in fetchGroupObject_fromFirstWildcard_orCurrentUser");
				return resMap;
			}
			
			try {
				//do deletes first
			List<String> successfulDeletes = null;
			List<Object> delMember = null;
			Object delMemberRaw = req.get("delMembers");
			if (delMemberRaw != null) {
				delMember = ConvertJSON.toList((String) delMemberRaw);
				successfulDeletes = new ArrayList<String>();
			}
			
			if (delMember != null) {
				for (Object strRaw : delMember) {
					String str = (String) strRaw;
					if (!str.isEmpty()) {
						AccountObject memberToDelete = accountTableObj.getFromID(str);
						if (memberToDelete != null) {
							groupObj.removeMember(memberToDelete);
						}
					}
				}
				groupObj.saveDelta();
				res.put("delMembers", delMember);
			}
			
			//then do additions
			List<String> successfulAdds = null;
			Map<String, Object> setMemberMap = null;
			Object setMemberMapRaw = req.get("setMembers");
			if (setMemberMapRaw != null) {
				setMemberMap = ConvertJSON.toMap((String) setMemberMapRaw);
				
				successfulAdds = new ArrayList<String>();
			}
			
			if (setMemberMap != null) {
				for (Entry<String, Object> setMember : setMemberMap.entrySet()) {
					AccountObject newMember = accountTableObj.getFromID(setMember.getKey());
					if (newMember != null) {
						//groupObj.addMember(newMember, (String)setMember.getValue()).saveAll();
			groupObj.setMember(newMember, (String) setMember.getValue()).saveDelta();
			successfulAdds.add(setMember.getKey());
		}
	}
	
	res.put("setMembers", successfulAdds);
}

} catch (Exception e) {
res.put("error", e.getMessage());
}

return resMap;
}	  );
	};
	
	/////////////////////////////////////////////
	//
	// Group members meta management
	//
	/////////////////////////////////////////////
	
	///
	/// # members/meta/${groupID}/${accountID} (GET) [Requires login]
	///
	/// Gets and return the current user info with relation to the group
	///
	/// Note: if ${accountID} is blank, it assumes the current user
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	      | Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | No parameters options                                                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	      | Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | groupID         | String                | group ID used in the request                                               |
	/// | groupID_exist   | boolean               | indicates if the account ID exists in the system                           |
	/// | groupID_valid   | boolean               | indicates if the account ID exists and is a group                          |
	/// | groupID_admin   | boolean               | indicates if the session has admin rights                                  |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | accountID       | String                | account ID used                                                            |
	/// | accountID_valid | boolean               | indicates if the account ID exists in the systen and group                 |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | meta            | {Object}              | Meta object that represents this account                                   |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | error           | String (Optional)     | Errors encounted if any                                                    |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	public static RESTFunction members_meta_GET = (req, res) -> {
		// Only runs function if logged in, and valid group object
		return fetchGroupObject_fromFirstWildcard_orCurrentUser(req, res, false, (reqObj, resMap, basePageObj,
			accountTableObj, currentUser, groupObj, accObj_b) -> {
			res.put("accountID", null);
			res.put("accountID_valid", false);
			res.put("meta", null);
			res.put("error", null);
			
			if (groupObj == null) {
				res.put("error", "Group object is null");
				return resMap;
			}
			
			try {
				
				String accountOID = req.getString("accountID");
				if (accountOID == null || accountOID.isEmpty()) {
					if (currentUser != null) {
						accountOID = currentUser._oid(); //if accountOID is null, try get oid from currentUser object
		} else {
			res.put("error", "Unable to obtain oid");
		}
	}
	
	res.put("accountID", accountOID);
	
	AccountObject user = accountTableObj.getFromID(accountOID);
	MetaObject groupUserInfo = null;
	
	if (user != null) {
		groupUserInfo = groupObj.getMember(user);
	} else {
		res.put("error", "User account not found in table");
		return resMap;
	}
	
	if (groupUserInfo != null) {
		res.put("meta", groupUserInfo);
		res.put("accountID_valid", true);
	} else {
		res.put("error", "User info not found in group");
		return resMap;
	}
	
} catch (Exception e) {
	res.put("error", e.getMessage());
}

return resMap;
}	  );
	};
	
	///
	/// # members/meta/${groupID}/${accountID} (POST) [Requires login]
	///
	/// Updates the accountID meta info. Requires the current user to be either the group itself,
	/// admin of group, or super user.
	///
	/// Note: if ${accountID} is blank, it assumes the current user
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	      | Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | meta            | {Object} Map<S, O>    | Meta object that represents this account                                   |
	/// | updateMode      | String (Optional)     | (Default) "delta" for only updating the given fields, or "full" for all    |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	      | Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | groupID         | String                | group ID used in the request                                               |
	/// | groupID_exist   | boolean               | indicates if the account ID exists in the system                           |
	/// | groupID_valid   | boolean               | indicates if the account ID exists and is a group                          |
	/// | groupID_admin   | boolean               | indicates if the session has admin rights                                  |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | accountID       | String                | account ID used                                                            |
	/// | accountID_valid | boolean               | indicates if the account ID exists in the systen and group                 |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | updateMode      | String                | (Default) "delta" for only updating the given fields, or "full" for all    |
	/// | updateMeta      | {Object}              | The updated changes done                                                   |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | error           | String (Optional)     | Errors encounted if any                                                    |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	@SuppressWarnings("unchecked")
	public static RESTFunction members_meta_POST = (req, res) -> {
		// Only runs function if logged in, and valid group object
		return fetchGroupObject_fromFirstWildcard_orCurrentUser(req, res, false, (reqObj, resMap, basePageObj,
			accountTableObj, currentUser, groupObj, accObj_b) -> {
			
			res.put("accountID", null);
			res.put("accountID_valid", false);
			res.put("updateMode", null);
			res.put("updateMeta", null);
			res.put("error", null);
			
			if (groupObj == null) {
				res.put("error", "Group object is null");
				return resMap;
			}
			
			try {
				
				String accountOID = req.getString("accountID");
				if (accountOID == null || accountOID.isEmpty()) {
					if (currentUser != null) {
						accountOID = currentUser._oid(); //if accountOID is null, try get oid from currentUser object
		} else {
			res.put("error", "Unable to obtain oid");
		}
	}
	
	res.put("accountID", accountOID);
	AccountObject user = accountTableObj.getFromID(accountOID);
	MetaObject groupUserInfo = null;
	
	if (user != null) {
		groupUserInfo = groupObj.getMember(user);
	} else {
		res.put("error", "User account not found in table");
		return resMap;
	}
	
	if (groupUserInfo == null) {
		res.put("error", "User info not found in group");
		return resMap;
	}
	
	res.put("accountID_valid", true);
	
	Object metaObjRaw = req.get("meta");
	if (metaObjRaw == null) {
		res.put("error", "Object to update with not found");
		return resMap;
	}
	
	Map<String, Object> metaObj = ConvertJSON.toMap((String) metaObjRaw);
	
	res.put("updateMeta", metaObj);
	groupUserInfo.putAll(metaObj);
	
	String updateMode = req.getString("updateMode", "delta");
	res.put("updateMode", updateMode);
	if (updateMode.equalsIgnoreCase("full")) {
		groupUserInfo.saveAll();
	} else {
		groupUserInfo.saveDelta();
	}
	
} catch (Exception e) {
	res.put("error", e.getMessage());
}

return resMap;
}	  );
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
	public static RESTFunction new_account_POST = (req, res) -> {
		// Only runs function if logged in, and valid group object
		return prepareAuthenticatedREST(req, res, (reqObj, resMap, basePageObj, accountTableObj, currentUser, groupObj,
			accObj_b) -> {
			
			boolean isGroup = req.getBoolean("isGroup", false);
			
			String userName = req.getString("username");
			if (userName == null || userName.isEmpty()) {
				res.put("error", "No username was supplied");
				return resMap;
			}
			
			String password = req.getString("password");
			if (!isGroup && (password == null || password.isEmpty())) {
				res.put("error", "No password was supplied");
				return resMap;
			}
			
			Object metaObjRaw = req.get("meta");
			Map<String, Object> givenMetaObj = new HashMap<String, Object>();
			if (metaObjRaw instanceof String) {
				String jsonMetaString = (String) metaObjRaw;
				
				if (jsonMetaString != null && !jsonMetaString.isEmpty()) {
					givenMetaObj = ConvertJSON.toMap(jsonMetaString);
				}
			}
			
			AccountObject newAccount = accountTableObj.newObject(userName);
			if (newAccount != null) {
				if (isGroup) {
					newAccount.setGroupStatus(true);
				}
				
				newAccount.setPassword(password);
				newAccount.putAll(givenMetaObj);
				newAccount.saveAll();
				
				res.put("meta", newAccount);
				res.put("accountID", newAccount._oid());
			} else {
				res.put("error", "Object already exists in account Table");
			}
			
			return resMap;
		});
	};
	
	/// # delete [POST]
	///
	/// Deletes an account
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	      | Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | accountID       | String (optional)     | Account ID to delete from accounts table                                   |
	/// | accountName     | String (optional      | Account Name to delete from accounts table                                 |
	/// | Be warned, passing both will result in an error unless they match to the same account                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	      | Description                                                                |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | accountID       | String                | account ID used                                                            |
	/// | accountName     | String                | account Name used                                                          |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	/// | error           | String (Optional)     | Errors encounted if any                                                    |
	/// +-----------------+-----------------------+----------------------------------------------------------------------------+
	///
	public static RESTFunction delete_account_POST = (req, res) -> {
		// Only runs function if logged in, and valid group object
		return prepareAuthenticatedREST(req, res, (reqObj, resMap, basePageObj, accountTableObj, currentUser, groupObj,
			accObj_b) -> {
			
			String accountID = req.getString("accountID");
			String accountName = req.getString("accountName");
			
			if ((accountID == null || accountID.isEmpty()) && (accountName == null || accountName.isEmpty())) {
				res.put("error", "Both accountID and accountName were not supplied");
				return resMap;
			}
			
			res.put("accountID", accountID);
			res.put("accountName", accountName);
			
			try {
				AccountObject accObj = null;
				if (!accountID.isEmpty()) {
					accObj = accountTableObj.getFromID(accountID);
					
					if (!accountName.isEmpty()) {
						if (!accObj.getNames().contains(accountName)) {
							res.put("error",
								"accountName given does not match the names found for this account - delete unsuccessful");
							return resMap;
						}
					}
					
					accountTableObj.removeFromID(accountID);
					
				} else if (!accountName.isEmpty()) {
					accObj = accountTableObj.getFromName(accountName);
					
					if (!accountID.isEmpty()) {
						if (!accObj._oid().equals(accountID)) {
							res.put("error", "accountID given does not match the accounts _oid - delete unsuccessful");
							return resMap;
						}
					}
					
					accountTableObj.removeFromName(accountName);
				}
			} catch (Exception e) {
				res.put("error", "Remove from table is currently not implemented. Stare at Eugene until this is fixed.");
			}
			
			return resMap;
		});
	};
	
	//-------------------------------------------------------------------------------------------------------------------------
	//
	// Work in progress (not final) end
	//
	//-------------------------------------------------------------------------------------------------------------------------
	
	/////////////////////////////////////////////
	//
	// RestBuilder template builder
	//
	/////////////////////////////////////////////
	
	///
	/// Takes the restbuilder and the account table object and implements its respective default API
	///
	public static RESTBuilder setupRESTBuilder(RESTBuilder rb, AccountTable at, String setPrefix) {
		
		rb.getNamespace(setPrefix + "login").put(HttpRequestType.GET, login_GET);
		rb.getNamespace(setPrefix + "login").put(HttpRequestType.POST, login_POST);
		rb.getNamespace(setPrefix + "logout").put(HttpRequestType.GET, logout_GET);
		rb.getNamespace(setPrefix + "password").put(HttpRequestType.POST, password_POST);
		
		rb.getNamespace(setPrefix + "info/name").put(HttpRequestType.GET, infoByName_GET);
		rb.getNamespace(setPrefix + "info/name/*").put(HttpRequestType.GET, infoByName_GET);
		
		rb.getNamespace(setPrefix + "info/id").put(HttpRequestType.GET, infoByID_GET);
		rb.getNamespace(setPrefix + "info/id/*").put(HttpRequestType.GET, infoByID_GET);
		
		rb.getNamespace(setPrefix + "members/list/*").put(HttpRequestType.GET, members_list_GET);
		rb.getNamespace(setPrefix + "members/list").put(HttpRequestType.GET, members_list_GET);
		
		rb.getNamespace(setPrefix + "members/list/*").put(HttpRequestType.POST, members_list_POST);
		rb.getNamespace(setPrefix + "members/list").put(HttpRequestType.POST, members_list_POST);
		
		rb.getNamespace(setPrefix + "members/meta/*").put(HttpRequestType.GET, members_meta_GET);
		rb.getNamespace(setPrefix + "members/meta/*/*").put(HttpRequestType.GET, members_meta_GET);
		
		rb.getNamespace(setPrefix + "members/meta/*").put(HttpRequestType.POST, members_meta_POST);
		rb.getNamespace(setPrefix + "members/meta/*/*").put(HttpRequestType.POST, members_meta_POST);
		
		rb.getNamespace(setPrefix + "new").put(HttpRequestType.POST, new_account_POST);
		rb.getNamespace(setPrefix + "delete").put(HttpRequestType.POST, delete_account_POST);
		
		//MetaTableApiBuilder Fall through
		rb.getNamespace(setPrefix + "meta/list").put(HttpRequestType.GET, list_GET_and_POST);
		rb.getNamespace(setPrefix + "meta/list").put(HttpRequestType.POST, list_GET_and_POST);
		
		rb.getNamespace(setPrefix + "meta/*").put(HttpRequestType.GET, meta_GET);
		rb.getNamespace(setPrefix + "meta/*").put(HttpRequestType.POST, meta_POST);
		rb.getNamespace(setPrefix + "meta").put(HttpRequestType.GET, meta_GET);
		rb.getNamespace(setPrefix + "meta").put(HttpRequestType.POST, meta_POST);
		
		rb.getNamespace(setPrefix + "delete").put(HttpRequestType.DELETE, meta_DELETE);
		rb.getNamespace(setPrefix + "delete/*").put(HttpRequestType.DELETE, meta_DELETE);
		//end fall through segment
		
		return rb;
	}
	
	public static RESTBuilder setupRESTBuilder(RESTBuilder rb, AccountTable at) {
		return setupRESTBuilder(rb, at, _apiSetPrefix_prefix);
	}
	
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
		setupRESTBuilder(this.restBuilder(), this.accountAuthTable(), _apiSetPrefix);
	}
	
	/// Process the request, not the authentication layer
	public boolean doJSON(Map<String, Object> outputData, Map<String, Object> templateData) throws Exception {
		return restBuilder().servletCall(_apiSetPrefix, this, outputData);
	}
	
}
