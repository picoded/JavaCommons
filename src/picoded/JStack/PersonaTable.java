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
import picoded.conv.GenericConvert;
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

///
/// The persona class is considered a hybrid class of both the user, group management class.
/// Where both users, and groups are considered the "same". Hence their distinction is normally detirmined
/// by one of the meta field values.
///
/// The intention here, is to facilitate complex hierachy creations rapidly. Especially against changing specs.
///
/// Any refences to the persona game, is completely coincidental
///
/// @TODO : Group handling layer
/// @TODO : Authentication handling
/// @TODO : 
///
public class PersonaTable extends JStackData implements UnsupportedDefaultMap<String, PersonaObject>  {

	///
	/// Constructor setup
	///--------------------------------------------------------------------------

	/// Setup the metatable with the default table name
	public PersonaTable(JStack inStack) {
		super( inStack );
		tableName = "PersonaTable";

		internalConstructor(inStack);
	}

	/// Setup the metatable with the given stack
	public PersonaTable(JStack inStack, String inTableName) {
		super( inStack, inTableName );

		internalConstructor(inStack);
	}

	/// Internal constructor that setsup the underlying MetaTable / KeyValuePair
	public void internalConstructor(JStack inStack) {
		personaID = new KeyValueMap(inStack, tableName+PERSONA_ID);
		personaHash = new KeyValueMap(inStack, tableName+PERSONA_HASH);
		personaSessions = new KeyValueMap(inStack, tableName+PERSONA_SESSIONS);
		personaMeta = new MetaTable(inStack, tableName+PERSONA_META);
		personaChild = new MetaTable(inStack, tableName+PERSONA_CHILD);
		personaChildMeta = new MetaTable(inStack, tableName+PERSONA_CHILDMETA);
	}

	///
	/// Table suffixes for the variosu sub tables
	///--------------------------------------------------------------------------
	
	/// The persona self ID's
	protected static String PERSONA_ID = "_ID";

	/// The persona self ID's
	protected static String PERSONA_HASH = "_IH";

	/// The login sessions used for authentication
	protected static String PERSONA_SESSIONS = "_LS";

	/// The persona self meta values
	protected static String PERSONA_META = "_SM";

	/// The child nodes mapping, from self
	protected static String PERSONA_CHILD = "_SC";

	/// The child persona meta values
	protected static String PERSONA_CHILDMETA = "_CM";

	///
	/// Underlying data structures
	///--------------------------------------------------------------------------

	/// @TODO better docs
	protected KeyValueMap personaID = null;
	
	/// @TODO better docs
	protected KeyValueMap personaHash = null;
	
	/// @TODO better docs
	protected KeyValueMap personaSessions = null;

	/// @TODO better docs
	protected MetaTable personaMeta = null;

	/// @TODO better docs
	protected MetaTable personaChild = null;

	/// @TODO better docs
	protected MetaTable personaChildMeta = null;

	//
	// JStack common setup functions
	//--------------------------------------------------------------------------

	/// Performs the full stack setup for the data object
	public void stackSetup() throws JStackException {
		personaID.stackSetup();
		personaHash.stackSetup();
		personaSessions.stackSetup();
		personaMeta.stackSetup();
		personaChild.stackSetup();
		personaChildMeta.stackSetup();
	}

	/// Performs the full stack teardown for the data object
	public void stackTeardown() throws JStackException {
		personaID.stackTeardown();
		personaHash.stackTeardown();
		personaSessions.stackTeardown();
		personaMeta.stackTeardown();
		personaChild.stackTeardown();
		personaChildMeta.stackTeardown();
	}

	//
	// Map compliant implementation, note most of them are aliases of their name varients
	//--------------------------------------------------------------------------
	
	/// Persona exists, this is an alias of containsName
	public boolean containsKey(Object name) {
		return containsName(name.toString());
	}

	/// Gets the user using the nice name, this is an alias of getFromName
	public PersonaObject get(Object name) {
		return getFromName(name);
	}
	
	//
	// Additional functionality add on
	//--------------------------------------------------------------------------
	
	/// Gets the persona using the object ID
	public PersonaObject getFromID(Object oid) {
		String _oid = oid.toString();
		
		if( containsID(_oid) ) {
			return new PersonaObject(this, _oid);
		}
		
		return null; 
	}
	
	/// Gets the persona using the nice name
	public PersonaObject getFromName(Object name) {
		String _oid = nameToID(name.toString());
		
		if( _oid != null ) {
			return getFromID(_oid);
		}
		
		return null; 
	}
	
	/// Generates a new persona object
	public PersonaObject newObject() {
		try {
			PersonaObject ret = new PersonaObject(this, null);
			ret.saveAll(); //ensures the blank object is now in DB
			return ret;
		} catch( JStackException e ) {
			throw new RuntimeException(e);
		}
	}
	
	/// Generates a new persona object with the given nice name
	public PersonaObject newObject(String name) {
		if(containsName(name)) {
			return null;
		}
		
		PersonaObject ret = newObject();
		
		if( ret.setName(name) ) {
			return ret;
		} else {
			removeFromID( ret._oid() );
		}
		
		return null;
	}
	
	/// Gets the persona UUID, using the configured name
	public String nameToID(String name) {
		return personaID.get(name);
	}
	
	/// Returns if the name exists
	public boolean containsName(String name) {
		return personaID.containsKey(name);
	}
	
	/// Returns if the persona object id exists
	public boolean containsID(Object oid) {
		return personaMeta.containsKey(oid);
	}
	
	/// Removes the personaObject using the name
	public void removeFromName(String name) {
		//@TODO implmentation
	}
	
	/// Removes the personaObject using the ID
	public void removeFromID(String oid) {
		//@TODO implmentation
	}

}
