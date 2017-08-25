package picoded.core.struct.query;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

/**
 * Represents the variosu query types used
 *
 * This is at the upper most layer, hence conversion to query strings
 * or reading through the query structure may provide much more insights
 **/
public enum QueryType {
	
	//--------------------------------------------------------------------
	// Combination types
	//--------------------------------------------------------------------
	
	AND(0), OR(1), NOT(2), //Is actually and inverse of AND
	
	//--------------------------------------------------------------------
	// Comparision types
	//--------------------------------------------------------------------
	
	EQUALS(10), NOT_EQUALS(11),
	
	LESS_THAN(20), LESS_THAN_OR_EQUALS(21),
	
	MORE_THAN(30), MORE_THAN_OR_EQUALS(31),
	
	LIKE(40);
	
	//////////////////////////////////////////////////////////////////////
	//
	// The following is cookie cutter code,
	//
	// This can be replaced when there is a way to default implement
	// static function and variables, etc, etc.
	//
	// Or the unthinkable, java allow typed macros / type annotations
	// (the closest the language now has to macros)
	//
	// As of now just copy this whole chunk downards, search and
	// replace the class name to its respective enum class to implment
	//
	// Same thing for value type if you want (not recommended)
	//
	//////////////////////////////////////////////////////////////////////
	
	//--------------------------------------------------------------------
	// Constructor setup
	//--------------------------------------------------------------------
	private final int id;
	
	QueryType(final int inID) {
		id = inID;
	}
	
	/**
	 * Return the numeric value representing the enum
	 **/
	public int getValue() {
		return id;
	}
	
	//--------------------------------------------------------------------
	// Public EnumSet
	//--------------------------------------------------------------------
	protected static final EnumSet<QueryType> typeSet = EnumSet.allOf(QueryType.class);
	
	//--------------------------------------------------------------------
	// Type mapping
	//--------------------------------------------------------------------
	/**
	 * The type mapping cache
	 **/
	private static volatile Map<String, QueryType> nameToTypeMap = null;
	private static volatile Map<Integer, QueryType> idToTypeMap = null;
	
	/**
	 * Setting up the type mapping
	 *
	 * Note that the redundent temp variable, is to ensure the final map is only set
	 * in an "atomic" fashion. In event of multiple threads triggering the initializeTypeMaps
	 * setup process.
	 **/
	protected static void initializeTypeMaps() {
		if (nameToTypeMap == null || idToTypeMap == null) {
			Map<String, QueryType> nameToTypeMap_wip = new HashMap<String, QueryType>();
			Map<Integer, QueryType> idToTypeMap_wip = new HashMap<Integer, QueryType>();
			
			for (QueryType type : QueryType.values()) {
				nameToTypeMap_wip.put(type.name(), type);
				idToTypeMap_wip.put(type.getValue(), type);
			}
			//synchronized(idToTypeMap_wip) {
			if (nameToTypeMap == null) {
				nameToTypeMap = nameToTypeMap_wip;
				idToTypeMap = idToTypeMap_wip;
			}
			//}
		}
	}
	
	/**
	 * Get from the respective id values
	 **/
	public static QueryType fromID(int id) {
		initializeTypeMaps();
		return idToTypeMap.get(id);
	}
	
	/**
	 * Get from the respective string name values
	 **/
	public static QueryType fromName(String name) {
		initializeTypeMaps();
		name = name.toUpperCase(Locale.ENGLISH);
		return nameToTypeMap.get(name);
	}
	
	/**
	 * Dynamically switches between name, id, or QueryType. Null returns null
	 **/
	public static QueryType fromTypeObject(Object type) {
		if (type == null) {
			return null;
		}
		
		QueryType mType = null;
		if (type instanceof QueryType) {
			mType = (QueryType) type;
		} else if (type instanceof Number) {
			mType = QueryType.fromID(((Number) type).intValue());
		} else {
			mType = QueryType.fromName(type.toString());
		}
		
		if (mType == null) {
			throw new RuntimeException("Invalid QueryType for: " + type.toString());
		}
		return mType;
	}
	
	//////////////////////////////////////////////////////////////////////
	//
	// End of cookie cutter code,
	//
	//////////////////////////////////////////////////////////////////////
}
