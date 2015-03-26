package picoded.objectSetDB;

import picoded.jSql.*;
import picoded.jCache.*;
import picoded.objectSetDB.*;
import picoded.objectSetDB.internal.DataStack;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Collections;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;

/// The core class of the ObjectSetDB system, where the data source, and object structures are defined
public class ObjectSetDB extends AbstractMap<String, Map<String, Map<String, Object>>> {
	
	//----------------------------
	// Static shared vars
	//----------------------------
	private static Map<String, Object> readOnlyBlankObjectStructur() {
		HashMap<String, Object> r = new HashMap<String, Object>();
		r.put(WildCard, WildCard);
		
		return Collections.unmodifiableMap(r);
	}
	
	public static final String WildCard = "*";
	public static final String LOBstructure = "LOB";
	public static final Map<String, Object> BlankObjectStructure = readOnlyBlankObjectStructur();
	
	//----------------------------
	// Protected vars
	//----------------------------
	
	/// Internal data stack of ObjectSetDB (shared across subclasses)
	protected DataStack dStack;
	
	//----------------------------
	// Constructors
	//----------------------------
	
	/// Default setup uses a single JSql sqlite db
	public ObjectSetDB() {
		dStack = new DataStack(null, new JSql[] { JSql.sqlite() }, null, null);
	}
	
	//----------------------------
	// Structure handling
	//----------------------------
	
	/// Gets the configured object data structure, default behaviour returns BlankObjectStructure
	/// Note that return object is final and READ ONLY
	public Map<String, Object> getStructure(String setName) {
		return dStack.getStructure(setName);
	}
	
	/// Gets the configured object data structure, that is writable,
	/// however default blank structure is not strictly equals to BlankObjectStructure
	public Map<String, Object> getWritableStructure(String setName) {
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.putAll(getStructure(setName));
		return ret;
	}
	
	/// Sets the configured object data structure, and returns "this" for chaining.
	/// This function does automated conversion of common basic class strings, to the respective class object
	public ObjectSetDB setStructure(String setName, Map<String, Object> structure) {
		dStack.setStructure(setName, structure);
		return this;
	}
	
	/// resets the configured object data structure to BlankObjectStructure, and returns "this" for chaining
	public ObjectSetDB resetStructure(String setName) {
		dStack.resetStructure(setName);
		return this;
	}
	
	//----------------------------
	// Object set fetching
	//----------------------------
	public ObjectSet get(String setName) {
		return dStack.getObjectSet(setName);
	}
	
	///----------------------------------------
	/// Map compliance
	///----------------------------------------
	
	@Override
	public Set<Map.Entry<String, Map<String, Map<String, Object>>>> entrySet() {
		return dStack.entrySet();
	}
}
