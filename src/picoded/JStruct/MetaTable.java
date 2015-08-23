package picoded.JStruct;

/// Java imports
import java.util.*;

/// Picoded imports
import picoded.conv.GUID;
import picoded.struct.CaseInsensitiveHashMap;
import picoded.struct.UnsupportedDefaultMap;
import picoded.struct.query.*;
import picoded.conv.ListValueConv;

/// MetaTable, servs as the core flexible backend storage implmentation for the whole
/// JStack setup. Its role can be viewed similarly to NoSql, or AWS SimpleDB
/// where almost everything is indexed and cached. 
/// 
/// On a performance basis, it is meant to trade off raw query performance of traditional optimized 
/// SQL lookup, over flexibility in data model. This is however heavily mintigated by the inclusion 
/// of a JCache layer for non-complex lookup cached reads. Which will in most cases be the main
/// read request load.
/// 
public interface MetaTable extends UnsupportedDefaultMap<String, MetaObject> {

	///
	/// Temp mode optimization, used to indicate pure session like data,
	/// that does not require persistance (or even SQL)
	///
	///--------------------------------------------------------------------------
	
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
	
	///
	/// Backend system setup / teardown
	///--------------------------------------------------------------------------
	
	/// Setsup the backend storage table, etc. If needed
	public void systemSetup();
	
	/// Teardown and delete the backend storage table, etc. If needed
	public void systemTeardown();
	
	/// Perform maintenance, mainly removing of expired data if applicable
	public void maintenance();
	
	/// 
	/// MetaObject operations
	///--------------------------------------------------------------------------
	
	/// Generates a new blank object, with a GUID
	public MetaObject newObject();
	
	/// Gets the MetaObject, regardless of its actual existance
	public MetaObject uncheckedGet(String _oid);
	
	/// PUT, returns the object ID (especially when its generated), note that this
	/// adds the value in a merger style. Meaning for example, existing values not explicitely
	/// nulled or replaced are maintained
	public MetaObject append(String _oid, Map<String, Object> obj);
	
	/// 
	/// Query operations
	///--------------------------------------------------------------------------
	
	/// Performs a search query, and returns the respective MetaObjects
	public default MetaObject[] query(String whereClause, Object[] whereValues) {
		return query(whereClause, whereValues, null);
	}
	
	/// Performs a search query, and returns the respective MetaObjects
	public default MetaObject[] query(String whereClause, Object[] whereValues, String orderByStr ) {
		return query(whereClause, whereValues, orderByStr, 0, 0);
	}
	
	/// Performs a search query, and returns the respective MetaObjects
	public default MetaObject[] query(String whereClause, Object[] whereValues, String orderByStr, int offset, int limit ) {
		
		// The return list
		List<MetaObject> retList = null;
		
		// Setup the query, if needed
		if(whereClause == null) { //null gets all
			retList = new ArrayList<MetaObject>( this.values() );
		} else {
			Query queryObj = Query.build(whereClause, whereValues);
			retList = queryObj.search(this);
		}
		
		// Sorting the order, if needed
		if( orderByStr != null && (orderByStr = orderByStr.trim()).length() > 0 ) {
			// Creates the order by sorting, with _oid
			OrderBy<MetaObject> sorter = new OrderBy<MetaObject>( orderByStr+" , _oid");
			
			// Sort it
			Collections.sort(retList, sorter);
		}
		
		// Get sublist if needed
		if( offset >= 1 ) {
			retList = retList.subList( offset, offset+limit );
		}
		
		// Convert to array
		return retList.toArray(new MetaObject[0]);
	}
}
