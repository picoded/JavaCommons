package picoded.JStruct;

/// Java imports
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import picoded.conv.ConvertJSON;
import picoded.security.NxtCrypt;
import picoded.struct.UnsupportedDefaultMap;
/// Picoded imports
/// hazelcast

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
///
///
/// @TODO : Group handling layer
///
public class AccountTable implements UnsupportedDefaultMap<String, AccountObject> {
	
	///
	/// Underlying data structures
	///--------------------------------------------------------------------------
	
	/// Provides a key value pair mapping
	/// of the account "names" to GUID
	///
	/// "names" are unique, and are usually either group names or emails
	/// GUID's are not unique, as a single GUID can have multiple "names"
	protected KeyValueMap keyValueMapAccountID = null; //to delete from
	
	/// Stores the account authentication hash, used for password based authentication
	protected KeyValueMap keyValueMapAccountHash = null; //to delete from
	
	/// Stores the account session authentication no-once information
	protected KeyValueMap keyValueMapAccountSessions = null;
	
	/// Holds the account object meta table values
	protected MetaTable meatAccountTable = null; //to delete from
	
	/// Handles the storage of the group mapping
	///
	/// Group[member] = role1,role2, ...
	/// MetaTable<Group_guid, MetaObject<Member_guid, "String array of roles">
	protected MetaTable groupChildRole = null; //to delete from
	
	/// Handles the Group-member meta field mapping
	///
	/// MetaTable<GroupOID-MemberOID, MetaObject>
	protected MetaTable groupChildMeta = null; //to delete from
	
	///Handles the Login Throttling Attempt Key (User Id) Value (Attempt) field mapping
	///
	/// KeyValueMap<
	protected KeyValueMap loginThrottlingAttempt = null;
	
	///Handles the Login Throttling Elapsed Time Key (User Id) Value (Elapsed) field mapping
	///
	/// KeyValueMap<
	protected KeyValueMap loginThrottlingElapsed = null;
	
	///
	/// Table suffixes for the variosu sub tables
	///--------------------------------------------------------------------------
	
	/// Account table name prefix
	protected String tableNamePrefix = null;
	
	/// The account self ID's
	protected static String accountID = "_ID";
	
	/// The account self ID's
	protected static String accountHash = "_IH";
	
	/// The login sessions used for authentication
	protected static String accountSessions = "_LS";
	
	/// The account self meta values
	protected static String accountMeta = "_SM";
	
	/// The child nodes mapping, from self
	protected static String accountChild = "_SC";
	
	/// The child account meta values
	protected static String accountChildMeta = "_CM";
	
	/// The Login Throttling Attempt account values
	protected static String accountLoginThrottlingAttempt = "_LA";
	
	/// The Login Throttling Elapsed account values
	protected static String accountLoginThrottlingElapsed = "_LE";
	
	private static String user = "User";
	
	///
	/// Constructor setup
	///--------------------------------------------------------------------------
	
	/// Setup the metatable with the default table name
	public AccountTable(JStruct jStructObj, String tableName) {
		tableNamePrefix = tableName;
		
		keyValueMapAccountID = jStructObj.getKeyValueMap(tableName + accountID);
		keyValueMapAccountHash = jStructObj.getKeyValueMap(tableName + accountHash);
		keyValueMapAccountSessions = jStructObj.getKeyValueMap(tableName + accountSessions);
		meatAccountTable = jStructObj.getMetaTable(tableName + accountMeta);
		groupChildRole = jStructObj.getMetaTable(tableName + accountChild);
		groupChildMeta = jStructObj.getMetaTable(tableName + accountChildMeta);
		loginThrottlingAttempt = jStructObj.getKeyValueMap(tableName + accountLoginThrottlingAttempt);
		loginThrottlingElapsed = jStructObj.getKeyValueMap(tableName + accountLoginThrottlingElapsed);
		keyValueMapAccountSessions.setTempHint(true); //optimization
	}
	
	//
	// Stack common setup / teardown functions
	//--------------------------------------------------------------------------
	
	/// Performs the full stack setup for the data object
	public void systemSetup() {
		keyValueMapAccountID.systemSetup();
		keyValueMapAccountHash.systemSetup();
		keyValueMapAccountSessions.systemSetup();
		meatAccountTable.systemSetup();
		groupChildRole.systemSetup();
		groupChildMeta.systemSetup();
		loginThrottlingAttempt.systemSetup();
		loginThrottlingElapsed.systemSetup();
		if (superUserGroup() == null) {
			newObject(superUserGroup).saveAll();
		}
	}
	
	/// Performs the full stack teardown for the data object
	public void systemTeardown() {
		keyValueMapAccountID.systemTeardown();
		keyValueMapAccountHash.systemTeardown();
		keyValueMapAccountSessions.systemTeardown();
		meatAccountTable.systemTeardown();
		groupChildRole.systemTeardown();
		groupChildMeta.systemTeardown();
		loginThrottlingAttempt.systemTeardown();
		loginThrottlingElapsed.systemTeardown();
	}
	
	//
	// Getters for metaTables
	// Discouraged from use
	//
	
	public MetaTable accountMetaTable() {
		return meatAccountTable;
	}
	
	public MetaTable groupChildRole() {
		return groupChildRole;
	}
	
	public MetaTable groupChildMeta() {
		return groupChildMeta;
	}
	
	//
	// Map compliant implementation,
	// Keys here refer to object ID
	//
	//--------------------------------------------------------------------------
	
	/// Gets and return the accounts table from the account ID
	@Override
	public AccountObject get(Object oid) {
		return getFromID(oid);
	}
	
	/// Account exists, this is an alias of containsName
	@Override
	public boolean containsKey(Object oid) {
		return containsID(oid.toString());
	}
	
	/// Removes the object, returns null
	@Override
	public AccountObject remove(Object oid) {
		removeFromID(oid.toString());
		return null;
	}
	
	// Returns all the account _oid in the system
	@Override
	public Set<String> keySet() {
		return meatAccountTable.keySet();
	}
	
	//
	// Additional functionality add on
	//--------------------------------------------------------------------------
	
	/// Gets the account using the object ID
	public AccountObject getFromID(Object oid) {
		if (containsID(oid.toString())) {
			return new AccountObject(this, oid.toString());
		}
		
		return null;
	}
	
	/// Gets the account using the object ID array,
	/// and returns the account object array
	public AccountObject[] getFromIDArray(String[] oidList) {
		AccountObject[] mList = new AccountObject[oidList.length];
		for (int a = 0; a < oidList.length; ++a) {
			mList[a] = getFromID(oidList[a]);
		}
		return mList;
	}
	
	/// Gets the account using the nice name
	public AccountObject getFromName(Object name) {
		if (nameToID(name.toString()) != null) {
			return getFromID(nameToID(name.toString()));
		}
		return null;
	}
	
	/// Generates a new account object
	public AccountObject newObject() {
		//AccountObject ret = new AccountObject(this, null);
		//ret.saveAll(); //ensures the blank object is now in DB
		return new AccountObject(this, null);
	}
	
	/// Generates a new account object with the given nice name
	public AccountObject newObject(String name) {
		//		if (containsName(name)) {
		//			return null;
		//		}
		
		AccountObject ret = newObject();
		
		if (ret.setName(name)) {
			return ret;
		} else {
			removeFromID(ret._oid());
		}
		
		return null;
	}
	
	/// Gets the account UUID, using the configured name
	public String nameToID(String name) {
		return keyValueMapAccountID.get(name);
	}
	
	/// Returns if the name exists
	public boolean containsName(String name) {
		return keyValueMapAccountID.containsKey(name);
	}
	
	/// Returns if the account object id exists
	public boolean containsID(Object oid) {
		return meatAccountTable.containsKey(oid);
	}
	
	/// Removes the accountObject using the name
	public void removeFromName(String name) {
		AccountObject ao = this.getFromName(name);
		if (ao != null) {
			removeFromID(ao._oid());
		}
	}
	
	/// Removes the accountObject using the ID
	public void removeFromID(String oid) {
		if (oid != null && !oid.isEmpty()) {
			AccountObject ao = this.getFromID(oid);
			
			//group_childRole and groupChild_meta
			if (ao != null) {
				if (ao.isGroup()) {
					MetaObject groupObj = groupChildRole.get(oid);
					groupChildRole.remove(oid);
					
					if (groupObj != null) {
						for (String userID : groupObj.keySet()) {
							String groupChildMetaKey = getGroupChildMetaKey(oid, userID);
							groupChildMeta.remove(groupChildMetaKey);
						}
					}
				} else {
					String[] groupIDs = ao.getGroups_id();
					if (groupIDs.length > 0) {
						for (String groupID : groupIDs) {
							MetaObject groupObj = groupChildRole.get(groupID);
							groupObj.remove(oid);
							
							String groupChildMetaKey = getGroupChildMetaKey(groupID, oid);
							groupChildMeta.remove(groupChildMetaKey);
						}
					}
				}
			}
			
			//accountID
			Set<String> accountIDNames = keyValueMapAccountID.getKeys(oid);
			if (accountIDNames != null) {
				for (String name : accountIDNames) {
					keyValueMapAccountID.remove(name, oid);
				}
			}
			
			//accountHash
			keyValueMapAccountHash.remove(oid);
			
			//accountMeta
			meatAccountTable.remove(oid);
		}
	}
	
	protected String getGroupChildMetaKey(String groupOID, String memberOID) {
		return groupOID + "-" + memberOID;
	}
	
	///
	/// login configuration and utiltities
	///--------------------------------------------------------------------------
	
	/// defined login lifetime, default as 3600 seconds (aka 1 hr)
	private int loginLifetime = 3600; // 1 hr = 60 (mins) * 60 (seconds) = 3600 seconds
	
	/// lifetime for http login token required for renewal, 1800 seconds (or half an hour)
	protected int loginRenewal = loginLifetime / 2; //
	
	/// Remember me lifetime, default as 2592000 seconds (aka 30 days)
	private int rmberMeLifetime = 2592000; // 1 mth ~= 30 (days) * 24 (hrs) * 3600 (seconds in an hr)
	
	/// Remember me lifetime, default as 15 days
	protected int rmberMeRenewal = rmberMeLifetime / 2; // 15 days
	
	/// Sets the cookie to be limited to http only
	protected boolean isHttpOnly = false;
	
	/// Sets the cookie to be via https only
	protected boolean isSecureOnly = false;
	
	/// Sets the cookie namespace prefix
	private String cookiePrefix = "Account_";
	
	/// Sets teh cookie domain, defaults is null
	protected String cookieDomain = null;
	
	/// The nonce size
	protected int nonceSize = 22;
	
	/// Gets and returns the session info, [nonceSalt, loginIP, browserAgent]
	protected String[] getSessionInfo(String oid, String nonce) {
		return keyValueMapAccountSessions.getStringArray(oid + "-" + nonce);
	}
	
	/// Sets the session info with the given nonceSalt, IP, and browserAgent
	protected String generateSession(String oid, int lifespan, String nonceSalt, String ipString,
		String browserAgent) {
		String nonce = NxtCrypt.randomString(nonceSize);
		String key = oid + "-" + nonce;
		keyValueMapAccountSessions.putWithLifespan(key,
			ConvertJSON.fromList(Arrays.asList(new String[] { nonceSalt, ipString, browserAgent })),
			lifespan);
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
			
			if (crumbsFlavour.equals(cookiePrefix + "Puid")) {
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
			if (rmbr != null && "1".equals(rmbr)) {
				if (keyValueMapAccountSessions.getLifespan(nonc) < rmberMeRenewal) { //needs renewal (perform it!)
					setLogin(ret, request, response, true);
				}
			} else {
				if (keyValueMapAccountSessions.getLifespan(nonc) < loginRenewal) { //needs renewal (perform it!)
					setLogin(ret, request, response, false);
				}
			}
		}
		
		return ret;
	}
	
	/// Internally sets the login to a user (handles the respective nonce) and set the cookies for the response
	protected boolean setLogin(AccountObject po, javax.servlet.http.HttpServletRequest request,
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
		javax.servlet.http.Cookie[] cookieJar = new javax.servlet.http.Cookie[noOfCookies];
		
		int index = 0;
		cookieJar[index] = new javax.servlet.http.Cookie(cookiePrefix + "Puid", puid);
		index++;
		cookieJar[index] = new javax.servlet.http.Cookie(cookiePrefix + "Nonc", nonc);
		index++;
		cookieJar[index] = new javax.servlet.http.Cookie(cookiePrefix + "Hash", cookieHash);
		index++;
		if (rmberMe) {
			cookieJar[index] = new javax.servlet.http.Cookie(cookiePrefix + "Rmbr", "1");
			index++;
		} else {
			cookieJar[index] = new javax.servlet.http.Cookie(cookiePrefix + "Rmbr", "0");
			index++;
		}
		cookieJar[index] = new javax.servlet.http.Cookie(cookiePrefix + "Expi",
			String.valueOf(expireTime));
		index++;
		
		/// The cookie "Expi" store the other cookies (Rmbr, user, Nonc etc.) expiry life time in seconds.
		/// This cookie value is used in JS (checkLogoutTime.js) -
		/// - for validating the login expiry time and show a message to user accordingly.
		
		for (int a = 0; a < noOfCookies; ++a) {
			/// Path is required for cross AJAX / domain requests,
			/// @TODO make this configurable?
			String cookiePath = (request.getContextPath() == null || request.getContextPath()
				.isEmpty()) ? "/" : request.getContextPath();
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
		javax.servlet.http.HttpServletResponse response, AccountObject accountObj,
		String rawPassword, boolean rmberMe) {
		if (accountObj != null && accountObj.validatePassword(rawPassword)) {
			setLogin(accountObj, request, response, rmberMe);
			return accountObj;
		}
		
		return null;
	}
	
	/// Login the user if valid
	public AccountObject loginAccount(javax.servlet.http.HttpServletRequest request,
		javax.servlet.http.HttpServletResponse response, String nicename, String rawPassword,
		boolean rmberMe) {
		return loginAccount(request, response, getFromName(nicename), rawPassword, rmberMe);
	}
	
	/// Logout any existing users
	public boolean logoutAccount(javax.servlet.http.HttpServletRequest request,
		javax.servlet.http.HttpServletResponse response) {
		if (response == null) {
			return false;
		}
		
		javax.servlet.http.Cookie[] cookieJar = new javax.servlet.http.Cookie[5];
		
		cookieJar[0] = new javax.servlet.http.Cookie(cookiePrefix + user, "-");
		cookieJar[1] = new javax.servlet.http.Cookie(cookiePrefix + "Nonc", "-");
		cookieJar[2] = new javax.servlet.http.Cookie(cookiePrefix + "Hash", "-");
		cookieJar[3] = new javax.servlet.http.Cookie(cookiePrefix + "Rmbr", "-");
		cookieJar[4] = new javax.servlet.http.Cookie(cookiePrefix + "Expi", "-");
		
		for (int a = 0; a < 5; ++a) {
			cookieJar[a].setMaxAge(1);
			
			/// Path is required for cross AJAX / domain requests,
			/// @TODO make this configurable?
			String cookiePath = (request.getContextPath() == null || request.getContextPath()
				.isEmpty()) ? "/" : request.getContextPath();
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
	protected List<String> membershipRoles = new ArrayList<String>(Arrays.asList(new String[] {
		"guest", "member", "manager", "admin" }));
	
	/// Returns the internal membership role list
	public List<String> membershipRoles() {
		return membershipRoles;
	}
	
	/// Checks if membership role exists
	public boolean hasMembershipRole(String role) {
		// Sanatize the role
		// Returns if it exists
		return membershipRoles.contains(role.toLowerCase(Locale.ENGLISH));
	}
	
	/// Add membership role if it does not exists
	public void addMembershipRole(String role) {
		// Sanatize the role
		// Already exists terminate
		if (hasMembershipRole(role.toLowerCase(Locale.ENGLISH))) {
			return;
		}
		// Add the role
		membershipRoles.add(role.toLowerCase(Locale.ENGLISH));
	}
	
	/// Checks and validates the membership role, throws if invalid
	protected String validateMembershipRole(String role) {
		if (!hasMembershipRole(role.toLowerCase(Locale.ENGLISH))) {
			throw new RuntimeException("Membership role does not exists: "
				+ role.toLowerCase(Locale.ENGLISH));
		}
		return role.toLowerCase(Locale.ENGLISH);
	}
	
	//
	// Super Users group managment
	//--------------------------------------------------------------------------
	
	/// Default super user group
	protected String superUserGroup = "SuperUsers";
	
	/// Gets the super user group
	public String getSuperUserGroupName() {
		return superUserGroup;
	}
	
	/// Change the super user group
	public String setSuperUserGroupName(String userGroup) {
		String old = superUserGroup;
		superUserGroup = userGroup;
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
		
		//initial query just to get everything out so i can filter
		MetaObject[] metaObjs = accountMetaTable().query(null, null, "oID", 0, 0);
		
		//		if (metaObjs == null) {
		//			return null;
		//		}
		
		//		boolean doGroupCheck = insideGroupAny != null && insideGroupAny.length > 0;
		//		boolean doRoleCheck = hasRoleAny != null && hasRoleAny.length > 0;
		
		for (MetaObject metaObj : metaObjs) {
			AccountObject ao = getFromID(metaObj._oid());
			
			if (ao == null || ao.getGroups() == null) {
				continue;
			}
			
			// Possible Error found: returns null in one of the array
			AccountObject[] userGroups = ao.getGroups();
			
			for (AccountObject userGroup : userGroups) {
				// To avoid null error in the array
				if (userGroup != null) {
					getAccountObjectList(ret, ao);
				}
			}
		}
		return ret.toArray(new AccountObject[ret.size()]);
	}
	
	private static List<AccountObject> getAccountObjectList(List<AccountObject> ret, AccountObject ao) {
		ret.add(ao);
		return ret;
		
	}
}
