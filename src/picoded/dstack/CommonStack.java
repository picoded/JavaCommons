package picoded.dstack;

// Java imports
import java.util.Map;

// Library imports
import picoded.core.struct.GenericConvertMap;
import picoded.core.struct.GenericConvertHashMap;
import picoded.core.struct.UnsupportedDefaultMap;

// Third party imports
import org.apache.commons.lang3.RandomUtils;

/**
* Minimal interface for all of picoded.datastack implmentation stack.
* This is used mainly internally via DStack, StructSimpleStack, JSqlStack, etc
**/
public interface CommonStack extends CommonStructure {

	//----------------------------------------------------------------
	//
	//  CommonStack naming API
	//
	//----------------------------------------------------------------

	/**
	 * Get its configured name value
	 * @return  name value (as a string)
	 */
	default String getName() {
		return configMap().getString("name");
	}

	//----------------------------------------------------------------
	//
	//  Get the various DStack structure
	//
	//----------------------------------------------------------------

	/**
	* Gets the KeyValueMap object, with provided name
	*
	* @param name - name of map in backend
	*
	* @return KeyValueMap
	**/
	default KeyValueMap getKeyValueMap(String name) {
		return (KeyValueMap) getStructure("KeyValueMap", name);
	}

	/**
	* Gets the AtomicLongMap object, with provided name
	*
	* @param name - name of map in backend
	*
	* @return AtomicLongMap
	**/
	default AtomicLongMap getAtomicLongMap(String name) {
		return (AtomicLongMap) getStructure("AtomicLongMap", name);
	}

	/**
	* Gets the DataTable object, with provided name
	*
	* @param name - name of map in backend
	*
	* @return DataTable
	**/
	default DataTable getDataTable(String name) {
		return (DataTable) getStructure("DataTable", name);
	}

	/**
	* Get the respective structure required,
	* If its missing the respective structure, a setup call is performed.
	* If a conflicting structure of a different type was made, an exception occurs
	*
	* @param   Type of structure to setup
	* @param   Name used to initialize the structure
	*
	* @return  The respective structure requested
	**/
	CommonStructure getStructure(String type, String name);

	//----------------------------------------------------------------
	//
	//  Preloading of DStack structures, systemSetup/Teardown
	//
	//----------------------------------------------------------------

	/**
	* This does the setup called on all the preloaded DStack structures, created via preload/get calls
	**/
	public void systemSetup();

	/**
	* This does the teardown called on all the preloaded DStack structures, created via preload/get calls
	**/
	public void systemDestroy();

	//----------------------------------------------------------------
	//
	//  @TODO Adding module support
	//
	//----------------------------------------------------------------

}
