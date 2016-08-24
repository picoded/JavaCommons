package picoded.JStruct;

/// Java imports
import java.util.*;

/// Picoded imports
import picoded.conv.GUID;
import picoded.struct.CaseInsensitiveHashMap;
import picoded.struct.UnsupportedDefaultMap;
import picoded.JStruct.internal.*;
import picoded.struct.query.*;
import picoded.conv.ListValueConv;

import org.apache.commons.lang3.RandomUtils;

/// MetaTable, serves as the core flexible backend storage implmentation for the whole
/// JStack setup. Its role can be viewed similarly to NoSql, or AWS SimpleDB
/// where almost everything is indexed and cached. 
/// 
/// On a performance basis, it is meant to trade off raw query performance of traditional optimized 
/// SQL lookup, over flexibility in data model. This is however heavily mintigated by the inclusion 
/// of a JCache layer for non-complex lookup cached reads. Which will in most cases be the main
/// read request load.
/// 
public interface MetaTable extends UnsupportedDefaultMap<String, MetaObject> {
	
	//
	// Temp mode optimization, used to indicate pure session like data,
	// that does not require persistance (or even SQL)
	//
	//--------------------------------------------------------------------------
	
	/// Gets if temp mode optimization hint is indicated
	/// Note that this only serve as a hint, as does not indicate actual setting
	///
	/// @returns boolean  temp mode value
	public boolean getTempHint();
	
	/// Sets temp mode optimization indicator hint
	/// Note that this only serve as a hint, as does not indicate actual setting
	///
	/// @param  mode  the new temp mode hint
	///
	/// @returns boolean  previous value if set
	public boolean setTempHint(boolean mode);
	
	//
	// Backend system setup / teardown
	//--------------------------------------------------------------------------
	
	/// Setsup the backend storage table, etc. If needed
	public void systemSetup();
	
	/// Teardown and delete the backend storage table, etc. If needed
	public void systemTeardown();
	
	/// perform increment maintenance, meant for minor changes between requests
	public default void incrementalMaintenance() {
		// 2 percent chance of trigering maintenance
		// This is to lower to overall performance cost incrementalMaintenance per request
		if (RandomUtils.nextInt(0, 100) <= 2) {
			maintenance();
		}
	}
	
	/// Perform maintenance, mainly removing of expired data if applicable
	public default void maintenance() {
		// does nothing?
	}
	
	// 
	// MetaObject operations
	//--------------------------------------------------------------------------
	
	/// Generates a new blank object, with a GUID
	///
	/// @returns the MetaObject
	public default MetaObject newObject() {
		MetaObject ret = new JStruct_MetaObject(this, null, null, false);
		
		// Baking in _createTime stamp
		ret.put("_createTime", (System.currentTimeMillis() / 1000L));
		
		return ret;
	}
	
	/// Gets the MetaObject, regardless of its actual existance
	///
	/// @returns the MetaObject
	public default MetaObject uncheckedGet(String _oid) {
		return new JStruct_MetaObject(this, _oid, null, false);
	}
	
	/// PUT, returns the object ID (especially when its generated), note that this
	/// adds the value in a merger style. Meaning for example, existing values not explicitely
	/// nulled or replaced are maintained
	///
	/// @returns the MetaObject
	public default MetaObject append(String _oid, Map<String, Object> obj) {
		
		/// Appending a MetaObject is equivalent of saveDelta
		MetaObject r = null;
		if (obj instanceof MetaObject && ((MetaObject) obj)._oid().equals(_oid)) {
			(r = (MetaObject) obj).saveDelta();
			return r;
		}
		
		/// Unchecked get, put, and save
		r = uncheckedGet(_oid);
		r.putAll(obj);
		r.saveDelta();
		
		return r;
	}
	
	/// Checked, or unchecked get indicated by a booolean
	///
	/// @param object GUID to fetch
	/// @param boolean indicator for unchecked get (skips existance checks)
	///
	/// @returns the MetaObject
	public default MetaObject get(String _oid, boolean isUnchecked) {
		if (isUnchecked) {
			return uncheckedGet(_oid);
		} else {
			return get(_oid);
		}
	}
	
	// 
	// MetaObject utility operations
	//--------------------------------------------------------------------------
	
	/// Get array of MetaObjects
	public default MetaObject[] getArrayFromID(String[] idArray, boolean isUnchecked) {
		MetaObject[] retArr = new MetaObject[idArray.length];
		for (int i = 0; i < idArray.length; ++i) {
			retArr[i] = get(idArray[i], isUnchecked);
		}
		return retArr;
	}
	
	// 
	// Query operations (to optimize on specific implementation)
	//--------------------------------------------------------------------------
	
	/// Performs a search query, and returns the respective MetaObjects
	///
	/// @param   where query statement
	/// @param   where clause values array
	///
	/// @returns  The MetaObject[] array
	public default MetaObject[] query(String whereClause, Object[] whereValues) {
		return query(whereClause, whereValues, null, 0, 0);
	}
	
	/// Performs a search query, and returns the respective MetaObjects
	///
	/// @param   where query statement
	/// @param   where clause values array
	/// @param   query string to sort the order by, use null to ignore
	///
	/// @returns  The MetaObject[] array
	public default MetaObject[] query(String whereClause, Object[] whereValues, String orderByStr) {
		return query(whereClause, whereValues, orderByStr, 0, 0);
	}
	
	/// Performs a search query, and returns the respective MetaObjects
	///
	/// @param   where query statement
	/// @param   where clause values array
	/// @param   query string to sort the order by, use null to ignore
	/// @param   offset of the result to display, use -1 to ignore
	/// @param   number of objects to return max
	///
	/// @returns  The MetaObject[] array
	public default MetaObject[] query(String whereClause, Object[] whereValues, String orderByStr, int offset, int limit) {
		
		// The return list
		List<MetaObject> retList = null;
		
		// Setup the query, if needed
		if (whereClause == null) { //null gets all
			retList = new ArrayList<MetaObject>(this.values());
		} else {
			Query queryObj = Query.build(whereClause, whereValues);
			retList = queryObj.search(this);
		}
		
		// Sort, offset, convert to array, and return
		return JStructUtils.sortAndOffsetListToArray(retList, orderByStr, offset, limit);
	}
	
	/// Performs a search query, and returns the respective MetaObject keys.
	/// This is the GUID key varient of query, that is relied by JStack
	///
	/// @param   where query statement
	/// @param   where clause values array
	/// @param   query string to sort the order by, use null to ignore
	/// @param   offset of the result to display, use -1 to ignore
	/// @param   number of objects to return max
	///
	/// @returns  The String[] array
	public default String[] queryKeys(String whereClause, Object[] whereValues, String orderByStr, int offset, int limit) {
		
		// The return list
		List<MetaObject> retList = null;
		
		// Setup the query, if needed
		if (whereClause == null) { //null gets all
			retList = new ArrayList<MetaObject>(this.values());
		} else {
			Query queryObj = Query.build(whereClause, whereValues);
			retList = queryObj.search(this);
		}
		
		// Sort, offset, convert to array, and return
		MetaObject[] retArr = JStructUtils.sortAndOffsetListToArray(retList, orderByStr, offset, limit);
		
		// Prepare the return object
		int retLength = retArr.length;
		String[] ret = new String[retLength];
		for (int a = 0; a < retLength; ++a) {
			ret[a] = retArr[a]._oid();
		}
		
		// Returns
		return ret;
	}
	
	/// Performs a search query, and returns the respective MetaObjects
	///
	/// @param   where query statement
	/// @param   where clause values array
	///
	/// @returns  The total count for the query
	public default long queryCount(String whereClause, Object[] whereValues) {
		
		// The return list
		List<MetaObject> retList = null;
		
		// Setup the query, if needed
		if (whereClause == null) { //null gets all
			retList = new ArrayList<MetaObject>(this.values());
		} else {
			Query queryObj = Query.build(whereClause, whereValues);
			retList = queryObj.search(this);
		}
		
		// Query and count
		return retList.size();
	}
	
	// 
	// Get from key names operations (to optimize on specific implementation)
	//--------------------------------------------------------------------------
	
	/// Performs a custom search by configured keyname
	/// 
	/// @param   keyName to lookup for
	///
	/// @returns  The MetaObject[] array
	public default MetaObject[] getFromKeyName(String keyName) {
		return getFromKeyName(keyName, null, -1, -1);
	}
	
	/// Performs a custom search by configured keyname
	/// 
	/// @param   keyName to lookup for
	/// @param   query string to sort the order by, use null to ignore
	///
	/// @returns  The MetaObject[] array
	public default MetaObject[] getFromKeyName(String keyName, String orderByStr) {
		return getFromKeyName(keyName, orderByStr, -1, -1);
	}
	
	/// Performs a custom search by configured keyname
	/// 
	/// @TODO: Optimize in JSQL layer
	///
	/// @param   keyName to lookup for
	/// @param   query string to sort the order by, use null to ignore
	/// @param   offset of the result to display, use -1 to ignore
	/// @param   number of objects to return max
	///
	/// @returns  The MetaObject[] array
	public default MetaObject[] getFromKeyName(String keyName, String orderByStr, int offset, int limit) {
		
		// The return list
		List<MetaObject> retList = new ArrayList<MetaObject>();
		
		// Iterate the list, add if containsKey
		for (MetaObject obj : values()) {
			if (obj.containsKey(keyName)) {
				retList.add(obj);
			}
		}
		
		// Sort, offset, convert to array, and return
		return JStructUtils.sortAndOffsetListToArray(retList, orderByStr, offset, limit);
	}
	
	/// Performs a custom search by configured keyname, and returns its ID array
	/// 
	/// @TODO: Optimize in JSQL layer
	///
	/// @param   keyName to lookup for
	///
	/// @returns  The MetaObject[] array
	public default String[] getFromKeyName_id(String keyName) {
		// The return list
		List<String> retList = new ArrayList<String>();
		
		// Iterate the list, add if containsKey
		for (MetaObject obj : values()) {
			if (obj.containsKey(keyName)) {
				retList.add(obj._oid());
			}
		}
		
		// Return
		return retList.toArray(new String[retList.size()]);
	}
	
	// 
	// Get key names handling
	//--------------------------------------------------------------------------
	
	/// Scans the object and get the various keynames used. 
	/// This is used mainly in adminstration interface, etc.
	///
	/// Note however, that in non-JSql mode, this function is not
	/// optimize, and does and iterative search for the various object keys.
	/// which is ridiculously expensive. So avoid calling this unless needed.
	///
	/// The seekDepth parameter is ignored in JSql mode, as its optimized.
	///
	/// @param  seekDepth, which detirmines the upper limit for iterating
	///         objects for the key names, use -1 to search all
	///
	/// @returns  The various key names used in the objects
	///
	public default Set<String> getKeyNames(int seekDepth) {
		Set<String> res = new HashSet<String>();
		
		// Iterate the list, get key names
		int idx = 0;
		for (MetaObject obj : values()) {
			
			// Break iteration once seekdepth limits reached
			if (idx >= seekDepth && seekDepth >= 0) {
				break;
			}
			
			// Add all the various key names
			res.addAll(obj.keySet());
		}
		
		return res;
	}
	
	/// getKeyNames varient with seekDepth defaulted to 10
	///
	/// @returns  The various key names used in the objects
	///
	public default Set<String> getKeyNames() {
		return getKeyNames(10);
	}
	
	// 
	// MetaType handling, does type checking and conversion
	//--------------------------------------------------------------------------
	
	/// Gets and return the internal MetaTypeMap
	public MetaTypeMap typeMap();
	
	/// Get convinent function
	public default MetaType getType(String name) {
		return typeMap().get(name);
	}
	
	/// Put convinent function
	public default MetaType putType(String name, Object value) {
		return typeMap().put(name, value);
	}
	
	/// Generic varient of put all
	public default <K, V> void setMappingType(Map<K, V> m) {
		typeMap().putAllGeneric(m);
	}
}
