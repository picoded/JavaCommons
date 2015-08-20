package picoded.struct.query;

import java.util.*;

/// Represents the variosu query types used
///
/// This is at the upper most layer, hence conversion to query strings
/// or reading through the query structure may provide much more insights
public enum QueryType {
	
	//
	// Combination types
	//--------------------------------------------------------------------
	
	AND(0),
	OR(1),
	NOT(2),
	
	//
	// Comparision types
	//--------------------------------------------------------------------
	
	EQUALS(10),
	
	LESS_THEN(20),
	LESS_THEN_AND_EQUALS(21),
	
	MORE_THEN(30),
	MORE_THEN_AND_EQUALS(31);
	
	//////////////////////////////////////////////////////////////////////
	//
	// The following is cookie cutter code,
	//
	// This can be replaced when there is a way to default implement
	// static function and variabels, etc, etc.
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
	
	//
	// Constructor setup
	//--------------------------------------------------------------------
	private final int ID;
	private QueryType(final int inID) { ID = inID; }
	
	/// Return the numeric value representing the enum
	public int getValue() { return ID; }
	
	//
	// Public EnumSet
	//--------------------------------------------------------------------
	public static final EnumSet<QueryType> typeSet = EnumSet.allOf(QueryType.class);
	
	//
	// Type mapping
	//--------------------------------------------------------------------
	
	/// The type mapping cache
	private static Map<String,QueryType> nameToTypeMap = null;
	private static Map<Integer,QueryType> idToTypeMap = null;
	
	/// Setting up the type mapping
	///
	/// Note that the redundent temp variable, is to ensure the final map is only set
	/// in an "atomic" fashion. In event of multiple threads triggerint the initializeTypeMaps
	/// setup process.
	///
	protected static void initializeTypeMaps() {
		if( nameToTypeMap == null || idToTypeMap == null ) {
			Map<String,QueryType> nameToTypeMap_wip = new HashMap<String,QueryType>();
			Map<Integer,QueryType> idToTypeMap_wip = new HashMap<Integer,QueryType>();
			
			for (QueryType type : QueryType.values()) {
				nameToTypeMap_wip.put( type.name(), type );
				idToTypeMap_wip.put( type.getValue(), type );
			}
			
			nameToTypeMap = nameToTypeMap_wip;
			idToTypeMap_wip = idToTypeMap_wip;
		}
	}
	
	/// Get from the respective ID values
	public static QueryType fromID(int id) {
		initializeTypeMaps();
		return idToTypeMap.get( id );
	}
	
	/// Get from the respective string name values
	public static QueryType fromName(String name) {
		initializeTypeMaps();
		name = name.toUpperCase();
		return nameToTypeMap.get( name );
	}
	
	/// Dynamically switches between name, id, or QueryType. Null returns null
	public static QueryType fromTypeObject(Object type) {
		if( type == null ) {
			return null;
		}
		
		QueryType mType = null;
		if(type instanceof QueryType) {
			mType = (QueryType)type;
		} else if(type instanceof Number) {
			mType = QueryType.fromID( ((Number)type).intValue() );
		} else {
			mType = QueryType.fromName(type.toString());
		} 
		
		if( mType == null ) {
			throw new RuntimeException("Invalid QueryType for: "+type.toString());
		}
		return mType;
	}
	
	//////////////////////////////////////////////////////////////////////
	//
	// End of cookie cutter code,
	//
	//////////////////////////////////////////////////////////////////////
}
