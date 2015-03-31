package picoded.objectSetDB;

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

///
/// An object set class, that represents a collection of objects found in ObjectSetDB
///
public class ObjectSet extends AbstractMap<String, Map<String, Object>> {
	
	/// Internal data stack of ObjectSetDB (shared across subclasses)
	protected DataStack dStack;
	
	/// Internal set name
	protected String sName;
	
	/// Returns the "safe", protected setName
	public String setName() {
		return sName;
	}
	
	/// Constructor for the ObjectSet, this should not be called directly, except via ObjectSetDB
	public ObjectSet(String setName, DataStack dObj) {
		sName = setName;
		dStack = dObj;
	}
	
	//----------------------------
	// Object map fetching
	//----------------------------
	public ObjectMap get(String objID) {
		return (new ObjectMap(sName, objID, dStack));
	}
	
	///----------------------------------------
	/// Map compliance
	///----------------------------------------
	@Override
	public Set<Map.Entry<String, Map<String, Object>>> entrySet() {
		throw new RuntimeException("entrySet / Iterator support is not (yet) implemented");
		
		/*
		if (entries == null) {
			entries = new AbstractSet() {
				public void clear() {
					list.clear();
				}

				public Iterator iterator() {
					return list.iterator();
				}

				public int size() {
					return list.size();
				}
			};
		}
		return entries;
		// */
	}
	
}
