package picoded.dstack;

// Java imports
import java.util.Map;

// Library imports
import picoded.struct.GenericConvertMap;
import picoded.struct.GenericConvertHashMap;
import picoded.struct.UnsupportedDefaultMap;

// Third party imports
import org.apache.commons.lang3.RandomUtils;

///
/// Minimal interface for all of picoded.datastack implmentation stack.
/// This is used mainly internally via DStack, StructSimpleStack, JSqlStack, etc
///
public interface CommonStack extends CommonStructure {
	
	//----------------------------------------------------------------
	//
	//  Get the various DStack structure
	//
	//----------------------------------------------------------------
	
	/// Gets the KeyValueMap object, with provided name
	///
	/// @param name - name of map in backend
	///
	/// @returns KeyValueMap
	default KeyValueMap getKeyValueMap(String name) {
		return (KeyValueMap) getStructure("KeyValueMap", name);
	}

	/// Gets the AtomicLongMap object, with provided name
	///
	/// @param name - name of map in backend
	///
	/// @returns AtomicLongMap
	default AtomicLongMap getAtomicLongMap(String name) {
		return (AtomicLongMap) getStructure("AtomicLongMap", name);
	}

	/// Gets the MetaTable object, with provided name
	///
	/// @param name - name of map in backend
	///
	/// @returns MetaTable
	default MetaTable getMetaTable(String name) {
		return (MetaTable) getStructure("MetaTable", name);
	}

	///
	/// Get the respective structure required, 
	/// If its missing the respective structure, a setup call is performed.
	/// If a conflicting structure of a different type was made, an exception occurs
	///
	/// @param   Type of structure to setup
	/// @param   Name used to initialize the structure
	///
	/// @return  The respective structure requested
	///
	CommonStructure getStructure(String type, String name);

	//----------------------------------------------------------------
	//
	//  Preloading of DStack structures, systemSetup/Teardown
	//
	//----------------------------------------------------------------
	
	/// This does the setup called on all the preloaded DStack structures, created via preload/get calls
	public void systemSetup();
	
	/// This does the teardown called on all the preloaded DStack structures, created via preload/get calls
	public void systemDestroy();
	
	//----------------------------------------------------------------
	//
	//  @TODO Adding module support
	//
	//----------------------------------------------------------------
	
}