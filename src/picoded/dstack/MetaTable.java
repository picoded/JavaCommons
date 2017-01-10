package picoded.dstack;

// Java imports
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// External libraries
import org.apache.commons.lang3.RandomUtils;

// Picoded imports
import picoded.struct.UnsupportedDefaultMap;
import picoded.struct.query.Query;
import picoded.dstack.core.Core_MetaObject;

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
public interface MetaTable extends DataStructureSetup<String, MetaObject> {
	
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
	/// Note that save does not trigger, unless its called.
	///
	/// @param  data to save
	///
	/// @returns the MetaObject
	default MetaObject newObject(Map<String,Object> data) {
		MetaObject ret = newObject();
		ret.putAll(data);
		return ret;
	}
	
	/// Get a MetaObject, and returns it. 
	/// 
	/// Existance checks is performed for such requests
	///
	/// @param  object GUID to fetch
	///
	/// @returns the MetaObject, null if not exists
	MetaObject get(String oid);
	
	/// Get a MetaObject, and returns it. Skips existance checks if required
	///
	/// @param  object GUID to fetch
	/// @param  boolean used to indicate if an existance check is done for the request
	///
	/// @returns the MetaObject
	MetaObject get(String oid, boolean isUnchecked);
	
	
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
	default MetaObject[] query(String whereClause, Object[] whereValues, String orderByStr, int offset, int limit) {
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
	String[] query_id(String whereClause, Object[] whereValues, String orderByStr, int offset, int limit);
	
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
	/// optimize, and does an iterative search for the various object keys.
	/// which is ridiculously expensive. So avoid calling this unless needed.
	///
	/// The seekDepth parameter is ignored in JSql mode, as its optimized.
	///
	/// @param  seekDepth, which detirmines the upper limit for iterating
	///         objects for the key names, use -1 to search all
	///
	/// @returns  The various key names used in the objects
	///
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
	
	/// getKeyNames varient with seekDepth defaulted to 10
	///
	/// @returns  The various key names used in the objects
	///
	default Set<String> getKeyNames() {
		return getKeyNames(10);
	}
	
}
