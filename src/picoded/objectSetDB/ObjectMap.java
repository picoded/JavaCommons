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
public class ObjectMap extends AbstractMap<String, Object> {

	/// Internal data stack of ObjectSetDB (shared across subclasses)
	protected DataStack dStack;

	/// Internal set name
	protected String sName;
	protected String mName;

	/// Returns the "safe", protected setName
	public String setName() {
		return sName;
	}

	/// Returns the "safe", protected map ID
	public String mapID() {
		return mName;
	}

	/// Constructor for the ObjectSet, this should not be called directly, except via ObjectSetDB
	public ObjectMap(String setName, String mapID, DataStack dObj) {
		sName = setName;
		mName = mapID;
		dStack = dObj;
	}

	///----------------------------------------
	/// Map compliance
	///----------------------------------------
	@Override
	public Set<Map.Entry<String, Object>> entrySet() {
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
