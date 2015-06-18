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
		personaSessions.stackSetup();
		personaMeta.stackSetup();
		personaChild.stackSetup();
		personaChildMeta.stackSetup();
	}

	/// Performs the full stack teardown for the data object
	public void stackTeardown() throws JStackException {
		personaID.stackTeardown();
		personaSessions.stackTeardown();
		personaMeta.stackTeardown();
		personaChild.stackTeardown();
		personaChildMeta.stackTeardown();
	}

	//
	// Map compliant implementation
	//--------------------------------------------------------------------------
	
	/// Persona user exists
	public boolean containsKey(Object oid) {
		return personaMeta.containsKey(oid);
	}

	/// Gets the user using the user object ID
	///
	/// Note: get("new") is syntax sugar for newObject();
	public PersonaObject get(Object oid) {
		String _oid = oid.toString();
		
		if( _oid.toLowerCase().equals("new") ) {
			return newObject();
		}
		
		if( containsKey(_oid) ) {
			return new PersonaObject(this, _oid);
		}
		
		return null; 
	}
	
	//
	// Additional functionality add on
	//--------------------------------------------------------------------------
	
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
	
	/// Gets the persona UUID, using the configured name
	public String nameToID(String name) {
		return personaID.get(name);
	}
	
	public boolean containsName(String name) {
		return personaID.containsKey(name);
	}
}
