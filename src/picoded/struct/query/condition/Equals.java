package picoded.struct.query.condition;

import java.util.*;
import picoded.struct.query.*;

import picoded.struct.query.QueryType;

public class Equals extends ConditionBase {
	
	//
	// Constructor Setup
	//--------------------------------------------------------------------
	
	/// The constructor with the field name, and default argument
	///
	/// @param   default field to test 
	/// @param   default argument name to test against
	/// @param   default argument map to get test value
	///
	public Equals(String field, String argName, Map<String,Object> defaultArgMap) {
		super(field, argName, defaultArgMap);
	}
	
	//
	// Required overwrites
	//--------------------------------------------------------------------
	
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
	
	/// Gets the query type 
	///
	/// [to override on extension]
	public QueryType type() {
		return QueryType.EQUALS;
	}
	
} 
