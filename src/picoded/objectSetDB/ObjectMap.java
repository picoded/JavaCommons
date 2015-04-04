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
	
	/// Inserts the raw value into the JSql stack
	protected void putRawVia_ACID_JSql(String meta, int idx, Object val, long expireUnixTime) throws ObjectSetException {
		try {
			ObjectSet_JSql[] ACID_JSqlSet = pSet.ACID_JSqlSet;
			
			// Updates from inner most to outer most
			for (int a = ACID_JSqlSet.length - 1; a >= 0; --a) {
				ACID_JSqlSet[a].put(mName, meta, idx, val, expireUnixTime);
			}
		} catch (JSqlException e) {
			throw new ObjectSetException(e);
		}
	}
	
	/// No Expire time varient of putRawVia_ACID_JSql
	protected void putRawVia_ACID_JSql(String meta, int idx, Object val) throws ObjectSetException {
		putRawVia_ACID_JSql(meta, idx, val, -1);
	}
	
	///----------------------------------------
	/// Basic get / put functions
	///----------------------------------------
	
	/// Gets the stored value
	public Object get(String meta) throws ObjectSetException {
		return ObjectSet_JSql.valueFromRawResult(getRawVia_ACID_JSql(meta, 0), 0);
	}
	
	/// Overwrite the stored value,
	/// note that this maybe silently ignored if the previous value is identical
	///
	/// !IMPORTANT, DOES NOT GURANTEE RETURN OF PREVIOUS VALUE
	public Object put(String meta, Object value) {
		try {
			putRawVia_ACID_JSql(meta, 0, value);
		} catch (ObjectSetException e) {
			throw new RuntimeException(e);
		}
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
