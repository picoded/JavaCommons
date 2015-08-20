package picoded.struct.query.condition;

import picoded.struct.query.*;

import java.util.function.*;
import java.util.*;

/// Acts as the base for all conditional types,
///
/// The field name and its respective value, represents the LHS (Left Hand Side)
/// While the argument name and its respective value, represents the RHS (Right Hand Side)
/// of the conditional comparision.
///
/// Base implmentation is equivalent as Equals
public class CombinationBase implements Query {
	
	//
	// Constructor vars
	//--------------------------------------------------------------------
	
	/// The children query objects
	protected Set<Query> _children = null;
	
	/// The constructed argument map
	protected Map<String,Object> _argMap = null;
	
	//
	// Constructor Setup
	//--------------------------------------------------------------------
	
	/// The constructor with the field name, and default argument
	///
	/// @param   children conditions to test
	/// @param   default argument map to get test value
	///
	public CombinationBase(Set<Query> childQuery, Map<String,Object> defaultArgMap) {
		_children = childQuery;
		_argMap = defaultArgMap;
	}
	
	/// The constructor with the field name, and default argument
	///
	/// @param   children conditions to test
	/// @param   default argument map to get test value
	///
	public CombinationBase(List<Query> childQuery, Map<String,Object> defaultArgMap) {
		_children = new HashSet<Query>(childQuery);
		_argMap = defaultArgMap;
	}
	
	//
	// Public test functions
	//--------------------------------------------------------------------
	
	/// The test operator, asserts if the element matches
	///
	/// @param   the object to test against
	///
	/// @returns  boolean indicating true / false
	public boolean test(Object t) {
		return test(t, _argMap);
	}
	
	/// To test against a specified value map,
	/// Note that the test varient without the Map
	/// is expected to test against its cached varient
	/// that is setup by the constructor if applicable
	///
	/// [to override on extension]
	///
	/// @param   the object to test against
	/// @param   the argument map, if applicable
	///
	/// @returns  boolean indicating true / false
	public boolean test(Object t, Map<String,Object>argMap) {
		boolean result = false; //blank combination is a failure
		
		for(Query child : _children) {
			if( child.test(t, argMap) ) {
				result = true;
			} else {
				return false; //breaks and return false on first failure
			}
		}
		
		return result;
	}
	
	//
	// Public accessors
	//--------------------------------------------------------------------
	
	/// Indicates if its a basic operator 
	public boolean isCombinationOperator() {
		return true;
	}
	
	/// Gets the query type 
	///
	/// [to override on extension]
	public QueryType type() {
		return QueryType.AND;
	}
	
	/// Gets the children conditions
	public Set<Query> childrenQuery() {
		return _children;
	}
} 
