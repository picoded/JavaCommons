package picoded.JStruct;

/// Java imports
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/// Picoded imports
import picoded.conv.GUID;
import picoded.security.NxtCrypt;
import picoded.JSql.*;
import picoded.JCache.*;
import picoded.JStruct.*;
import picoded.JStruct.internal.*;
import picoded.struct.*;

/// hazelcast
import com.hazelcast.core.*;
import com.hazelcast.config.*;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class AccountObject extends JStruct_MetaObject {
	
	/// The original table
	protected AccountTable mainTable = null;
	
	/// Constructor full setup
	protected AccountObject(AccountTable accTable, JStruct_MetaTable inTable, String inOID, boolean isCompleteData) {
		super( inTable, inOID );
		mainTable = accTable;
	}
	
	/// Simplified Constructor
	protected AccountObject(AccountTable accTable, String inOID) {
		super( (JStruct_MetaTable)(accTable.accountMeta), inOID  );
		mainTable = accTable;
	}
	
	// Internal utility functions
	//-------------------------------------------------------------------------
	
	/// Gets and returns the stored password hash
	protected String getPasswordHash() {
		return mainTable.accountHash.get(_oid);
	}
	
	// Password management
	//-------------------------------------------------------------------------
	
	/// Indicates if the current account has a configured password, it is possible there is no password
	/// if it functions as a group. Or is passwordless login
	public boolean hasPassword() {
		String h = getPasswordHash();
		return (h != null && h.length() > 0);
	}
	
	/// Remove the account password
	public void removePassword() {
		mainTable.accountHash.remove(_oid);
	}
	
	/// Validate if the given password is valid
	public boolean validatePassword(String pass) {
		String hash = getPasswordHash();
		if (hash != null) {
			return NxtCrypt.validatePassHash(hash, pass);
		}
		return false;
	}
	
	/// Set the account password
	public boolean setPassword(String pass) {
		if (pass == null) {
			removePassword();
		} else {
			mainTable.accountHash.put(_oid, NxtCrypt.getPassHash(pass));
		}
		return true;
	}
	
	/// Set the account password, after checking old password
	public boolean setPassword(String pass, String oldPass) {
		if (validatePassword(oldPass)) {
			setPassword(pass);
			return true;
		}
		return false;
	}
	
	// Display name management
	//-------------------------------------------------------------------------
	
	/// Gets and return the various "nice-name" (not UUID) for this account
	public Set<String> getNames() {
		return mainTable.accountID.getKeys(_oid);
	}
	
	/// Sets the name for the account, returns true or false if it succed.
	public boolean setName(String name) {
		if (name == null || name.length() <= 0) {
			throw new RuntimeException("AccountObject nice name cannot be blank");
		}
		
		if (mainTable.containsName(name)) {
			return false;
		}
		
		saveDelta(); //ensure itself is registered
		
		/// Technically a race condition =X
		mainTable.accountID.put(name, _oid);
		return true;
	}
	
	/// Removes the old name from the database
	/// @TODO Add-in security measure to only removeName of this user, instead of ANY
	public void removeName(String name) {
		mainTable.accountID.remove(name);
	}
	
	/// Sets the name as a unique value, delete all previous alias
	public boolean setUniqueName(String name) {
		
		// The old name list, to check if new name already is set
		Set<String> oldNamesList = getNames();
		if( !(Arrays.asList(oldNamesList).contains(name)) ) {
			if( !setName(name) ) { //does not own the name, but fail to set =(
				return false;
			}
		}
		
		// Iterate the names, delete uneeded ones
		for(String oldName : oldNamesList) {
			// Skip new name
			if(oldName.equals(name)) {
				continue;
			}
			removeName(oldName);
		}
		
		return true;
	}
	
	
	// Group management utility function
	//-------------------------------------------------------------------------
	
	/// Gets the cached child map
	protected MetaObject _group_userToRoleMap = null;
	
	/// Gets the child map (cached?)
	protected MetaObject group_userToRoleMap() {
		if( _group_userToRoleMap != null ) {
			return _group_userToRoleMap;
		}
		
		return ( _group_userToRoleMap = mainTable.group_childRole.uncheckedGet( this._oid() ) );
	}
	
	// Group status check
	//-------------------------------------------------------------------------
	
	/// Returns if set as group
	public boolean isGroup() {
		Object status = this.get("isGroup");
		if( status instanceof Number && //
		   ((Number)status).intValue() >= 1 ) {
			return true;
		} else {
			return false;
		}
		//return ( group_userToRoleMap().size() > 1 );
	}
	
	/// Sets if the account is a group
	public void setGroupStatus(boolean enabled) {
		if( enabled ) {
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
	public String getMemberRole( AccountObject memberObject ) {
		return group_userToRoleMap().getString( memberObject._oid() ); 
	}
	
	/// Gets and returns the member meta map, if it exists
	/// Only returns if member exists, else null
	public MetaObject getMember( AccountObject memberObject ) {
		String memberOID = memberObject._oid();
		String level = group_userToRoleMap().getString(memberOID);
		
		if( level == null || level.length() <= 0 ) {
			return null;
		}
		
		return mainTable.groupChild_meta.uncheckedGet( this._oid()+"-"+memberOID );
	}
	
	/// Gets and returns the member meta map, if it exists
	/// Only returns if member exists and matches role, else null
	public MetaObject getMember( AccountObject memberObject, String role ) {
		role = mainTable.validateMembershipRole( role );
		
		String memberOID = memberObject._oid();
		String level = group_userToRoleMap().getString(memberOID);
		
		if( level == null || !level.equals(role) ) {
			return null;
		}
		
		return mainTable.groupChild_meta.uncheckedGet( this._oid()+"-"+memberOID );
	}
	
	/// Adds the member to the group with the given role, if it was not previously added
	///
	/// Returns the group-member unique meta object, null if previously exists
	public MetaObject addMember( AccountObject memberObject, String role ) {
		// Gets the existing object, if exists terminates
		if( getMember( memberObject ) != null ) {
			return null;
		}
		
		// Set and return a new member object
		return setMember( memberObject, role );
	}
	
	/// Adds the member to the group with the given role, or update the role if already added
	///
	/// Returns the group-member unique meta object
	public MetaObject setMember( AccountObject memberObject, String role ) {
		role = mainTable.validateMembershipRole( role );
		
		String memberOID = memberObject._oid();
		String level = group_userToRoleMap().getString(memberOID);
		MetaObject childMeta = null;
		
		if( level == null || !level.equals( role ) ) {
			
			memberObject.saveDelta();
			setGroupStatus(true);
			
			group_userToRoleMap().put(memberOID, role);
			group_userToRoleMap().saveDelta();
			
			childMeta = mainTable.groupChild_meta.uncheckedGet( this._oid()+"-"+memberOID );
			childMeta.put( "role", role );
			childMeta.saveDelta();
		} else {
			childMeta = mainTable.groupChild_meta.uncheckedGet( this._oid()+"-"+memberOID );
		}
		
		return childMeta;
	}
	
	/// Returns the list of groups the member is in
	///
	public String[] getMembers_id() {
		List<String> retList = new ArrayList<String>();
		for( String key : group_userToRoleMap().keySet() ) {
			if( key.equals("_oid") ) {
				continue;
			}
			retList.add(key);
		}
		return retList.toArray( new String[retList.size()] );
	}
	
	/// Returns the list of members in the group
	///
	public String[] getGroups_id() {
		return mainTable.group_childRole.getFromKeyName_id( _oid() );
	}
	
	/// Gets all the members object related to the group
	///
	public AccountObject[] getMembersAccountObject() {
		String[] idList = getMembers_id();
		AccountObject[] objList = new AccountObject[idList.length];
		for(int a=0; a<idList.length; ++a) {
			objList[a] = mainTable.getFromID( idList[a] );
		}
		return objList;
	}
	
	// Group management of users
	//-------------------------------------------------------------------------
	
	/// Gets all the groups the user is in
	///
	public AccountObject[] getGroups() {
		return mainTable.getFromIDArray( getGroups_id() );
	}
	
	// Is super user group handling
	//-------------------------------------------------------------------------
	
	/// Returns if its a super user
	///
	public boolean isSuperUser() {
		AccountObject superUserGrp = mainTable.superUserGroup();
		if(superUserGrp == null) {
			return false;
		}
		
		String superUserGroupRole = superUserGrp.getMemberRole( this );
		return ( superUserGroupRole != null && superUserGroupRole.equalsIgnoreCase("admin") );
	}
}
