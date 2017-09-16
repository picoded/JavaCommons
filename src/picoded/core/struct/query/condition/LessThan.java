package picoded.core.struct.query.condition;

import java.util.Map;

import picoded.core.struct.query.CompareUtils;
import picoded.core.struct.query.QueryType;

public class LessThan extends ConditionBase {
	
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
	public LessThan(String field, String argName, Map<String, Object> defaultArgMap) {
		super(field, argName, defaultArgMap);
	}
	
	//
	// Required overwrites
	//--------------------------------------------------------------------
	
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
	@Override
	protected boolean testValues(Object fieldValue, Object argValue) {
		if (argValue == null || fieldValue == null) {
			return false;
		} //else {
		return CompareUtils.dynamicCompare(fieldValue, argValue) < 0;
		//}
	}
	
	/**
	 * The operator symbol support
	 *
	 * [to override on extension]
	 **/
	@Override
	public String operatorSymbol() {
		return "<";
	}
	
	/**
	 * Gets the query type
	 *
	 * [to override on extension]
	 **/
	@Override
	public QueryType type() {
		return QueryType.LESS_THAN;
	}
	
}
