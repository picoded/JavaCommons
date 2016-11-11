package picoded.struct.query;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import picoded.struct.ArrayListMap;
import picoded.struct.query.internal.QueryFilter;


///
/// Representas a query condition, that can be used as a java Predicate against a collection
///
public interface Query extends Predicate<Object> {
	
	//
	// Static builder
	//--------------------------------------------------------------------
	
	/// Build the query using no predefiend arguments
	static Query build(String queryString) {
		return QueryFilter.buildQuery(queryString, null, null);
	}
	
	/// Build the query using argumented array
	static Query build(String queryString, Object[] argumentArr) {
		return QueryFilter.buildQuery(queryString, null, argumentArr);
	}
	
	/// Build the query using the parameter map
	static Query build(String queryString, Map<String, Object> paramMap) {
		return QueryFilter.buildQuery(queryString, paramMap, null);
	}
	
	//
	// Public test functions
	//--------------------------------------------------------------------
	
	/// The test operator, asserts if the element matches
	/// against its cached argument map value
	///
	/// @param   the object to test against
	///
	/// @returns  boolean indicating true / false
	@Override
	boolean test(Object t);
	
	/// To test against a specified value map,
	/// Note that the test varient without the Map
	/// is expected to test against its cached varient
	/// that is setup by the constructor if applicable
	///
	/// @param   the object to test against
	/// @param   the argument map, if applicable
	///
	/// @returns  boolean indicating true / false
	boolean test(Object t, Map<String, Object> argMap);
	
	//
	// Public accessors
	//--------------------------------------------------------------------
	
	/// Gets the query type
	QueryType type();
	
	//
	// Condition only accessors
	//--------------------------------------------------------------------
	
	/// Gets the field name
	default String fieldName() {
		return null;
	}
	
	/// Gets the argument name
	default String argumentName() {
		return null;
	}
	
	/// Gets the default argument map
	default Map<String, Object> defaultArgumentMap() {
		return null;
	}
	
	/// Gets the default argument map
	default Object defaultArgumentValue() {
		if (defaultArgumentMap() != null) {
			return defaultArgumentMap().get(argumentName());
		}
		return null;
	}
	
	/// Indicates if its a basic operator
	default boolean isBasicOperator() {
		return false;
	}
	
	//
	// Combination only accessors
	//--------------------------------------------------------------------
	
	/// Indicates if its a combination operator
	default boolean isCombinationOperator() {
		return false;
	}
	
	/// Gets the children conditions
	default List<Query> childrenQuery() {
		return null;
	}
	
	//
	// Query mapping search, modification, and arguments output
	//--------------------------------------------------------------------
	
	/// Fetch the nested query map, of basic operators
	default Map<String, List<Query>> fieldQueryMap() {
		return fieldQueryMap(new ArrayListMap<String, Query>());
	}
	
	/// Fetch the nested query map, of basic operators.
	/// This is the internally used recursive function.
	default Map<String, List<Query>> fieldQueryMap(ArrayListMap<String, Query> ret) {
		
		if (isBasicOperator()) {
			// Basic operator returns self
			ret.addToListIfNotExists(fieldName(), this);
			return ret.genericMap();
		} else if (isCombinationOperator()) {
			// Child nodes iteration 
			for (Query child : childrenQuery()) {
				child.fieldQueryMap(ret);
			}
		}
		
		// Return as generic
		return ret.genericMap();
	}
	
	/// Does a replacement of the selected query
	/// and replace the amended query object (maybe itself)
	///
	/// If amendment fail, it replace itself
	default Query replaceQuery(Query original, Query replacement) {
		
		// The original itself needs replacement
		if (this == original) {
			return replacement;
		}
		
		// Iterate only combination operators
		if (isCombinationOperator()) {
			
			// Does the replacement
			List<Query> subList = childrenQuery();
			int subListLen = subList.size();
			
			// Replace the nodes, or do recursive calls
			for (int i = 0; i < subListLen; ++i) {
				Query node = subList.get(i);
				
				if (node.equals(original)) {
					subList.set(i, replacement);
				} else if (node.equals(replacement)) {
					// ignroe replacement nodes
				} else {
					// recursive call
					node.replaceQuery(original, replacement);
				}
			}
			
			// Apply the sublist ?
			// childrenQuery(subList);
		}
		
		return this;
	}
	
	/// Returns the argument values used,
	/// In a list, in accordance to their query component order
	default List<Object> queryArgumentsList() {
		return queryArgumentsList(new ArrayList<Object>());
	}
	
	/// Returns the argument values used,
	/// In a list, in accordance to their query component order
	///
	/// This is the internally used recursive function.
	default List<Object> queryArgumentsList(List<Object> ret) {
		
		if (isBasicOperator()) {
			
			// Push basic operator args to return output
			ret.add(this.defaultArgumentValue());
			
		} else if (isCombinationOperator()) {
			
			// Child nodes iteration 
			// Recursive scan
			for (Query child : childrenQuery()) {
				child.queryArgumentsList(ret);
			}
			
		}
		
		return ret;
	}
	
	/// Returns all query arguments used,
	/// As a map, in accordance to the default map, value used
	///
	/// This is the internally used recursive function.
	default Map<String, Object> queryArgumentsMap() {
		return queryArgumentsMap(new HashMap<String, Object>());
	}
	
	/// Returns all query arguments used,
	/// As a map, in accordance to the default map, value used
	///
	/// This is the internally used recursive function.
	default Map<String, Object> queryArgumentsMap(Map<String, Object> ret) {
		
		if (isBasicOperator()) {
			
			// Push basic operator args to return output
			ret.put(this.argumentName(), this.defaultArgumentValue());
			
		} else if (isCombinationOperator()) {
			
			// Child nodes iteration 
			// Recursive scan
			for (Query child : childrenQuery()) {
				child.queryArgumentsMap(ret);
			}
			
		}
		
		return ret;
	}
	
	//
	// To string conversion
	//--------------------------------------------------------------------
	
	/// Gets the operator symbol
	String operatorSymbol();
	
	/// Returns the query string
	@Override
	String toString();
	
	/// Returns the query string, with the SQL array format (not map format)
	default String toSqlString() {
		return toString().replaceAll(":[0-9]+", "?");
	}
	
	//
	// Map based search
	//--------------------------------------------------------------------
	
	/// Searches using the query, and returns the resulting set
	default <K, V> List<V> search(Map<K, V> set) {
		List<V> ret = new ArrayList<V>();
		//for (K key : set.keySet()) {
		for (Entry<K, V> entry : set.entrySet()) {
			V val = entry.getValue();
			if (test(val)) {
				ret.add(val);
			}
		}
		return ret;
	}
	
	/// Searches using the query, and sorted by the comparator
	default <K, V> List<V> search(Map<K, V> set, Comparator<V> compareFunc) {
		List<V> ret = search(set);
		Collections.sort(ret, compareFunc);
		return ret;
	}
	
	/// Searches using the query, and sorted by the order by query
	default <K, V> List<V> search(Map<K, V> set, String orderBy) {
		return search(set, new OrderBy<V>(orderBy));
	}
	
	//
	// Name value pair extraction from query
	//--------------------------------------------------------------------
	
	/// Extract out the respective query keys, and values
	default Map<String, List<Object>> keyValuesMap() {
		return keyValuesMap(new HashMap<String, List<Object>>());
	}
	
	/// Extract out the respective query keys, and values
	Map<String, List<Object>> keyValuesMap(Map<String, List<Object>> mapToReturn);
	
}
