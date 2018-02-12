package picoded.core.struct.query;

import java.util.*;

/**
 * Utility of query functions, used to query a collection and sort its results
 * 
 * As per everything else in the query library. This is meant to search and sort
 * collections of Map<String,Object> type objects
 */
public class QueryUtils {

	/**
	 * Takes a list of values, and extract its offset of values.
	 * 
	 * This can be considered a "silent" version of subList,
	 * where out of bound results would simply return a blank list.
	 * 
	 * @param inList  list to get get subList from. Null returns a blank list
	 * @param offset  result to return from, use -1 to ignore
	 * @param limit   number of results to return at max, use -1 to ignore
	 * 
	 * @return  sublist of the result chosen (if any)
	 */
	public static <V> List<V> offsetSubList(List<V> inList, int offset, int limit) {
		// Parameter check
		if( inList == null ) {
			return new ArrayList<V>();
		}

		// Get sublist if needed
		if (offset >= 1 || limit >= 1) {
			// Get size of raw list
			int size = inList.size();
			
			// Out of bound, return blank
			if (offset >= size) {
				return new ArrayList<V>();
			}
			
			// Get the ending index (based on limit)
			int end = size;
			if (limit > -1) {
				end = offset + limit;
			}

			// Ensures the upper end does not go out of bound
			if (end > size) {
				end = size;
			}
			
			// Get and return sublist
			return inList.subList(offset, end);
		}

		// no offset occur, return the list itself
		return inList;
	}

	/**
	 * Takes a list and sort by the orderBy string
	 * 
	 * @param  inList   list to sort, and return
	 * @param  orderBy  order by string to apply
	 * 
	 * @return  list of result
	 */
	public static <V> List<V> sort(List<V> inList, String orderBy) {
		return null;
	}
}