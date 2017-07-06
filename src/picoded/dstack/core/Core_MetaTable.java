package picoded.dstack.core;

// Java imports
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Picoded imports
import picoded.conv.ConvertJSON;
import picoded.set.ObjectToken;
import picoded.struct.query.*;
import picoded.dstack.*;

///
/// Common base utility class of MetaTable
///
/// Does not actually implement its required feature,
/// but helps provide a common base line for all the various implementation.
///
abstract public class Core_MetaTable extends Core_DataStructure<String, MetaObject> implements
	MetaTable {

	//--------------------------------------------------------------------------
	//
	// Generic Utility functions
	//
	//--------------------------------------------------------------------------

	/// Ensures the returned value is not refrencing the input value, cloning if needed
	///
	/// @return  The cloned value, with no risk of modifying the original.
	public Object detachValue(Object in) {
		if (in instanceof byte[]) { //bytearray support
			byte[] ori = (byte[]) in;
			byte[] cop = new byte[ori.length];
			for (int a = 0; a < ori.length; ++a) {
				cop[a] = ori[a];
			}
			return cop;
		}
		return ConvertJSON.toObject(ConvertJSON.fromObject(in));
	}

	//--------------------------------------------------------------------------
	//
	// Query Utility functions
	//
	//--------------------------------------------------------------------------

	///
	/// Utility funciton, used to sort and limit the result of a query
	///
	/// @param   list of MetaObject to sort and return
	/// @param   query string to sort the order by, use null to ignore
	/// @param   offset of the result to display, use -1 to ignore
	/// @param   number of objects to return max
	///
	/// @returns  The MetaObject list to return
	///
	public static List<MetaObject> sortAndOffsetList(List<MetaObject> retList, String orderByStr,
		int offset, int limit) {

		// Sorting the order, if needed
		if (orderByStr != null && (orderByStr = orderByStr.trim()).length() > 0) {
			// Creates the order by sorting, with _oid
			OrderBy<MetaObject> sorter = new OrderBy<MetaObject>(orderByStr + " , _oid");

			// Sort it
			Collections.sort(retList, sorter);
		}

		// Get sublist if needed
		if (offset >= 1 || limit >= 1) {
			int size = retList.size();

			// Out of bound, return blank
			if (offset >= size) {
				return new ArrayList<MetaObject>();
			}

			// Ensures the upper end does not go out of bound
			int end = size;
			if (limit > -1) {
				end = offset + limit;
			}
			if (end > size) {
				end = size;
			}

			// // Out of range
			// if (end <= offset) {
			// 	return new MetaObject[0];
			// }

			// Get sublist
			retList = retList.subList(offset, end);
		}

		// Returns the list, you can easily convert to an array via "toArray(new MetaObject[0])"
		return retList;
	}

	//--------------------------------------------------------------------------
	//
	// MetaObject removal
	//
	//--------------------------------------------------------------------------

	/// Removes a metaobject if it exists, from the DB
	///
	/// @param  object GUID to fetch, OR the MetaObject itself
	///
	/// @returns NULL
	public MetaObject remove(Object key) {
			if( key instanceof MetaObject ) {
				// Removal via MetaObject itself
				metaObjectRemoteDataMap_remove( ((MetaObject)key)._oid() );
			} else {
				// Remove using the ID
				metaObjectRemoteDataMap_remove(key.toString());
			}
			return null;
	}

	/// [Internal use, to be extended in future implementation]
	///
	/// Removes the complete remote data map, for MetaObject.
	/// This is used to nuke an entire object
	///
	/// @param  Object ID to remove
	///
	/// @return  nothing
	abstract protected void metaObjectRemoteDataMap_remove(String oid);

	//--------------------------------------------------------------------------
	//
	// Functions, used by MetaObject
	// [Internal use, to be extended in future implementation]
	//
	//--------------------------------------------------------------------------

	/// [Internal use, to be extended in future implementation]
	///
	/// Gets the complete remote data map, for MetaObject.
	/// This is used to get the raw map data from the backend.
	///
	/// @param  Object ID to get
	///
	/// @return  The raw Map object to build the MetaObject, null if does not exists
	abstract protected Map<String, Object> metaObjectRemoteDataMap_get(String oid);

	/// [Internal use, to be extended in future implementation]
	///
	/// Updates the actual backend storage of MetaObject
	/// either partially (if supported / used), or completely
	///
	/// @param   Object ID to get
	/// @param   The full map of data. This is required as not all backend implementations allow partial update
	/// @param   Keys to update, this is used to optimize certain backends
	abstract protected void metaObjectRemoteDataMap_update(String oid, Map<String, Object> fullMap,
		Set<String> keys);

	//--------------------------------------------------------------------------
	//
	// Query functions
	// [Should really be overwritten, as this does the inefficent lazy way]
	//
	//--------------------------------------------------------------------------

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
	public String[] query_id(String whereClause, Object[] whereValues, String orderByStr,
		int offset, int limit) {

		// The return list of MetaObjects
		List<MetaObject> retList = null;

		// Setup the query, if needed
		if (whereClause == null) {
			// Null gets all
			retList = new ArrayList<MetaObject>(this.values());
		} else {
			// Performs a search query
			Query queryObj = Query.build(whereClause, whereValues);
			retList = queryObj.search(this);
		}

		// Sort, offset, convert to array, and return
		retList = sortAndOffsetList(retList, orderByStr, offset, limit);

		// Prepare the actual return string array
		int retLength = retList.size();
		String[] ret = new String[retLength];
		for (int a = 0; a < retLength; ++a) {
			ret[a] = retList.get(a)._oid();
		}

		// Returns
		return ret;
	}

	/// Performs a custom search by configured keyname
	///
	/// @param   keyName to lookup for
	/// @param   query string to sort the order by, use null to ignore
	/// @param   offset of the result to display, use -1 to ignore
	/// @param   number of objects to return max
	///
	/// @returns  The MetaObject[] array
	public MetaObject[] getFromKeyName(String keyName, String orderByStr, int offset, int limit) {

		// The return list
		List<MetaObject> retList = new ArrayList<MetaObject>();

		// Iterate the list, add if containsKey
		for (MetaObject obj : values()) {
			if (obj.containsKey(keyName)) {
				retList.add(obj);
			}
		}

		// Sort, offset, convert to array, and return
		return sortAndOffsetList(retList, orderByStr, offset, limit).toArray(new MetaObject[0]);
	}

	//--------------------------------------------------------------------------
	//
	// MetaObject operations
	//
	//--------------------------------------------------------------------------

	/// Generates a new blank object, with a GUID
	///
	/// @returns the MetaObject
	public MetaObject newObject() {
		// Generating a new object
		MetaObject ret = new Core_MetaObject(this, null, null, false);

		// Baking in _createTime stamp
		ret.put("_createTime", currentSystemTimeInSeconds());

		// Actual return
		return ret;
	}

	/// Get a MetaObject, and returns it. Skips existance checks if required
	///
	/// @param  object GUID to fetch
	/// @param  boolean used to indicate if an existance check is done for the request
	///
	/// @returns the MetaObject
	public MetaObject get(String oid, boolean isUnchecked) {
		if (isUnchecked) {
			return new Core_MetaObject(this, oid, null, false);
		} else {
			return get(oid);
		}
	}

	/// Get a MetaObject, and returns it.
	///
	/// Existance checks is performed for such requests
	///
	/// @param  object GUID to fetch
	///
	/// @returns the MetaObject, null if not exists
	public MetaObject get(Object oid) {
		// String oid
		String soid = (oid != null) ? oid.toString() : null;

		// Get remote data map
		Map<String, Object> fullRemote = metaObjectRemoteDataMap_get(soid);

		// Return null, if there is no data
		if (fullRemote == null) {
			return null;
		}

		// Return a Metaobject
		return new Core_MetaObject(this, soid, fullRemote, true);
	}

	//--------------------------------------------------------------------------
	//
	// Constructor and maintenance
	//
	//--------------------------------------------------------------------------

	///
	/// Maintenance step call, however due to the nature of most implementation not
	/// having any form of time "expirary", this call does nothing in most implementation.
	///
	/// As such im making that the default =)
	///
	@Override
	public void maintenance() {
		// Does nothing
	}
}
