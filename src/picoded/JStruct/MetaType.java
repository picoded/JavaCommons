package picoded.JStruct;

/// MetaType enums represents the various data types,
/// that the struct/JSql/JCache/JStack varients of MetaTable can support.
public enum MetaType {
	
	//
	// Null or dynamic types
	//--------------------------------------------------------------------
	
	/// Null field
	NULL(1),
	
	/// Unspecified storage type, to use auto detection
	MIXED(2),
	
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
	DOUBLE(22),
	
	/// Float type
	FLOAT(22),
	
	/// String type
	STRING(23),
	
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
	BINARY(33);
	
	//
	// Constructor setup
	//--------------------------------------------------------------------
	private final int ID;
	private MetaType(final int inID) { ID = inID; }
	public int getValue() { return ID; }
	
	
}
