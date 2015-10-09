package picoded.JStruct.internal;

/// Java imports
import java.util.logging.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/// Picoded imports
import picoded.conv.*;
import picoded.struct.*;
import picoded.enums.*;
import picoded.JStruct.*;
import picoded.struct.query.*;

public class JStructUtils {
	
	///
	/// Utility funciton, used to sort and limit the result of a query
	///
	/// @param   list of MetaObject to sort
	/// @param   query string to sort the order by, use null to ignore
	/// @param   offset of the result to display, use -1 to ignore
	/// @param   number of objects to return max
	///
	/// @returns  The MetaObject[] array
	///
	public static MetaObject[] sortAndOffsetListToArray(List<MetaObject> retList, String orderByStr, int offset,
		int limit) {
		
		// Ensure the list is editable
		if (!(retList instanceof ArrayList)) {
			retList = new ArrayList<MetaObject>(retList);
		}
		
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
				return new MetaObject[0];
			}
			
			// Ensures the upper end does not go out of bound
			int end = size;
			if (limit > -1) {
				end = offset + limit;
			}
			if (end > size) {
				end = size;
			}
			
			// Out of range
			if (end <= offset) {
				return new MetaObject[0];
			}
			
			// Get sublist
			retList = retList.subList(offset, end);
		}
		
		// Convert to array, and return
		return retList.toArray(new MetaObject[0]);
	}
	
}
