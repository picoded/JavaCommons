package picoded.struct.query.condition;

import picoded.struct.query.*;
import picoded.struct.query.internal.*;

import java.util.function.*;
import java.util.*;

/// Acts as the base for all conditional types,
///
/// The field name and its respective value, represents the LHS (Left Hand Side)
/// While the argument name and its respective value, represents the RHS (Right Hand Side)
/// of the conditional comparision.
///
/// Base implmentation is equivalent as Equals
public class ConditionBase implements Query {
	
	//
	// Constructor vars
	//--------------------------------------------------------------------
	
	/// The field name, this/null is reserved to refering to itself
	protected String _fieldName = null;
	
	/// The constructed argument name
	protected String _argName = null;
	
	/// The constructed argument map
	protected Map<String,Object> _argMap = null;
	
	//
	// Constructor Setup
	//--------------------------------------------------------------------
	
	/// The constructor with the field name, and default argument
	///
	/// @param   default field to test 
	/// @param   default argument name to test against
	/// @param   default argument map to get test value
	///
	public ConditionBase(String field, String argName, Map<String,Object> defaultArgMap) {
		_fieldName = field;
		_argName = argName;
		_argMap = defaultArgMap;
	}
	
	//
	// Core protected functions
	//--------------------------------------------------------------------
	
	/// Gets the arg value to test
	///
	/// @param   map to extract out the field value 
	/// @param   field name of extraction
	///
	/// @TODO: Support FullyQualifiedDomainName extraction?
	///
	/// @returns  The extracted object
	///
	protected Object getArgumentValue(Map<String,Object> argMap, String argName) {
		if( argMap == null || argName == null ) {
			return null;
		}
		return argMap.get(argName);
	}
	
	/// To test against the specific value, this is the actual
	/// argument which is being used. After fetching both 
	/// the field and argument value
	///
	/// [to override on extension]
	///
	/// @param   the object to test against
	/// @param   the argument actual value
	///
	/// @returns  boolean indicating success or failure
	///
	protected boolean testValues(Object fieldValue, Object argValue) {
		if( argValue == null ) {
			if( fieldValue == null ) {
				return true;
			}
		} else if( argValue.equals(fieldValue) ) {
			return true;
		}
		return false;
	}
	
	/// Gets the field value and tests it, 
	/// this is a combination of getFieldValue, and testValues
	///
	/// @param   object to extract out the field value 
	/// @param   parameter map to use
	///
	/// @returns  boolean indicating success or failure
	///
	protected boolean getAndTestFieldValue(Object t, Map<String,Object> argMap) {
		Object fieldValue = QueryUtils.getFieldValue(t, _fieldName);
		Object argValue = getArgumentValue(argMap, _argName);
			
		//System.out.println("> "+fieldValue+" = "+argValue);
		return testValues(fieldValue, argValue);
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
		return getAndTestFieldValue(t, _argMap);
	}
	
	/// To test against a specified value map,
	/// Note that the test varient without the Map
	/// is expected to test against its cached varient
	/// that is setup by the constructor if applicable
	///
	/// @param   the object to test against
	/// @param   the argument map, if applicable
	///
	/// @returns  boolean indicating true / false
	public boolean test(Object t, Map<String,Object>argMap) {
		return getAndTestFieldValue(t, argMap);
	}
	
	//
	// Public accessors
	//--------------------------------------------------------------------
	
	/// Indicates if its a basic operator 
	public boolean isBasicOperator() {
		return true;
	}
	
	/// Gets the query type 
	///
	/// [to override on extension]
	public QueryType type() {
		return QueryType.EQUALS;
	}
	
	/// Gets the field name
	public String fieldName() {
		return _fieldName;
	}
	
	/// Gets the argument name
	public String argumentName() {
		return _argName;
	}
	
	/// Gets the default argument map
	public Map<String,Object> defaultArgumentMap() {
		return _argMap;
	}
	
	//
	// String handling
	//--------------------------------------------------------------------
	
	/// The operator symbol support
	///
	/// [to override on extension]
	public String operatorSymbol() {
		return "=";
	}
	
	/// The query string
	public String toString() {
		return "\""+fieldName() + "\" " + operatorSymbol() + " " + ":" + argumentName();
	}
	
} 
