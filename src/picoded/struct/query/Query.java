package picoded.struct.query;

import java.util.function.*;
import java.util.*;

///
/// Representas a query condition, that can be used as a java Predicate against a collection
///
public interface Query extends Predicate<Object> {
	
	//
	// Public test functions
	//--------------------------------------------------------------------
	
	/// The test operator, asserts if the element matches
	/// against its cached argument map value
	///
	/// @param   the object to test against
	///
	/// @returns  boolean indicating true / false
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
	boolean test(Object t, Map<String,Object>argMap);
	
	//
	// Public accessors
	//--------------------------------------------------------------------
	
	/// Gets the query type
	public QueryType type();
	
	//
	// condition only accessors
	//--------------------------------------------------------------------
	
	/// Gets the field name
	public String fieldName();
	
	/// Gets the argument name
	public String argumentName();
	
	/// Gets the default argument map
	public Map<String,Object> defaultArgumentMap();
	
}
