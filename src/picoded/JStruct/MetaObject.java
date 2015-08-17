package picoded.JStruct;

/// Java imports
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.*;
import java.util.List;
import java.util.ArrayList;

/// Picoded imports
import picoded.conv.GUID;
import picoded.JSql.*;
import picoded.JCache.*;
import picoded.struct.CaseInsensitiveHashMap;
import picoded.struct.GenericConvertMap;

/// Represents a single object node in the MetaTable collection.
///
/// NOTE: This class should not be initialized directly, but through MetaTable class
public class MetaObject implements GenericConvertMap<String, Object> {

	// ///
	// /// Constructor vars
	// ///--------------------------------------------------------------------------
	// 
	// /// Stores the key to value map
	// protected ConcurrentHashMap<String, Object> valueMap = new ConcurrentHashMap<String, Object>();
	// 
	// /// Read write lock
	// protected ReentrantReadWriteLock accessLock = new ReentrantReadWriteLock();
	// 
	// ///
	// /// Constructor setup
	// ///--------------------------------------------------------------------------
	// 
	// /// Constructor
	// public MetaObject() {
	// 	// does nothing =X
	// }
	// 
}
