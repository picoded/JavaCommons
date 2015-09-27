package picoded.JStruct;

/// Java imports
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Arrays;

/// Picoded imports
import picoded.conv.GUID;
import picoded.conv.GenericConvert;
import picoded.conv.ConvertJSON;
import picoded.JSql.*;
import picoded.JCache.*;
import picoded.JStruct.*;
import picoded.JStruct.internal.*;
import picoded.struct.CaseInsensitiveHashMap;
import picoded.struct.UnsupportedDefaultMap;
import picoded.struct.GenericConvertMap;
import picoded.security.NxtCrypt;

/// hazelcast
import com.hazelcast.core.*;
import com.hazelcast.config.*;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

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
	protected KeyValueMap accountID = null; //to delete from
	
	/// Stores the account authentication hash, used for password based authentication
	protected KeyValueMap accountHash = null; //to delete from
	
	/// Stores the account session authentication no-once information 
	protected KeyValueMap accountSessions = null;
	
	/// Holds the account object meta table values
	protected MetaTable accountMeta = null; //to delete from
	
	/// Handles the storage of the group mapping
	///
	/// Group[member] = role1,role2, ...
	/// MetaTable<Group_guid, MetaObject<Member_guid, "String array of roles">
	protected MetaTable group_childRole = null; //to delete from
	
	/// Handles the Group-member meta field mapping
	///
	/// MetaTable<GroupOID-MemberOID, MetaObject>
	protected MetaTable groupChild_meta = null; //to delete from
	
	///
	/// Table suffixes for the variosu sub tables
	///--------------------------------------------------------------------------
	
	/// Account table name prefix
	protected String tableNamePrefix = null;
	
	/// The account self ID's
	protected static String ACCOUNT_ID = "_ID";
	
	/// The account self ID's
	protected static String ACCOUNT_HASH = "_IH";
	
	/// The login sessions used for authentication
	protected static String ACCOUNT_SESSIONS = "_LS";
	
	/// The account self meta values
	protected static String ACCOUNT_META = "_SM";
	
	/// The child nodes mapping, from self
	protected static String ACCOUNT_CHILD = "_SC";
	
	/// The child account meta values
	protected static String ACCOUNT_CHILDMETA = "_CM";
	
	///
	/// Constructor setup
	///--------------------------------------------------------------------------
	
	/// Setup the metatable with the default table name
	public AccountTable(JStruct jStructObj, String tableName) {
		tableNamePrefix = tableName;
		
		accountID = jStructObj.getKeyValueMap(tableName + ACCOUNT_ID);
		accountHash = jStructObj.getKeyValueMap(tableName + ACCOUNT_HASH);
		accountSessions = jStructObj.getKeyValueMap(tableName + ACCOUNT_SESSIONS);
		accountMeta = jStructObj.getMetaTable(tableName + ACCOUNT_META);
		group_childRole = jStructObj.getMetaTable(tableName + ACCOUNT_CHILD);
		groupChild_meta = jStructObj.getMetaTable(tableName + ACCOUNT_CHILDMETA);
		
		accountSessions.setTempHint(true); //optimization
	}
	
	//
	// Stack common setup / teardown functions
	//--------------------------------------------------------------------------
	
	/// Performs the full stack setup for the data object
	public void systemSetup() {
		accountID.systemSetup();
		accountHash.systemSetup();
		accountSessions.systemSetup();
		accountMeta.systemSetup();
		group_childRole.systemSetup();
		groupChild_meta.systemSetup();
		
		if (superUserGroup() == null) {
			newObject(_superUserGroup).saveAll();
		}
	}
	
	/// Performs the full stack teardown for the data object
	public void systemTeardown() {
		accountID.systemTeardown();
		accountHash.systemTeardown();
		accountSessions.systemTeardown();
		accountMeta.systemTeardown();
		group_childRole.systemTeardown();
		groupChild_meta.systemTeardown();
	}
	
	//
	// Getters for metaTables
	// Discouraged from use
	//
	
	public MetaTable accountMetaTable() {
		return accountMeta;
	}
	
	public MetaTable groupChildRole() {
		return group_childRole;
	}
	
	public MetaTable groupChildMeta() {
		return groupChild_meta;
	}
	
	//
	// Map compliant implementation,
	// Keys here refer to object ID
	//
	//--------------------------------------------------------------------------
	
	/// Gets and return the accounts table from the account ID
	public AccountObject get(Object oid) {
		return getFromID(oid);
	}
	
	/// Account exists, this is an alias of containsName
	public boolean containsKey(Object oid) {
		return containsID(oid.toString());
	}
	
	/// Removes the object, returns null
	public AccountObject remove(Object oid) {
		removeFromID(oid.toString());
		return null;
	}
	
	// Returns all the account _oid in the system
	public Set<String> keySet() {
		return accountMeta.keySet();
	}
	
	//
	// Additional functionality add on
	//--------------------------------------------------------------------------
	
	/// Gets the account using the object ID
	public AccountObject getFromID(Object oid) {
		String _oid = oid.toString();
		
		if (containsID(_oid)) {
			return new AccountObject(this, _oid);
		}
		
		return null;
	}
	
	/// Gets the account using the object ID array, 
	/// and returns the account object array
	public AccountObject[] getFromIDArray(String[] _oidList) {
		AccountObject[] mList = new AccountObject[_oidList.length];
		for (int a = 0; a < _oidList.length; ++a) {
			mList[a] = getFromID(_oidList[a]);
		}
		return mList;
	}
	
	/// Gets the account using the nice name
	public AccountObject getFromName(Object name) {
		String _oid = nameToID(name.toString());
		
		if (_oid != null) {
			return getFromID(_oid);
		}
		
		return null;
	}
	
	/// Generates a new account object
	public AccountObject newObject() {
		AccountObject ret = new AccountObject(this, null);
		//ret.saveAll(); //ensures the blank object is now in DB
		return ret;
	}
	
	/// Generates a new account object with the given nice name
	public AccountObject newObject(String name) {
		if (containsName(name)) {
			return null;
		}
		
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
		return accountID.get(name);
	}
	
	/// Returns if the name exists
	public boolean containsName(String name) {
		return accountID.containsKey(name);
	}
	
	/// Returns if the account object id exists
	public boolean containsID(Object oid) {
		return accountMeta.containsKey(oid);
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
					MetaObject groupObj = group_childRole.get(oid);
					group_childRole.remove(oid);
					
					if (groupObj != null) {
						for (String userID : groupObj.keySet()) {
							String groupChildMetaKey = getGroupChildMetaKey(oid, userID);
							groupChild_meta.remove(groupChildMetaKey);
						}
					}
				} else {
					String[] groupIDs = ao.getGroups_id();
					if (groupIDs != null) {
						for (String groupID : groupIDs) {
							MetaObject groupObj = group_childRole.get(groupID);
							groupObj.remove(oid);
							
							String groupChildMetaKey = getGroupChildMetaKey(groupID, oid);
							groupChild_meta.remove(groupChildMetaKey);
						}
					}
				}
			}
			
			//accountID
			Set<String> accountIDNames = accountID.getKeys(oid);
			if (accountIDNames != null) {
				for (String name : accountIDNames) {
					accountID.remove(name, oid);
				}
			}
			
			//accountHash
			accountHash.remove(oid);
			
			//accountMeta
			accountMeta.remove(oid);
		}
	}
	
	protected String getGroupChildMetaKey(String groupOID, String memberOID) {
		return groupOID + "-" + memberOID;
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
		
		// Invalid cookie hash, exits
		if (!NxtCrypt.slowEquals(hash, computedCookieHash)) {
			logoutAccount(request, response);
			return null;
		}
		
		// From this point onwards, the session is valid. Now it performs checks for the renewal process
		//---------------------------------------------------------------------------------------------------
		
		if (response != null) { //assume renewal process check
			if (rmbr != null && rmbr.equals("1")) {
				if (accountSessions.getLifespan(nonc) < rmberMeRenewal) { //needs renewal (perform it!)
					_setLogin(ret, request, response, true);
				}
			} else {
				if (accountSessions.getLifespan(nonc) < loginRenewal) { //needs renewal (perform it!)
					_setLogin(ret, request, response, false);
				}
			}
		}
		
		return ret;
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
	
}
