package picoded.dstack.module.account;

import java.util.*;

import picoded.dstack.*;
import picoded.dstack.core.*;
import picoded.conv.*;
import picoded.struct.*;
import picoded.security.*;

///
/// Represents a single group / user account.
///
public class AccountObject extends Core_MetaObject {

	///////////////////////////////////////////////////////////////////////////
	//
	// Constructor and setup
	//
	///////////////////////////////////////////////////////////////////////////

	/// The original account table
	protected AccountTable mainTable = null;

	/// [INTERNAL USE ONLY]
	///
	/// Cosntructor setup, using an account table,
	/// and the account GUID
	protected AccountObject(AccountTable accTable, String inOID) {
		super((Core_MetaTable) (accTable.accountMetaTable), inOID);
		mainTable = accTable;
	}

	///////////////////////////////////////////////////////////////////////////
	//
	// Getting and setting login ID's
	//
	///////////////////////////////////////////////////////////////////////////

	/// Checks if the current account has the provided loginID
	///
	/// @param  LoginID to use
	///
	/// @return TRUE if login ID belongs to this account
	public boolean hasLoginID(String name) {
		return _oid.equals(mainTable.accountLoginIdMap.get(name));
	}

	/// Gets and return the various login "nice-name" (not UUID) for this account
	///
	/// @return  Set of loginID's used by this account
	public Set<String> getLoginIDSet() {
		return mainTable.accountLoginIdMap.keySet(_oid);
	}

	/// Sets the name for the account, returns true or false if it succed.
	///
	/// @param  LoginID to setup for this account
	///
	/// @return TRUE if login ID is configured to this account
	public boolean setLoginID(String name) {
		if (name == null || name.length() <= 0) {
			throw new RuntimeException("AccountObject loding ID cannot be blank");
		}

		if (mainTable.hasLoginID(name)) {
			return false;
		}

		// ensure its own OID is registered
		saveDelta();

		// Technically a race condition =X
		//
		// But name collision, if its an email collision should be a very rare event.
		mainTable.accountLoginIdMap.put(name, _oid);

		// Success of failure
		return hasLoginID(name);
	}

	/// Removes the old name from the database
	///
	/// @param  LoginID to setup for this account
	public void removeLoginID(String name) {
		if(hasLoginID(name)) {
			mainTable.accountLoginIdMap.remove(name);
		}
	}

	/// Sets the name as a unique value, delete all previous alias
	///
	/// @param  LoginID to setup for this account
	///
	/// @return TRUE if login ID is configured to this account
	public boolean setUniqueLoginID(String name) {

		// The old name list, to check if new name already is set
		Set<String> oldNamesList = getLoginIDSet();

		// Check if name exist in list
		if (Arrays.asList(oldNamesList).contains(name)) {
			// Already exists in the list, does nothing
		} else {
			// Name does not exist, attempt to set the name
			if (!setLoginID(name)) {
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
			mainTable.accountLoginIdMap.remove(oldName);
		}

		return true;
	}

	///////////////////////////////////////////////////////////////////////////
	//
	// Password management
	//
	///////////////////////////////////////////////////////////////////////////

	/// Gets and returns the stored password hash,
	/// Intentionally made protected to avoid accidental use externally
	///
	/// @return  Password salted hash, as per NxtCrypt usage
	protected String getPasswordHash() {
		return mainTable.accountAuthMap.get(_oid);
	}

	/// Indicates if the current account has a configured password, it is possible there is no password
	/// if it functions as a group. Or is passwordless login
	///
	/// @return  True if password was configured
	public boolean hasPassword() {
		String h = getPasswordHash();
		return (h != null && h.length() > 0);
	}

	/// Remove the account password
	/// This should only be used for group type account objects
	public void removePassword() {
		mainTable.accountAuthMap.remove(_oid);
	}

	/// Validate if the given password is valid
	///
	/// @param  raw Password string to validate
	///
	/// @return  True if password is valid
	public boolean validatePassword(String pass) {
		String hash = getPasswordHash();
		if (hash != null) {
			return NxtCrypt.validatePassHash(hash, pass);
		}
		return false;
	}

	/// Set the account password
	///
	/// @param  raw Password string to setup
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

	/// Set the account password, after checking old password
	///
	/// @param  raw Password string to setup
	/// @param  old Password to validate
	///
	/// @return  True if password change was valid
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

	/// Checks if the current session is associated with the account
	///
	/// @param  Session ID to validate
	///
	/// @return TRUE if login ID belongs to this account
	public boolean hasSession(String sessionID) {
		return sessionID != null && _oid.equals(mainTable.sessionLinkMap.get(sessionID));
	}

	/// List the various session ID's involved with this account
	///
	/// @return  Set of login session ID's
	public Set<String> getSessionSet() {
		return mainTable.sessionLinkMap.keySet(_oid);
	}

	/// Get and return the session information meta
	///
	/// @param  Session ID to get
	///
	/// @return  Information meta if session is valid
	public Map<String,Object> getSessionInfo(String sessionID) {
		// Validate that session is legit
		if(!hasSession(sessionID)) {
			return null;
		}

		// Return the session information
		return mainTable.sessionInfoMap.getStringMap(sessionID, null);
	}

	/// Generate a new session with the provided meta information
	///
	/// Additionally if no tokens are generated and issued in the next
	/// 30 seconds, the session will expire.
	///
	/// Subseqently session expirary will be tag to
	/// the most recently generated token.
	///
	/// Additionally info object is INTENTIONALLY NOT stored as a
	/// MetaObject, for performance reasons.
	///
	/// @param  Meta information map associated with the session,
	///         a blank map is assumed if not provided.
	///
	/// @return  The session ID used
	public String newSession(Map<String,Object> info) {

		// Normalize the info object map
		if( info == null ) {
			info = new HashMap<String,Object>();
		}

		// Set the session expirary time : 30 seconds (before tokens)
		long expireTime = (System.currentTimeMillis()) / 1000L + AccountTable.SESSION_NEW_LIFESPAN;

		// Generate a base58 guid for session key
		String sessionID = GUID.base58();

		// As unlikely as it is, on GUID collision,
		// we do not want any session swarp EVER
		if( mainTable.sessionLinkMap.get( sessionID ) != null ) {
			throw new RuntimeException("GUID collision for sessionID : "+sessionID);
		}

		// Time to set it all up, with expire timestamp
		mainTable.sessionLinkMap.putWithExpiry( sessionID, _oid, expireTime );
		mainTable.sessionInfoMap.putWithExpiry( sessionID, ConvertJSON.fromMap(info), expireTime + AccountTable.SESSION_RACE_BUFFER );

		// Return the session key
		return sessionID;
	}

	/// Revoke a session, associated to this account.
	///
	/// This will also revoke all tokens associated to this session
	///
	/// @param  SessionID to revoke
	public void revokeSession(String sessionID) {
		// Validate the session belongs to this account !
		if( hasSession(sessionID) ) {
			// Session ownership validated, time to revoke!

			// Revoke all tokens associated to this session
			revokeAllToken(sessionID);

			// Revoke the session info
			mainTable.sessionLinkMap.remove(sessionID);
			mainTable.sessionInfoMap.remove(sessionID);
		}
	}

	/// Revoke all sessions, associated to this account
	public void revokeAllSession() {
		Set<String> sessions = getSessionSet();
		for(String oneSession : sessions) {
			revokeSession(oneSession);
		}
	}

	///////////////////////////////////////////////////////////////////////////
	//
	// Session token management
	//
	///////////////////////////////////////////////////////////////////////////

	/// Checks if the current session token is associated with the account
	///
	/// @param  Session ID to validate
	/// @param  Token ID to validate
	///
	/// @return TRUE if login ID belongs to this account
	public boolean hasToken(String sessionID, String tokenID) {
		return hasSession(sessionID) && sessionID.equals(mainTable.sessionTokenMap.get(tokenID));
	}

	/// Get the token set associated with the session and account
	///
	/// @param   Session ID to fetch from
	///
	/// @return  The list of token ID's currently associated to this session
	///          null, if session is not valid.
	public Set<String> getTokenSet(String sessionID) {
		if( hasSession(sessionID) ) {
			return mainTable.sessionTokenMap.keySet(sessionID);
		}
		return new HashSet<String>();
	}

	/// Generate a new token, with a timeout
	///
	/// Note that this token will update the session timeout,
	/// even if there was a longer session previously set.
	///
	/// @param  Session ID to generate token from
	/// @param  The expire timestamp of the token
	///
	/// @return  The tokenID generated, null on invalid session
	public String newToken(String sessionID, long expireTime) {

		// Terminate if session is invalid
		if( !hasSession(sessionID) ) {
			return null;
		}

		// Generate a base58 guid for session key
		String tokenID = GUID.base58();

		// Issue the token
		registerToken(sessionID, tokenID, GUID.base58(), expireTime);

		// Return the token
		return tokenID;
	}

	/// Internal function, used to issue a token ID to the session
	/// with the next token ID predefined
	///
	/// @param  Session ID to generate token from
	/// @param  Token ID to setup
	/// @param  Next token ID
	/// @param  The expire timestamp of the token
	///
	/// @return  The tokenID generated, null on invalid session
	protected void registerToken(String sessionID, String tokenID, String nextTokenID, long expireTime) {
		// Current token check, does nothing if invalid
		if( hasToken(sessionID, tokenID) ) {
			return;
		}

		// Check if token has already been registered
		String existingTokenSession = mainTable.sessionTokenMap.getString(tokenID, null);
		if( existingTokenSession != null ) {
			// Check if token has valid session
			if( sessionID.equals(existingTokenSession) ) {
				// Assume setup was already done, terminating
				return;
			} else {
				// Invalid next token, was issued to another session
				// EITHER a GUID collision occured, OR spoofing is being attempted
				throw new RuntimeException("FATAL : Unable to register token previously registered to another session ID");
			}
		}

		// Renew every session!
		mainTable.sessionLinkMap.setExpiry(sessionID, expireTime + AccountTable.SESSION_RACE_BUFFER );
		mainTable.sessionInfoMap.setExpiry(sessionID, expireTime + AccountTable.SESSION_RACE_BUFFER * 2 );

		// Register the token
		mainTable.sessionTokenMap.putWithExpiry(tokenID, sessionID, expireTime );
		mainTable.sessionNextTokenMap.putWithExpiry(tokenID, nextTokenID , expireTime );
	}

	/// Checks if the current session token is associated with the account
	///
	/// @param  Session ID to validate
	/// @param  Token ID to revoke
	///
	/// @return TRUE if login ID belongs to this account
	public boolean revokeToken(String sessionID, String tokenID) {
		if( hasToken(sessionID, tokenID) ) {
			mainTable.sessionTokenMap.remove(tokenID);
			return true;
		}
		return false;
	}

	/// Revokes all tokens associated to a session
	///
	/// @param  Session ID to revoke
	public void revokeAllToken(String sessionID) {
		Set<String> tokens = getTokenSet(sessionID);
		for(String oneToken : tokens) {
			revokeToken(sessionID, oneToken);
		}
	}

	/// Get token expiriry
	///
	/// @param  Session ID to validate
	/// @param  Token ID to check
	///
	/// @return  The token expiry timestamp
	public long getTokenExpiry(String sessionID, String tokenID) {
		if( hasToken(sessionID, tokenID) ) {
			return mainTable.sessionTokenMap.getExpiry(tokenID);
		}
		return -1;
	}

	/// Get token remaining lifespan
	///
	/// @param  Session ID to validate
	/// @param  Token ID to check
	///
	/// @return  The token remaining timespan, -1 means invalid token
	public long getTokenLifespan(String sessionID, String tokenID) {
		// Get expiry timestamp
		long expiry = getTokenExpiry( sessionID, tokenID );

		// Invalid tokens are -1
		if(expiry <= -1) {
			return -1;
		}

		long lifespan = expiry - (System.currentTimeMillis()) / 1000L;
		if(lifespan < -1) {
			return -1;
		}
		return lifespan;
	}

	/// Internal function, used to get the next token given a session and current token.
	/// DOES NOT : Validate the next token if it exists
	///
	/// @param  Session ID to validate
	/// @param  Token ID to check
	///
	/// @return The next token
	protected String getUncheckedNextToken(String sessionID, String tokenID) {
		if( hasToken(sessionID, tokenID) ) {
			return mainTable.sessionNextTokenMap.get(tokenID);
		}
		return null;
	}

	/// Get the next token AFTER validating if it is issued
	///
	/// @param  Session ID to validate
	/// @param  Token ID to check
	///
	/// @return The next token ID
	public String getNextToken(String sessionID, String tokenID) {
		String nextToken = getUncheckedNextToken(sessionID, tokenID);
		if( mainTable.sessionTokenMap.containsKey(nextToken) ) {
			return nextToken;
		}
		return null;
	}

	/// Generate next token in line, with expiry
	/// Note that expiry is NOT set, if token was previously issued
	///
	/// @param  Session ID to validate
	/// @param  Token ID to check
	/// @param  The expire timestamp of the token
	///
	/// @return The next token ID
	public String issueNextToken(String sessionID, String tokenID, long expireTime) {
		// Get NextToken, and returns (if previously issued)
		String nextToken = getUncheckedNextToken(sessionID, tokenID);
		// Terminate if nextToken is invalid
		if( nextToken == null ) {
			return null;
		}

		// Issue next token
		registerToken(sessionID, nextToken, GUID.base58(), expireTime);
		// Return the next token, after its been issued
		return nextToken;
	}
	/*

	///////////////////////////////////////////////////////////////////////////
	//
	// Group management
	//
	///////////////////////////////////////////////////////////////////////////

	/// Gets the cached child map
	protected MetaObject _group_userToRoleMap = null;

	/// Gets the child map (cached?)
	protected MetaObject group_userToRoleMap() {
		if (_group_userToRoleMap != null) {
			return _group_userToRoleMap;
		}

		return (_group_userToRoleMap = mainTable.group_childRole.uncheckedGet(this._oid()));
	}

	// Group status check
	//-------------------------------------------------------------------------

	/// Returns if set as group
	public boolean isGroup() {
		Object status = this.get("isGroup");
		if (status instanceof Number && //
			((Number) status).intValue() >= 1) {
			return true;
		} else {
			return false;
		}
		//return ( group_userToRoleMap().size() > 1 );
	}

	/// Sets if the account is a group
	public void setGroupStatus(boolean enabled) {
		if (enabled) {
			this.put("isGroup", new Integer(1));
		} else {
			this.put("isGroup", new Integer(0));

			// group_userToRoleMap().clear();
			// group_userToRoleMap().saveDelta();
		}
		this.saveDelta();
	}

	// Group management of users
	//-------------------------------------------------------------------------

	/// Gets and returns the member role, if it exists
	public String getMemberRole(AccountObject memberObject) {
		return group_userToRoleMap().getString(memberObject._oid());
	}

	/// Gets and returns the member meta map, if it exists
	/// Only returns if member exists, else null
	public MetaObject getMember(AccountObject memberObject) {
		String memberOID = memberObject._oid();
		String level = group_userToRoleMap().getString(memberOID);

		if (level == null || level.length() <= 0) {
			return null;
		}

		return mainTable.groupChild_meta.uncheckedGet(mainTable.getGroupChildMetaKey(this._oid(), memberOID));
	}

	/// Gets and returns the member meta map, if it exists
	/// Only returns if member exists and matches role, else null
	public MetaObject getMember(AccountObject memberObject, String role) {
		role = mainTable.validateMembershipRole(role);

		String memberOID = memberObject._oid();
		String level = group_userToRoleMap().getString(memberOID);

		if (level == null || !level.equals(role)) {
			return null;
		}

		return mainTable.groupChild_meta.uncheckedGet(this._oid() + "-" + memberOID);
	}

	/// Adds the member to the group with the given role, if it was not previously added
	///
	/// Returns the group-member unique meta object, null if previously exists
	public MetaObject addMember(AccountObject memberObject, String role) {
		// Gets the existing object, if exists terminates
		if (getMember(memberObject) != null) {
			return null;
		}

		// Set and return a new member object
		return setMember(memberObject, role);
	}

	/// Adds the member to the group with the given role, or update the role if already added
	///
	/// Returns the group-member unique meta object
	public MetaObject setMember(AccountObject memberObject, String role) {
		role = mainTable.validateMembershipRole(role);

		String memberOID = memberObject._oid();
		String level = group_userToRoleMap().getString(memberOID);
		MetaObject childMeta = null;

		if (level == null || !level.equals(role)) {

			memberObject.saveDelta();
			setGroupStatus(true);

			group_userToRoleMap().put(memberOID, role);
			group_userToRoleMap().saveDelta();

			childMeta = mainTable.groupChild_meta.uncheckedGet(this._oid() + "-" + memberOID);
			childMeta.put("role", role);
			childMeta.saveDelta();
		} else {
			childMeta = mainTable.groupChild_meta.uncheckedGet(this._oid() + "-" + memberOID);
		}

		return childMeta;
	}

	public boolean removeMember(AccountObject memberObject) {
		if (!this.isGroup()) {
			return false;
		}

		String memberOID = memberObject._oid();
		String level = group_userToRoleMap().getString(memberOID);

		group_userToRoleMap().remove(memberOID);
		group_userToRoleMap().saveAll();

		mainTable.groupChild_meta.remove(this._oid() + "-" + memberOID);

		System.out.println("Remove member called successfully");

		return true;
	}

	/// Returns the list of groups the member is in
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

	/// Returns the list of members in the group
	///
	public String[] getGroups_id() {
		return mainTable.group_childRole.getFromKeyName_id(_oid());
	}

	/// Gets all the members object related to the group
	///
	public AccountObject[] getMembersAccountObject() {
		String[] idList = getMembers_id();
		AccountObject[] objList = new AccountObject[idList.length];
		for (int a = 0; a < idList.length; ++a) {
			objList[a] = mainTable.getFromID(idList[a]);
		}
		return objList;
	}

	// Group management of users
	//-------------------------------------------------------------------------

	/// Gets all the groups the user is in
	///
	public AccountObject[] getGroups() {
		return mainTable.getFromIDArray(getGroups_id());
	}

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
	*/

	/// This method logs the details about login faailure for the user based on User ID
	public void logLoginFailure(String userID) {
		mainTable.loginThrottlingAttemptMap.put(userID, 1);
		int elapsedTime = ((int) (System.currentTimeMillis() / 1000)) + 2;
		mainTable.loginThrottlingExpiryMap.put(userID, elapsedTime);
	}

	/// This method returns time left before next permitted login attempt for the user based on User ID
	public int getNextLoginTimeAllowed(String userId) {
		long val = getExpiryTime(userId);
		if (val == -1) {
			return 0;
		}
		int allowedTime = (int) val - (int) (System.currentTimeMillis() / 1000);
		return allowedTime > 0 ? allowedTime : 0;
	}

	/// This method would be added in on next login failure for the user based on User ID
	public long getTimeElapsedNextLogin(String userId) {
		long elapsedValue = getExpiryTime(userId);
		if (elapsedValue == -1) {
			return (System.currentTimeMillis() / 1000) + 2;
		}
		return elapsedValue;
	}

	/// This method would be added the delay for the user based on User ID
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
		return (mainTable.loginThrottlingAttemptMap.get(userID)!=null) ? mainTable.loginThrottlingAttemptMap.get(userID) : -1;
	}

	public long getExpiryTime(String userID) {
		return (mainTable.loginThrottlingExpiryMap.get(userID)!=null) ? mainTable.loginThrottlingExpiryMap.get(userID) : -1;
	}

	/// This method remove the entries for the user (should call after successful login)
	public void resetLoginThrottle(String userId) {
		mainTable.loginThrottlingAttemptMap.put(userId, null);
		mainTable.loginThrottlingExpiryMap.put(userId, null);
	}

	// /// Gets value of key from the Account Meta table
	// public Object getMetaValue(String oid, String key) {
	// 	return mainTable.accountMetaTable.get(oid).get(key);
	// }


}
