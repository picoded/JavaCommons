package picoded.core.struct.query.utils;

import java.util.Map;

public interface CollectionQueryForIDInterface <K, V extends Map<String,Object>> 
	extends CollectionQueryInterface<V>
{
	
	/**
	 * Performs a search query, and returns the respective DataObject keys.
	 *
	 * This is the GUID key varient of query, this is critical for stack lookup
	 *
	 * @param   where query statement
	 * @param   where clause values array
	 *
	 * @return  The String[] array
	 **/
	default K[] query_id(String whereClause, Object[] whereValues) {
		return query_id(whereClause, whereValues, null, -1, -1);
	}
	
	/**
	 * Performs a search query, and returns the respective DataObject keys.
	 *
	 * This is the GUID key varient of query, this is critical for stack lookup
	 *
	 * @param   where query statement
	 * @param   where clause values array
	 * @param   query string to sort the order by, use null to ignore
	 *
	 * @return  The String[] array
	 **/
	default K[] query_id(String whereClause, Object[] whereValues, String orderByStr) {
		return query_id(whereClause, whereValues, orderByStr, -1, -1);
	}
	
	/**
	 * Performs a search query, and returns the respective DataObject keys.
	 *
	 * This is the GUID key varient of query, this is critical for stack lookup
	 *
	 * @param   where query statement
	 * @param   where clause values array
	 * @param   query string to sort the order by, use null to ignore
	 * @param   offset of the result to display, use -1 to ignore
	 * @param   number of objects to return max, use -1 to ignore
	 *
	 * @return  The String[] array
	 **/
	K[] query_id(String whereClause, Object[] whereValues, String orderByStr, int offset,
		int limit);
	
}