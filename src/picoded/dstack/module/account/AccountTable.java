package picoded.dstack.module.account;

import java.util.*;
import java.util.function.BiFunction;

import picoded.dstack.*;
import picoded.dstack.module.*;
import picoded.core.conv.*;
import picoded.core.struct.*;
import org.apache.commons.lang3.ArrayUtils;

import static picoded.servlet.api.module.account.AccountConstantStrings.*;

/**
 * The account class is considered a hybrid class of both the user, group management class.
 * Where both users, and groups are considered the "same". Hence their distinction is normally detirmined
 * by one of the meta field values.
 *
 * The intention here, is to facilitate complex hierachy creations rapidly. Especially against changing specs.
 *
 * Any refences to the persona game, is completely coincidental !
 * (PS: old joke, the original name for this class was PersonaTable)
 **/
public class AccountTable extends ModuleStructure implements
	UnsupportedDefaultMap<String, AccountObject> {
	
	///////////////////////////////////////////////////////////////////////////
	//
	// Underlying data structures
	//
	///////////////////////////////////////////////////////////////////////////
	
	//
	// Login authentication
	//
	//--------------------------------------------------------------------------
	
	/**
	 * Provides a key value pair mapping of the account login ID to AccountID (GUID)
	 *
	 * KeyValueMap<uniqueLoginName,AccountID>
	 *
	 * login ID are unique, and are usually usernames or emails
	 * AccountID's are not unique, as a single AccountID can have multiple "names"
	 **/
	protected KeyValueMap accountLoginNameMap = null; //to delete from
	
	/**
	 * Stores the account authentication hash, used for password based authentication
	 *
	 * KeyValueMap<AccountID,passwordHash>
	 **/
	protected KeyValueMap accountAuthMap = null; //to delete from
	
	//
	// Login session
	//
	//--------------------------------------------------------------------------
	
	/**
	 * Stores the account session key, to accountID link
	 *
	 * KeyValueMap<sessionID, accountID>
	 **/
	protected KeyValueMap sessionLinkMap = null;
	
	/**
	 * Stores the account meta information
	 *
	 * KeyValueMap<sessionID, info-about-access>
	 **/
	protected KeyValueMap sessionInfoMap = null;
	
	/**
	 * Stores the account token key, to session key
	 *
	 * KeyValueMap<tokenID, sessionID>
	 **/
	protected KeyValueMap sessionTokenMap = null;
	
	/**
	 * Stores the next token ID to reissue
	 * This limits race conditions where multiple tokens are issued
	 *
	 * KeyValueMap<tokenID, next-tokenID>
	 **/
	protected KeyValueMap sessionNextTokenMap = null;
	
	//
	// Account meta information
	//
	//--------------------------------------------------------------------------
	
	/**
	 * Account meta information
	 * Used to pretty much store all individual information
	 * directly associated with the account
	 *
	 * Note: Consider this the "PRIMARY TABLE"
	 *
	 * DataObjectMap<AccountOID, DataObject>
	 **/
	protected DataObjectMap accountDataTable = null;
	
	/**
	 * Account private infromation
	 * which by default, is not retrivable by the API
	 * used more for internal variables
	 *
	 * DataObjectMap<AccountOID, DataObject>
	 **/
	protected DataObjectMap accountPrivateDataTable = null;
	
	//
	// Login throttling information
	//
	//--------------------------------------------------------------------------
	
	/**
	 * Handles the Login Throttling Attempt Key (AccountID) Value (Attempt) field mapping
	 *
	 * KeyLongMap<UserOID, attempts>
	 **/
	protected KeyLongMap loginThrottlingAttemptMap = null;
	
	/**
	 * Handles the Login Throttling Attempt Key (AccountID) Value (Timeout) field mapping
	 *
	 * KeyLongMap<UserOID, expireTimestamp>
	 **/
	protected KeyLongMap loginThrottlingExpiryMap = null;
	
	//
	// Account Related Token Maps
	//
	//--------------------------------------------------------------------------
	
	/**
	 * Stores the verification token against the account ID together with the expiry time
	 **/
	protected KeyValueMap accountVerificationMap = null;
	
	/**
	 * Stores the verification token for account password resets with expiry time
	 **/
	protected KeyValueMap accountPasswordTokenMap = null;
	
	///////////////////////////////////////////////////////////////////////////
	//
	// Table suffixes for the variosu sub tables
	// (Ignore this, as its generally not as important as knowing the above)
	//
	///////////////////////////////////////////////////////////////////////////
	
	/**
	 * The account self ID's
	 **/
	protected static String SUFFIX_ACCOUNT_LOGIN_ID = "_ID";
	
	/**
	 * The account self ID's
	 **/
	protected static String SUFFIX_ACCOUNT_HASH = "_IH";
	
	/**
	 * The login sessions used for authentication
	 **/
	protected static String SUFFIX_LOGIN_SESSION = "_LS";
	
	/**
	 * The login sessions info, used for authentication
	 **/
	protected static String SUFFIX_LOGIN_SESSION_INFO = "_LI";
	
	/**
	 * The login token used for authentication
	 **/
	protected static String SUFFIX_LOGIN_TOKEN = "_LT";
	
	/**
	 * The next login token used for authentication
	 **/
	protected static String SUFFIX_LOGIN_NEXT_TOKEN = "_LN";
	
	/**
	 * The account self meta values
	 **/
	protected static String SUFFIX_ACCOUNT_META = "_AM";
	
	/**
	 * The account self private meta values
	 **/
	protected static String SUFFIX_ACCOUNT_PRIVATE_META = "_AP";
	
	/**
	 * The child account membership
	 **/
	protected static String SUFFIX_MEMBER_ROLE = "_GR";
	
	/**
	 * The child account meta information
	 **/
	protected static String SUFFIX_MEMBER_META = "_GM";
	
	/**
	 * The child account private meta information
	 **/
	protected static String SUFFIX_MEMBER_PRIVATE_META = "_GP";
	
	/**
	 * The Login Throttling Attempt account values
	 **/
	protected static String ACCOUNT_LOGIN_THROTTLING_ATTEMPT = "_TA";
	
	/**
	 * The Login Throttling Timeout Expiry account values
	 **/
	protected static String ACCOUNT_LOGIN_THROTTLING_EXPIRY = "_TE";
	
	/**
	 * The Account Verification values for account verification tokens
	 **/
	protected static String ACCOUNT_VERIFICATION = "_AV";
	
	/**
	 * The Account Password token prefix
	 **/
	protected static String ACCOUNT_PASSWORD_TOKEN = "_PT";
	
	///////////////////////////////////////////////////////////////////////////
	//
	// Constructor setup : Setup the actual tables, with the various names
	//
	///////////////////////////////////////////////////////////////////////////
	
	/**
	 * Load the provided stack object
	 * with the variosu meta table / key valuemap / etc
	 **/
	public AccountTable(CommonStack inStack, String inName) {
		super(inStack, inName);
		internalStructureList = setupInternalStructureList();
	}
	
	/**
	 * Get the list of local CommonStructure's
	 * this is used internally by setup/destroy/maintenance
	 **/
	protected List<CommonStructure> setupInternalStructureList() {
		
		// Login auth information
		accountLoginNameMap = stack.keyValueMap(name + SUFFIX_ACCOUNT_LOGIN_ID);
		accountAuthMap = stack.keyValueMap(name + SUFFIX_ACCOUNT_HASH);
		
		// Login session infromation
		sessionLinkMap = stack.keyValueMap(name + SUFFIX_LOGIN_SESSION);
		sessionInfoMap = stack.keyValueMap(name + SUFFIX_LOGIN_SESSION_INFO);
		sessionTokenMap = stack.keyValueMap(name + SUFFIX_LOGIN_TOKEN);
		sessionNextTokenMap = stack.keyValueMap(name + SUFFIX_LOGIN_NEXT_TOKEN);
		
		// Account meta information
		accountDataTable = stack.dataObjectMap(name + SUFFIX_ACCOUNT_META);
		accountPrivateDataTable = stack.dataObjectMap(name + SUFFIX_ACCOUNT_PRIVATE_META);
		
		// Login throttling information
		loginThrottlingAttemptMap = stack.keyLongMap(name + ACCOUNT_LOGIN_THROTTLING_ATTEMPT);
		loginThrottlingExpiryMap = stack.keyLongMap(name + ACCOUNT_LOGIN_THROTTLING_EXPIRY);
		
		// Account Verification information
		accountVerificationMap = stack.keyValueMap(name + ACCOUNT_VERIFICATION);
		
		// Account Password Token information
		accountPasswordTokenMap = stack.keyValueMap(name + ACCOUNT_PASSWORD_TOKEN);
		
		// Side note: For new table, edit here and add into the return List
		
		// @TODO - Consider adding support for temporary tabls typehints
		
		// Return it as a list
		return Arrays.asList(accountLoginNameMap, accountAuthMap, sessionLinkMap, sessionInfoMap,
			sessionTokenMap, sessionNextTokenMap, accountDataTable, accountPrivateDataTable,
			loginThrottlingAttemptMap, loginThrottlingExpiryMap, accountVerificationMap,
			accountPasswordTokenMap);
	}
	
	///////////////////////////////////////////////////////////////////////////
	//
	// Basic account object existance checks, setup, and destruction
	//
	///////////////////////////////////////////////////////////////////////////
	
	/**
	 * Checks if the email exists
	 *
	 * @param  email to check
	 *
	 * @return TRUE if email exists
	 */
	public boolean isEmailExist(String email) {
		return (accountLoginNameMap.getValue(email) == null) ? false : true;
	}
	
	/**
	 * Returns if the name exists
	 *
	 * @param  Login ID to use, normally this is an email, or nice username
	 *
	 * @return TRUE if login ID exists
	 **/
	public boolean hasLoginName(String inLoginName) {
		return accountLoginNameMap.containsKey(inLoginName);
	}
	
	/**
	 * Returns if the account object id exists
	 *
	 * @param  Account OID to use
	 *
	 * @return  TRUE of account ID exists
	 **/
	public boolean containsKey(Object oid) {
		return accountDataTable.containsKey(oid);
	}
	
	/**
	 * Generates a new account object.
	 *
	 * Note without setting a name, or any additional values.
	 * This call in some sense is quite, err useless.
	 **/
	public AccountObject newEntry() {
		AccountObject ret = new AccountObject(this, null);
		// ret.saveAll(); //ensures the blank object is now in DB
		return ret;
	}
	
	/**
	 * Generates a new account object with the given nice name
	 *
	 * @param  Unique Login ID to use, normally this is an email, or nice username
	 *
	 * @return AccountObject if succesfully created
	 **/
	public AccountObject newEntry(String name) {
		// Quick fail check
		if (hasLoginName(name)) {
			return null;
		}
		
		// Creating account object, setting the name if valid
		AccountObject ret = newEntry();
		if (ret.setLoginName(name)) {
			return ret;
		} else {
			// Removal step is required on failure,
			// as it helps prevent "orphaned" account objects
			// in new account race conditions
			remove(ret._oid());
		}
		
		return null;
	}
	
	/**
	 * Removes the accountObject using the ID
	 *
	 * @param  Account OID to use, or alternatively its object
	 *
	 * @return NULL
	 **/
	public AccountObject remove(Object inOid) {
		if (inOid != null) {
			
			// Alternatively, instead of string use DataObject
			if (inOid instanceof DataObject) {
				inOid = ((DataObject) inOid)._oid();
			}
			
			// Get oid as a string, and fetch the account object
			String oid = inOid.toString();
			AccountObject ao = this.get(oid);
			
			// Remove login ID's AKA nice names
			Set<String> loginIdMapNames = accountLoginNameMap.keySet(oid);
			if (loginIdMapNames != null) {
				for (String name : loginIdMapNames) {
					accountLoginNameMap.remove(name);
				}
			}
			// Remove account meta information
			accountLoginNameMap.remove(oid);
			accountPrivateDataTable.remove(oid);
			accountDataTable.remove(oid);
			
			// Remove login authentication details
			accountAuthMap.remove(oid);
			
			// Remove thorttling information
			loginThrottlingAttemptMap.remove(oid);
			loginThrottlingExpiryMap.remove(oid);
			System.out.println("Account Object: " + oid + " has been successfully removed.");
		}
		
		return null;
	}
	
	///////////////////////////////////////////////////////////////////////////
	//
	// Account object getters
	//
	///////////////////////////////////////////////////////////////////////////
	
	/**
	 * Gets and return the accounts object using the account ID
	 *
	 * @param  Account ID to use
	 *
	 * @return  AccountObject representing the account ID if found
	 **/
	public AccountObject get(Object oid) {
		// Possibly a valid OID?
		if (oid != null) {
			String _oid = oid.toString();
			if (containsKey(_oid)) {
				return new AccountObject(this, _oid);
			}
		}
		// Account object invalid here
		return null;
	}
	
	/**
	 * Gets the account UUID, using the configured name
	 *
	 * @param  The login ID (nice-name/email)
	 *
	 * @return  Account ID associated, if any
	 **/
	public String loginNameToAccountID(String name) {
		return accountLoginNameMap.getValue(name);
	}
	
	/**
	 * Gets the account using the nice name
	 *
	 * @param  The login ID (nice-name/email)
	 *
	 * @return  AccountObject representing the account ID if found
	 **/
	public AccountObject getFromLoginName(Object name) {
		String _oid = loginNameToAccountID(name.toString());
		if (_oid != null) {
			return get(_oid);
		}
		return null;
	}
	
	/**
	 * Gets the account using the Session ID
	 *
	 * @param  The Session ID
	 *
	 * @return  AccountObject representing the account ID if found
	 **/
	public AccountObject getFromSessionID(String sessionID) {
		String _oid = sessionLinkMap.getValue(sessionID);
		if (_oid != null) {
			return get(_oid);
		}
		return null;
	}
	
	///////////////////////////////////////////////////////////////////////////
	//
	// Login throttling configuration
	//
	///////////////////////////////////////////////////////////////////////////
	
	/**
	 * Login throttling lambda function, which can be overwritten for
	 * custom login throttling requirements
	 *
	 * @param   Account object in which login failed
	 * @param   Login attempts failed
	 *
	 * @return  Number of seconds to lock the account,
	 *          0 means an account is not locked
	 *          -1 means an account is locked permenantly
	 **/
	public BiFunction<AccountObject, Long, Long> calculateDelay = (inAO, attempts) -> {
		// Tries - Seconds locked
		// 1     - 0
		// 2     - 1
		// 3     - 3
		// 4     - 7
		// 5     - 15
		return (long) (Math.pow(2, (attempts - 1)) - 1);
	};
	
	///////////////////////////////////////////////////////////////////////////
	//
	// Map compliance
	//
	///////////////////////////////////////////////////////////////////////////
	
	/**
	 * Returns all the account _oid in the system
	 *
	 * @return  Set of account oid's
	 **/
	public Set<String> keySet() {
		return accountDataTable.keySet();
	}
	
	/** Returns the accountDataTable
	 *
	 * @return accountDataTable
	 **/
	public DataObjectMap accountDataTable() {
		return accountDataTable;
	}
	
	/** Returns the accountVerificationMap
	 *
	 * @return list of accountVerification data
	 **/
	public KeyValueMap accountVerificationMap() {
		return accountVerificationMap;
	}
	
	/** Returns the accountPasswordTokenMap
	 *
	 * @return list of accountPasswordToken data
	 **/
	public KeyValueMap accountPasswordTokenMap() {
		return accountPasswordTokenMap;
	}
	
	///////////////////////////////////////////////////////////////////////////
	//
	// Additional functionality add on
	//
	///////////////////////////////////////////////////////////////////////////
	
	/**
	 * Gets the account using the object ID array,
	 * and returns an account object array
	 *
	 * @param   Account object ID array
	 *
	 * @return  Array of corresponding account objects
	 **/
	public AccountObject[] getFromArray(String[] _oidList) {
		AccountObject[] mList = new AccountObject[_oidList.length];
		for (int a = 0; a < _oidList.length; ++a) {
			mList[a] = get(_oidList[a]);
		}
		return mList;
	}
	
	///////////////////////////////////////////////////////////////////////////
	//
	// Static login settings,
	//
	// highly doubt any of these needs to be changed.
	// but who knows in the future
	//
	///////////////////////////////////////////////////////////////////////////
	
	/**
	 * New session lifespan without token
	 **/
	public static int SESSION_NEW_LIFESPAN = 30;
	
	/**
	 * Race condition buffer for tokens
	 **/
	public static int SESSION_RACE_BUFFER = 10;
	
	///////////////////////////////////////////////////////////////////////////
	//
	// Login configuration and utilities
	//
	///////////////////////////////////////////////////////////////////////////
	
	/**
	 * defined login lifetime, default as 3600 seconds (aka 1 hr)
	 **/
	public int loginLifetime = 3600; // 1 hr = 60 (mins) * 60 (seconds) = 3600 seconds
	
	/**
	 * lifetime for http login token required for renewal, 1800 seconds (or half an hour)
	 **/
	public int loginRenewal = loginLifetime / 2; //
	
	/**
	 * Remember me lifetime, default as 2592000 seconds (aka 30 days)
	 **/
	public int rememberMeLifetime = 2592000; // 1 mth ~= 30 (days) * 24 (hrs) * 3600 (seconds in an hr)
	
	/**
	 * Remember me lifetime, default the same as loginRenewal
	 **/
	public int rememberMeRenewal = loginRenewal;
	
	/**
	 * Sets the cookie to be limited to http only
	 **/
	public boolean isHttpOnly = false;
	
	/**
	 * Sets the cookie to be via https only
	 **/
	public boolean isSecureOnly = false;
	
	/**
	 * Sets the cookie namespace prefix
	 **/
	public String cookiePrefix = "account_";
	
	/**
	 * Sets teh cookie domain, defaults is null
	 **/
	public String cookieDomain = null;
	
	/**
	 * The nonce size
	 **/
	public int nonceSize = 22;
	
	/**
	 * Cookie path settings to overwrite, use NULL to use contextPath (as detected)
	 **/
	public String cookiePath = null;
	
	///////////////////////////////////////////////////////////////////////////
	//
	// Actual login handling
	//
	// These features depends on the following packages
	//
	// import javax.servlet.ServletException;
	// import javax.servlet.http.HttpServletRequest;
	// import javax.servlet.http.HttpServletResponse;
	// import javax.servlet.http.Cookie;
	//
	///////////////////////////////////////////////////////////////////////////
	
	/**
	 * Internal call to store the actual cookie and the respective values
	 *
	 * @param   HTTP request to read server settings from
	 * @param   HTTP Response to write into
	 * @param   Session ID to store
	 * @param   Token ID to store
	 * @param   Remember me settings
	 * @param   The cookie lifetime, 0 deletes the cookie, else its ignore if remember me is false
	 * @param   The cookie expire timestamp
	 **/
	protected boolean storeCookiesInsideTheCookieJar(javax.servlet.http.HttpServletRequest request,
		javax.servlet.http.HttpServletResponse response, String sessionID, String tokenID,
		boolean rememberMe, int lifeTime, long expireTime) {
		
		// instant failure without response object
		if (response == null) {
			return false;
		}
		
		// Setup the cookie jar
		int noOfCookies = 4;
		javax.servlet.http.Cookie cookieJar[] = new javax.servlet.http.Cookie[noOfCookies];
		
		// Store session and token cookies
		cookieJar[0] = new javax.servlet.http.Cookie(cookiePrefix + "ses", sessionID);
		cookieJar[1] = new javax.servlet.http.Cookie(cookiePrefix + "tok", tokenID);
		
		// Remember me configuration
		// Should this be handled usign server side storage data?
		// If its not a valid security threat, this should be ok right?
		if (rememberMe) {
			cookieJar[2] = new javax.servlet.http.Cookie(cookiePrefix + "rmb", "1");
		} else {
			cookieJar[2] = new javax.servlet.http.Cookie(cookiePrefix + "rmb", "0");
		}
		
		// The cookie "exp"-iry store the other cookies (Rmbr, user, Nonc etc.) expiry life time in seconds.
		// This cookie value is used in JS (checkLogoutTime.js) for validating the login expiry time
		// and show a message to user accordingly.
		//
		// Note that this cookie IGNORES isHttpOnly setting
		cookieJar[3] = new javax.servlet.http.Cookie(cookiePrefix + "exp", String.valueOf(expireTime));
		
		// Storing the cookie jar with the browser
		for (int a = 0; a < noOfCookies; ++a) {
			
			/**
			 * Cookie Path is required for cross AJAX / domain requests,
			 * This is taken from the request settings, if not defined
			 **/
			String cPath = cookiePath;
			if (cPath == null) {
				if (request.getContextPath() == null || request.getContextPath().isEmpty()) {
					cPath = "/";
				} else {
					cPath = request.getContextPath();
				}
			}
			cookieJar[a].setPath(cPath);
			
			// If remember me is configured
			if (rememberMe || lifeTime == 0) {
				cookieJar[a].setMaxAge(lifeTime);
			} else {
				cookieJar[a].setMaxAge(-1);
			}
			
			// Set isHttpOnly flag, to prevent JS based session attacks
			// this is ignored for the expire timestamp field (index = 3)
			if (isHttpOnly && a != 3) {
				cookieJar[a].setHttpOnly(isHttpOnly);
			}
			
			// Set it to be https strict if relevent
			if (isSecureOnly) {
				cookieJar[a].setSecure(isSecureOnly);
			}
			
			// Set a strict cookie domain
			if (cookieDomain != null && cookieDomain.length() > 0) {
				cookieJar[a].setDomain(cookieDomain);
			}
			
			// Actually inserts the cookie
			response.addCookie(cookieJar[a]);
		}
		
		// Valid
		return true;
	}
	
	/**
	 * Utility function to get the configured cookie lifetime, with the relevent settings
	 *
	 * @param  remember configuration boolean
	 *
	 * @return configured lifetime (not expire time)
	 **/
	protected int getLifeTime(boolean rememberMe) {
		if (rememberMe) {
			return rememberMeLifetime;
		} else {
			return loginLifetime;
		}
	}
	
	/**
	 * Performs the login to a user (handles the respective session tokens) and set the cookies for the response.
	 *
	 * As this does the login without the actual password authentication steps.
	 * Unless you are creating a custom login intergration. DO NOT USE this, and use loginUser instead, with the
	 * relevent username and password.
	 *
	 * The cookie is configured to store the following information under the "cookiePrefix" (default Account_)
	 * + Session ID
	 * + Token ID
	 * + Expiriry Timestamp (for JS to read)
	 * + Remember Me flag
	 *
	 * @param  Account object used
	 * @param  The http request to read
	 * @param  The http response to write into
	 * @param  Indicator for "remember me" functionality
	 * @param  Session information map to use, useful to set custom flags, can be null
	 *
	 * @return  Login success or failure
	 **/
	public boolean bypassSecurityChecksAndPerformNewAccountLogin(AccountObject ao,
		javax.servlet.http.HttpServletRequest request,
		javax.servlet.http.HttpServletResponse response, boolean rememberMe,
		Map<String, Object> sessionInfo) {
		// Null check
		if (request == null) {
			return false;
		}
		
		// Prepare the vars
		//-----------------------------------------------------
		String aoid = ao._oid();
		
		// Detirmine the login lifetime
		int lifeTime = getLifeTime(rememberMe);
		long expireTime = ((System.currentTimeMillis()) / 1000L + lifeTime) * 1000L;
		
		// Session info handling
		//-----------------------------------------------------
		
		// Prepare the session info
		if (sessionInfo == null) {
			sessionInfo = new HashMap<String, Object>();
		}
		
		// Lets do some USER_AGENT sniffing
		sessionInfo.put("USER_AGENT", request.getHeader("USER_AGENT"));
		
		// @TODO : Conisder sniffing additional info such as IP address
		
		// Generate the session and tokens
		//-----------------------------------------------------
		
		String sessionID = ao.newSession(sessionInfo);
		String tokenID = ao.newToken(sessionID, expireTime);
		
		// Store the cookies, and end
		//-----------------------------------------------------
		return storeCookiesInsideTheCookieJar(request, response, sessionID, tokenID, rememberMe,
			lifeTime, expireTime);
	}
	
	/**
	 * Logout any existing users
	 *
	 * @param  The http request to read
	 * @param  The http response to write into
	 *
	 * @return  Logout success or failure
	 **/
	public boolean logoutAccount(javax.servlet.http.HttpServletRequest request,
		javax.servlet.http.HttpServletResponse response) {
		if (response == null) {
			return false;
		}
		
		return storeCookiesInsideTheCookieJar(request, response, "-", "-", false, 0, 0);
	}
	
	/**
	 * Validates the user retur true/false, with an update response cookie / token if needed
	 *
	 * NOTE: login session renewal will not be performed on request url containing the keyword "logout"
	 *
	 * @param   http servlet request
	 * @param   http servlet response (optional)
	 *
	 * @return  Valid logged in account objec
	 **/
	public AccountObject getRequestUser(javax.servlet.http.HttpServletRequest request,
		javax.servlet.http.HttpServletResponse response) {
		// Null check
		if (request == null) {
			return null;
		}
		
		javax.servlet.http.Cookie[] cookieJar = request.getCookies();
		if (cookieJar == null) {
			return null;
		}
		
		// Gets the existing cookie settings
		//----------------------------------------------------------
		String sessionID = null;
		String tokenID = null;
		boolean rememberMe = false;
		
		for (javax.servlet.http.Cookie crumbs : cookieJar) {
			String crumbsFlavour = crumbs.getName();
			
			if (crumbsFlavour == null) {
				continue;
			} else if (crumbsFlavour.equals(cookiePrefix + "ses")) {
				sessionID = crumbs.getValue();
			} else if (crumbsFlavour.equals(cookiePrefix + "tok")) {
				tokenID = crumbs.getValue();
			} else if (crumbsFlavour.equals(cookiePrefix + "rmb")) {
				rememberMe = "1".equals(crumbs.getValue());
			}
		}
		
		// Time to validate the cookie settings
		//----------------------------------------------------------
		
		// Check if a session id and token id was provided
		// in a valid format
		if (sessionID == null || tokenID == null || sessionID.length() < 22 || tokenID.length() < 22) {
			return null;
		}
		
		// If an invalid session / token ID is provided, assume logout
		AccountObject ret = getFromSessionID(sessionID);
		
		// Session ID fails to fetch an account object
		if (ret == null) {
			logoutAccount(request, response);
			return null;
		}
		
		// Get the token lifespan, not that this also
		// check for invalid session and token
		long tokenLifespan = ret.getTokenLifespan(sessionID, tokenID);
		if (tokenLifespan <= 0) {
			// Does logout on invalid token
			logoutAccount(request, response);
			return null;
		}
		
		// From this point onwards, the session is valid. Now it performs checks for the renewal process
		// Does nothing if response object is not given
		//---------------------------------------------------------------------------------------------------
		
		// Do not set cookies if it is logout request, and return the result.
		// This is to prevent session renewal and revoking from happening simultainously
		// creating unexpected behaviour
		//
		// @TODO : Consider a more fixed pattern?
		if (request.getPathInfo() != null && request.getPathInfo().indexOf("logout") > 0) {
			return ret;
		}
		
		if (response != null) {
			
			// Renewal checking
			boolean needRenewal = false;
			if (rememberMe) {
				if (tokenLifespan < rememberMeRenewal) { // needs renewal (perform it!)
					needRenewal = true;
				}
			} else {
				if (tokenLifespan < loginRenewal) { // needs renewal (perform it!)
					needRenewal = true;
				}
			}
			
			// Actual renewal process
			if (needRenewal) {
				// Detirmine the renewed login lifetime and expirary to set (if new issued token)
				long expireTime = (System.currentTimeMillis()) / 1000L + getLifeTime(rememberMe);
				expireTime *= 1000L;	// change to miliseconds

				// Issue the next token
				String nextTokenID = ret.issueNextToken(sessionID, tokenID, expireTime);
				
				// Get the actual expiry of the next token (if it was previously issued)
				expireTime = ret.getTokenExpiry(sessionID, nextTokenID);				
				
				// If nextTokenID and expireTime fails, assume login failure
				if (nextTokenID == null || expireTime < 0) {
					logoutAccount(request, response);
					return null;
				}
				
				// Get lifespan
				long lifespan = expireTime - (System.currentTimeMillis()) / 1000L;
				
				// Setup the next token
				storeCookiesInsideTheCookieJar(request, response, sessionID, nextTokenID, rememberMe,
					(int) lifespan, expireTime); // lifespan in seconds
			}
		}
		
		// Return the validated account object
		//---------------------------------------------------------------------------------------------------
		return ret;
	}

	public void setLoginLifetimeValue(int inLoginLifetime){
		loginLifetime = inLoginLifetime;
		loginRenewal = loginLifetime / 2;
	}

	/**
	 * Login the user if the given values are valid, and return its account object
	 *
	 * @param   http servlet request
	 * @param   http servlet response
	 * @param   Account object to perform login
	 * @param   Raw password to validate
	 * @param   Remember me boolean (if set)
	 *
	 * @return  The logged in account object
	 **/
	public AccountObject loginAccount(javax.servlet.http.HttpServletRequest request,
		javax.servlet.http.HttpServletResponse response, AccountObject accountObj,
		String rawPassword, boolean rememberMe) {
		if (accountObj != null && accountObj.validatePassword(rawPassword)) {
			bypassSecurityChecksAndPerformNewAccountLogin(accountObj, request, response, rememberMe,
				null);
			return accountObj;
		}
		return null;
	}
	
	/**
	 * Login the user if the given values are valid, and return its account object
	 *
	 * @param   http servlet request
	 * @param   http servlet response
	 * @param   Account nice login ID (normally email)
	 * @param   Raw password to validate
	 * @param   Remember me boolean (if set)
	 *
	 * @return  The logged in account object
	 **/
	public AccountObject loginAccount(javax.servlet.http.HttpServletRequest request,
		javax.servlet.http.HttpServletResponse response, String nicename, String rawPassword,
		boolean rememberMe) {
		return loginAccount(request, response, getFromLoginName(nicename), rawPassword, rememberMe);
	}
}
