package picoded.objectSetDB;

import picoded.objectSetDB.*;
import picoded.objectSetDB.internal.*;
import picoded.jSql.*;

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
/// An object map. this represents an object, found in an object set.
/// And does the final interface with the DB
///
public class ObjectMap extends AbstractMap<String, Object> {
	
	/// Internal data stack of ObjectSetDB (shared across subclasses)
	protected DataStack dStack;
	
	/// Internal set name
	protected String sName;
	protected String mName;
	protected ObjectSet pSet;
	
	/// Returns the "safe", protected setName
	public String setName() {
		return sName;
	}
	
	/// Returns the "safe", protected map ID
	public String mapID() {
		return mName;
	}
	
	/// Returns the parent set
	public ObjectSet parentSet() {
		return pSet;
	}
	
	/// Constructor for the ObjectSet, this should not be called directly, except via ObjectSetDB
	public ObjectMap(String setName, String objID, DataStack dObj, ObjectSet parent) {
		sName = setName;
		mName = objID;
		dStack = dObj;
		pSet = parent;
	}
	
	///----------------------------------------
	/// JSql data layer fetching
	///----------------------------------------
	protected JSqlResult getRawVia_ACID_JSql(String meta, int idx) throws ObjectSetException {
		try {
			ObjectSet_JSql[] ACID_JSqlSet = pSet.ACID_JSqlSet;
			for (int a = 0; a < ACID_JSqlSet.length; ++a) {
				JSqlResult r = ACID_JSqlSet[a].getRaw(mName, meta, idx, 1);
				
				if (r.rowCount() > 0) {
					return r;
				}
			}
			return null;
		} catch (JSqlException e) {
			throw new ObjectSetException(e);
		}
	}
	
	protected JSqlResult putRawVia_ACID_JSql(String meta, int idx) throws ObjectSetException {
		try {
			ObjectSet_JSql[] ACID_JSqlSet = pSet.ACID_JSqlSet;
			for (int a = 0; a < ACID_JSqlSet.length; ++a) {
				JSqlResult r = ACID_JSqlSet[a].getRaw(mName, meta, idx, 1);
				
				if (r.rowCount() > 0) {
					return r;
				}
			}
			return null;
		} catch (JSqlException e) {
			throw new ObjectSetException(e);
		}
	}
	
	///----------------------------------------
	/// Basic get / put functions
	///----------------------------------------
	public Object get(String meta) throws ObjectSetException {
		return ObjectSet_JSql.valueFromRawResult(getRawVia_ACID_JSql(meta, 0), 0);
	}
	
	public Object put(String meta, Object value) {
		
		return null;
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
