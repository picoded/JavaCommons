package picoded.struct.query;

import java.util.function.*;
import java.util.*;
import picoded.struct.query.internal.QueryFilter;

///
/// Representas a query condition, that can be used as a java Predicate against a collection
///
public interface Query extends Predicate<Object> {
	
	//
	// Static builder
	//--------------------------------------------------------------------
	
	/// Build the query using no predefiend arguments
	public static Query build(String queryString) {
		return QueryFilter.buildQuery(queryString, null, null);
	}
	
	/// Build the query using argumented array
	public static Query build(String queryString, Object[] argumentArr) {
		return QueryFilter.buildQuery(queryString, null, argumentArr);
	}
	
	/// Build the query using the parameter map
	public static Query build(String queryString, Map<String,Object> paramMap) {
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
	public boolean test(Object t);
	
	/// To test against a specified value map,
	/// Note that the test varient without the Map
	/// is expected to test against its cached varient
	/// that is setup by the constructor if applicable
	///
	/// @param   the object to test against
	/// @param   the argument map, if applicable
	///
	/// @returns  boolean indicating true / false
	public boolean test(Object t, Map<String,Object>argMap);
	
	//
	// Public accessors
	//--------------------------------------------------------------------
	
	/// Gets the query type
	public QueryType type();
	
	//
	// Condition only accessors
	//--------------------------------------------------------------------
	
	/// Gets the field name
	public default String fieldName() {
		return null;
	}
	
	/// Gets the argument name
	public default String argumentName() {
		return null;
	}
	
	/// Gets the default argument map
	public default Map<String,Object> defaultArgumentMap() {
		return null;
	}
	
	/// Indicates if its a basic operator
	public default boolean isBasicOperator() {
		return false;
	}
	
	//
	// Combination only accessors
	//--------------------------------------------------------------------
	
	/// Indicates if its a combination operator
	public default boolean isCombinationOperator() {
		return false;
	}
	
	/// Gets the children conditions
	public default List<Query> childrenQuery() {
		return null;
	}
	
	//
	// To string conversion
	//--------------------------------------------------------------------
	
	/// Gets the operator symbol
	public String operatorSymbol();
	
	/// Returns the query string
	public String toString();
	
	//
	// Map based search
	//--------------------------------------------------------------------
	
	// Searches using the query, and returns the resulting set
	public default <K,V> List<V> search(Map<K,V> set) {
		List<V> ret = new ArrayList<V>();
		for(K key : set.keySet()) {
			V val = set.get(key);
			if( test(val) ) {
				ret.add(val);
			}
		}
		return ret;
	}
	
	// Searches using the query, and sorted by the comparator
	public default <K,V> List<V> search(Map<K,V> set, Comparator<V> compareFunc ) {
		List<V> ret = search(set);
		Collections.sort(ret, compareFunc);
		return ret;
	}
	
	// Searches using the query, and sorted by the comparator query
	public default <K,V> List<V> search(Map<K,V> set, String comperatorString ) {
		return search(set, new OrderBy<V>(comperatorString));
	}
	
}
