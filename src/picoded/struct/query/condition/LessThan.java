package picoded.struct.query.condition;

import java.text.Collator;
import java.text.NumberFormat;
import java.text.RuleBasedCollator;
import java.util.*;

import picoded.struct.query.QueryType;

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
			if(fieldValue instanceof String){
				if(((String) fieldValue).matches("[0-9]+")){
					try{
						Number fieldAsNumber = NumberFormat.getNumberInstance(Locale.ENGLISH).parse((String)fieldValue);
						Number argAsNumber = NumberFormat.getNumberInstance(Locale.ENGLISH).parse((String)argValue);
						
						return fieldAsNumber.doubleValue() < argAsNumber.doubleValue() ? true : false;
					}catch(Exception ex){
						throw new RuntimeException("exception in testValues-> " +ex.getMessage());
					}
				}else{
					Collator rbc = RuleBasedCollator.getInstance();
					int result = rbc.compare(fieldValue, argValue);
					return result < 0 ? true : false;
				}
			}else{
				Double a = normalizeNumber(fieldValue);
				Double b = normalizeNumber(argValue);
				
				return a < b ? true : false;
			}
		}
		return false;
	}
	
	private Double normalizeNumber(Object number){
		Double val = null;
		if(number instanceof Integer){
			val = (Double)((Integer)number * 1.0);
		}else if(number instanceof Float){
			val = (Double)((Float)number * 1.0);
		}else if(number instanceof Double){
			val = (Double)number;
		}
		return val;
	}
	
	
	/// Gets the query type 
	///
	/// [to override on extension]
	public QueryType type() {
		return QueryType.LESS_THAN;
	}
	
} 
