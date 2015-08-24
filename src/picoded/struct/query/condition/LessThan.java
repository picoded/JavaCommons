package picoded.struct.query.condition;

import java.text.Collator;
import java.text.NumberFormat;
import java.text.RuleBasedCollator;
import java.util.*;

import picoded.struct.query.QueryType;
import picoded.struct.query.internal.QueryUtils;

public class LessThan extends ConditionBase {
	
	//
	// Constructor Setup
	//--------------------------------------------------------------------
	
	/// The constructor with the field name, and default argument
	///
	/// @param   default field to test 
	/// @param   default argument name to test against
	/// @param   default argument map to get test value
	///
	public LessThan(String field, String argName, Map<String,Object> defaultArgMap) {
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
		}else{
			Object fieldObj = QueryUtils.normalizeObject(fieldValue);
			Object argObj = QueryUtils.normalizeObject(argValue);
			
			if(fieldObj instanceof String && argObj instanceof String){
				Collator collator = RuleBasedCollator.getInstance(Locale.ENGLISH);
				int result = collator.compare(fieldObj, argObj);
				return result < 0 ? true : false;
			}else if(fieldObj instanceof Double && argObj instanceof Double){
				return (Double)fieldObj < (Double)argObj ? true : false;
			}else{
				throw new RuntimeException("These values cannot be compared");
			}
		}
		return false;
	}
	
	/// The operator symbol support
	///
	/// [to override on extension]
	public String operatorSymbol() {
		return "<";
	}
	
	/// Gets the query type 
	///
	/// [to override on extension]
	public QueryType type() {
		return QueryType.LESS_THAN;
	}
	
} 
