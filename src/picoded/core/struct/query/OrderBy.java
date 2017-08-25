package picoded.core.struct.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Locale;

import picoded.core.struct.MutablePair;
import picoded.core.struct.query.internal.QueryUtils;

/**
 * Utility class that provides SQL styel OrderBy functionality
 * to sort object collection lists
 **/
public class OrderBy<T> implements Comparator<T>, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	//-----------------------------------------------------------------
	// Constructor setup
	//-----------------------------------------------------------------
	
	/**
	 * Order types used, tracked internally
	 **/
	public enum OrderType {
		ASC, DESC
	}
	
	/**
	 * Comparision configuration, Mutable pair represents fieldname, then sorting order
	 **/
	protected List<MutablePair<String, OrderType>> _comparisionConfig = new ArrayList<MutablePair<String, OrderType>>();
	
	/**
	 * Constructor built with given order by string
	 **/
	public OrderBy(String orderByString) {
		// Clear out excess whitespace
		orderByString = orderByString.trim().replaceAll("\\s+", " ");
		
		// Order by string split array
		String[] orderByArr = orderByString.split(",");
		
		// Terminates if null
		if (orderByArr == null) {
			return;
		}
		
		// Iterate order by array, and set each configuration up
		for (String orderSet : orderByArr) {
			
			// Get orderset, without excess whitespace
			orderSet = orderSet.trim();
			if (orderSet.length() <= 0) {
				throw new RuntimeException("Invalid OrderBy string query: " + orderByString);
			}
			
			// Default ordering is asecending
			OrderBy.OrderType ot = OrderBy.OrderType.ASC;
			
			// Check for DESC / ASC suffix
			if (orderSet.length() > 4) {
				String lowerCaseOrderSet = orderSet.toLowerCase(Locale.ENGLISH);
				if (lowerCaseOrderSet.endsWith(" desc")) {
					ot = OrderBy.OrderType.DESC;
					orderSet = orderSet.substring(0, orderSet.length() - 5).trim();
				} else if (lowerCaseOrderSet.endsWith(" asc")) {
					ot = OrderBy.OrderType.ASC;
					orderSet = orderSet.substring(0, orderSet.length() - 4).trim();
				}
			}
			
			// Unwrap the field name (since they will be passed by params anyway)
			String field = QueryUtils.unwrapFieldName(orderSet);
			
			// Add the comparision option
			_comparisionConfig.add(new MutablePair<String, OrderType>(field, ot));
		}
		
		// Done
	}
	
	//--------------------------------------------------------------------
	// String manipulations
	//--------------------------------------------------------------------
	
	/**
	 *
	 * Converts the order by query string to an SQL safe order by query string
	 *
	 * @return    the SQL orderby query string
	 **/
	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		
		boolean first = true;
		for (MutablePair<String, OrderType> set : _comparisionConfig) {
			if (!first) {
				ret.append(", ");
			} else {
				first = false;
			}
			
			String safeKeyName = set.getLeft().replaceAll("\\\\", "\\\\").replaceAll("\\\"", "\\\"")
				.replaceAll("\\'", "\\'");
			ret.append('"' + safeKeyName + '"');
			ret.append(" ");
			
			if (set.getRight() == OrderType.ASC) {
				ret.append("ASC");
			} else {
				ret.append("DESC");
			}
		}
		
		return ret.toString();
	}
	
	/**
	 * Does a keyname subtitution
	 *
	 * @param {String}   The original key string
	 * @param {String}   The replacement key string
	 *
	 * @return  boolean result if all relevent keys were found, and replaced
	 **/
	public boolean replaceKeyName(String original, String replacement) {
		boolean res = false;
		for (MutablePair<String, OrderType> set : _comparisionConfig) {
			if (original.equals(set.getLeft())) {
				set.setLeft(replacement);
			}
		}
		return res;
	}
	
	//--------------------------------------------------------------------
	// To OrderBy implmentation
	//--------------------------------------------------------------------
	
	/**
	 * Gets the orderby keys used in sorting
	 **/
	public Set<String> getKeyNames() {
		Set<String> resSet = new HashSet<String>();
		for (MutablePair<String, OrderType> set : _comparisionConfig) {
			resSet.add(set.getLeft());
		}
		return resSet;
	}
	
	//--------------------------------------------------------------------
	// Comparator implmentation
	//--------------------------------------------------------------------
	
	/**
	 * Dynamic comparator setup
	 *
	 * @params o1 - the first object to be compared.
	 * @params o2 - the second object to be compared.
	 *
	 * @returns -1, 0, or 1 as the first argument is less than, equal to, or greater than the second.
	 **/
	@Override
	public int compare(T o1, T o2) {
		
		/**
		 * Scan and compare, and return the differences
		 **/
		for (MutablePair<String, OrderType> comparePair : _comparisionConfig) {
			Object left = QueryUtils.getFieldValue(o1, comparePair.getLeft());
			Object right = QueryUtils.getFieldValue(o2, comparePair.getLeft());
			
			int diff = CompareUtils.dynamicCompare(left, right);
			
			// Skip if equals
			if (diff == 0) {
				continue;
			}
			
			// Return its value / flipped value
			if (comparePair.getRight() == OrderType.ASC) {
				return diff;
			} //else {
			return -diff;
			//}
		}
		
		return CompareUtils.dynamicCompare(o1, o2); //fallback
	}
}
