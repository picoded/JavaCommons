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

public class PersonaObject extends MetaObject {
	
	/// Java serialversion uid: http://stackoverflow.com/questions/285793/what-is-a-serialversionuid-and-why-should-i-use-it
	private static final long serialVersionUID = 1L;
	
	protected PersonaTable mainTable = null;
	
	/// Protected constructor as this class is NOT meant to be constructed directly
	protected PersonaObject(PersonaTable pTable, String inOID) {
		super(pTable.personaMeta, inOID);
	}
	
	/// Gets and return the various "nice-name" (not UUID) for this persona
	protected String[] getNames() {
		return mainTable.personaID.getKeys( _oid );
	}
	
	/// Sets the name for the persona, returns true or false if it succed.
	protected boolean setName(String name) {
		
		return false;
	}
}
