package picoded.JStack;

/// Java imports
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.*;
import java.util.List;
import java.util.ArrayList;
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
	
	// Custom additional functions
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
	
}
