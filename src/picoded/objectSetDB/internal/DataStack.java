package picoded.objectSetDB.internal;

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

import picoded.jSql.*;
import picoded.jCache.*;
import picoded.objectSetDB.*;

/// Internal usage only for objectSetDB, this handles the actual core ObjectSetDB logic
///
/// Provides management (and sharing) of the internal DataStack used in objectSetDB
public class DataStack extends AbstractMap<String, Map<String, Map<String, Object>>> {
	
	/// Caching layer, that is used for ACID storage
	public JCache[] ACID_JCache = null;
	
	/// SQL layer, that is used for ACID storage
	public JSql[] ACID_JSql = null;
	
	/// Caching layer, that is used for LOBS storage
	public JCache[] LOBS_JCache = null;
	
	/// SQL layer, that is used for LOBS storage
	public JSql[] LOBS_JSql = null;
	
	/// Object structure mapping for the Data Stack,
	/// @TODO: evaluate usage of ConcurrentHashMap?
	public Map<String, Map<String, Object>> objectStructures = new HashMap<String, Map<String, Object>>();
	
	/// The data stack constructor, used to setup acid and lobs storage layers
	public DataStack(JCache[] acidCache, JSql[] acidSql, JCache[] lobsCache, JSql[] lobsSql) {
		ACID_JCache = (acidCache != null) ? acidCache : (new JCache[] {});
		ACID_JSql = (acidSql != null) ? acidSql : (new JSql[] {});
		
		LOBS_JCache = (lobsCache != null) ? lobsCache : ACID_JCache;
		LOBS_JSql = (lobsSql != null) ? lobsSql : ACID_JSql;
		
		if (ACID_JSql.length == 0 && ACID_JCache.length == 0) {
			throw new IllegalArgumentException("Empty ACID JSql & JCache stack setup is not allowed");
		}
		
		if (LOBS_JSql.length == 0 && LOBS_JCache.length == 0) {
			throw new IllegalArgumentException("Empty LOBS JSql & JCache stack setup is not allowed");
		}
	}
	
	//----------------------------
	// Structure handling
	//----------------------------
	
	/// Gets the currently configured Object structure, note that this will be READ ONLY
	public Map<String, Object> getStructure(String setName) {
		Map<String, Object> ret = null;
		if (objectStructures.containsKey(setName)) {
			ret = objectStructures.get(setName);
		}
		return (ret != null) ? ret : ObjectSetDB.BlankObjectStructure;
	}
	
	/// Setup the configured Object structure, does automated conversion of common basic class strings, to class object
	public void setStructure(String setName, Map<String, Object> structure) {
		if (structure == null || //
			(structure.size() == 1 && structure.get(ObjectSetDB.WildCard).toString().trim().equals(ObjectSetDB.WildCard)) //
		) {
			objectStructures.remove(setName);
		}
		
		HashMap<String, Object> sMap = new HashMap<String, Object>();
		sMap.putAll(structure);
		
		for (Map.Entry<String, Object> entry : sMap.entrySet()) {
			//System.out.println(entry.getKey() + "/" + entry.getValue());
			String k = entry.getKey();
			Object v = entry.getValue();
			
			if (v instanceof String) {
				
				//skips true wildcard test
				if (v == ObjectSetDB.WildCard) {
					continue;
				}
				
				//normalize wildcards
				if (v.equals(ObjectSetDB.WildCard)) {
					sMap.put(k, ObjectSetDB.WildCard);
					continue;
				}
				
				//Check for standard class mapping
				v = v.toString().toUpperCase();
				
				boolean changed = true;
				if (v.equals(ObjectSetDB.LOBstructure)) {
					if (v != ObjectSetDB.LOBstructure) {
						v = ObjectSetDB.LOBstructure;
					} else {
						changed = false;
					}
				} else if (v.equals("INT") || v.equals("INTEGER")) {
					v = Integer.class;
				} else if (v.equals("DOUBLE")) {
					v = Double.class;
				} else if (v.equals("STR") || v.equals("STRING")) {
					v = String.class;
				} else if (v.equals("FLOAT")) {
					v = Float.class;
				} else if (v.equals("LONG")) {
					v = Long.class;
				} else if (v.equals("MAP") || v.equals("OBJ") || v.equals("OBJECT")) {
					v = Map.class;
				} else if (v.equals("LIST") || v.equals("ARRAY")) {
					v = List.class;
				} else {
					changed = false;
				}
				
				if (changed) {
					sMap.put(k, v);
				}
				
				//else keeps the string, assume is map name?
			}
			
			if ( //
			v == Integer.class || v == Double.class || //
				v == Float.class || v == Long.class || //
				v == String.class || //
				v == Map.class || v == List.class
			//
			) {
				//valid classes, continue?
				continue;
			} else {
				throw new IllegalArgumentException("Invalid object structure found for set " + setName + " -> " + //
					k + " : " + v.getClass().getName());
			}
		}
		
		objectStructures.put(setName, Collections.unmodifiableMap(sMap));
	}
	
	/// Resets the configured Object structure
	public void resetStructure(String setName) {
		objectStructures.remove(setName);
	}
	
	/// Gets the sub structure info directly
	public Object getSubStructure(String setName, String parameterName) {
		Map<String, Object> s = getStructure(setName);
		if (s == ObjectSetDB.BlankObjectStructure) {
			return ObjectSetDB.WildCard;
		}
		
		if (s.containsKey(parameterName)) {
			return s.get(parameterName);
		}
		
		// Fallback to wildcard config
		return s.get(ObjectSetDB.WildCard);
	}
	
	///----------------------------------------
	/// ObjectSet fetching
	///----------------------------------------
	ConcurrentHashMap<String, ObjectSet> objSetCache = new ConcurrentHashMap<String, ObjectSet>();
	
	/// Gets a cached ObjectSet, this is thread safe?
	public ObjectSet getObjectSet(String setName) {
		ObjectSet ret = objSetCache.get(setName);
		if (ret != null) {
			return ret;
		}
		
		ret = new ObjectSet(setName, this);
		ObjectSet nRet = objSetCache.putIfAbsent(setName, ret);
		
		if (nRet != null) { //Use old value, possible in "race condition"
			return nRet;
		}
		return ret; //return the newly constructed set
	}
	
	///----------------------------------------
	/// Map compliance
	///----------------------------------------
	
	@Override
	public Set<Map.Entry<String, Map<String, Map<String, Object>>>> entrySet() {
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
