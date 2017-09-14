package picoded.dstack.module.account;

import java.util.*;

import picoded.dstack.*;
import picoded.dstack.core.*;
import picoded.core.conv.*;
import picoded.core.struct.*;
import picoded.util.security.*;

import static picoded.servlet.api.module.account.AccountConstantStrings.*;

/**
 * Represents a single group / user account.
 **/
public class AccountObject extends Core_DataObject {

	///////////////////////////////////////////////////////////////////////////
	//
	// Constructor and setup
	//
	///////////////////////////////////////////////////////////////////////////

	/**
	 * The original account table
	 **/
	protected AccountTable mainTable = null;

	/**
	 * [INTERNAL USE ONLY]
	 *
	 * Cosntructor setup, using an account table,
	 * and the account GUID
	 **/
	protected AccountObject(AccountTable accTable, String inOID) {
		super((Core_DataTable) (accTable.accountDataTable), inOID);
		mainTable = accTable;
	}

	///////////////////////////////////////////////////////////////////////////
	//
	// Getting and setting login ID's
	//
	///////////////////////////////////////////////////////////////////////////

	/**
	 * Checks if the current account has the provided LoginName
	 *
	 * @param  LoginName to use
	 *
	 * @return TRUE if login ID belongs to this account
	 **/
	public boolean hasLoginName(String name) {
		return _oid.equals(mainTable.accountLoginNameMap.get(name));
	}

	/**
	 * Gets and return the various login "nice-name" (not UUID) for this account
	 *
	 * @return  Set of LoginName's used by this account
	 **/
	public Set<String> getLoginNameSet() {
		return mainTable.accountLoginNameMap.keySet(_oid);
	}

	/**
	 * Sets the name for the account, returns true or false if it succed.
	 *
	 * @param  LoginName to setup for this account
	 *
	 * @return TRUE if login ID is configured to this account
	 **/
	public boolean setLoginName(String name) {
		if (name == null || name.length() <= 0) {
			throw new RuntimeException("AccountObject loding ID cannot be blank");
		}

		if (mainTable.hasLoginName(name)) {
			return false;
		}

		// ensure its own OID is registered
		saveDelta();

		// Technically a race condition =X
		//
		// But name collision, if its an email collision should be a very rare event.
		mainTable.accountLoginNameMap.put(name, _oid);
		// Populate the name list
		populateLoginNameList(name);

		// Success of failure
		return hasLoginName(name);
	}

	/**
	 * Removes the old name from the database
	 *
	 * @param  LoginName to setup for this account
	 **/
	public void removeLoginName(String name) {
		if (hasLoginName(name)) {
			mainTable.accountLoginNameMap.remove(name);
			removeLoginNameFromList(name);
		}
	}

	/**
	 * Sets the name as a unique value, delete all previous alias
	 *
	 * @param  LoginName to setup for this account
	 *
	 * @return TRUE if login ID is configured to this account
	 **/
	public boolean setUniqueLoginName(String name) {

		// The old name list, to check if new name already is set
		Set<String> oldNamesList = getLoginNameSet();

		// Check if name exist in list
		if (Arrays.asList(oldNamesList).contains(name)) {
			// Already exists in the list, does nothing
		} else {
			// Name does not exist, attempt to set the name
			if (!setLoginName(name)) {
				// Failed to setup the name, terminate
				return false;
			}
		}

		// Iterate the names, delete uneeded ones
		for (String oldName : oldNamesList) {
			// Skip the unique name,
			// prevent it from being deleted
			if (oldName.equals(name)) {
				continue;
			}
			// Remove the login ID
			mainTable.accountLoginNameMap.remove(oldName);
			removeLoginNameFromList(oldName);
		}

		return true;
	}

	///////////////////////////////////////////////////////////////////////////
	//
	// Syncronysing login names from authentication table, to account info
	//
	// NOTE: This is not actually used for authentication, but for convienience
	//
	///////////////////////////////////////////////////////////////////////////

	/**
	* Add the login name list to the account info object
	*
	* @param Name to be added
	*/
	protected void populateLoginNameList(String name){
		if (name == null || name.length() <= 0) {
			throw new RuntimeException("No Login Name to be set.");
		}

		// Get the current list of login names
		List<String> loginNames = this.getList(LOGINNAMELIST, new ArrayList<String>());
		// Add if not exists
		if (!loginNames.contains(name)){
			loginNames.add(name);
		}
		// Put it back to the object
		this.put(LOGINNAMELIST, loginNames);
		// Save the object
		this.saveDelta();
	}

	/**
	* Remove the login name in the list of the account info object
	*
	* @param Name to be removed
	*/
	protected void removeLoginNameFromList(String name){
		if (name == null || name.length() <= 0) {
			throw new RuntimeException("No Login Name to be remove.");
		}

		// Get the current list of login names
		List<String> loginNames = this.getList(LOGINNAMELIST, new ArrayList<String>());
		// Add if not exists
		if (!loginNames.contains(name)){
			loginNames.remove(name);
		}
		// Put it back to the object
		this.put(LOGINNAMELIST, loginNames);
		// Save the object
		this.saveDelta();
	}

	///////////////////////////////////////////////////////////////////////////
	//
	// Password management
	//
	///////////////////////////////////////////////////////////////////////////

	/**
	 * Gets and returns the stored password hash,
	 * Intentionally made protected to avoid accidental use externally
	 *
	 * @return  Password salted hash, as per NxtCrypt usage
	 **/
	protected String getPasswordHash() {
		return mainTable.accountAuthMap.get(_oid);
	}

	/**
	 * Indicates if the current account has a configured password, it is possible there is no password
	 * if it functions as a group. Or is passwordless login
	 *
	 * @return  True if password was configured
	 **/
	public boolean hasPassword() {
		String h = getPasswordHash();
		return (h != null && h.length() > 0);
	}

	/**
	 * Remove the account password
	 * This should only be used for group type account objects
	 **/
	public void removePassword() {
		mainTable.accountAuthMap.remove(_oid);
	}

	/**
	 * Validate if the given password is valid
	 *
	 * @param  raw Password string to validate
	 *
	 * @return  True if password is valid
	 **/
	public boolean validatePassword(String pass) {
		String hash = getPasswordHash();
		if (hash != null) {
			return NxtCrypt.validatePassHash(hash, pass);
		}
		return false;
	}

	/**
	 * Set the account password
	 *
	 * @param  raw Password string to setup
	 **/
	public void setPassword(String pass) {
		// ensure its own OID is registered
		saveDelta();

		// Setup the password
		if (pass == null) {
			removePassword();
		} else {
			mainTable.accountAuthMap.put(_oid, NxtCrypt.getPassHash(pass));
		}
	}

	/**
	 * Set the account password, after checking old password
	 *
	 * @param  raw Password string to setup
	 * @param  old Password to validate
	 *
	 * @return  True if password change was valid
	 **/
	public boolean setPassword(String pass, String oldPass) {
		if (validatePassword(oldPass)) {
			setPassword(pass);
			return true;
		}
		return false;
	}

	///////////////////////////////////////////////////////////////////////////
	//
	// Session management
	//
	///////////////////////////////////////////////////////////////////////////

	/**
	 * Checks if the current session is associated with the account
	 *
	 * @param  Session ID to validate
	 *
	 * @return TRUE if login ID belongs to this account
	 **/
	public boolean hasSession(String sessionID) {
		return sessionID != null && _oid.equals(mainTable.sessionLinkMap.get(sessionID));
	}

	/**
	 * List the various session ID's involved with this account
	 *
	 * @return  Set of login session ID's
	 **/
	public Set<String> getSessionSet() {
		return mainTable.sessionLinkMap.keySet(_oid);
	}

	/**
	 * Get and return the session information meta
	 *
	 * @param  Session ID to get
	 *
	 * @return  Information meta if session is valid
	 **/
	public Map<String, Object> getSessionInfo(String sessionID) {
		// Validate that session is legit
		if (!hasSession(sessionID)) {
			return null;
		}

		// Return the session information
		return mainTable.sessionInfoMap.getStringMap(sessionID, null);
	}

	/**
	 * Generate a new session with the provided meta information
	 *
	 * Additionally if no tokens are generated and issued in the next
	 * 30 seconds, the session will expire.
	 *
	 * Subseqently session expirary will be tag to
	 * the most recently generated token.
	 *
	 * Additionally info object is INTENTIONALLY NOT stored as a
	 * DataObject, for performance reasons.
	 *
	 * @param  Meta information map associated with the session,
	 *         a blank map is assumed if not provided.
	 *
	 * @return  The session ID used
	 **/
	public String newSession(Map<String, Object> info) {

		// Normalize the info object map
		if (info == null) {
			info = new HashMap<String, Object>();
		}

		// Set the session expirary time : 30 seconds (before tokens)
		long expireTime = (System.currentTimeMillis()) / 1000L + AccountTable.SESSION_NEW_LIFESPAN;

		// Generate a base58 guid for session key
		String sessionID = GUID.base58();

		// As unlikely as it is, on GUID collision,
		// we do not want any session swarp EVER
		if (mainTable.sessionLinkMap.get(sessionID) != null) {
			throw new RuntimeException("GUID collision for sessionID : " + sessionID);
		}

		// Time to set it all up, with expire timestamp
		mainTable.sessionLinkMap.putWithExpiry(sessionID, _oid, expireTime);
		mainTable.sessionInfoMap.putWithExpiry(sessionID, ConvertJSON.fromMap(info), expireTime
			+ AccountTable.SESSION_RACE_BUFFER);

		// Return the session key
		return sessionID;
	}

	/**
	 * Revoke a session, associated to this account.
	 *
	 * This will also revoke all tokens associated to this session
	 *
	 * @param  SessionID to revoke
	 **/
	public void revokeSession(String sessionID) {
		// Validate the session belongs to this account !
		if (hasSession(sessionID)) {
			// Session ownership validated, time to revoke!

			// Revoke all tokens associated to this session
			revokeAllToken(sessionID);

			// Revoke the session info
			mainTable.sessionLinkMap.remove(sessionID);
			mainTable.sessionInfoMap.remove(sessionID);
		}
	}

	/**
	 * Revoke all sessions, associated to this account
	 **/
	public void revokeAllSession() {
		Set<String> sessions = getSessionSet();
		for (String oneSession : sessions) {
			revokeSession(oneSession);
		}
	}

	///////////////////////////////////////////////////////////////////////////
	//
	// Session token management
	//
	///////////////////////////////////////////////////////////////////////////

	/**
	 * Checks if the current session token is associated with the account
	 *
	 * @param  Session ID to validate
	 * @param  Token ID to validate
	 *
	 * @return TRUE if login ID belongs to this account
	 **/
	public boolean hasToken(String sessionID, String tokenID) {
		return hasSession(sessionID) && sessionID.equals(mainTable.sessionTokenMap.get(tokenID));
	}

	/**
	 * Get the token set associated with the session and account
	 *
	 * @param   Session ID to fetch from
	 *
	 * @return  The list of token ID's currently associated to this session
	 *          null, if session is not valid.
	 **/
	public Set<String> getTokenSet(String sessionID) {
		if (hasSession(sessionID)) {
			return mainTable.sessionTokenMap.keySet(sessionID);
		}
		return new HashSet<String>();
	}

	/**
	 * Generate a new token, with a timeout
	 *
	 * Note that this token will update the session timeout,
	 * even if there was a longer session previously set.
	 *
	 * @param  Session ID to generate token from
	 * @param  The expire timestamp of the token
	 *
	 * @return  The tokenID generated, null on invalid session
	 **/
	public String newToken(String sessionID, long expireTime) {

		// Terminate if session is invalid
		if (!hasSession(sessionID)) {
			return null;
		}

		// Generate a base58 guid for session key
		String tokenID = GUID.base58();

		// Issue the token
		registerToken(sessionID, tokenID, GUID.base58(), expireTime);

		// Return the token
		return tokenID;
	}

	/**
	 * Internal function, used to issue a token ID to the session
	 * with the next token ID predefined
	 *
	 * @param  Session ID to generate token from
	 * @param  Token ID to setup
	 * @param  Next token ID
	 * @param  The expire timestamp of the token
	 *
	 * @return  The tokenID generated, null on invalid session
	 **/
	protected void registerToken(String sessionID, String tokenID, String nextTokenID,
		long expireTime) {
		// Current token check, does nothing if invalid
		if (hasToken(sessionID, tokenID)) {
			return;
		}

		// Check if token has already been registered
		String existingTokenSession = mainTable.sessionTokenMap.getString(tokenID, null);
		if (existingTokenSession != null) {
			// Check if token has valid session
			if (sessionID.equals(existingTokenSession)) {
				// Assume setup was already done, terminating
				return;
			} else {
				// Invalid next token, was issued to another session
				// EITHER a GUID collision occured, OR spoofing is being attempted
				throw new RuntimeException(
					"FATAL : Unable to register token previously registered to another session ID");
			}
		}

		// Renew every session!
		mainTable.sessionLinkMap.setExpiry(sessionID, expireTime + AccountTable.SESSION_RACE_BUFFER);
		mainTable.sessionInfoMap.setExpiry(sessionID, expireTime + AccountTable.SESSION_RACE_BUFFER
			* 2);

		// Register the token
		mainTable.sessionTokenMap.putWithExpiry(tokenID, sessionID, expireTime);
		mainTable.sessionNextTokenMap.putWithExpiry(tokenID, nextTokenID, expireTime);
	}

	/**
	 * Checks if the current session token is associated with the account
	 *
	 * @param  Session ID to validate
	 * @param  Token ID to revoke
	 *
	 * @return TRUE if login ID belongs to this account
	 **/
	public boolean revokeToken(String sessionID, String tokenID) {
		if (hasToken(sessionID, tokenID)) {
			mainTable.sessionTokenMap.remove(tokenID);
			return true;
		}
		return false;
	}

	/**
	 * Revokes all tokens associated to a session
	 *
	 * @param  Session ID to revoke
	 **/
	public void revokeAllToken(String sessionID) {
		Set<String> tokens = getTokenSet(sessionID);
		for (String oneToken : tokens) {
			revokeToken(sessionID, oneToken);
		}
	}

	/**
	 * Get token expiriry
	 *
	 * @param  Session ID to validate
	 * @param  Token ID to check
	 *
	 * @return  The token expiry timestamp
	 **/
	public long getTokenExpiry(String sessionID, String tokenID) {
		if (hasToken(sessionID, tokenID)) {
			return mainTable.sessionTokenMap.getExpiry(tokenID);
		}
		return -1;
	}

	/**
	 * Get token remaining lifespan
	 *
	 * @param  Session ID to validate
	 * @param  Token ID to check
	 *
	 * @return  The token remaining timespan, -1 means invalid token
	 **/
	public long getTokenLifespan(String sessionID, String tokenID) {
		// Get expiry timestamp
		long expiry = getTokenExpiry(sessionID, tokenID);

		// Invalid tokens are -1
		if (expiry <= -1) {
			return -1;
		}

		long lifespan = expiry - (System.currentTimeMillis()) / 1000L;
		if (lifespan < -1) {
			return -1;
		}
		return lifespan;
	}

	/**
	 * Internal function, used to get the next token given a session and current token.
	 * DOES NOT : Validate the next token if it exists
	 *
	 * @param  Session ID to validate
	 * @param  Token ID to check
	 *
	 * @return The next token
	 **/
	protected String getUncheckedNextToken(String sessionID, String tokenID) {
		if (hasToken(sessionID, tokenID)) {
			return mainTable.sessionNextTokenMap.get(tokenID);
		}
		return null;
	}

	/**
	 * Get the next token AFTER validating if it is issued
	 *
	 * @param  Session ID to validate
	 * @param  Token ID to check
	 *
	 * @return The next token ID
	 **/
	public String getNextToken(String sessionID, String tokenID) {
		String nextToken = getUncheckedNextToken(sessionID, tokenID);
		if (mainTable.sessionTokenMap.containsKey(nextToken)) {
			return nextToken;
		}
		return null;
	}

	/**
	 * Generate next token in line, with expiry
	 * Note that expiry is NOT set, if token was previously issued
	 *
	 * @param  Session ID to validate
	 * @param  Token ID to check
	 * @param  The expire timestamp of the token
	 *
	 * @return The next token ID
	 **/
	public String issueNextToken(String sessionID, String tokenID, long expireTime) {
		// Get NextToken, and returns (if previously issued)
		String nextToken = getUncheckedNextToken(sessionID, tokenID);
		// Terminate if nextToken is invalid
		if (nextToken == null) {
			return null;
		}

		// Issue next token
		registerToken(sessionID, nextToken, GUID.base58(), expireTime);
		// Return the next token, after its been issued
		return nextToken;
	}

	///////////////////////////////////////////////////////////////////////////
	//
	// Group Configuration and Management
	//
	///////////////////////////////////////////////////////////////////////////

	// Group Status and Management
	// -------------------------------------------------------------------------

	// Group Management of Users
	//-------------------------------------------------------------------------

	/**
	 * Gets the cached child map
	 **/
	protected DataObject _group_userToRoleMap = null;

	/**
	 * Gets the child map (cached?)
	 **/
	protected DataObject group_userToRoleMap() {
		if (_group_userToRoleMap != null) {
			return _group_userToRoleMap;
		}
		// true = uncheckedGet
		return (_group_userToRoleMap = mainTable.memberRolesTable.get(this._oid(), true));
	}

	/**
	 * Gets and returns the member role, if it exists
	 **/
	public String getMemberRole(AccountObject memberObject) {
		return group_userToRoleMap().getString(memberObject._oid());
	}

	/**
	 * Gets and returns the member meta map, if it exists
	 * Only returns if member exists, else null
	 **/
	public DataObject getMember(AccountObject memberObject) {
		String memberOID = memberObject._oid();
		String level = group_userToRoleMap().getString(memberOID);
		if (level == null || level.length() <= 0) {
			return null;
		}
		// true = uncheckedGet
		return mainTable.memberDataTable.get(
			AccountTable.getGroupChildMetaKey(this._oid(), memberOID), true);
	}

	/**
	 * Gets and returns the member meta map, if it exists
	 * Only returns if member exists and matches role, else null
	 **/
	public DataObject getMember(AccountObject memberObject, String role) {
		role = mainTable.validateMembershipRole(this._oid(), role);
		String memberOID = memberObject._oid();
		String level = group_userToRoleMap().getString(memberOID);
		if (level == null || !level.equals(role)) {
			return null;
		}
		// true = uncheckedGet
		return mainTable.memberDataTable.get(
			AccountTable.getGroupChildMetaKey(this._oid(), memberOID), true);
	}

	/**
	 * Adds the member to the group with the given role, if it was not previously added
	 *
	 * Returns the group-member unique meta object, null if previously exists
	 **/
	public DataObject addMember(AccountObject memberObject, String role) {
		// Gets the existing object, if exists terminates
		if (getMember(memberObject) != null) {
			return null;
		}
		// Set and return a new member object
		return setMember(memberObject, role);
	}

	/**
	 * Adds the member to the group with the given role, or update the role if already added
	 *
	 * Returns the group-member unique meta object
	 **/
	public DataObject setMember(AccountObject memberObject, String role) {
		role = mainTable.validateMembershipRole(this._oid(), role);
		if (role == null) {
			return null;
		}
		String memberOID = memberObject._oid();
		String level = group_userToRoleMap().getString(memberOID);
		DataObject childMeta = null;

		if (level == null || !level.equals(role)) {

			memberObject.saveDelta();
			setGroupStatus(true);

			group_userToRoleMap().put(memberOID, role);
			group_userToRoleMap().saveDelta();

			// true = uncheckedGet
			childMeta = mainTable.memberDataTable.get(
				AccountTable.getGroupChildMetaKey(this._oid(), memberOID), true);
			childMeta.put(PROPERTIES_ROLE, role);
			childMeta.saveDelta();
		} else {
			// true = uncheckedGet
			childMeta = mainTable.memberDataTable.get(
				AccountTable.getGroupChildMetaKey(this._oid(), memberOID), true);
		}
		return childMeta;
	}

	public DataObject removeMember(AccountObject memberObject) {
		if (!this.isGroup()) {
			return this;
		}

		String memberOID = memberObject._oid();
		String userRoleInGroup = group_userToRoleMap().getString(memberOID);
		if (userRoleInGroup == null) {
			return null;
		}
		group_userToRoleMap().remove(memberOID);
		group_userToRoleMap().saveAll();

		mainTable.memberDataTable.remove(AccountTable.getGroupChildMetaKey(this._oid(), memberOID));
		mainTable.memberPrivateDataTable.remove(AccountTable.getGroupChildMetaKey(this._oid(),
			memberOID));
		System.out.println("Remove member called successfully");

		return memberObject;
	}

	// Group Status Check
	//-------------------------------------------------------------------------

	/**
	 * Returns if set as group
	 **/
	public boolean isGroup() {
		Object status = this.get(PROPERTIES_IS_GROUP);
		if (status instanceof Number && //
			((Number) status).intValue() >= 1) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Sets if the account is a group
	 **/
	public void setGroupStatus(boolean enabled) {
		if (enabled) {
			this.put(PROPERTIES_IS_GROUP, new Integer(1));
		} else {
			this.put(PROPERTIES_IS_GROUP, new Integer(0));
		}
		this.saveDelta();
	}

	// Group Configuration
	// -------------------------------------------------------------------------
	protected DataObject _group_membershipRoles = null;

	public DataObject group_membershipRoles() {
		if (_group_membershipRoles != null) {
			return _group_membershipRoles;
		}
		return (_group_membershipRoles = mainTable.accountPrivateDataTable.get(this._oid(), true));
	}

	public DataObject addNewMembershipRole(String role) {
		List<String> currentRoles = group_membershipRoles().getList(PROPERTIES_MEMBERSHIP_ROLE, "[]");
		if (currentRoles.contains(role)) {
			return group_membershipRoles();
		}
		currentRoles.add(role);
		return setMembershipRoles(currentRoles);
	}

	public DataObject setMembershipRoles(List<String> roles) {
		this.setGroupStatus(true);
		_group_membershipRoles = null;
		if (!roles.contains("admin"))
			roles.add("admin");
		group_membershipRoles().put(PROPERTIES_MEMBERSHIP_ROLE, roles);
		group_membershipRoles().saveDelta();
		return group_membershipRoles();
	}

	public DataObject removeMembershipRole(String role) {
		List<String> currentRoles = group_membershipRoles().getList(PROPERTIES_MEMBERSHIP_ROLE, "[]");
		if (!currentRoles.contains(role)) {
			return null;
		}
		currentRoles.remove(role);
		return setMembershipRoles(currentRoles);
	}

	/// Returns the list of members in the group
	///
	public String[] getMembers_id() {
		List<String> retList = new ArrayList<String>();
		for (String key : group_userToRoleMap().keySet()) {
			if (key.equals("_oid")) {
				continue;
			}
			retList.add(key);
		}
		return retList.toArray(new String[retList.size()]);
	}

	/// Returns the list of groups the member is in
	///
	public String[] getGroups_id() {
		return mainTable.memberRolesTable.getFromKeyName_id(_oid());
	}

	/// Gets all the members object related to the group
	///
	public AccountObject[] getMembersAccountObject() {
		String[] idList = getMembers_id();
		AccountObject[] objList = new AccountObject[idList.length];
		for (int a = 0; a < idList.length; ++a) {
			objList[a] = mainTable.get(idList[a]);
		}
		return objList;
	}

	/// Gets all the groups object the user is in
	///
	public AccountObject[] getGroups() {
		return mainTable.getFromArray(getGroups_id());
	}

	// Group management of users
	//-------------------------------------------------------------------------

	// Is super user group handling
	//-------------------------------------------------------------------------

	/// Returns if its a super user
	///
	public boolean isSuperUser() {
		AccountObject superUserGrp = mainTable.superUserGroup();
		if (superUserGrp == null) {
			return false;
		}

		String superUserGroupRole = superUserGrp.getMemberRole(this);
		return (superUserGroupRole != null && superUserGroupRole.equalsIgnoreCase("admin"));
	}

	/**
	 * This method logs the details about login faailure for the user based on User ID
	 **/
	public void logLoginFailure(String userID) {
		mainTable.loginThrottlingAttemptMap.put(userID, 1);
		int elapsedTime = ((int) (System.currentTimeMillis() / 1000)) + 2;
		mainTable.loginThrottlingExpiryMap.put(userID, elapsedTime);
	}

	/**
	 * This method returns time left before next permitted login attempt for the user based on User ID
	 **/
	public int getNextLoginTimeAllowed(String userId) {
		long val = getExpiryTime(userId);
		if (val == -1) {
			return 0;
		}
		int allowedTime = (int) val - (int) (System.currentTimeMillis() / 1000);
		return allowedTime > 0 ? allowedTime : 0;
	}

	/**
	 * This method would be added in on next login failure for the user based on User ID
	 **/
	public long getTimeElapsedNextLogin(String userId) {
		long elapsedValue = getExpiryTime(userId);
		if (elapsedValue == -1) {
			return (System.currentTimeMillis() / 1000) + 2;
		}
		return elapsedValue;
	}

	/**
	 * This method would be added the delay for the user based on User ID
	 **/
	public void addDelay(AccountObject ao) {
		String userId = ao._oid();
		long attemptValue = getAttempts(userId);
		if (attemptValue == -1) {
			logLoginFailure(userId);
		} else {
			attemptValue++;
			int elapsedValue = (int) (System.currentTimeMillis() / 1000);
			elapsedValue += mainTable.calculateDelay.apply(ao, attemptValue);
			mainTable.loginThrottlingAttemptMap.put(userId, attemptValue);
			mainTable.loginThrottlingExpiryMap.put(userId, elapsedValue);
		}
	}

	public long getAttempts(String userID) {
		return (mainTable.loginThrottlingAttemptMap.get(userID) != null) ? mainTable.loginThrottlingAttemptMap
			.get(userID) : -1;
	}

	public long getExpiryTime(String userID) {
		return (mainTable.loginThrottlingExpiryMap.get(userID) != null) ? mainTable.loginThrottlingExpiryMap
			.get(userID) : -1;
	}

	/**
	 * This method remove the entries for the user (should call after successful login)
	 **/
	public void resetLoginThrottle(String userId) {
		mainTable.loginThrottlingAttemptMap.put(userId, null);
		mainTable.loginThrottlingExpiryMap.put(userId, null);
	}

	///////////////////////////////////////////////////////////////////////////
	//
	// Private Meta Data Table Management
	//
	///////////////////////////////////////////////////////////////////////////
	private DataObject _accountPrivateDataTable = null;

	public void setPrivateMetaData(String key, Object value){
		// Create a new private data for the account if it does not exists
		if ( mainTable.accountPrivateDataTable.get(this._oid()) == null ){
			_accountPrivateDataTable = mainTable.accountPrivateDataTable.get(this._oid(), true);
		}
		// Set the table if it is null
		if ( _accountPrivateDataTable == null ){
			_accountPrivateDataTable = mainTable.accountPrivateDataTable.get(this._oid());
		}
		// Put in the details and save it
		_accountPrivateDataTable.put(key, value);
		_accountPrivateDataTable.saveDelta();
	}

	public String getPrivateMetaStringData(String key){
		if ( _accountPrivateDataTable != null ){
			return _accountPrivateDataTable.getString(key, "");
		}
		// Create a new private data for the account if it does not exists
		if ( mainTable.accountPrivateDataTable.get(this._oid()) == null ){
			_accountPrivateDataTable = mainTable.accountPrivateDataTable.get(this._oid(), true);
		}

		_accountPrivateDataTable = mainTable.accountPrivateDataTable.get(this._oid());
		return _accountPrivateDataTable.getString(key, "");
	}

}
