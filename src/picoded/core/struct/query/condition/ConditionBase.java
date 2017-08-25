package picoded.core.struct.query.condition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import picoded.core.conv.GenericConvert;
import picoded.core.struct.query.Query;
import picoded.core.struct.query.QueryType;
import picoded.core.struct.query.internal.QueryUtils;

/**
 * Acts as the base for all conditional types,
 *
 * The field name and its respective value, represents the LHS (Left Hand Side)
 * While the argument name and its respective value, represents the RHS (Right Hand Side)
 * of the conditional comparision.
 *
 * Base implmentation is equivalent as Equals
 **/
public class ConditionBase implements Query {
	
	//
	// Constructor vars
	//--------------------------------------------------------------------
	
	/**
	 * The field name, this/null is reserved to refering to itself
	 **/
	protected String _fieldName = null;
	/**
	 * The constructed argument name
	 **/
	protected String _argName = null;
	/**
	 * The constructed argument map
	 **/
	protected Map<String, Object> _argMap = null;
	
	//
	// Constructor Setup
	//--------------------------------------------------------------------
	
	/**
	 * The constructor with the field name, and default argument
	 *
	 * @param   default field to test
	 * @param   default argument name to test against
	 * @param   default argument map to get test value
	 **/
	public ConditionBase(String field, String argName, Map<String, Object> defaultArgMap) {
		_fieldName = field;
		_argName = argName;
		_argMap = defaultArgMap;
	}
	
	//
	// Core protected functions
	//--------------------------------------------------------------------
	
	/**
	 * Gets the arg value to test
	 *
	 * @param   map to extract out the field value
	 * @param   field name of extraction
	 *
	 * @return  The extracted object
	 **/
	protected Object getArgumentValue(Map<String, Object> argMap, String argName) {
		if (argMap == null || argName == null) {
			return null;
		}
		return GenericConvert.fetchNestedObject(argMap, argName, null);
	}
	
	/**
	 * To test against the specific value, this is the actual
	 * argument which is being used. After fetching both
	 * the field and argument value
	 *
	 * [to override on extension]
	 *
	 * @param   the object to test against
	 * @param   the argument actual value
	 *
	 * @return  boolean indicating success or failure
	 **/
	protected boolean testValues(Object fieldValue, Object argValue) {
		if (argValue == null) {
			if (fieldValue == null) {
				return true;
			}
		} else if (argValue.equals(fieldValue)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Gets the field value and tests it,
	 * this is a combination of getFieldValue, and testValues
	 *
	 * @param   object to extract out the field value
	 * @param   parameter map to use
	 *
	 * @return  boolean indicating success or failure
	 **/
	@SuppressWarnings("unchecked")
	protected boolean getAndTestFieldValue(Object t, Map<String, Object> argMap) {
		
		// Argument value to test against
		Object argValue = getArgumentValue(argMap, _argName);
		
		// Allow operation across all key / val mappings, wildcard search
		if (t instanceof Map) {
			if ("key".equalsIgnoreCase(_fieldName)) {
				
				// Test against all the key values
				for (Map.Entry<Object, Object> e : ((Map<Object, Object>) t).entrySet()) {
					if (testValues(e.getKey(), argValue)) {
						return true;
					}
				}
				
				// Failed to find any key
				return false;
			} else if ("val".equalsIgnoreCase(_fieldName)) {
				
				// Test against all the stored values
				for (Map.Entry<Object, Object> e : ((Map<Object, Object>) t).entrySet()) {
					if (testValues(e.getValue(), argValue)) {
						return true;
					}
				}
				
				// Failed to find any value
				return false;
			}
		}
		
		//System.out.println("> "+ QueryUtils.getFieldValue(t, _fieldName) +" = "+argValue);
		// Get the target value to test, and test it
		return testValues(QueryUtils.getFieldValue(t, _fieldName), argValue);
	}
	
	//
	// Public test functions
	//--------------------------------------------------------------------
	
	/**
	 * The test operator, asserts if the element matches
	 *
	 * @param   the object to test against
	 *
	 * @return  boolean indicating true / false
	 **/
	@Override
	public boolean test(Object t) {
		return getAndTestFieldValue(t, _argMap);
	}
	
	/**
	 * To test against a specified value map,
	 * Note that the test varient without the Map
	 * is expected to test against its cached varient
	 * that is setup by the constructor if applicable
	 *
	 * @param   the object to test against
	 * @param   the argument map, if applicable
	 *
	 * @return  boolean indicating true / false
	 **/
	@Override
	public boolean test(Object t, Map<String, Object> argMap) {
		return getAndTestFieldValue(t, argMap);
	}
	
	//
	// Public accessors
	//--------------------------------------------------------------------
	
	/**
	 * Indicates if its a basic operator
	 **/
	@Override
	public boolean isBasicOperator() {
		return true;
	}
	
	/**
	 * Gets the query type
	 *
	 * [to override on extension]
	 **/
	@Override
	public QueryType type() {
		return QueryType.EQUALS;
	}
	
	/**
	 * Gets the field name
	 **/
	@Override
	public String fieldName() {
		return _fieldName;
	}
	
	/**
	 * Gets the argument name
	 **/
	@Override
	public String argumentName() {
		return _argName;
	}
	
	/**
	 * Gets the default argument map
	 **/
	@Override
	public Map<String, Object> defaultArgumentMap() {
		return _argMap;
	}
	
	//
	// String handling
	//--------------------------------------------------------------------
	
	/**
	 * The operator symbol support
	 *
	 * [to override on extension]
	 **/
	@Override
	public String operatorSymbol() {
		return "=";
	}
	
	/**
	 * The query string
	 **/
	@Override
	public String toString() {
		return "\"" + fieldName() + "\" " + operatorSymbol() + " " + ":" + argumentName();
	}
	
	//
	// Name value pair extraction from query
	//--------------------------------------------------------------------
	
	/**
	 * Extract out the respective query keys, and values
	 **/
	@Override
	public Map<String, List<Object>> keyValuesMap(Map<String, List<Object>> mapToReturn) {
		String key = _fieldName;
		Object val = (_argMap != null) ? _argMap.get(_argName) : null;
		
		if (mapToReturn.get(key) == null) {
			mapToReturn.put(key, new ArrayList<Object>());
		}
		mapToReturn.get(key).add(val);
		
		return mapToReturn;
	}
	
}
