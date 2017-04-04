package picoded.dstack;

/// Java imports
import java.util.*;
import picoded.struct.*;

/// Represents a single object node in the MetaTable collection.
/// DO NOT use _underscore values, as it is a reserved namespace.
///
/// NOTE: This class should not be initialized directly, but through MetaTable class
public interface MetaObject extends GenericConvertMap<String, Object> {
	
	/// @return The object ID String
	String _oid();
	
	/// Gets and return its current value
	///
	/// @param   Key to fetch value
	///
	/// @return  Value if found, null if not found
	@Override
	Object get(Object key);
	
	/// Put and set its delta value, put null is considered "remove"
	/// 
	/// @param   Key to put with
	/// @param   Value to put with
	///
	/// @return  Null, return value is used to provide Map compatibility support
	Object put(String key, Object value);
	
	/// Remove operation
	///
	/// @param   Key to find and remove
	///
	/// @return  Null, return value is used to provide Map compatibility support
	@Override
	Object remove(Object key);
	
	/// Search and return all the keys for this object map
	///
	/// @return valid keySet()
	@Override
	Set<String> keySet();
	
	/// Save the delta changes of this object to storage, if possible. Else does a save all.
	default void saveDelta() {
		saveAll();
	}
	
	/// Save all the configured data, ignore delta handling. Used to gurantee a flush
	void saveAll();
	
}
