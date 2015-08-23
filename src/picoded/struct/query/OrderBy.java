package picoded.struct.query;

import java.util.function.*;
import java.util.*;

import picoded.struct.query.internal.*;
import picoded.struct.query.*;
import picoded.struct.*;

/// Utility class that provides SQL styel OrderBy functionality
/// to sort object collection lists
public class OrderBy<T> implements Comparator<T> {
	
	//
	// Constructor setup
	//-----------------------------------------------------------------
	
	/// Order types used
	protected enum OrderType {
		ASC,
		DESC
	}
	
	/// Comparision configuration, Mutable pair represents fieldname, then sorting order
	protected List<MutablePair<String,OrderType>> _comparisionConfig = new ArrayList<MutablePair<String,OrderType>>();
	
	/// Constructor built with given order by string
	public OrderBy(String orderByString) {
		// Clear out excess whitespace
		orderByString = orderByString.replaceAll("\\s+"," ").trim();
		
		// Order by string split array
		String[] orderByArr = orderByString.split(",");
		
		// Terminates if null
		if( orderByArr == null ) {
			return;
		}
		
		// Iterate order by array, and set each configuration up
		for(String orderSet : orderByArr) {
			String[] orderByItem = orderSet.trim().split(" ");
			
			if( orderByItem.length <= 0 ) {
				throw new RuntimeException("Invalid OrderBy string query: "+orderByString);
			}
			
			String field = QueryUtils.unwrapFieldName(orderByItem[0]);
			OrderType ot = OrderType.ASC;
			
			if( orderByItem.length >= 2 ) {
				String typeStr = orderByItem[1];
				if( typeStr.equalsIgnoreCase("DESC") ) {
					ot = OrderType.DESC;
				} else if( typeStr.equalsIgnoreCase("ASC") ) {
					ot = OrderType.ASC;
				} else {
					throw new RuntimeException("Invalid OrderType string query: "+orderByString);
				}
			}
			
			_comparisionConfig.add( new MutablePair<String,OrderType>(field, ot) );
		}
		
		// Done
	}
	
	//
	// Core protected functions
	//--------------------------------------------------------------------
	
	//
	// Comparator implmentation
	//--------------------------------------------------------------------
	
	///
	/// Dynamic comparator setup
	///
	/// @params o1 - the first object to be compared.
	/// @params o2 - the second object to be compared.
	///
	/// @returns -1, 0, or 1 as the first argument is less than, equal to, or greater than the second.
	///
	public int compare(T o1, T o2) {
		
		/// Scan and compare, and return the differences
		for(MutablePair<String,OrderType> comparePair : _comparisionConfig) {
			Object left = QueryUtils.getFieldValue(o1, comparePair.getLeft());
			Object right = QueryUtils.getFieldValue(o2, comparePair.getLeft());
			
			int diff = CompareUtils.dynamicCompare(left, right);
			
			// Skip if equals
			if( diff == 0 ) {
				continue; 
			}
			
			// Return its value / flipped value
			if( comparePair.getRight() == OrderType.ASC ) {
				return diff;
			} else {
				return -diff;
			}
		}
		
		return CompareUtils.dynamicCompare(o1, o2); //fallback
	}
}
