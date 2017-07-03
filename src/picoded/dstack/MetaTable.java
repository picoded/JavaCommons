package picoded.dstack;

// Java imports
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Collections;

// External libraries
import org.apache.commons.lang3.RandomUtils;

// Picoded imports
import picoded.struct.UnsupportedDefaultMap;
import picoded.struct.query.Query;
import picoded.dstack.core.Core_MetaObject;
import picoded.struct.GenericConvertMap;

///
/// MetaTable, serves as the core flexible backend storage implmentation for the whole
/// JStack setup. Its role can be viewed similarly to NoSql, or AWS SimpleDB
/// where almost everything is indexed and cached. 
///
/// On a performance basis, it is meant to trade off raw query performance of traditional optimized 
/// SQL lookup, over flexibility in data model. This is however heavily mintigated by the inclusion 
/// of a JCache layer for non-complex lookup cached reads. Which will in most cases be the main
/// read request load.
/// 
public interface MetaTable extends UnsupportedDefaultMap<String, MetaObject>, CommonStructure {
	
	// MetaObject optimizations
	//----------------------------------------------
	
	/// Does a simple, get and check for null (taking advantage of the null is delete feature)
	///
	/// Default implementation used keySet, which has extremly high performance
	/// penalties in deployments with large scale deployment
	@Override
	default boolean containsKey(Object key) {
		return get(key) != null;
	}
	
	// 
	// MetaObject operations
	//--------------------------------------------------------------------------
	
	/// Generates a new blank object, with a GUID.
	/// Note that save does not trigger, unless its called.
	///
	/// @returns the MetaObject
	MetaObject newObject();
	
	/// Generates a new blank object, with a GUID.
	/// And append all the relevent data to it.
	///
	/// Note that this does trigger a save all
	///
	/// @param  data to save
	///
	/// @returns the MetaObject
	default MetaObject newObject(Map<String, Object> data) {
		MetaObject ret = newObject();
		ret.putAll(data);
		ret.saveAll();
		return ret;
	}
	
	/// Get a MetaObject, and returns it. 
	/// 
	/// Existance checks is performed for such requests
	///
	/// @param  object GUID to fetch
	///
	/// @returns the MetaObject, null if not exists
	MetaObject get(Object oid);
	
	/// Get a MetaObject, and returns it. Skips existance checks if required
	///
	/// @param  object GUID to fetch
	/// @param  boolean used to indicate if an existance check is done for the request
	///
	/// @returns the MetaObject
	MetaObject get(String oid, boolean isUnchecked);
	
	/// Removes a metaobject if it exists, from the DB
	///
	/// @param  object GUID to fetch
	///
	/// @returns NULL
	MetaObject remove(Object key);
	
	// MetaObject utility operations
	//--------------------------------------------------------------------------
	
	/// Get array of MetaObjects
	default MetaObject[] getArrayFromID(String[] idArray, boolean isUnchecked) {
		MetaObject[] retArr = new MetaObject[idArray.length];
		for (int i = 0; i < idArray.length; ++i) {
			retArr[i] = get(idArray[i], isUnchecked);
		}
		return retArr;
	}
	
	// Query operations (to optimize on specific implementation)
	//--------------------------------------------------------------------------
	
	/// Performs a search query, and returns the respective MetaObjects
	///
	/// @param   where query statement
	/// @param   where clause values array
	///
	/// @returns  The MetaObject[] array
	default MetaObject[] query(String whereClause, Object[] whereValues) {
		return query(whereClause, whereValues, null, 0, 0);
	}
	
	/// Performs a search query, and returns the respective MetaObjects
	///
	/// @param   where query statement
	/// @param   where clause values array
	/// @param   query string to sort the order by, use null to ignore
	///
	/// @returns  The MetaObject[] array
	default MetaObject[] query(String whereClause, Object[] whereValues, String orderByStr) {
		return query(whereClause, whereValues, orderByStr, 0, 0);
	}
	
	/// Performs a search query, and returns the respective MetaObjects
	///
	/// @param   where query statement
	/// @param   where clause values array
	/// @param   query string to sort the order by, use null to ignore
	/// @param   offset of the result to display, use -1 to ignore
	/// @param   number of objects to return max, use -1 to ignore
	///
	/// @returns  The MetaObject[] array
	default MetaObject[] query(String whereClause, Object[] whereValues, String orderByStr,
		int offset, int limit) {
		return getArrayFromID(query_id(whereClause, whereValues, orderByStr, offset, limit), true);
	}
	
	/// Performs a search query, and returns the respective MetaObject keys.
	///
	/// This is the GUID key varient of query, this is critical for stack lookup
	///
	/// @param   where query statement
	/// @param   where clause values array
	/// @param   query string to sort the order by, use null to ignore
	///
	/// @returns  The String[] array
	default String[] query_id(String whereClause, Object[] whereValues, String orderByStr) {
		return query_id(whereClause, whereValues, orderByStr, -1, -1);
	}
	
	/// Performs a search query, and returns the respective MetaObject keys.
	///
	/// This is the GUID key varient of query, this is critical for stack lookup
	///
	/// @param   where query statement
	/// @param   where clause values array
	/// @param   query string to sort the order by, use null to ignore
	/// @param   offset of the result to display, use -1 to ignore
	/// @param   number of objects to return max, use -1 to ignore
	///
	/// @returns  The String[] array
	String[] query_id(String whereClause, Object[] whereValues, String orderByStr, int offset,
		int limit);
	
	/// Performs a search query, and returns the respective MetaObjects
	///
	/// @param   where query statement
	/// @param   where clause values array
	///
	/// @returns  The total count for the query
	default long queryCount(String whereClause, Object[] whereValues) {
		// Query and count
		return query_id(whereClause, whereValues, null).length;
	}
	
	// 
	// Get from key names operations (to optimize on specific implementation)
	//--------------------------------------------------------------------------
	
	/// Performs a custom search by configured keyname
	/// 
	/// @param   keyName to lookup for
	///
	/// @returns  The MetaObject[] array
	default MetaObject[] getFromKeyName(String keyName) {
		return getFromKeyName(keyName, null, -1, -1);
	}
	
	/// Performs a custom search by configured keyname
	/// 
	/// @param   keyName to lookup for
	/// @param   query string to sort the order by, use null to ignore
	///
	/// @returns  The MetaObject[] array
	default MetaObject[] getFromKeyName(String keyName, String orderByStr) {
		return getFromKeyName(keyName, orderByStr, -1, -1);
	}
	
	/// Performs a custom search by configured keyname
	/// 
	/// @param   keyName to lookup for
	/// @param   query string to sort the order by, use null to ignore
	/// @param   offset of the result to display, use -1 to ignore
	/// @param   number of objects to return max
	///
	/// @returns  The MetaObject[] array
	default MetaObject[] getFromKeyName(String keyName, String orderByStr, int offset, int limit) {
		return getArrayFromID(getFromKeyName_id(keyName, orderByStr, offset, limit), true);
	}
	
	/// Performs a custom search by configured keyname, and returns its ID array
	///
	/// @param   keyName to lookup for
	///
	/// @returns  The MetaObject[] array
	default String[] getFromKeyName_id(String keyName) {
		return getFromKeyName_id(keyName, null, -1, -1);
	}
	
	/// Performs a custom search by configured keyname, and returns its ID array
	///
	/// @param   keyName to lookup for
	/// @param   query string to sort the order by, use null to ignore
	///
	/// @returns  The MetaObject[] array
	default String[] getFromKeyName_id(String keyName, String orderByStr) {
		return getFromKeyName_id(keyName, orderByStr, -1, -1);
	}
	
	/// Performs a custom search by configured keyname, and returns its ID array
	///
	/// @param   keyName to lookup for
	/// @param   query string to sort the order by, use null to ignore
	/// @param   offset of the result to display, use -1 to ignore
	/// @param   number of objects to return max, use -1 to ignore
	///
	/// @returns  The MetaObject[] array
	default String[] getFromKeyName_id(String keyName, String orderByStr, int offset, int limit) {
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
	/// optimized, and does an iterative search for the various object keys.
	/// which is ridiculously expensive. So avoid calling this unless needed.
	///
	/// The seekDepth parameter is ignored in JSql mode, as its optimized.
	///
	/// @param  seekDepth, which detirmines the upper limit for iterating
	///         objects for the key names, use -1 to search all
	///
	/// @returns  The various key names used in the objects
	default Set<String> getKeyNames(int seekDepth) {
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
	
	/// getKeyNames varient with seekDepth defaulted to 25
	///
	/// @returns  The various key names used in the objects
	default Set<String> getKeyNames() {
		return getKeyNames(25);
	}
	
	// Resolving class inheritence conflict
	//--------------------------------------------------------------------------
	
	///
	/// Removes all data, without tearing down setup
	///
	/// This is equivalent of "TRUNCATE TABLE {TABLENAME}"
	///
	void clear();
	
	// Special iteration support
	//--------------------------------------------------------------------------

	/// Gets and return a random object ID
	///
	/// @return  Random object ID
	default String randomObjectID() {
		// Gets list of possible ID's
		Set<String> idSet = keySet();

		// Randomly pick and ID, and fetch the object
		int size = idSet.size();
		if(size > 0) {
			int chosen = ThreadLocalRandom.current().nextInt(size);
			int idx = 0;
			for(String idString : idSet) {
				if (idx >= chosen) {
					return idString;
				}
				idx++;
			}
		}

		// Possibly a blank set here, return null
		return null;
	}

	/// Gets and returns a random object,
	/// Useful for random validation / checks
	///
	/// @return  Random MetaObject
	default MetaObject randomObject() {
		String oid = randomObjectID();
		if( oid != null ) {
			return get(oid);
		}
		return null;
	}
	
	///
	/// Gets and return the next object ID key for iteration given the current ID, 
	/// null gets the first object in iteration.
	///
	/// It is important to note actual iteration sequence is implementation dependent.
	/// And does not gurantee that newly added objects, after the iteration started,
	/// will be part of the chain of results.
	///
	/// Similarly if the currentID was removed midway during iteration, the return 
	/// result is not properly defined, and can either be null, or the closest object matched
	/// or even a random object.
	///
	/// It is however guranteed, if no changes / writes occurs. A complete iteration
	/// will iterate all existing objects.
	///
	/// The larger intention of this function, is to allow a background thread to slowly
	/// iterate across all objects, eventually. With an acceptable margin of loss on,
	/// recently created/edited object. As these objects will eventually be iterated in
	/// repeated rounds on subsequent calls.
	///
	/// Due to its roughly random nature in production (with concurrent objects generated)
	/// and its iterative nature as an eventuality. The phrase looselyIterate was chosen,
	/// to properly reflect its nature.
	///
	/// Another way to phrase it, in worse case scenerio, its completely random, eventually iterating all objects
	/// In best case scenerio, it does proper iteration as per normal.
	///
	/// @param   Current object ID, can be NULL
	///
	/// @return  Next object ID, if found
	///
	default String looselyIterateObjectID(String currentID) {
		// By default this is an inefficent implementation
		// of sorting the keyset, and returning in the respective order
		ArrayList<String> idList = new ArrayList<String>(keySet());
		Collections.sort(idList);
		int size = idList.size();

		// Blank set, nothing to iterate
		if( size == 0 ) {
			return null;
		}

		// But it works
		if( currentID == null ) {
			// return first object
			return idList.get(0);
		}

		// Time to iterate it all
		for(int idx=0; idx<size; ++idx) {
			if( currentID.equals( idList.get(idx) ) ) {
				// Current position found
				if( idx >= (size - 1) ) {
					// This is last object, return null (end)
					return null;
				}
				// Else get the next object
				return idList.get(idx+1);
			}
			// If position not found, continue iterating
		}

		// If this position is reached, 
		// possibly object was deleted mid iteration
		//
		// Fallsback to a random object to iterate
		return randomObjectID();
	}
	
	/// MetaObject varient of randomlyIterateObjectID
	///
	/// @param   MetaObject to iterate next from, can be null
	///
	/// @return  Random MetaObject
	default MetaObject looselyIterateObject(MetaObject currentObj) {
		String currentID = (currentObj != null)? currentObj._oid() : null;
		String retID = looselyIterateObjectID(currentID);
		return (retID != null)? get(retID) : null;
	}
	
}
