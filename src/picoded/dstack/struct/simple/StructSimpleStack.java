package picoded.dstack.struct.simple;

// Java imports
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// Picoded imports
import picoded.conv.ConvertJSON;
import picoded.set.ObjectToken;
import picoded.dstack.*;
import picoded.dstack.core.*;

/**
* Reference implementation of CommonStack
* This is done via a minimal implementation via internal data structures.
*
* Built ontop of the Core_CommonStack implementation.
**/
public class StructSimpleStack extends Core_CommonStack {

	/**
	* Common structure initialization interface, to be overwritten by actual implementation
	*
	* @param   Type of structure to setup
	* @param   Name used to initialize the structure
	*
	* @return  The CommonStructure that was initialized
	**/
	public CommonStructure initializeStructure(String type, String name) {
		if ("DataTable".equalsIgnoreCase(type)) {
			return new StructSimple_DataTable();
		} else if ("KeyValueMap".equalsIgnoreCase(type)) {
			return new StructSimple_KeyValueMap();
		} else if ("AtomicLongMap".equalsIgnoreCase(type)) {
			return new StructSimple_AtomicLongMap();
		}
		return null;
	}

}
