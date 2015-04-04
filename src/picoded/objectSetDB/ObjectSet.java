package picoded.objectSetDB;

import picoded.objectSetDB.*;
import picoded.objectSetDB.internal.*;

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

		// Additional setup steps
		setup_jSqlSets();
	}

	//----------------------------
	// Object map fetching
	//----------------------------
	public ObjectMap get(String objID) {
		return (new ObjectMap(sName, objID, dStack, this));
	}

	//----------------------------
	// ACID JSql objects handling
	//----------------------------
	protected ObjectSet_JSql[] ACID_JSqlSet = null;

	protected void setup_jSqlSets() {
		int stackLen = (dStack != null && dStack.ACID_JSql != null) ? dStack.ACID_JSql.length : 0;
		ACID_JSqlSet = new ObjectSet_JSql[stackLen];
		for (int a = 0; a < stackLen; ++a) {
			ACID_JSqlSet[a] = new ObjectSet_JSql(dStack.ACID_JSql[a], sName);
		}
	}

	protected void tableSetup_jSqlSets() {
		for (int a = 0; a < ACID_JSqlSet.length; ++a) {
			ACID_JSqlSet[a].tableSetup();
		}
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
