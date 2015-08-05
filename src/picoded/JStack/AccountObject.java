package picoded.JStack;

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
import picoded.struct.CaseInsensitiveHashMap;
import picoded.struct.UnsupportedDefaultMap;
import picoded.struct.GenericConvertMap;

/// hazelcast
import com.hazelcast.core.*;
import com.hazelcast.config.*;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class AccountObject extends MetaObject {
	
	/// The original table
	protected AccountTable mainTable = null;
	
	/// Protected constructor as this class is NOT meant to be constructed directly
	protected AccountObject(AccountTable pTable, String inOID) {
		super(pTable.accountMeta, inOID);
		mainTable = pTable;
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
	public String[] getNames() {
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
		
		/// Technically a race condition =X
		mainTable.accountID.put(name, _oid);
		return true;
	}
	
	/// Removes the old name from the database
	/// @TODO Add-in security measure to only removeName of this user, instead of ANY
	public void removeName(String name) {
		mainTable.accountID.remove(name);
	}
	
	/// Sets the name as a unique value
	public boolean setUniqueName(String name) {
		
		// The old name list, to check if new name already is set
		String[] oldNamesList = getNames();
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
	protected MetaObject _childMap = null;
	
	/// Gets the child map (cached?)
	protected MetaObject childMap() throws JStackException {
		if( _childMap != null ) {
			return _childMap;
		}
		
		return ( _childMap = mainTable.accountChild.lazyGet( this._oid() ) );
	}
	
	// Group management of users
	//-------------------------------------------------------------------------
	
	/// Gets and returns the member role, if it exists
	public String getMemberRole( AccountObject memberObject ) throws JStackException {
		return childMap().getString( memberObject._oid() ); 
	}
	
	/// Gets and returns the member meta map, if it exists
	/// Only returns if member exists, else null
	public MetaObject getMember( AccountObject memberObject ) throws JStackException {
		String memberOID = memberObject._oid();
		String level = childMap().getString(memberOID);
		
		if( level == null || level.length() <= 0 ) {
			return null;
		}
		
		return mainTable.accountChildMeta.lazyGet( this._oid()+"-"+memberOID );
	}
	
	/// Gets and returns the member meta map, if it exists
	/// Only returns if member exists and matches role, else null
	public MetaObject getMember( AccountObject memberObject, String role ) throws JStackException {
		role = mainTable.validateMembershipRole( role );
		
		String memberOID = memberObject._oid();
		String level = childMap().getString(memberOID);
		
		if( level == null || !level.equals(role) ) {
			return null;
		}
		
		return mainTable.accountChildMeta.lazyGet( this._oid()+"-"+memberOID );
	}
	
	/// Adds the member to the group with the given role, if it was not previously added
	///
	/// Returns the group-member unique meta object, null if previously exists
	public MetaObject addMember( AccountObject memberObject, String role ) throws JStackException {
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
	public MetaObject setMember( AccountObject memberObject, String role ) throws JStackException {
		role = mainTable.validateMembershipRole( role );
		
		String memberOID = memberObject._oid();
		String level = childMap().getString(memberOID);
		MetaObject childMeta = null;
		
		if( level == null || !level.equals( role ) ) {
			childMap().put(memberOID, role);
			childMap().saveDelta();
			
			childMeta = mainTable.accountChildMeta.lazyGet( this._oid()+"-"+memberOID );
			childMeta.put( "role", role );
			childMeta.saveDelta();
		} else {
			childMeta = mainTable.accountChildMeta.lazyGet( this._oid()+"-"+memberOID );
		}
		
		return childMeta;
	}
	
	/// Returns the list of members in the group
	///
	public String[] getMembers_id() throws JStackException {
		return mainTable.accountChild.getFromKeyNames_id( _oid() );
	}
	
	/// Returns the list of groups the member is in
	///
	public String[] getGroups_id() throws JStackException {
		List<String> retList = new ArrayList<String>();
		for( String key : childMap().keySet() ) {
			if( key.equals("_oid") ) {
				continue;
			}
			retList.add(key);
		}
		return retList.toArray( new String[retList.size()] );
	}
	
	/// Gets all the members object related to the group
	///
	public AccountObject[] getMembers() throws JStackException {
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
	public AccountObject[] getGroups() throws JStackException {
		return mainTable.getFromIDArray( getGroups_id() );
	}
}
