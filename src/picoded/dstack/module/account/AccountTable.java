package picoded.dstack.module.account;

import java.util.*;

import picoded.dstack.*;
import picoded.dstack.module.*;
import picoded.conv.*;
import picoded.struct.*;

///
/// The account class is considered a hybrid class of both the user, group management class.
/// Where both users, and groups are considered the "same". Hence their distinction is normally detirmined
/// by one of the meta field values.
///
/// The intention here, is to facilitate complex hierachy creations rapidly. Especially against changing specs.
///
/// Any refences to the persona game, is completely coincidental !
/// (PS: old joke, the original name for this class was PersonaTable)
///
public class AccountTable extends ModuleStructure implements UnsupportedDefaultMap<String, AccountObject> {

	///////////////////////////////////////////////////////////////////////////
	//
	// Underlying data structures
	//
	///////////////////////////////////////////////////////////////////////////

	//
	// Login authentication
	//
	//--------------------------------------------------------------------------

	/// Provides a key value pair mapping of the account login ID to AccountID (GUID)
	///
	/// KeyValueMap<uniqueLoginID,AccountID>
	///
	/// login ID are unique, and are usually usernames or emails
	/// AccountID's are not unique, as a single AccountID can have multiple "names"
	protected KeyValueMap accountLoginIdMap = null; //to delete from

	/// Stores the account authentication hash, used for password based authentication
	///
	/// KeyValueMap<uniqueLoginID,passwordHash>
	protected KeyValueMap accountAuthMap = null; //to delete from

	//
	// Login session
	//
	//--------------------------------------------------------------------------

	/// Stores the account session key,
	/// account linkage, and activity
	///
	/// KeyValueMap<sessionID, info-including-accountID>
	protected KeyValueMap sessionInfoMap = null;

	/// Stores the account token key,
	/// to login information / meta data map
	///
	/// KeyValueMap<tokenID, sessionID>
	protected KeyValueMap sessionTokenMap = null;

	//
	// Account meta information
	//
	//--------------------------------------------------------------------------

	/// Account meta information
	/// Used to pretty much store all individual information
	/// directly associated with the account
	///
	/// Note: Consider this the "PRIMARY TABLE"
	///
	/// MetaTable<AccountOID, MetaObject>
	protected MetaTable accountMetaTable = null;

	/// Account private infromation
	/// which by default, is not retrivable by the API
	/// used more for internal variables
	///
	/// MetaTable<AccountOID, MetaObject>
	protected MetaTable accountPrivateMetaTable = null;	

	//
	// Group hirachy, and membership information
	//
	//--------------------------------------------------------------------------

	/// Handles the storage of the group role mapping
	///
	/// MetaTable<Group_guid, MetaObject<Member_guid, "[role1, role2, ...]">
	protected MetaTable memberRolesTable = null; 

	/// Handles the storage of the group child meta information
	///
	/// MetaTable<GroupOID-AccountOID, MetaObject>
	protected MetaTable memberMetaTable = null; 
	
	/// Handles the storage of the group child private meta information
	///
	/// MetaTable<GroupOID-AccountOID, MetaObject>
	protected MetaTable memberPrivateMetaTable = null; 

	//
	// Login throttling information
	//
	//--------------------------------------------------------------------------

	/// Handles the Login Throttling Attempt Key (AccountID) Value (Attempt) field mapping
	///
	/// AtomicLongMap<UserOID, attempts>
	protected AtomicLongMap loginThrottlingAttemptMap = null;
	
	/// Handles the Login Throttling Attempt Key (AccountID) Value (Timeout) field mapping
	///
	/// AtomicLongMap<UserOID, expireTimestamp>
	protected AtomicLongMap loginThrottlingExpiryMap = null;
	
	///////////////////////////////////////////////////////////////////////////
	//
	// Table suffixes for the variosu sub tables
	// (Ignore this, as its generally not as important as knowing the above)
	//
	///////////////////////////////////////////////////////////////////////////

	/// The account self ID's
	protected static String SUFFIX_ACCOUNT_LOGIN_ID = "_ID";

	/// The account self ID's
	protected static String SUFFIX_ACCOUNT_HASH = "_IH";

	/// The login sessions used for authentication
	protected static String SUFFIX_LOGIN_SESSION = "_LS";

	/// The login sessions used for authentication
	protected static String SUFFIX_LOGIN_TOKEN = "_LT";

	/// The account self meta values
	protected static String SUFFIX_ACCOUNT_META = "_AM";

	/// The account self private meta values
	protected static String SUFFIX_ACCOUNT_PRIVATE_META = "_AP";

	/// The child account membership
	protected static String SUFFIX_MEMBER_ROLE = "_GR";

	/// The child account meta information
	protected static String SUFFIX_MEMBER_META = "_GM";

	/// The child account private meta information
	protected static String SUFFIX_MEMBER_PRIVATE_META = "_GP";

	/// The Login Throttling Attempt account values
	protected static String ACCOUNT_LOGIN_THROTTLING_ATTEMPT = "_TA";

	/// The Login Throttling Timeout Expiry account values
	protected static String ACCOUNT_LOGIN_THROTTLING_EXPIRY = "_TE";

	///////////////////////////////////////////////////////////////////////////
	//
	// Constructor setup : Setup the actual tables, with the various names
	//
	///////////////////////////////////////////////////////////////////////////

	/// Load the provided stack object
	/// with the variosu meta table / key valuemap / etc
	public AccountTable(CommonStack inStack, String inName) {
		super(inStack, inName);
		internalStructureList = setupInternalStructureList();
	}

	/// Get the list of local CommonStructure's
	/// this is used internally by setup/destroy/maintenance
	protected List<CommonStructure> setupInternalStructureList() {

		// Login auth information
		accountLoginIdMap = stack.getKeyValueMap(name + SUFFIX_ACCOUNT_LOGIN_ID);
		accountAuthMap = stack.getKeyValueMap(name + SUFFIX_ACCOUNT_HASH);
		
		// Login session infromation
		sessionInfoMap = stack.getKeyValueMap(name + SUFFIX_LOGIN_SESSION);
		sessionTokenMap = stack.getKeyValueMap(name + SUFFIX_LOGIN_TOKEN);
		
		// Account meta information
		accountMetaTable = stack.getMetaTable(name + SUFFIX_ACCOUNT_META);
		accountPrivateMetaTable = stack.getMetaTable(name + SUFFIX_MEMBER_PRIVATE_META);
		
		// Group hirachy, and membership information
		memberRolesTable = stack.getMetaTable(name + SUFFIX_MEMBER_ROLE);
		memberMetaTable = stack.getMetaTable(name + SUFFIX_MEMBER_META);
		memberPrivateMetaTable = stack.getMetaTable(name + SUFFIX_MEMBER_PRIVATE_META);

		// Login throttling information
		loginThrottlingAttemptMap = stack.getAtomicLongMap(name + ACCOUNT_LOGIN_THROTTLING_ATTEMPT);
		loginThrottlingExpiryMap = stack.getAtomicLongMap(name + ACCOUNT_LOGIN_THROTTLING_EXPIRY);

		// @TODO - Consider adding support for temporary tabls typehints

		// Return it as a list
		return Arrays.asList(
			accountLoginIdMap, accountAuthMap,
			sessionInfoMap, sessionTokenMap,
			accountMetaTable, accountPrivateMetaTable,
			memberRolesTable, memberMetaTable, memberPrivateMetaTable,
			loginThrottlingAttemptMap, loginThrottlingExpiryMap
		);
	}
	
	///////////////////////////////////////////////////////////////////////////
	//
	// Basic account object existance checks, setup, and destruction
	//
	///////////////////////////////////////////////////////////////////////////

	/// Returns if the name exists
	/// 
	/// @param  Login ID to use, normally this is an email, or nice username
	///
	/// @return TRUE if login ID exists
	public boolean hasLoginID(String inLoginID) {
		return accountLoginIdMap.containsKey(inLoginID);
	}

	/// Returns if the account object id exists
	///
	/// @param  Account OID to use
	///
	/// @return  TRUE of account ID exists
	public boolean containsKey(Object oid) {
		return accountMetaTable.containsKey(oid);
	}

	/// Generates a new account object.
	///
	/// Note without setting a name, or any additional values. 
	/// This call in some sense is quite, err useless.
	public AccountObject newObject() {
		AccountObject ret = new AccountObject(this, null);
		// ret.saveAll(); //ensures the blank object is now in DB
		return ret;
	}

	/// Generates a new account object with the given nice name
	///
	/// @param  Unique Login ID to use, normally this is an email, or nice username
	///
	/// @return AccountObject if succesfully created
	public AccountObject newObject(String name) {
		// Quick fail check
		if (hasLoginID(name)) {
			return null;
		}

		// Creating account object, setting the name if valid
		AccountObject ret = newObject();
		if (ret.setLoginID(name)) {
			return ret;
		} else {
			// Removal step is required on failure,
			// as it helps prevent "orphaned" account objects
			// in new account race conditions
			remove(ret._oid());
		}

		return null;
	}

	/// Removes the accountObject using the ID
	///
	/// @param  Account OID to use, or alternatively its object
	///
	/// @return NULL
	public AccountObject remove(Object inOid) {
		if (inOid != null) {

			// Alternatively, instead of string use MetaObject
			if( inOid instanceof MetaObject ) {
				inOid = ((MetaObject)inOid)._oid();
			}

			// Get oid as a string, and fetch the account object
			String oid = inOid.toString();
			AccountObject ao = this.get(oid);
			
			// Remove account meta information
			accountLoginIdMap.remove(oid);
			accountPrivateMetaTable.remove(oid);
			
			// @TODO - handle removal of group data
			// memberRolesTable
			// memberMetaTable
			// memberPrivateMetaTable
			//-----------------------------------------
			// //group_childRole and groupChild_meta
			// if (ao != null) {
			// 	if (ao.isGroup()) {
			// 		MetaObject groupObj = group_childRole.get(oid);
			// 		group_childRole.remove(oid);
			// 		if (groupObj != null) {
			// 			for (String userID : groupObj.keySet()) {
			// 				String groupChildMetaKey = getGroupChildMetaKey(oid, userID);
			// 				groupChild_meta.remove(groupChildMetaKey);
			// 			}
			// 		}
			// 	} else {
			// 		String[] groupIDs = ao.getGroups_id();
			// 		if (groupIDs != null) {
			// 			for (String groupID : groupIDs) {
			// 				MetaObject groupObj = group_childRole.get(groupID);
			// 				groupObj.remove(oid);
			// 				String groupChildMetaKey = getGroupChildMetaKey(groupID, oid);
			// 				groupChild_meta.remove(groupChildMetaKey);
			// 			}
			// 		}
			// 	}
			// }
			//-----------------------------------------

			// Remove login ID's AKA nice names
			Set<String> loginIdMapNames = accountLoginIdMap.keySet(oid);
			if (loginIdMapNames != null) {
				for (String name : loginIdMapNames) {
					accountLoginIdMap.remove(name, oid);
				}
			}

			// Remove login authentication details
			accountAuthMap.remove(oid);

			// Remove thorttling information
			loginThrottlingAttemptMap.remove(oid);
			loginThrottlingExpiryMap.remove(oid);

		}

		return null;
	}

	///////////////////////////////////////////////////////////////////////////
	//
	// Account object getters
	//
	///////////////////////////////////////////////////////////////////////////

	/// Gets and return the accounts object using the account ID
	///
	/// @param  Account ID to use
	///
	/// @return  AccountObject representing the account ID if found
	public AccountObject get(Object oid) {
		// Possibly a valid OID?
		if( oid != null ) {
			String _oid = oid.toString();
			if (containsKey(_oid)) {
				return new AccountObject(this, _oid);
			}
		}
		// Account object invalid here
		return null;
	}

	/// Gets the account UUID, using the configured name
	///
	/// @param  The login ID (nice-name/email)
	///
	/// @return  Account ID associated, if any
	public String loginToAccountID(String name) {
		return accountLoginIdMap.get(name);
	}

	/// Gets the account using the nice name
	///
	/// @param  The login ID (nice-name/email)
	///
	/// @return  AccountObject representing the account ID if found
	public AccountObject getFromLoginID(Object name) {
		String _oid = loginToAccountID(name.toString());
		if (_oid != null) {
			return get(_oid);
		}
		return null;
	}

	///////////////////////////////////////////////////////////////////////////
	//
	// Map compliance
	//
	///////////////////////////////////////////////////////////////////////////

	/// Returns all the account _oid in the system
	///
	/// @return  Set of account oid's
	public Set<String> keySet() {
		return accountMetaTable.keySet();
	}

	///////////////////////////////////////////////////////////////////////////
	//
	// Additional functionality add on
	//
	///////////////////////////////////////////////////////////////////////////

	/*
	//
	// Additional functionality add on
	//--------------------------------------------------------------------------

	/// Gets the account using the object ID array,
	/// and returns the account object array
	public AccountObject[] getFromIDArray(String[] _oidList) {
		AccountObject[] mList = new AccountObject[_oidList.length];
		for (int a = 0; a < _oidList.length; ++a) {
			mList[a] = getFromID(_oidList[a]);
		}
		return mList;
	}

	/// Removes the accountObject using the name
	public void removeFromName(String name) {
		AccountObject ao = this.getFromName(name);
		if (ao != null) {
			removeFromID(ao._oid());
		}
	}

	protected String getGroupChildMetaKey(String groupOID, String AccountOID) {
		return groupOID + "-" + AccountOID;
	}

	///
	/// login configuration and utiltities
	///--------------------------------------------------------------------------

	/// defined login lifetime, default as 3600 seconds (aka 1 hr)
	public int loginLifetime = 3600; // 1 hr = 60 (mins) * 60 (seconds) = 3600 seconds

	/// lifetime for http login token required for renewal, 1800 seconds (or half an hour)
	public int loginRenewal = loginLifetime / 2; //

	/// Remember me lifetime, default as 2592000 seconds (aka 30 days)
	public int rmberMeLifetime = 2592000; // 1 mth ~= 30 (days) * 24 (hrs) * 3600 (seconds in an hr)

	/// Remember me lifetime, default as 15 days
	public int rmberMeRenewal = rmberMeLifetime / 2; // 15 days

	/// Sets the cookie to be limited to http only
	public boolean isHttpOnly = false;

	/// Sets the cookie to be via https only
	public boolean isSecureOnly = false;

	/// Sets the cookie namespace prefix
	public String cookiePrefix = "Account_";

	/// Sets teh cookie domain, defaults is null
	public String cookieDomain = null;

	/// The nonce size
	public int nonceSize = 22;

	/// Gets and returns the session info, [nonceSalt, loginIP, browserAgent]
	protected String[] getSessionInfo(String oid, String nonce) {
		return accountSessions.getStringArray(oid + "-" + nonce);
	}

	/// Sets the session info with the given nonceSalt, IP, and browserAgent
	protected String generateSession(String oid, int lifespan, String nonceSalt, String ipString, String browserAgent) {
		String nonce = NxtCrypt.randomString(nonceSize);
		String key = oid + "-" + nonce;
		accountSessions.putWithLifespan(key,
			ConvertJSON.fromList(Arrays.asList(new String[] { nonceSalt, ipString, browserAgent })), lifespan);
		return nonce;
	}

	///
	/// httpServlet authentication utility
	///
	/// These features depends on the following packages
	///
	/// import javax.servlet.ServletException;
	/// import javax.servlet.http.HttpServletRequest;
	/// import javax.servlet.http.HttpServletResponse;
	/// import javax.servlet.http.Cookie;
	///
	///--------------------------------------------------------------------------

	/// Validates the user retur true/false, without updating the response cookie
	public AccountObject getRequestUser(javax.servlet.http.HttpServletRequest request) {
		return getRequestUser(request, null);
	}

	/// Validates the user retur true/false, with an update response cookie / token if needed
	public AccountObject getRequestUser(javax.servlet.http.HttpServletRequest request,
		javax.servlet.http.HttpServletResponse response) {

		// don't set cookies if it is logout request
		if (request.getPathInfo() != null && request.getPathInfo().indexOf("logout") != -1) {
			return null;
		}

		javax.servlet.http.Cookie[] cookieJar = request.getCookies();

		if (cookieJar == null) {
			return null;
		}

		// Gets the existing cookie settings
		//----------------------------------------------------------

		String puid = null;
		String nonc = null;
		String hash = null;
		String rmbr = null;
		String crumbsFlavour = null;

		for (javax.servlet.http.Cookie crumbs : cookieJar) {
			crumbsFlavour = crumbs.getName();

			if (crumbsFlavour == null) {
				continue;
			} else if (crumbsFlavour.equals(cookiePrefix + "Puid")) {
				puid = crumbs.getValue();
			} else if (crumbsFlavour.equals(cookiePrefix + "Nonc")) {
				nonc = crumbs.getValue();
			} else if (crumbsFlavour.equals(cookiePrefix + "Hash")) {
				hash = crumbs.getValue();
			} else if (crumbsFlavour.equals(cookiePrefix + "Rmbr")) {
				rmbr = crumbs.getValue();
			}
		}

		// Check if all values are present, and if user ID is valid
		if (puid == null || nonc == null || hash == null) {
			return null;
		}

		if (puid.length() < 22 || !containsID(puid)) {
			logoutAccount(request, response);
			return null;
		}

		AccountObject ret = null;
		String[] sessionInfo = getSessionInfo(puid, nonc);
		if (sessionInfo == null || sessionInfo.length <= 0 || //
			sessionInfo[0] == null || (ret = getFromID(puid)) == null //
		) {
			logoutAccount(request, response);
			return null;
		}

		String passHash = ret.getPasswordHash();

		String computedCookieHash = NxtCrypt.getSaltedHash(passHash, sessionInfo[0]);
		// Remove the special characters from the hash value
		// Cookie with special characters failed to save at browser, specially with + and = signs
		computedCookieHash = computedCookieHash.replaceAll("\\W", "");

		// Invalid cookie hash, exits
		if (!NxtCrypt.slowEquals(hash, computedCookieHash)) {
			logoutAccount(request, response);
			return null;
		}

		// From this point onwards, the session is valid. Now it performs checks for the renewal process
		//---------------------------------------------------------------------------------------------------

		if (response != null) { //assume renewal process check
			if (rmbr != null && rmbr.equals("1")) {
				if (accountSessions.getLifespan(puid + "-" + nonc) < rmberMeRenewal) { //needs renewal (perform it!)
					_setLogin(ret, request, response, true);
				}
			} else {
				if (accountSessions.getLifespan(puid + "-" + nonc) < loginRenewal) { //needs renewal (perform it!)
					_setLogin(ret, request, response, false);
				}
			}
		}

		return ret;
	}

	public void setLoginLifetimeValue(int inLoginLifetime){
		loginLifetime = inLoginLifetime;
		loginRenewal = loginLifetime / 2;
	}

	/// Internally sets the login to a user (handles the respective nonce) and set the cookies for the response
	private boolean _setLogin(AccountObject po, javax.servlet.http.HttpServletRequest request,
		javax.servlet.http.HttpServletResponse response, boolean rmberMe) {

		// Prepare the vars
		//-----------------------------------------------------
		String puid = po._oid();

		// Detirmine the login lifetime
		int noncLifetime;
		if (rmberMe) {
			noncLifetime = rmberMeLifetime;
		} else {
			noncLifetime = loginLifetime;
		}
		long expireTime = (System.currentTimeMillis()) / 1000L + noncLifetime;

		String passHash = po.getPasswordHash();
		if (passHash == null || passHash.length() <= 1) {
			return false;
		}
		String nonceSalt = NxtCrypt.randomString(nonceSize);

		// @TODO: include session details, such as login IP and BrowserAgent
		String nonc = generateSession(puid, noncLifetime, nonceSalt, null, null);

		String cookieHash = NxtCrypt.getSaltedHash(passHash, nonceSalt);
		// Remove the special characters from the hash value
		// Cookie with special characters failed to save at browser, specially with + and = signs
		cookieHash = cookieHash.replaceAll("\\W", "");

		// Store the cookies
		//-----------------------------------------------------
		int noOfCookies = 5;
		javax.servlet.http.Cookie cookieJar[] = new javax.servlet.http.Cookie[noOfCookies];

		int index = 0;
		cookieJar[index++] = new javax.servlet.http.Cookie(cookiePrefix + "Puid", puid);
		cookieJar[index++] = new javax.servlet.http.Cookie(cookiePrefix + "Nonc", nonc);
		cookieJar[index++] = new javax.servlet.http.Cookie(cookiePrefix + "Hash", cookieHash);
		if (rmberMe) {
			cookieJar[index++] = new javax.servlet.http.Cookie(cookiePrefix + "Rmbr", "1");
		} else {
			cookieJar[index++] = new javax.servlet.http.Cookie(cookiePrefix + "Rmbr", "0");
		}
		cookieJar[index++] = new javax.servlet.http.Cookie(cookiePrefix + "Expi", String.valueOf(expireTime));

		/// The cookie "Expi" store the other cookies (Rmbr, user, Nonc etc.) expiry life time in seconds.
		/// This cookie value is used in JS (checkLogoutTime.js) for validating the login expiry time and show a message to user accordingly.

		for (int a = 0; a < noOfCookies; ++a) {
			/// Path is required for cross AJAX / domain requests,
			/// @TODO make this configurable?
			String cookiePath = (request.getContextPath() == null || request.getContextPath().isEmpty()) ? "/" : request
				.getContextPath();
			cookieJar[a].setPath(cookiePath);

			if (!rmberMe) { //set to clear on browser close
				cookieJar[a].setMaxAge(noncLifetime);
			}

			if (a < 4 && isHttpOnly) {
				cookieJar[a].setHttpOnly(isHttpOnly);
			}
			if (isSecureOnly) {
				cookieJar[a].setSecure(isSecureOnly);
			}

			if (cookieDomain != null && cookieDomain.length() > 0) {
				cookieJar[a].setDomain(cookieDomain);
			}

			//Actually inserts the cookie
			response.addCookie(cookieJar[a]);
		}

		return true;
	}

	/// Login the user if valid
	public AccountObject loginAccount(javax.servlet.http.HttpServletRequest request,
		javax.servlet.http.HttpServletResponse response, AccountObject accountObj, String rawPassword, boolean rmberMe) {
		if (accountObj != null && accountObj.validatePassword(rawPassword)) {
			_setLogin(accountObj, request, response, rmberMe);
			return accountObj;
		}

		return null;
	}

	/// Login the user if valid
	public AccountObject loginAccount(javax.servlet.http.HttpServletRequest request,
		javax.servlet.http.HttpServletResponse response, String nicename, String rawPassword, boolean rmberMe) {
		return loginAccount(request, response, getFromName(nicename), rawPassword, rmberMe);
	}

	/// Facebook Login user
	public AccountObject loginAccountViaFacebook(javax.servlet.http.HttpServletRequest request,
		javax.servlet.http.HttpServletResponse response, AccountObject accountObj, String rawPassword, boolean rmberMe) {
		if (accountObj != null) {
			_setLogin(accountObj, request, response, rmberMe);
			return accountObj;
		}

		return null;
	}

	/// Logout any existing users
	public boolean logoutAccount(javax.servlet.http.HttpServletRequest request,
		javax.servlet.http.HttpServletResponse response) {
		if (response == null) {
			return false;
		}

		javax.servlet.http.Cookie cookieJar[] = new javax.servlet.http.Cookie[5];

		cookieJar[0] = new javax.servlet.http.Cookie(cookiePrefix + "User", "-");
		cookieJar[1] = new javax.servlet.http.Cookie(cookiePrefix + "Nonc", "-");
		cookieJar[2] = new javax.servlet.http.Cookie(cookiePrefix + "Hash", "-");
		cookieJar[3] = new javax.servlet.http.Cookie(cookiePrefix + "Rmbr", "-");
		cookieJar[4] = new javax.servlet.http.Cookie(cookiePrefix + "Expi", "-");

		for (int a = 0; a < 5; ++a) {
			cookieJar[a].setMaxAge(1);

			/// Path is required for cross AJAX / domain requests,
			/// @TODO make this configurable?
			String cookiePath = (request.getContextPath() == null || request.getContextPath().isEmpty()) ? "/" : request
				.getContextPath();
			cookieJar[a].setPath(cookiePath);

			if (a < 4 && isHttpOnly) {
				cookieJar[a].setHttpOnly(isHttpOnly);
			}
			if (isSecureOnly) {
				cookieJar[a].setSecure(isSecureOnly);
			}

			if (cookieDomain != null && cookieDomain.length() > 0) {
				cookieJar[a].setDomain(cookieDomain);
			}

			//Actually inserts the cookie
			response.addCookie(cookieJar[a]);
		}
		return true;
	}

	///
	/// Group Membership roles managment
	///--------------------------------------------------------------------------
	protected List<String> membershipRoles = new ArrayList<String>(Arrays.asList(new String[] { "guest", "member",
		"manager", "admin" }));

	/// Returns the internal membership role list
	public List<String> membershipRoles() {
		return membershipRoles;
	}

	/// Checks if membership role exists
	public boolean hasMembershipRole(String role) {
		// Sanatize the role
		role = role.toLowerCase();

		// Returns if it exists
		return membershipRoles.contains(role);
	}

	/// Add membership role if it does not exists
	public void addMembershipRole(String role) {
		// Sanatize the role
		role = role.toLowerCase();

		// Already exists terminate
		if (hasMembershipRole(role)) {
			return;
		}

		// Add the role
		membershipRoles.add(role);
	}

	/// Checks and validates the membership role, throws if invalid
	protected String validateMembershipRole(String role) {
		role = role.toLowerCase();
		if (!hasMembershipRole(role)) {
			throw new RuntimeException("Membership role does not exists: " + role);
		}
		return role;
	}

	//
	// Super Users group managment
	//--------------------------------------------------------------------------

	/// Default super user group
	protected String _superUserGroup = "SuperUsers";

	/// Gets the super user group
	public String getSuperUserGroupName() {
		return _superUserGroup;
	}

	/// Change the super user group
	public String setSuperUserGroupName(String userGroup) {
		String old = _superUserGroup;
		_superUserGroup = userGroup;
		return old;
	}

	/// Returns the super user group
	public AccountObject superUserGroup() {
		return getFromName(getSuperUserGroupName());
	}

	//
	// Getting users based on filters
	// TODO: To optimise because Sam is dumb
	// --------------------------------------------------------------------------
	public AccountObject[] getUsersByGroupAndRole(String[] insideGroupAny, String[] hasRoleAny) {
		List<AccountObject> ret = new ArrayList<AccountObject>();

		MetaObject[] metaObjs = accountMetaTable().query(null, null, "oID", 0, 0); //initial query just to get everything out so i can filter

		if (metaObjs == null) {
			return null;
		}

		boolean doGroupCheck = (insideGroupAny != null && insideGroupAny.length > 0);
		boolean doRoleCheck = (hasRoleAny != null && hasRoleAny.length > 0);

		for (MetaObject metaObj : metaObjs) {
			AccountObject ao = getFromID(metaObj._oid());

			if (ao == null) {
				continue;
			}

			// Possible Error found: returns null in one of the array
			AccountObject[] userGroups = ao.getGroups();

			if (userGroups == null) {
				continue;
			}

			for (AccountObject userGroup : userGroups) {

				// To avoid null error in the array
				if (userGroup == null) {
					continue;
				}

				if (doGroupCheck) {
					if (ArrayUtils.contains(insideGroupAny, userGroup._oid())) {
						if (doRoleCheck) {
							String memberRole = userGroup.getMemberRole(ao);
							if (ArrayUtils.contains(hasRoleAny, memberRole)) {
								ret.add(ao);
							}
						} else {
							ret.add(ao);
						}
					}
				} else {
					if (doRoleCheck) {
						String memberRole = userGroup.getMemberRole(ao);
						if (ArrayUtils.contains(hasRoleAny, memberRole)) {
							ret.add(ao);
						}
					} else {
						ret.add(ao);
					}
				}
			}
		}

		return ret.toArray(new AccountObject[ret.size()]);
	}

	*/
}