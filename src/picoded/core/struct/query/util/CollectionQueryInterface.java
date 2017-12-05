package picoded.core.struct.query.utils;

import java.util.Map;

/**
 * Utility interface, to provide a standardised interface to query
 * Collection for various results.
 * 
 * Ideally this should be optimized for the relevent backend.
 */
public interface CollectionQueryInterface <V extends Map<String,Object>> {


	// Query operations (to optimize on specific implementation)
	//--------------------------------------------------------------------------
	
	/**
	 * Performs a search query, and returns the respective DataObjects
	 *
	 * @param   where query statement
	 * @param   where clause values array
	 *
	 * @return  The DataObject[] array
	 **/
	default V[] query(String whereClause, Object[] whereValues) {
		return query(whereClause, whereValues, null, 0, 0);
	}
	
	/**
	 * Performs a search query, and returns the respective DataObjects
	 *
	 * @param   where query statement
	 * @param   where clause values array
	 * @param   query string to sort the order by, use null to ignore
	 *
	 * @return  The DataObject[] array
	 **/
	default V[] query(String whereClause, Object[] whereValues, String orderByStr) {
		return query(whereClause, whereValues, orderByStr, 0, 0);
	}
	
	/**
	 * Performs a search query, and returns the respective DataObjects
	 *
	 * @param   where query statement
	 * @param   where clause values array
	 * @param   query string to sort the order by, use null to ignore
	 * @param   offset of the result to display, use -1 to ignore
	 * @param   number of objects to return max, use -1 to ignore
	 *
	 * @return  The DataObject[] array
	 **/
	V[] query(String whereClause, Object[] whereValues, String orderByStr,
		int offset, int limit);
	
	/**
	 * Performs a search query, and returns the respective DataObjects
	 *
	 * @param   where query statement
	 * @param   where clause values array
	 *
	 * @return  The total count for the query
	 **/
	long queryCount(String whereClause, Object[] whereValues);
	
}