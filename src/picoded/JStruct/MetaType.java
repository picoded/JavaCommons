package picoded.JStruct;

import java.util.Map;
import java.util.HashMap;

/// MetaType enums represents the various data types,
/// that the struct/JSql/JCache/JStack varients of MetaTable can support.
public enum MetaType {
	
	//
	// Null or dynamic types
	//--------------------------------------------------------------------
	
	/// Null field
	NULL(0),
	
	/// Unspecified storage type, to use auto detection
	MIXED(1),
	
	//
	// UUID based
	//--------------------------------------------------------------------
	
	/// A UUID identifier
	UUID(11),
	
	/// Another MetaTable identifier. This is used for linked MetaTable's
	/// via a commonly agreeded on UUID
	METATABLE(12),
	
	//
	// Standard based
	//--------------------------------------------------------------------
	
	/// Integer type
	INTEGER(21),
	
	/// Long type
	LONG(22),
	
	/// Double type
	DOUBLE(23),
	
	/// Float type
	FLOAT(24),
	
	/// String type
	STRING(25),
	
	//
	// Storage types
	//
	// This can be a form of optimziation, as TEXT / BINARY will
	// normally be stored as String if not optimized as such
	//--------------------------------------------------------------------
	
	/// JSON String storage, can be any internal value
	/// this is used to define the storage format.
	JSON(31),
	
	/// Text type, this uses TEXT based indexing
	/// (mainly affects SQL layer)
	TEXT(32),
	
	/// Binary type, this is used to store raw data
	/// 
	/// Note that binary type has a special optimization
	/// involved where data is not pulled until 
	/// explitcitely requested for.
	BINARY(33),
	
	//
	// Array based, varients of above
	//--------------------------------------------------------------------
	UUID_ARRAY(511),
	METATABLE_ARRAY(512),
	
	INTEGER_ARRAY(521),
	LONG_ARRAY(522),
	DOUBLE_ARRAY(523),
	FLOAT_ARRAY(524),
	STRING_ARRAY(525),
	
	JSON_ARRAY(531),
	TEXT_ARRAY(532),
	BINARY_ARRAY(533);
	
	//
	// Constructor setup
	//--------------------------------------------------------------------
	private final int ID;
	private MetaType(final int inID) { ID = inID; }
	
	/// Return the numeric value representing the enum
	public int getValue() { return ID; }
	
	//
	// Type mapping
	//--------------------------------------------------------------------
	
	/// The type mapping cache
	private static Map<String,MetaType> nameToTypeMap = new HashMap<String,MetaType>();
	private static Map<Integer,MetaType> idToTypeMap = new HashMap<Integer,MetaType>();
	
	/// Setting up the type mapping
	static {
		for (MetaType type : MetaType.values()) {
			nameToTypeMap.put( type.name(), type );
			idToTypeMap.put( type.getValue(), type );
		}
	}
	
	/// Get from the respective ID values
	public static MetaType fromID(int id) {
		return idToTypeMap.get( id );
	}
	
	/// Get from the respective string name values
	public static MetaType fromName(String name) {
		name = name.toUpperCase();
		return nameToTypeMap.get( name );
	}
	
	/// Dynamically switches between name, id, or MetaType. Null returns null
	public static MetaType fromTypeObject(Object type) {
		if( type == null ) {
			return null;
		}
		
		MetaType mType = null;
		if(type instanceof MetaType) {
			mType = (MetaType)type;
		} else if(type instanceof Number) {
			mType = MetaType.fromID( ((Number)type).intValue() );
		} else {
			mType = MetaType.fromName(type.toString());
		} 
		
		if( mType == null ) {
			throw new RuntimeException("Invalid MetaTable type for: "+type.toString());
		}
		return mType;
	}
}
